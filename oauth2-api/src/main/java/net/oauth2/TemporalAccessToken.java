package net.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

public class TemporalAccessToken<T extends AccessToken> {
	
	private T token;
	private Instant validSinceMoment;
	private TemporalUnit ttlUnit;
	private Duration ttl;

	public TemporalAccessToken(T token, Instant validSinceMoment, TemporalUnit temporalUnit) {
		this.token = token;
		this.validSinceMoment = validSinceMoment;
		this.ttlUnit = temporalUnit;
		this.ttl = Duration.of(this.token.getExpiresIn(), this.ttlUnit);
	}
	
	public TemporalAccessToken(T token, Instant validSinceMoment) {
		this(token, validSinceMoment, ChronoUnit.SECONDS);
	}
	
	public TemporalAccessToken<T> ttlUnit(TemporalUnit unit){
		this.ttlUnit = unit;
		return this;
	}
	
	public TemporalUnit ttlUnit() {
		return this.ttlUnit;
	}

	
	public T token(){
		return this.token;
	}
	
	public Instant validSince(){
		return this.validSinceMoment;
	}
	
	public Duration ttl(){
		return ttl;
	}
	
	public Duration ttlLeft(){
		if(isExpired())
			return Duration.ZERO;
		return Duration.between(Instant.now(), validSinceMoment.plus(ttl));
	}
	
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
	
	public boolean isExpired(){
		return isExpired(this.token, this.validSinceMoment, this.ttlUnit);
	}
	
	public static <T extends AccessToken> boolean isExpired(T token, Instant since, TemporalUnit expireInUnits){
		Instant expireMoment = since.plus(token.getExpiresIn(), expireInUnits);
		return expireMoment.isBefore(Instant.now());
	}
	
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
