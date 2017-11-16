/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.okhttp3;

import java.io.IOException;

import net.oauth2.AccessToken;
import net.oauth2.client.TokenProvider;
import net.oauth2.client.http.ResourceOAuthHeader;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A request interceptor for HTTP client, which transparently uses a token provider 
 * to automatically fetch/refresh OAuth tokens and inject them to the resource requests 
 * sent with this client.     
 *
 * @param <T>
 */
public class OAuthResourceRequestInterceptor<T extends AccessToken> implements Interceptor {

	TokenProvider tokenProvider;
	
	public OAuthResourceRequestInterceptor(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		T token = tokenProvider.get();
		if (token != null) {
			String headerValue = ResourceOAuthHeader.format(token);
			request = chain.request().newBuilder()
						.addHeader(ResourceOAuthHeader.HTTP_HEADER_NAME_AUTHORIZATION, headerValue).build(); 
		}
		return chain.proceed(request);
	}

}
