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
 * A token provider implementation that synchronously sends requests to a OAuth Token Service to fetch
 * access tokens upon each invocation of the {@link #get()} method.
 *
 */
public class SimpleTokenProvider implements TokenProvider{

	private final TokenService tokenService;
	
	public SimpleTokenProvider(TokenService tokenService) {
		this.tokenService = tokenService;
	}
	
	/**
	 * Fetches a Token from a TokenService upon each invocation.  
	 */
	@Override
	public <T extends AccessToken> T get() throws OAuth2ProtocolException, IOException {
		T token = tokenService.fetch();
		return token;
	}
	
}
