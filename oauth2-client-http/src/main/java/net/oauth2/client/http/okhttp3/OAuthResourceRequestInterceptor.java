package net.oauth2.client.http.okhttp3;

import java.io.IOException;

import net.oauth2.AccessToken;
import net.oauth2.client.TokenProvider;
import net.oauth2.client.http.ResourceOAuthHeader;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

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
