/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2.client.http;

import net.oauth2.AccessToken;

/**
 * Utility class for formatting OAuth HTTP request headers
 */
public class ResourceOAuthHeader {

	private static final String HTTP_HEADER_AUTHORIZATION_VALUE_PATTERN = "%s %s";
	
	public static final String HTTP_HEADER_NAME_AUTHORIZATION = "Authorization";
	
	/**
	 * Formats a OAuth access token into standard OAuth value for the Authorization HTTP request header
	 * @param token
	 * @return
	 */
	public static String format(AccessToken token){
		return String.format(HTTP_HEADER_AUTHORIZATION_VALUE_PATTERN, token.getTokenType(), token.getAccessToken());
	}

}
