/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.AccessToken;
import net.oauth2.TemporalAccessToken;

@RunWith(MockitoJUnitRunner.class)
public class AutoRenewingTokenProviderTest {

	@Mock
	TokenService tokenService;
	@Mock
	ScheduledThreadPoolExecutor scheduler;
	@Mock
	ScheduledFuture<Void> future;
	@Mock
	AutoRenewingTokenProvider<AccessToken>.TokenRenewTask tokenRenewTask;

	@After
	public void afterTest() {
		Mockito.reset(this.tokenService);
	}

	@Test
	public void testGetTokenWhenStarted() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		TemporalAccessToken<AccessToken> expectedTemporalToken = TemporalAccessToken.create(new AccessToken(new HashMap<>()));
		trs.tokenRenewTask = this.tokenRenewTask; 
		given(trs.tokenRenewTask.getToken()).willReturn(expectedTemporalToken);
		AccessToken token = trs.get();
		assertEquals(expectedTemporalToken.token(), token);
		verify(tokenRenewTask, times(2)).getToken();
	}
	
	@Test
	public void testGetTokenNullWhileNotStarted()  {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		AccessToken token = trs.get();
		assertNull(token);
	}
	
	@Test
	public void testDelayDuration()  {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		TemporalAccessToken<AccessToken> expectedTemporalToken = TemporalAccessToken
				.create(new AccessToken(null, null, 1L, null, null));
		trs.tokenRenewTask = this.tokenRenewTask; 
		trs.future = this.future;
		given(trs.tokenRenewTask.getToken()).willReturn(expectedTemporalToken);		
		Duration delayDuration = trs.estimatedRepetitionsDelay();
		assertNotNull(delayDuration);
		assertEquals(0, delayDuration.compareTo(Duration.ofSeconds(1L)));
	}
	
/*	@Test
	public void testDelayDurationWhileNotStarted() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		try{
			trs.estimatedRepetitionsDelay();
			fail("expected exeception not thrown");
		} catch (IllegalStateException e){
			assertEquals("Not started", e.getMessage());
		}
	}*/
	
	@Test
	public void testDelayDurationWhenTokenNull() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		trs.tokenRenewTask = this.tokenRenewTask; 
		trs.future = this.future;
		given(trs.tokenRenewTask.getToken()).willReturn(null);
		try{
			trs.estimatedRepetitionsDelay();
			fail("expected exeception not thrown");
		} catch (IllegalStateException e){
			assertEquals("No token to estimate for", e.getMessage());
		} 
	}
	
	@Test
	public void testDelayDurationWhenExpireLessThanOne() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		TemporalAccessToken<AccessToken> expectedTemporalToken = TemporalAccessToken
				.create(new AccessToken(null, null, 0L, null, null));
		trs.tokenRenewTask = this.tokenRenewTask; 
		trs.future = this.future;
		given(trs.tokenRenewTask.getToken()).willReturn(expectedTemporalToken);
		try{
			trs.estimatedRepetitionsDelay();
			fail("expected exeception not thrown");
		} catch (IllegalArgumentException e){
			assertEquals("The token has no valid expires_in property: 0", e.getMessage());
		}
	}
	
/*	@Test
	public void testDelayNullWhileNotStarted() throws OAuth2ProtocolException, IOException {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		try{
			trs.getEstimatedRepetitionsDelay();
			fail("expected exeception not thrown");
		} catch (IllegalStateException e){
			assertEquals("Not started", e.getMessage());
		}
	}*/
	
	@Test
	public void testDelayNullWhileNotStarted() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		try{
			trs.estimatedRepetitionsDelay();
			fail("expected exeception not thrown");
		} catch (IllegalStateException e){
			assertEquals("No token to estimate for", e.getMessage());
		}
	}

	@Test
	public void testStartFailAlreadyStarted() throws IOException {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		trs.future = this.future;
		try{
			trs.start();
			fail("expected exeception not thrown");
		} catch (IllegalStateException e){
			assertEquals("Already started", e.getMessage());
		}
	}

