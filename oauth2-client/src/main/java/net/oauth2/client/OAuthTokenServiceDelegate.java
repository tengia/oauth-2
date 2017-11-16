/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.RefreshTokenGrantRequest;
import net.oauth2.client.http.TokenServiceHttpClient;

/**
 * Delegate for the operations on a OAuth Token Service
 *  
 * @param <T>
 */
public class OAuthTokenServiceDelegate<T extends AccessToken> implements TokenService {

	protected static Logger LOGGER = LoggerFactory.getLogger(OAuthTokenServiceDelegate.class);
	
	private AccessTokenGrantRequest grant;
	private TokenServiceHttpClient client;
	private RefreshTokenGrantRequest refreshTokenGrantRequest;
	private String pathToTokenEndpoint;
	
	public OAuthTokenServiceDelegate(final AccessTokenGrantRequest grant, TokenServiceHttpClient client, String pathToTokenEndpoint) {
		this.grant = grant;
		this.client = client;
		if(pathToTokenEndpoint == null)
			pathToTokenEndpoint = DEFAULT_URL_PATH;
		this.pathToTokenEndpoint = pathToTokenEndpoint;
	}
	
	public OAuthTokenServiceDelegate(final AccessTokenGrantRequest grant, TokenServiceHttpClient client) {
		this(grant, client, null);
	}
	
	protected Logger getLogger(){
		return LOGGER;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T fetch() throws IOException, OAuth2ProtocolException {
		this.getLogger().trace("Fetching Access Token");
		
		T accessToken = (T) this.client.post(this.pathToTokenEndpoint, this.grant);
		
		if(accessToken != null){
			String refreshTokenString = accessToken.getRefreshToken();
			Collection<String> actualScopes = accessToken.getScopes();
			if(refreshTokenString!=null){
				this.refreshTokenGrantRequest = RefreshTokenGrantRequest.renew(this.grant, refreshTokenString, actualScopes);
			}
		} else {
			this.getLogger().trace("Access Token fetched was null");	
		}

		this.getLogger().trace("Access Token fetched");
		
		return accessToken;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T refresh(String refreshToken) throws OAuth2ProtocolException, IOException {
		if (refreshToken == null)
			throw new IllegalArgumentException("refreshToken is null");
		
		if (this.refreshTokenGrantRequest == null)
			throw new IllegalStateException("No refresh token grant initialized. Either authroization server does not support refreshing tokens or fetchToken was never invoked on this instance prior ot invoking refresh.");
		
		this.getLogger().trace("Refreshing Access Token");
		
		T token = this.client.post(this.pathToTokenEndpoint, this.refreshTokenGrantRequest);
		
		if(token != null){
			Collection<String> scopes = token.getScopes();
			String refreshTokenString = token.getRefreshToken();//did we get a new refresh string?
			if(refreshTokenString == null)
				refreshTokenString = refreshToken;
			else{
				 /*If a new refresh token is issued, the refresh token scope MUST be identical to that of the refresh token included by the client in the request.*/
				if(this.refreshTokenGrantRequest.getScopes()!=scopes && (this.refreshTokenGrantRequest.getScopes().size()!= scopes.size() || !this.refreshTokenGrantRequest.getScopes().containsAll(scopes)))
					throw new IllegalStateException("The new refresh token scope'"+scopes+"' is not identical to that of the refresh token included by the client in the request: " + this.refreshTokenGrantRequest.getScopes());
			}

			RefreshTokenGrantRequest newRefreshGrant = RefreshTokenGrantRequest.renew(this.grant, refreshTokenString, scopes);
			this.setRefreshTokenGrantRequest(newRefreshGrant);
			
			this.getLogger().trace("Access token refreshed");
		} else {
			this.getLogger().trace("Access token from refresh request was null");
		}

		return token;
	}
	
	void setRefreshTokenGrantRequest(RefreshTokenGrantRequest refreshGrant){
		this.refreshTokenGrantRequest  = refreshGrant;		
	}

	RefreshTokenGrantRequest getRefreshTokenGrantRequest(){
		return this.refreshTokenGrantRequest;		
	}
	
/*	public static void main(String[] args) throws OAuth2ProtocolException, IOException {
		String clientId = "794e1695-35ad-3adb-8f2b-047a269f4f22";
		String clientSecret = "abcd1234";
		String user = clientId;
		String pass = clientSecret;
		
		String url = "https://oauthasservices-a25bdd9cf.hana.ondemand.com/oauth2/api/v1/";
		TokenServiceHttpClient client = new HttpsURLConnectionClientAdapter.Builder()
				.tokenClass(AccessToken.class)
				.baseUrl(url)
				.basicAuthentication(user, pass).build();
		
		ClientCredentialsGrantRequest cc = new ClientCredentialsGrantRequest(clientId, clientSecret, null);
		DefaultTokenService<AccessToken> dt = new DefaultTokenService<>(cc, client);
		AccessToken token = dt.fetchToken();
		System.out.println(token);
	}*/
	
}
