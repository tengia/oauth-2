package net.oauth2.client;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commons.util.Observable;
import commons.util.ObservableMixin;
import net.oauth2.AccessToken;
import net.oauth2.TemporalAccessToken;

/**
 * A TokenProvider implementation that leverages the OAuth token infrastructure
 * to auto-renew tokens asynchronously and always be able to provide a valid
 * token to consumers as long as the Token Service can supply them.
 * 
 * <ul>
 * <li>Asynchronous renewal of expiring Access Tokens</li>
 * <li>Push notifications for token changes to subscribers</li>
 * <li>Auto recovery with retry policies for resilient implementations</li>
 * <li>Auto adjust to Token Service capabilities to refresh or always fetch new
 * Access Token as appropriate</li>
 * <li>Resumable, can survive restarts*</li>
 * <li>Configurable renew schedule for efficient networking</li>
 * </ul>
 * <div><b>*</b> Persistence of tokens is not in scope for this client. An
 * external actor taking care of this is required to recover and resume renewal
 * of tokens.</div>
 * 
 * @param <T>
 *            The type of the AccessToken managed by this TokenProvider. Can be
 *            a subclass of AccessToken.
 */
public class AutoRenewingTokenProvider<T extends AccessToken>
		implements TokenProvider, Resumable<T>, TokenChangeObservable<T>, TokenProviderJob<T> {

	private final static Logger LOGGER = LoggerFactory.getLogger(AutoRenewingTokenProvider.class);

	private final TokenService tokenService;
	ScheduledExecutorService schedulerExecutor;
	private RetryPolicy retryPolicy;
	private Observable<TokenChangeObserver<T>> observable;

	TokenRenewTask tokenRenewTask;
	ScheduledFuture<?> future;
	private TemporalUnit tokenExpireInTimeUnits = ChronoUnit.SECONDS;// used by
																		// java.time.Instance
	private double delayModifier = 0.9;
	private boolean strictlyRefresh = false;

	public AutoRenewingTokenProvider(final TokenService tokenService, ScheduledExecutorService executor,
			RetryPolicy retryPolicy, Observable<TokenChangeObserver<T>> observable, boolean strictlyRefresh) {
		this.tokenService = tokenService;
		this.schedulerExecutor = executor;
		this.retryPolicy = retryPolicy;
		this.observable = observable;
		this.strictlyRefresh = strictlyRefresh;
	}

	/**
	 * Default constructor utilizing the provided TokenService. Configures this
	 * instance with single thread scheduler service, no retry policy,
	 * ObservableMixin observable and non strict refresh policy.
	 * 
	 * @param tokenService
	 *            the service delegate used to fetch and refresh tokens by this
	 *            instance.
	 */
	public AutoRenewingTokenProvider(final TokenService tokenService) {
		this(tokenService, Executors.newSingleThreadScheduledExecutor(), new NoRetryPolicy(),
				new ObservableMixin<TokenChangeObserver<T>>(), false);
	}

	class TokenRenewTask implements Runnable {

		private final AutoRenewingTokenProvider<T> svc;
		TemporalAccessToken<T> token;

		TokenRenewTask(AutoRenewingTokenProvider<T> svc, TemporalAccessToken<T> token) {
			this.svc = svc;
			this.token = token;
			if (token.token().getRefreshToken() == null && this.svc.strictlyRefresh())
				throw new IllegalArgumentException(
						"Cannot start refresh token timer without a valid token with refresh_token value when set to strictly refresh");
		}

		@Override
		public void run() {
			TemporalAccessToken<T> newToken = null;
			int retries = 0;
			RetryPolicy retryPolicy = this.svc.getRetryPolicy();
			while (newToken == null && retries > -1 && retries < retryPolicy.maxRetries()) {
				retries++;
				try {
					newToken = this.svc.renew(this.token);
					// Update Access Token provisioned by this provider only if
					// this is the last attempt. Intermediate nulls will not be
					// considered
					if (newToken != null || (newToken == null && retries == retryPolicy.maxRetries())) {
						TemporalAccessToken<T> previousToken = this.token;
						this.token = newToken;
						this.svc.fireTokenUpdate(this.token, previousToken);
					}
				} catch (IOException e) {
					LOGGER.error("Token refresh task failed", e);
					if (retryPolicy.onException(e)) {
						try {
							TimeUnit.MILLISECONDS.sleep(retryPolicy.periodBetweenRetries());
						} catch (InterruptedException ie) {
						}
					} else {
						break;
					}
				}
			}
		}

		TemporalAccessToken<T> getToken() {
			return this.token;
		}
	}

	protected TemporalAccessToken<T> renew(TemporalAccessToken<T> token) throws OAuth2ProtocolException, IOException {
		T newToken = null;
		String refreshToken = token.token().getRefreshToken();
		// automatically fallback to fetch new token if refreshToken is null,
		// unless instructed otherwise
		if (refreshToken == null && !strictlyRefresh())
			newToken = this.getTokenService().fetch();
		if (newToken == null)
			newToken = this.getTokenService().refresh(refreshToken);
		if (newToken == null)
			return null;
		TemporalAccessToken<T> temporalToken = new TemporalAccessToken<>(newToken, Instant.now(), this.tokenExpireInTemporalUnit());
		return temporalToken;
	}

	public AutoRenewingTokenProvider<T> strictlyRefresh(boolean strictlyRefresh) {
		this.strictlyRefresh = strictlyRefresh;
		return this;
	}

	boolean strictlyRefresh() {
		return this.strictlyRefresh;
	}

	TokenService getTokenService() {
		return this.tokenService;
	}

	RetryPolicy getRetryPolicy() {
		return this.retryPolicy;
	}

	void fireTokenUpdate(final TemporalAccessToken<T> token, final TemporalAccessToken<T> previous) {
		// Notify the list of registered listeners
		if (this.observable != null) {
			try {
				this.observable.notify((listener) -> listener.tokenChanged(token, previous));
			} catch (Throwable t) {
				LOGGER.error("Change listener error", t);
			}
		}
	}

	/**
	 * Sets the time unit of measure (e.g. ChronoUnit.SECONDS,
	 * ChronUnit.MINUTES, etc.) for the <i>expires_in</i> property of access
	 * tokens. Designed for chaining.
	 * 
	 * @param temporalUnit
	 * @return owning instance for chaining.
	 */
	public AutoRenewingTokenProvider<T> tokenExpireInTemporalUnit(TemporalUnit temporalUnit) {
		this.tokenExpireInTimeUnits = temporalUnit;
		return this;
	}

	TemporalUnit tokenExpireInTemporalUnit() {
		return this.tokenExpireInTimeUnits;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.oauth2.client.ScheduledRefreshing#schedule(double)
	 */
	@Override
	public AutoRenewingTokenProvider<T> schedule(double delayModifier) {
		if (!(delayModifier > 0) || delayModifier > 1)
			throw new IllegalArgumentException("delayModifier must be value between (0-1]");
		this.delayModifier = delayModifier;
		return this;
	}

	double delayModifier() {
		return this.delayModifier;
	}

	@Override
	public Duration estimatedRepetitionsDelay() {
		T token = this.get();
		if (token == null)
			throw new IllegalStateException("No token to estimate for");
		if (token.getExpiresIn() < 1)
			throw new IllegalArgumentException("The token has no valid expires_in property: " + token.getExpiresIn());
		TemporalUnit ttlUnit = this.tokenRenewTask.getToken().ttlUnit();
		long delay = Math.round(token.getExpiresIn() * this.delayModifier);
		Duration delayDuraiton = Duration.of(delay, ttlUnit);
		return delayDuraiton;
	}

	/**
	 * Converts a {@code TimeUnit} to a {@code ChronoUnit}.
	 * <p>
	 * This handles the seven units declared in {@code TimeUnit}.
	 * 
	 * @param unit
	 *            the unit to convert, not null
	 * @return the converted unit, not null
	 */
	static ChronoUnit chronoUnit(TimeUnit unit) {
		if (unit == null)
			throw new IllegalArgumentException();
		switch (unit) {
		case NANOSECONDS:
			return ChronoUnit.NANOS;
		case MICROSECONDS:
			return ChronoUnit.MICROS;
		case MILLISECONDS:
			return ChronoUnit.MILLIS;
		case SECONDS:
			return ChronoUnit.SECONDS;
		case MINUTES:
			return ChronoUnit.MINUTES;
		case HOURS:
			return ChronoUnit.HOURS;
		case DAYS:
			return ChronoUnit.DAYS;
		default:
			throw new IllegalArgumentException("Unknown TimeUnit constant");
		}
	}

	public AutoRenewingTokenProvider<T> setRetryPolicy(RetryPolicy retryPolicy) {
		if (retryPolicy == null)
			throw new IllegalArgumentException("retryPolicy is null");
		this.retryPolicy = retryPolicy;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.oauth2.client.ScheduledRefreshing#start()
	 */
	@Override
	public ScheduledFuture<?> start() throws IOException {
		if (this.isActive())
			throw new IllegalStateException("Already started");

		// fetching from a remote service will inevitably pose some delay so we
		// defensively choose to count the fetch time from the very start.
		T newToken = this.getTokenService().fetch();
		if (newToken == null)
			throw new IllegalStateException("The token fetched from this TokenService is null");
		TemporalAccessToken<T> accessToken = new TemporalAccessToken<>(newToken, Instant.now(), this.tokenExpireInTemporalUnit());
		this.fireTokenUpdate(accessToken, null);

		this.tokenRenewTask = new TokenRenewTask(this, accessToken);
		long delayMillis = this.estimatedRepetitionsDelay().toMillis();
		this.future = this.schedulerExecutor.scheduleWithFixedDelay(this.tokenRenewTask, 0L, delayMillis,
				TimeUnit.MILLISECONDS);
		return this.future;
	}

	public boolean isActive() {
		return this.future != null && !this.future.isDone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.oauth2.client.ScheduledRefreshing#stop(boolean)
	 */
	@Override
	public void stop(boolean graceful) {
		// silently ignore if executor not started
		if (this.future != null) {
			if (graceful)
				this.schedulerExecutor.shutdown();
			else
				this.schedulerExecutor.shutdownNow();
			this.future = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.oauth2.client.Resumble#resume(T, java.time.Instant, boolean)
	 */
	@Override
	public ScheduledFuture<?> resume(T token, Instant fetchMoment, boolean refetchIfExpired) {
		if (this.isActive())
			throw new IllegalStateException("Already started");
		if (token == null)
			throw new IllegalArgumentException("Cannot resume with token null");
		// fetching from a remote service will inevitably pose some delay so we
		// defensively choose to count the fetch time from the very start.
		TemporalAccessToken<T> _token = new TemporalAccessToken<>(token, fetchMoment);
		if (_token.isExpired() && !refetchIfExpired)
			throw new IllegalStateException("Cannot resume an expired token");
		this.fireTokenUpdate(_token, null);

		this.tokenRenewTask = new TokenRenewTask(this, _token);
		long delayMillis = this.estimatedRepetitionsDelay().toMillis();
		this.future = this.schedulerExecutor.scheduleWithFixedDelay(this.tokenRenewTask,
				_token.ttlLeft(ChronoUnit.MILLIS), delayMillis, TimeUnit.MILLISECONDS);
		return this.future;
	}

	public void suspend(final boolean graceful) {
		this.stop(graceful);
	}

	/**
	 * Provides a (cached) Access Token upon invocation. The Access Token is
	 * transparently and asynchronously refreshed upon expiration. Consider
	 * using the observe() method for push notification on changes.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.oauth2.client.TokenProvider#get()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		if (this.tokenRenewTask == null || this.tokenRenewTask.getToken() == null)
			return null;
		return this.tokenRenewTask.getToken().token();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.oauth2.client.TokenChangePushing#observe(net.oauth2.client.
	 * TokenChangeListener)
	 */
	@Override
	public AutoRenewingTokenProvider<T> attach(TokenChangeObserver<T> changeObserver) {
		if (this.observable == null)
			throw new UnsupportedOperationException("This instance is not configured with observable");
		this.observable.attach(changeObserver);
		return this;
	}

}