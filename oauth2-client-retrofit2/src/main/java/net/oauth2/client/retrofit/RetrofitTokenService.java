/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.retrofit;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.ProtocolError;
import net.oauth2.RefreshTokenGrantRequest;
import net.oauth2.client.TokenService;
import net.oauth2.jackson.OAuth2ObjectMapper;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;

/**
 * A squareup Retrofit Token Service delegate for fetching refreshing OAuth tokens.
 *
 * @param <S>
 * @param <T>
 */
public class RetrofitTokenService<S extends TokenEndpoint, T extends AccessToken> implements TokenService {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(RetrofitTokenService.class);

	protected String serviceBaseUrl;
	protected final AccessTokenGrantRequest grant;
	protected final TokenEndpoint tokenService;
	protected RefreshTokenGrantRequest refreshTokenGrantRequest;
	protected OkHttpClient tokenSvcClient;
	
	private static final ObjectMapper DEFAULT_MAPPER = new OAuth2ObjectMapper(new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
	
	protected ObjectMapper objectMapper;

	private Class<?> accessTokenClass;
	
	@SuppressWarnings("unchecked")
	public RetrofitTokenService(final String serviceBaseUrl, final AccessTokenGrantRequest grant, final OkHttpClient httpClient, final Class<S> serviceClass, ObjectMapper objectMapper, Class<T> accessTokenClass) {
		if (serviceBaseUrl == null)
			throw new IllegalArgumentException("serviceBaseUrl is null");
		if (grant == null)
			throw new IllegalArgumentException("grant is null");
		if (httpClient == null)
			throw new IllegalArgumentException("httpClient is null");

		this.serviceBaseUrl = serviceBaseUrl;
		this.tokenSvcClient = httpClient;
		TokenEndpoint.Builder builder = TokenEndpoint.builder().client(this.tokenSvcClient).baseUrl(this.serviceBaseUrl);
		if(serviceClass == null)
			this.tokenService = builder.build();
		else
			this.tokenService = builder.build(serviceClass);
		
		if(objectMapper == null)
			objectMapper = DEFAULT_MAPPER;
		this.objectMapper = objectMapper;
		
		if(accessTokenClass == null)
			accessTokenClass = (Class<T>) AccessToken.class;
		this.accessTokenClass = accessTokenClass;
		
		this.grant = grant;
		this.refreshTokenGrantRequest = new RefreshTokenGrantRequest(null, this.grant.getClientId(), this.grant.getClientSecret(), null);		
	}
	
	@SuppressWarnings("unchecked")
	public RetrofitTokenService(final String serviceBaseUrl, final AccessTokenGrantRequest grant, final String userId, final String password, final Class<S> serviceClass, Class<T> accessTokenClass) {
		if (serviceBaseUrl == null)
			throw new IllegalArgumentException("serviceBaseUrl is null");
		if (grant == null)
			throw new IllegalArgumentException("grant is null");
		
		this.serviceBaseUrl = serviceBaseUrl;
		this.tokenSvcClient = httpClient(userId, password);
		
		TokenEndpoint.Builder builder = TokenEndpoint.builder().client(this.tokenSvcClient).baseUrl(this.serviceBaseUrl);
		if(serviceClass == null)
			this.tokenService = builder.build();
		else
			this.tokenService = builder.build(serviceClass);
		
		this.objectMapper = DEFAULT_MAPPER;
		
		if(accessTokenClass == null)
			accessTokenClass = (Class<T>) AccessToken.class;
		this.accessTokenClass = accessTokenClass;
		
		this.grant = grant;
		this.refreshTokenGrantRequest = new RefreshTokenGrantRequest(null, this.grant.getClientId(), this.grant.getClientSecret(), null);
	}
	
	protected OkHttpClient httpClient(String userId, String password){
		return new OkHttpClient.Builder()
				.addInterceptor(chain -> {
					Request request = chain.request();
					String auth = request.header("Authorization");
					if (auth == null && !userId.equals("") && !password.equals("")) {
						request = request.newBuilder()
						.addHeader("Authorization", Credentials.basic(userId, password)).build();
					}
					return chain.proceed(request);
				})
				.build();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T fetch() throws IOException {
		LOGGER.trace("Fetching Access Token");

		Map<String, Object> grantRequestFormFields;
		try {
			grantRequestFormFields = this.grant.map();
		} catch (Exception e) {
			throw new IOException(e);
		}
		Response<String> response = this.tokenService.getAccessToken(TokenEndpoint.DEFAULT_URL_PATH, grantRequestFormFields).execute();
		
		T token = null;
		if (response.isSuccessful()) {
			
			String tokenString = response.body();
			token = (T) this.objectMapper.readValue(tokenString, this.accessTokenClass);
			
			String refreshTokenString = token.getRefreshToken();
			Collection<String> actualScopes = token.getScopes();
			if(refreshTokenString!=null){
				this.refreshTokenGrantRequest = RefreshTokenGrantRequest.renew(this.grant, refreshTokenString, actualScopes);
			}			

			LOGGER.trace("Access Token fetched");
			
		} else {
			this.handleProtocolError(response, "fetch");
		}
		return token;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T refresh(String refreshToken) throws IOException {
		if (refreshToken == null)
			throw new IllegalArgumentException("refreshToken is null");
		
		if (this.refreshTokenGrantRequest == null)
			throw new IllegalStateException("No refresh token grant initialized. Either authroization server does not support refreshing tokens or fetchToken was never invoked on this instance prior ot invoking refresh.");

		LOGGER.trace("Refreshing Access Token");
		Map<String, Object> grantRequestFormFields;
		try {
			grantRequestFormFields = this.refreshTokenGrantRequest.map();
		} catch (Exception e) {
			throw new IOException(e);
		}

		Response<String> response = this.tokenService.refreshToken(TokenEndpoint.DEFAULT_URL_PATH, grantRequestFormFields).execute();

		T token = null;
		if (response.isSuccessful()) {
			String tokenString = response.body();
			token = (T) this.objectMapper.readValue(tokenString, this.accessTokenClass);
			
			Collection<String> scopes = token.getScopes();
			if(token.getRefreshToken() != null){
				 /*If a new refresh token is issued, the refresh token scope MUST be identical to that of the refresh token included by the client in the request.*/
				if(this.refreshTokenGrantRequest.getScopes()!=scopes && (this.refreshTokenGrantRequest.getScopes().size()!= scopes.size() || !this.refreshTokenGrantRequest.getScopes().containsAll(scopes)))
					throw new IllegalStateException("The new refresh token scope'"+scopes+"' is not identical to that of the refresh token included by the client in the request: " + this.refreshTokenGrantRequest.getScopes());
			}
			
			LOGGER.trace("Access token refreshed");

		} else {
			this.handleProtocolError(response, "refresh");
		}

		return token;
	}

	protected void handleProtocolError(Response<String> response, String operationName) throws IOException {
		ProtocolError error = null;
		final String errorMsg = "Access Token " + operationName + " failed.";
		try {
			error = this.objectMapper.readValue(response.errorBody().string(), ProtocolError.class);
			LOGGER.error(errorMsg + "[{}]: {}", error.getError(), error.getDescription());
			throw new IOException(errorMsg + " [ " + error.getError() + "] " + error.getDescription());
		} catch (Throwable t) {
			LOGGER.error(t.getMessage(), t);
		} finally {
			if (error == null) {
				throw new IOException(errorMsg);
			}
		}
	}
	
}