/*	@Test
	public void testStart() throws IOException, InterruptedException, ExecutionException {
		long expireInPeriod = 600L;// Tokens TTL is 600ms
		AccessToken fetchedToken = new AccessToken("test-token", "token-type", expireInPeriod, "refresh-token", null);
		given(this.tokenService.fetch()).willReturn(fetchedToken);
		AccessToken refreshedToken = new AccessToken("test-token1", "token-type", expireInPeriod, "refresh-token",
				null);
		given(this.tokenService.refresh("refresh-token")).willReturn(refreshedToken);
		RefreshingTokenProvider<AccessToken> trs = new RefreshingTokenProvider<>(this.tokenService);
		trs.schedule(100L, TimeUnit.MILLISECONDS);// period between running task to check if token expired and refresh is 100ms
		trs.tokenExpireInTimeUnit = ChronoUnit.MILLIS;
		trs.setRefreshBeforeExpireThreshold(200L, ChronoUnit.MILLIS);// set the trigger to run the refresh to 200ms before the actual token expiration(will run exactly at the expire or after)

		trs.start();
		TimeUnit.MILLISECONDS.sleep(1200L);
		trs.schedulerExecutor.shutdownNow();

		assertEquals(refreshedToken, trs.get());
		verify(tokenService).fetch();
		verify(tokenService, times(2)).refresh(eq("refresh-token"));
	}*/

	@Test
	public void testStopGracfulWhenStarted() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		trs.future = this.future;
		trs.schedulerExecutor = this.scheduler;
		trs.stop(true);
		verify(this.scheduler).shutdown();
	}

	@Test
	public void testStopForcefulWhenStarted() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		trs.future = this.future;
		trs.schedulerExecutor = this.scheduler;
		trs.stop(false);
		verify(this.scheduler).shutdownNow();
	}

	@Test
	public void testStopWhenNotStarted() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		trs.schedulerExecutor = this.scheduler;
		trs.stop(false);
		verify(this.scheduler, times(0)).shutdown();
	}

/*	@Test(expected = IllegalStateException.class)
	public void testSetWakeUpIntervalSecondsInIllegalState() {
		RefreshingTokenProvider<AccessToken> trs = new RefreshingTokenProvider<>(this.tokenService);
		trs.future = this.future;
		trs.schedule(666, TimeUnit.SECONDS);
		assertTrue(trs.getRefreshBeforeExpireThreshold() != 666);
	}

	@Test(expected = IllegalStateException.class)
	public void testSetMinutesBeforeExpirationWakeupInIllegalState() {
		RefreshingTokenProvider<AccessToken> trs = new RefreshingTokenProvider<>(this.tokenService);
		trs.future = this.future;
		trs.setRefreshBeforeExpireThreshold(222, ChronoUnit.SECONDS);
		assertTrue(trs.getRefreshBeforeExpireThreshold() != 222);
	}*/

	@Test
	public void testSetRetryPolicy() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		RetryPolicy policy = new RetryPolicy() {
			@Override
			public long periodBetweenRetries() {
				return 0;
			}

			@Override
			public long maxRetries() {
				return 0;
			}

			@Override
			public boolean onException(Throwable t) {
				return false;
			}
		};
		trs.setRetryPolicy(policy);
		assertEquals(policy, trs.getRetryPolicy());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetRetryPolicyNull() {
		AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService);
		RetryPolicy policy = null;
		trs.setRetryPolicy(policy);
		assertNull(trs.getRetryPolicy());
	}

	@Test
	public void testNoRetryPolicy() {
		RetryPolicy policy = new NoRetryPolicy();
		assertEquals(1, policy.maxRetries());
		assertFalse(policy.onException(new IOException()));
		assertEquals(0, policy.periodBetweenRetries());
	}

	@Test
	public void testMinRetryPolicy() {
		RetryPolicy policy = new MinimalRetryPolicy();
		assertEquals(3, policy.maxRetries());
		assertTrue(policy.onException(new IOException()));
		assertEquals(60000L, policy.periodBetweenRetries());
	}

