/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2.client;

import java.io.IOException;

import net.oauth2.AccessToken;

/**
 * Functional capability for synchronous provisioning of Access Tokens.
 * Clients will need to actively call the {@link #get) method to receive a token and the call will 
 * block waiting until the token is delivered. 
 * 
 * Simplistic implementations will result in network requests to a token service on each call. But a 
 * combination with a job that continuously updates tokens asynchronously and get method that returns the 
 * last known good immediately may be another option.
 * 
 * For asynchronous token providers see {@link TokenProviderJob}     
 *
 */
public interface TokenProvider {

	/**
	 * Return token synchronously
	 * 
	 * @return
	 * @throws OAuth2ProtocolException
	 */
	<T extends AccessToken> T get() throws OAuth2ProtocolException, IOException;

}