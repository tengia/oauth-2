/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.apache.httpcomponents;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import net.oauth2.AccessToken;
import net.oauth2.client.TokenProvider;
import net.oauth2.client.http.ResourceOAuthHeader;

public class OAuthResourceRequestInterceptor<T extends AccessToken> implements HttpRequestInterceptor {
		
	TokenProvider tokenProvider;
	
	public OAuthResourceRequestInterceptor(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		T token = tokenProvider.get();
		String headerValue = ResourceOAuthHeader.format(token);
		request.addHeader(ResourceOAuthHeader.HTTP_HEADER_NAME_AUTHORIZATION, headerValue);
	}

}
