/*
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies
 * this distribution, and is available at
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A Java object model binding for the standard OAuth 2 access token format.
 * https://tools.ietf.org/html/rfc6749#section-4.1.4
 */
public class AccessToken implements ParametersMap {

	/**
	 * The access token string as issued by the authorization server.
	 * Required.
	 */
	private String accessToken;
	/**
	 * The type of this token, typically just the string “bearer”.
	 * Required
	 */
	private String tokenType;
	/**
	 * If the access token expires, the server should reply with the duration of time the access token is granted for.
	 * Required
	 */
	private long expiresIn;
	/**
	 * If the access token will expire, then it is useful to return a refresh token which applications can use to obtain another access token.
	 * However, tokens issued with the implicit grant cannot be issued a refresh token.
	 * Optional
	 */
	private String refreshToken;
	/**
	 *  If the scope the user granted is identical to the scope the app requested, this parameter is optional.
	 *  If the granted scope is different from the requested scope, such as if the user modified the scope, then this parameter is required.
	 *  Optional
	 */
	private Collection<String> scopes;

	/**
	 * Initializes an oauth token object from standard properties.
	 *
	 * @param accessToken the "access_token" string.
	 * @param tokenType the "token_type" string. Defaults to "Bearer".
	 * @param expiresIn the "expires_in" integer number for seconds until expire.
	 * @param refreshToken the "refresh_token" string.
	 * @param scopes the token "scope" as a collection of scope strings
	 */
	public AccessToken(final String accessToken, String tokenType, final long expiresIn, final String refreshToken, final Collection<String> scopes) {
		this.accessToken = accessToken;
		if (tokenType == null)
			tokenType = "Bearer";
		this.tokenType = tokenType;
		this.expiresIn = expiresIn;
		this.refreshToken = refreshToken;
		this.scopes = scopes;
	}

	/**
	 * Initialize from map of properties such as "access_token" and "expires_in".
	 *
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	public AccessToken(Map<String, Object> map) {
		if(map == null)
			throw new IllegalArgumentException("map is null");
		this.accessToken = (String) map.get("access_token");
		this.tokenType = (String) map.getOrDefault("token_type", "Bearer");
		if(map.containsKey("expires_in")){
			//Create a new long so that the case where expires_in is an integer is handled.
			this.expiresIn = new Long(String.valueOf(map.get("expires_in")));
		}
		this.refreshToken = (String) map.get("refresh_token");
		if(map.containsKey("scope")){
            if(map.get("scope") instanceof Collection<?>){
                this.scopes = (Collection<String>) map.get("scope");
            } else{
                this.scopes = Arrays.asList(((String) map.get("scope")).split(" "));
            }
        }
	}

	/**
	 * Returns the "token_type" string in this oauth token.
	 */
	public final String getTokenType() {
		return tokenType;
	}

	/**
	 * Returns the "refresh_token" string in this oauth token.
	 */
	public final String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * Returns the "scope" in this oauth token, modeled as collection of scope strings.
	 */
	public final Collection<String> getScopes() {
		return scopes;
	}

	/**
	 * Returns the "access_token" string in this oauth token.
	 */
	public final String getAccessToken() {
		return accessToken;
	}

	/**
	 * Returns the "expires_in" number in this access token. This models the duration
	 * of time the access token is granted for, if the access token expires.
	 */
	public final long getExpiresIn() {
		return expiresIn;
	}

	/**
	 * Checks if scope is one of the configured scopes for this access token.
	 * @param scope
	 * @return true if the scope is assigned to this token, false otherwise
	 */
	public boolean hasScope(String scope){
		return this.scopes != null && this.scopes.contains(scope);
	}

	@Override
	public String toString() {
		return "AccessToken [accessToken=" + accessToken + ", tokenType=" + tokenType + ", expiresIn=" + expiresIn
				+ ", refreshToken=" + refreshToken + ", scopes=" + scopes + "]";
	}

	private static Map<String, String> propertyMap;

	/**
	 * Maps Bean introspection property descriptors name to OAuth2 valid payload
	 * property names.
	 *
	 * @return
	 */
	protected static Map<String, String> getPropertyMap() {
		if (propertyMap == null) {
			propertyMap = new HashMap<>();
			propertyMap.put("tokenType", "token_type");
			propertyMap.put("refreshToken", "refresh_token");
			propertyMap.put("accessToken", "access_token");
			propertyMap.put("getExpiresIn", "expires_in");
			propertyMap.put("scopes", "scope");
			propertyMap = Collections.unmodifiableMap(propertyMap);
		}
		return propertyMap;
	}

	/**
	 * Returns the properties of this token object as map.
	 *
	 */
	public Map<String, Object> map() throws Exception {
		Map<String, Object> grant = BeanUtils.asMap(this, getPropertyMap());
		return grant;
	}

}