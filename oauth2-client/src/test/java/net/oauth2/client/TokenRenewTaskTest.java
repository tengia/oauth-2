package net.oauth2.client;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.AccessToken;
import net.oauth2.TemporalAccessToken;

@RunWith(MockitoJUnitRunner.class) 
public class TokenRenewTaskTest {

	@Mock AutoRenewingTokenProvider<AccessToken> refreshingTokenProvider;
	@Mock TokenService tokenService;
	@Mock MinimalRetryPolicy minimialRetryPolicy;
	@Mock NoRetryPolicy noRetryPolicy;
	
	@Test(expected=IllegalArgumentException.class)
	public void testNewInstanceFailNullRefreshToken() {
		AccessToken token = new AccessToken(null, null, 0, null, null);
		TemporalAccessToken<AccessToken> temporalToken = TemporalAccessToken.create(token);
		given(this.refreshingTokenProvider.strictlyRefresh()).willReturn(true);
		refreshingTokenProvider.new TokenRenewTask(this.refreshingTokenProvider, temporalToken);
		
		verifyZeroInteractions(this.refreshingTokenProvider);
	}
	
	@Test
	public void testNewInstance() {
		final long expiresInSeconds = 3600;
		AccessToken token = new AccessToken(null, null, expiresInSeconds, "", null);
		TemporalAccessToken<AccessToken> temporalToken = TemporalAccessToken.create(token);
		
		given(this.refreshingTokenProvider.tokenExpireInTemporalUnit()).willReturn(ChronoUnit.MILLIS);
		refreshingTokenProvider.new TokenRenewTask(this.refreshingTokenProvider, temporalToken);
		
		verifyNoMoreInteractions(this.refreshingTokenProvider);
	}
	
	@Test
	public void testRun() throws IOException {
		final long expiresIn = 1L;
		AccessToken token = new AccessToken(null, null, expiresIn, "test-refresh-token", null);
		Instant tokenFetchMoment = Instant.now().minus(2L, ChronoUnit.SECONDS);
		TemporalAccessToken<AccessToken> temporalToken = new TemporalAccessToken<>(token, tokenFetchMoment);
		AccessToken refreshedToken = new AccessToken(null, null, expiresIn, "test-refresh-token", null);
		TemporalAccessToken<AccessToken> temporalRefreshedToken = TemporalAccessToken.create(refreshedToken);

		given(this.refreshingTokenProvider.getRetryPolicy()).willReturn(this.noRetryPolicy);
		given(this.noRetryPolicy.maxRetries()).willReturn(new Long(1));
		given(this.refreshingTokenProvider.renew(any())).willReturn(temporalRefreshedToken);
		
		AutoRenewingTokenProvider<AccessToken>.TokenRenewTask task = refreshingTokenProvider.new TokenRenewTask(this.refreshingTokenProvider, temporalToken);
				
		task.run();
		
		verify(this.refreshingTokenProvider).getRetryPolicy();
		verify(this.noRetryPolicy).maxRetries();
		verify(this.refreshingTokenProvider).renew(any());
		verify(this.refreshingTokenProvider).fireTokenUpdate(eq(temporalRefreshedToken), eq(temporalToken));
		verifyNoMoreInteractions(this.refreshingTokenProvider, this.noRetryPolicy, this.tokenService);
	}

	@Test
	public void testRunNewTokenNull() throws IOException {
		final long expiresIn = 0;
		AccessToken token = new AccessToken(null, null, expiresIn, "test-refresh-token", null);
		Instant tokenFetchMoment = Instant.now().minus(10, ChronoUnit.MILLIS);
		TemporalAccessToken<AccessToken> temporalToken = new TemporalAccessToken<>(token, tokenFetchMoment);
		
		AutoRenewingTokenProvider<AccessToken>.TokenRenewTask task = refreshingTokenProvider.new TokenRenewTask(this.refreshingTokenProvider, temporalToken);
		
		given(this.refreshingTokenProvider.renew(temporalToken)).willReturn(null);
		given(this.refreshingTokenProvider.getRetryPolicy()).willReturn(new NoRetryPolicy());
		
		task.run();
		
		verify(this.refreshingTokenProvider, times(1)).renew(Matchers.<TemporalAccessToken<AccessToken>>any());
		verify(this.refreshingTokenProvider, times(1)).fireTokenUpdate(null, temporalToken);
	}

