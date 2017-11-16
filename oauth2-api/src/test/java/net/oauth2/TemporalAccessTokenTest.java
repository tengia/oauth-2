/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TemporalAccessTokenTest {

	@Test
	public void testTemporalAccessTokenTInstantTemporalUnit() {
		AccessToken token = new AccessToken(null, null, 123L, null, null);
		Instant validSince = Instant.now();
		
		TemporalAccessToken<AccessToken> ttoken = new TemporalAccessToken<>(token, validSince, ChronoUnit.MILLIS);
		
		assertEquals(token, ttoken.token());
		assertEquals(validSince, ttoken.validSince());
		assertEquals(ChronoUnit.MILLIS, ttoken.ttlUnit());
	}

	@Test
	public void testTemporalAccessTokenTInstant() {
		AccessToken token = new AccessToken(null, null, 123L, null, null);
		Instant validSince = Instant.now();
		
		TemporalAccessToken<AccessToken> ttoken = new TemporalAccessToken<>(token, validSince);
		
		assertEquals(token, ttoken.token());
		assertEquals(validSince, ttoken.validSince());
		assertEquals(ChronoUnit.SECONDS, ttoken.ttlUnit());
	}

	@Test
	public void testTtlUnit() {
		AccessToken token = new AccessToken(null, null, 123L, null, null);
		Instant validSince = Instant.now();
		
		TemporalAccessToken<AccessToken> ttoken = new TemporalAccessToken<>(token, validSince);
		ttoken.ttlUnit(ChronoUnit.MILLIS);
		
		assertEquals(ChronoUnit.MILLIS, ttoken.ttlUnit());
	}

	@Test
	public void testTtl() {
		AccessToken token = new AccessToken(null, null, 123L, null, null);
		Instant validSince = Instant.now();
		
		TemporalAccessToken<AccessToken> ttoken = new TemporalAccessToken<>(token, validSince);
		Duration ttlDuration = ttoken.ttl();
		
		assertTrue(token.getExpiresIn() == TimeUnit.SECONDS.convert(ttlDuration.toNanos(), TimeUnit.NANOSECONDS));
	}

	@Test
	public void testTtlLeft() {
		AccessToken token = new AccessToken(null, null, 1500L, null, null);
		Instant validSince = Instant.now().minus(1, ChronoUnit.SECONDS);
		
		TemporalAccessToken<AccessToken> ttoken = new TemporalAccessToken<>(token, validSince, ChronoUnit.MILLIS);
		Duration ttlLeftDuration = ttoken.ttlLeft();
		//a millisecond or so is lost while processing these lines so comparison is not exact but within limits
		assertTrue((500 - TimeUnit.MILLISECONDS.convert(ttlLeftDuration.toNanos(), TimeUnit.NANOSECONDS)) < 10);
	}

	@Test
	public void testTtlLeftChronoUnit() {
		AccessToken token = new AccessToken(null, null, 1500L, null, null);
		Instant validSince = Instant.now().minus(1, ChronoUnit.SECONDS);
		
		TemporalAccessToken<AccessToken> ttoken = new TemporalAccessToken<>(token, validSince, ChronoUnit.SECONDS);
		long ttlLeft = ttoken.ttlLeft(ChronoUnit.MILLIS);
		
		assertTrue((500 - ttlLeft) < 10);
	}

	@Test
	public void testIsExpiredTrue() {
		AccessToken token = new AccessToken(null, null, 500L, null, null);
		Instant validSince = Instant.now().minus(1, ChronoUnit.SECONDS);
		
		TemporalAccessToken<AccessToken> ttoken = new TemporalAccessToken<>(token, validSince, ChronoUnit.MILLIS);
		boolean expired = ttoken.isExpired();
		
		assertTrue(expired);
	}
	
	@Test
	public void testIsExpiredFalse() {
		AccessToken token = new AccessToken(null, null, 1500L, null, null);
		Instant validSince = Instant.now().minus(1, ChronoUnit.SECONDS);
		
		TemporalAccessToken<AccessToken> ttoken = new TemporalAccessToken<>(token, validSince, ChronoUnit.SECONDS);
		boolean expired = ttoken.isExpired();
		
		assertFalse(expired);
	}

}
