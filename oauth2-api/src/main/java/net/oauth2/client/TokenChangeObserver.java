package net.oauth2.client;

import net.oauth2.AccessToken;
import net.oauth2.TemporalAccessToken;

/**
 * Functional interface for listeners to asynchronous (push) provisioning of
 * Access Token update events.
 *
 * @param <T>
 */
@FunctionalInterface
public interface TokenChangeObserver<T extends AccessToken> {

	/**
	 * Fired by {@link TokenChangeObservable} when a token value has changed,
	 * either because it was fetched for the first time, or because it was
	 * refreshed.
	 * 
	 * @param newToken
	 * @param oldToken
	 */
	public void tokenChanged(TemporalAccessToken<T> newToken, TemporalAccessToken<T> oldToken);

}