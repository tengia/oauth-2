package net.oauth2.client;

import net.oauth2.AccessToken;

/**
 * Functional capability to install listeners for token updates and support
 * asynchronous push notification on changes.
 *
 * @param <T>
 */
public interface TokenChangeObservable<T extends AccessToken> {

	/**
	 * Subscribes an observer to notification upon Access Token changes.
	 * 
	 * @param tokenChangeObserver
	 *            {@link TokenChangeObserver} subscriber for token changes. A
	 *            lambda expression, inner class or normal class implementing
	 *            the interface are all suitable arguments:
	 * 			  <pre>
	 *            attach(new TokenChangeObserver<>(){
	 *            	 public void tokenChanged(TemporalAccessToken<T> newToken, TemporalAccessToken<T> oldToken){
	 *            		System.out.println(newToken + ":" + oldToken);
	 *            	 }
	 *            });
	 *            </pre>
	 *            <pre>
	 *            attach((newToken, oldToken) -> System.out.println(newToken + ":" + oldToken));
	 *            </pre>
	 */
	TokenChangeObservable<T> attach(TokenChangeObserver<T> tokenChangeObserver);

}