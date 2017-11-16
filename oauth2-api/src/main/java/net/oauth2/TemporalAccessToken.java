/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

/**
 * Convenience wrapper for temporal operations with access tokens. 
 *
 * @param <T> AccessToken type
 */
public class TemporalAccessToken<T extends AccessToken> {
	
	private T token;
	private Instant validSinceMoment;
	private TemporalUnit ttlUnit;
	private Duration ttl;

	/**
	 * Constructs temporal access token wrappers considering the supplied moment as initial moment
	 * of the validity of the token and using the supplied temporal unit as reference unit for 
	 * temporal operations including operands such as the expires_in property. 
	 *   
	 * @param token
	 * @param validSinceMoment
	 * @param temporalUnit
	 */
	public TemporalAccessToken(T token, Instant validSinceMoment, TemporalUnit temporalUnit) {
		this.token = token;
		this.validSinceMoment = validSinceMoment;
		this.ttlUnit = temporalUnit;
		this.ttl = Duration.of(this.token.getExpiresIn(), this.ttlUnit);
	}
	
	/**
	 * Constructs temporal access token wrapper using the supplied moment as first moment of validity 
	 * for the token and defaulting to ChornoUnit.SECONDS as reference temporal unit for temporal  
	 * properties such as expires-in. 
	 * @param token
	 * @param validSinceMoment
	 */
	public TemporalAccessToken(T token, Instant validSinceMoment) {
		this(token, validSinceMoment, ChronoUnit.SECONDS);
	}
	
	/**
	 * Sets the reference temporal unit used in this wrapper and returns its instance for method
	 * chaining.  
	 */
	public TemporalAccessToken<T> ttlUnit(TemporalUnit unit){
		this.ttlUnit = unit;
		return this;
	}
	
	/**
	 * Returns the reference temporal unit used in this wrapper.
	 * @return
	 */
	public TemporalUnit ttlUnit() {
		return this.ttlUnit;
	}

	/**
	 * Returns the wrapped token instance
	 * @return
	 */
	public T token(){
		return this.token;
	}
	
	/**
	 * Returns the java.time.Instant since the token is valid (was issued)
	 * @return
	 */
	public Instant validSince(){
		return this.validSinceMoment;
	}
	
	/**
	 * Returns the time-to-live duration in which the token is valid.
	 * @return
	 */
	public Duration ttl(){
		return ttl;
	}
	
	/**
	 * Returns the remaining time-to-live until this token expires as duration.
	 * @return
	 */
	public Duration ttlLeft(){
		if(isExpired())
			return Duration.ZERO;
		return Duration.between(Instant.now(), validSinceMoment.plus(ttl));
	}
	
	/**
	 * Returns the remaining time-to-live until this token expires, as long value 
	 * in the requested temporal units.
	 * @param unit
	 * @return
	 */
	public long ttlLeft(ChronoUnit unit){
		if(isExpired())
			return 0;
		Duration duration = this.ttlLeft();
		switch(unit){
			case NANOS: return TimeUnit.NANOSECONDS.convert(duration.getSeconds(), TimeUnit.SECONDS) + duration.getNano();
			case MILLIS: return TimeUnit.MILLISECONDS.convert(duration.toNanos(), TimeUnit.NANOSECONDS);
			case SECONDS: return duration.getSeconds();
			case MINUTES: return duration.toMinutes();
			case HOURS: return duration.toHours();
			case DAYS: return duration.toDays();
			default: throw new IllegalArgumentException("Unsupported time unit: " + unit); 
		}
	}
	
	/**
	 * Checks if this token has expired. 
	 * The check is performed against the current moment, the moment since this token was valid
	 * and its time-to-live. 
	 * @return
	 */
	public boolean isExpired(){
		return isExpired(this.token, this.validSinceMoment, this.ttlUnit);
	}
	
	/**
	 * Static check for tokens whether they are still valid if they have been 
	 * active since the provided moment. 
	 * @param token
	 * @param since
	 * @param expireInUnits
	 * @return
	 */
	public static <T extends AccessToken> boolean isExpired(T token, Instant since, TemporalUnit expireInUnits){
		Instant expireMoment = since.plus(token.getExpiresIn(), expireInUnits);
		return expireMoment.isBefore(Instant.now());
	}
	
	/**
	 * Static object factory defaulting the initial moment of token validitiy to the current moment.
	 *  
	 * @param token
	 * @return
	 */
	public static <T extends AccessToken> TemporalAccessToken<T> create(T token){
		return new TemporalAccessToken<>(token, Instant.now());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result + ((ttl == null) ? 0 : ttl.hashCode());
		result = prime * result + ((ttlUnit == null) ? 0 : ttlUnit.hashCode());
		result = prime * result + ((validSinceMoment == null) ? 0 : validSinceMoment.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		TemporalAccessToken<T> other = (TemporalAccessToken<T>) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		if (ttl == null) {
			if (other.ttl != null)
				return false;
		} else if (!ttl.equals(other.ttl))
			return false;
		if (ttlUnit == null) {
			if (other.ttlUnit != null)
				return false;
		} else if (!ttlUnit.equals(other.ttlUnit))
			return false;
		if (validSinceMoment == null) {
			if (other.validSinceMoment != null)
				return false;
		} else if (!validSinceMoment.equals(other.validSinceMoment))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TemporalAccessToken [token=" + (token!=null?"["+token.toString()+"]":null) + ", validSinceMoment=" + validSinceMoment + ", ttlUnit=" + ttlUnit
				+ ", ttl=" + ttl + "]";
	}
	
}
