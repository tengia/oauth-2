/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

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