	@Test
	public void testRunNoRetryPolicyWithException() throws IOException {
		final long expiresInSeconds = 20;
		AccessToken token = new AccessToken(null, null, expiresInSeconds, "test-refresh-token", null);
		Instant tokenFetchMoment = Instant.now().minus(10, ChronoUnit.MILLIS);
		TemporalAccessToken<AccessToken> temporalToken = new TemporalAccessToken<>(token, tokenFetchMoment);

		given(this.refreshingTokenProvider.tokenExpireInTemporalUnit()).willReturn(ChronoUnit.MILLIS);
		AutoRenewingTokenProvider<AccessToken>.TokenRenewTask task = refreshingTokenProvider.new TokenRenewTask(this.refreshingTokenProvider, temporalToken);
		
		given(this.refreshingTokenProvider.renew(any())).willThrow(new IOException());
		given(this.refreshingTokenProvider.getRetryPolicy()).willReturn(this.noRetryPolicy);
				
		given(this.noRetryPolicy.onException(any(IOException.class))).willReturn(false);
		given(this.noRetryPolicy.maxRetries()).willReturn(new Long(1));
		
		task.run();

		verify(this.refreshingTokenProvider).getRetryPolicy();
		verify(this.refreshingTokenProvider).renew(any());		
		verify(this.noRetryPolicy).onException(any(IOException.class));
		verify(this.noRetryPolicy).maxRetries();

		verifyNoMoreInteractions(this.refreshingTokenProvider, this.tokenService, this.noRetryPolicy);
	}
	
	@Test
	public void testRunMinimalRetryPolicyWithException() throws IOException {
		final long expiresInSeconds = 11;
		AccessToken token = new AccessToken(null, null, expiresInSeconds, "test-refresh-token", null);
		Instant tokenFetchMoment = Instant.now().minus(10, ChronoUnit.MILLIS);
		TemporalAccessToken<AccessToken> temporalToken = new TemporalAccessToken<>(token, tokenFetchMoment);

		given(this.refreshingTokenProvider.tokenExpireInTemporalUnit()).willReturn(ChronoUnit.MILLIS);
		AutoRenewingTokenProvider<AccessToken>.TokenRenewTask task = refreshingTokenProvider.new TokenRenewTask(this.refreshingTokenProvider, temporalToken);
		
		given(this.refreshingTokenProvider.renew(any())).willThrow(new IOException());
		given(this.refreshingTokenProvider.getRetryPolicy()).willReturn(this.minimialRetryPolicy);
		given(this.minimialRetryPolicy.onException(any(IOException.class))).willReturn(true);
		given(this.minimialRetryPolicy.maxRetries()).willReturn(new Long(3));
		given(this.minimialRetryPolicy.periodBetweenRetries()).willReturn(new Long(100));
		
		task.run();
		
		try {
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		verify(this.refreshingTokenProvider, times(3)).renew(Matchers.<TemporalAccessToken<AccessToken>>any());
		verify(this.refreshingTokenProvider).getRetryPolicy();
		verify(this.minimialRetryPolicy, times(4)).maxRetries();
		verify(this.minimialRetryPolicy, times(3)).onException(any(IOException.class));
		verify(this.minimialRetryPolicy, times(3)).periodBetweenRetries();
		verifyNoMoreInteractions(this.refreshingTokenProvider, this.tokenService, this.minimialRetryPolicy);
	}
	
	@SuppressWarnings("unchecked")
	@After
	public void afterTest(){
		Mockito.reset(this.refreshingTokenProvider);
	}

}
