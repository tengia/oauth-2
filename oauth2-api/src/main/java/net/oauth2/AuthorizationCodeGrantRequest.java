/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Models the "authorization_code" type of oauth token grant requests.
 * https://tools.ietf.org/html/rfc6749#section-1.3.1
 */
public class AuthorizationCodeGrantRequest extends AccessTokenGrantRequest {

	private final String redirectUri;
	private final String code;

	public AuthorizationCodeGrantRequest(final String code, final String clientId, final String clientSecret,
			final String redirectUrl, final Collection<String> scopes) {
		super("authorization_code", clientId, clientSecret, scopes);
		this.code = code;
		this.redirectUri = redirectUrl;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public String getCode() {
		return code;
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
			Map<String, String> superMap = AccessTokenGrantRequest.getPropertyMap();
			propertyMap = new HashMap<>();
			propertyMap.putAll(superMap);
			propertyMap.put("redirectUri", "redirect_uri");
			propertyMap.put("code", "code");
			propertyMap = Collections.unmodifiableMap(propertyMap);
		}
		return propertyMap;
	}

	public Map<String, Object> map() throws Exception {
		Map<String, Object> grant = BeanUtils.asMap(this, getPropertyMap());
		return grant;
	}

	@Override
	public String toString() {
		return "AuthorizationCodeGrantRequest [redirectUri=" + redirectUri + ", code=" + code + ", getScopes()="
				+ getScopes() + ", getGrantType()=" + getGrantType() + ", getClientId()=" + getClientId()
				+ ", getClientSecret()=" + getClientSecret() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((redirectUri == null) ? 0 : redirectUri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthorizationCodeGrantRequest other = (AuthorizationCodeGrantRequest) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (redirectUri == null) {
			if (other.redirectUri != null)
				return false;
		} else if (!redirectUri.equals(other.redirectUri))
			return false;
		return true;
	}
	
}