/*	@Test
	public void testPushTokenWhenStarted() throws OAuth2ProtocolException, IOException, InterruptedException {
		long expireInPeriod = 600L;// Tokens TTL is 600ms
		AccessToken fetchedToken = new AccessToken("test-token", "token-type", expireInPeriod, "refresh-token", null);
		given(this.tokenService.fetch()).willReturn(fetchedToken);
		AccessToken refreshedToken = new AccessToken("test-token1", "token-type", expireInPeriod, "refresh-token",
				null);
		given(this.tokenService.refresh("refresh-token")).willReturn(refreshedToken);
		RefreshingTokenProvider<AccessToken> trs = new RefreshingTokenProvider<>(this.tokenService);
		trs.schedule(100L, TimeUnit.MILLISECONDS);// period between running task to check if token expired and refresh is 100ms
		trs.tokenExpireInTimeUnit = ChronoUnit.MILLIS;
		trs.setRefreshBeforeExpireThreshold(200L, ChronoUnit.MILLIS);// set the trigger to run the refresh to 200ms before the actual token expiration(will run exactly at the expire or after)

		final AtomicInteger i = new AtomicInteger(0);
		trs.observe(new TokenChangeListener<AccessToken>() {		
			@Override
			public void tokenChanged(AccessToken newToken, AccessToken oldToken) {
				int a= i.getAndIncrement();
				//System.out.println("["+a+"]"+oldToken+ " : " + newToken);
				if(a==0){
					assertEquals(fetchedToken, newToken);
					assertNull(oldToken);
				} else {
					try {
						assertEquals(refreshedToken, trs.get());
					} catch (Exception e) {
						fail(e.getMessage());
						throw new RuntimeException(e);
					}
				}
			}
		});
		
		trs.start();
		
		TimeUnit.MILLISECONDS.sleep(1200L);
		trs.schedulerExecutor.shutdownNow();
		
		verify(tokenService).fetch();
		verify(tokenService, times(2)).refresh(eq("refresh-token"));
	}*/
	
	@Test
	public void testRefresh() throws IOException {
		long expireInPeriod = 600L;// Tokens TTL is 600ms
		final TemporalAccessToken<AccessToken> temporalFetchedToken = TemporalAccessToken.create(new AccessToken("fetched",null,expireInPeriod,"refresh-token",null)).ttlUnit(ChronoUnit.MILLIS);
		given(this.tokenService.fetch()).willReturn(temporalFetchedToken.token());
		final TemporalAccessToken<AccessToken> refreshedToken = TemporalAccessToken.create(new AccessToken("refreshed",null,expireInPeriod,"refresh-token",null)).ttlUnit(ChronoUnit.MILLIS);
		given(this.tokenService.refresh(anyString())).willReturn(refreshedToken.token());		
		
		final AutoRenewingTokenProvider<AccessToken> trs = new AutoRenewingTokenProvider<>(this.tokenService)
			.schedule(0.9)// period between running task to check if token expired and refresh is 90% of token validity duration ~540ms
			.tokenExpireInTemporalUnit(ChronoUnit.MILLIS)
			.attach(tokenChangeObserver);
		trs.start();
		
		try {
			TimeUnit.MILLISECONDS.sleep(600);
		} catch (InterruptedException e) {}
		trs.schedulerExecutor.shutdownNow();
		
		assertEquals(refreshedToken.token(), trs.get());
		verify(tokenService).fetch();
		verify(tokenService, times(2)).refresh(eq("refresh-token"));
		verify(tokenChangeObserver,times(3)).tokenChanged(tokenChangeNewTokenObserverCaptor.capture(), tokenChangeOldTokenObserverCaptor.capture());
		List<TemporalAccessToken<AccessToken>> newTokens = tokenChangeNewTokenObserverCaptor.getAllValues();
		assertEquals(temporalFetchedToken.token(), newTokens.get(0).token());
		assertEquals(refreshedToken.token(), newTokens.get(1).token());
		List<TemporalAccessToken<AccessToken>> oldTokens = tokenChangeOldTokenObserverCaptor.getAllValues();
		assertNull(oldTokens.get(0));
		assertEquals(temporalFetchedToken.token(), oldTokens.get(1).token());
		assertEquals(refreshedToken.token(), oldTokens.get(2).token());
	}
	
	@Mock TokenChangeObserver<AccessToken> tokenChangeObserver;
	@Captor ArgumentCaptor<TemporalAccessToken<AccessToken>> tokenChangeNewTokenObserverCaptor;
	@Captor ArgumentCaptor<TemporalAccessToken<AccessToken>> tokenChangeOldTokenObserverCaptor;
	
	@Test
	public void test() throws IOException {
		
		final TemporalAccessToken<AccessToken> fetchedToken = TemporalAccessToken.create(new AccessToken("fetched",null,10L,"refresh_token",null)).ttlUnit(ChronoUnit.MILLIS);
		given(this.tokenService.fetch()).willReturn(fetchedToken.token());
		final TemporalAccessToken<AccessToken> refreshedToken = TemporalAccessToken.create(new AccessToken("refreshed",null,10L,"refresh_token",null)).ttlUnit(ChronoUnit.MILLIS);
		given(this.tokenService.refresh(anyString())).willReturn(refreshedToken.token());
		
		AutoRenewingTokenProvider<AccessToken> provider = new AutoRenewingTokenProvider<>(this.tokenService)
				.schedule(0.9)
				.strictlyRefresh(true)
				.setRetryPolicy(new NoRetryPolicy())
				.tokenExpireInTemporalUnit(ChronoUnit.MILLIS)
				.attach(tokenChangeObserver);
		
		provider.start();
		try {
			TimeUnit.MILLISECONDS.sleep(20);
		} catch (InterruptedException e) {}
		provider.stop(true);
		
		verify(tokenChangeObserver,times(3)).tokenChanged(tokenChangeNewTokenObserverCaptor.capture(), tokenChangeOldTokenObserverCaptor.capture());
		List<TemporalAccessToken<AccessToken>> newTokens = tokenChangeNewTokenObserverCaptor.getAllValues();
		assertEquals(fetchedToken.token(), newTokens.get(0).token());
		assertEquals(refreshedToken.token(), newTokens.get(1).token());
		List<TemporalAccessToken<AccessToken>> oldTokens = tokenChangeOldTokenObserverCaptor.getAllValues();
		assertNull(oldTokens.get(0));
		assertEquals(fetchedToken.token(), oldTokens.get(1).token());
	}
}