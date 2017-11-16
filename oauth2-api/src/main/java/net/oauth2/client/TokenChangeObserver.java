/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

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