package net.oauth2.client;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;

import net.oauth2.AccessToken;

/**
 * A Token Provider that repeatedly provides Access Token. Implementations may
 * use the refresh capabilities of the OAuth infrastructure, if any, or re-fetch
 * a new Access Token with each repetition.
 * 
 * @param <T>
 *            The AccessToken type
 */
public interface TokenProviderJob<T extends AccessToken> {

	/**
	 * <div>Schedules the repeated executions by setting their delay as
	 * percentage of the Time-To-Live (TTL) of the Access Token. This value is
	 * used then to calculate the estimated delay between repetitions (execution
	 * interval) of the repeating task for refreshing tokens. The token TTL is
	 * specified by its <i>expires_in</i> property in the temporal unit provided
	 * by {@link #getEstimatedRepetitionsDelayTemporalUnit()} (usually
	 * {@link ChronoUnit#SECONDS}). The argument must be a double value in the
	 * range (0-1] where 1 stands for 100% of the Access Token TTL.</div>
	 * 
	 * <p>
	 * <b>A few notes on fine-tuning the repetitions schedule.</b>
	 * </p>
	 * 
	 * Providing 1 as argument to this method will initiate refresh
	 * <b>exactly</b> at the expected moment of expiration. However, calculating
	 * this moment cannot be entirely precise because:
	 * <ol>
	 * <li>Tokens provide TTL, but not the exact moment since when this is
	 * accounted for. Hence, it's up to the client to approximate this moment on
	 * behalf of the token service and that is always at best <i>close</i> to
	 * true.</li>
	 * <li>Since there is networking involved the networking lag is a variable
	 * that can be forecasted but the precision of the forecast cannot be
	 * considered 100% reliable.</li>
	 * </ol>
	 * Therefore, it's a good practice to set the scheduled runs as a percentage
	 * close to 1 rather than exactly 1, e.g. 0.9. <div>The value precision
	 * should be calculated with respect to the actual TTL amount of the access
	 * token and considering that ultimately it will be rounded to seconds using
	 * Math.round. Here is a few examples to give you the idea. <div>It is quite
	 * often the case (without being normative) that the TTL of tokens is 1 hour
	 * (3600 seconds). A value of 0.9 set to the schedule will wake up the
	 * refresh routine then 6 minutes before the expire time.</div>
	 * <div>Consider the case when a TTL is 1 second, which is highly unlikely,
	 * but technically possible. The execution scheduled at 0.9*TTL will be then
	 * triggered each 900 milliseconds, which is probably not what you
	 * want.</div><div>On the other extreme, consider that the TTL is one year
	 * (31536K seconds). A schedule triggering refresh each 0.9*TTL then is
	 * about 36.5 days before end of TTL, which is probably also not what you
	 * intended. You would rather use a value of higher precision, such as
	 * 0.99998, which would bring you closer to the end of TTL (~10 minutes
	 * beforehand), while leaving enough room to recover from failures and still
	 * keeping resource usage at minimum.</div>
	 * 
	 * @param delayModifier
	 */
	TokenProviderJob<T> schedule(double delayModifier);

	/**
	 * Starts a token refresh background job after successfully fetching an
	 * AccessToken from TokenService. There can be only one refreshing job
	 * associated with a RefreshingTokenProvider instance. Attempts to start
	 * this token provider more than once will throw
	 * {@link IllegalStateException}.
	 * 
	 * @return a {@link ScheduledFuture} for direct access to the API of the
	 *         underlying refresh job scheduler service.
	 * @throws IOException
	 *             if the attempt to fetch an access token to start the refresh
	 *             job failed
	 * @throws IllegalStateException
	 *             if a job has already been started or if fetching a token did
	 *             not throw an IOException but the token was null.
	 */
	ScheduledFuture<?> start() throws IOException;

	/**
	 * Stops the asynchronous refreshing job started by this provider (if any)
	 * 
	 * @param graceful
	 *            false if hard shutdown is required, true otherwise.
	 */
	void stop(boolean graceful);

	/**
	 * Convenience check that returns the active status of this Token Provider
	 * Job. A job is active when it has been started successfully and is not
	 * done yet. The future returned form the {@link #start()} operation can be
	 * used for the same purpose.
	 * 
	 * @return the active status of this Token Provider Job
	 */
	boolean isActive();
	
	/**
	 * <div>Provides the real estimated delay between two token renew
	 * repetitions. The temporal unit of measure of this value is provided by
	 * the {@link #getEstimatedRepetitionsDelayTemporalUnit()} method.</div>
	 * <div>Note that invoking this method is valid only at a time when the TTL
	 * of the access tokens provisioned by the underlying OAuth infrastructure
	 * is already known, or an IllegalStateException is thrown.</div>
	 * 
	 * @return the actual estimated delay of token renew repetitions
	 */
	Duration estimatedRepetitionsDelay();
		
}