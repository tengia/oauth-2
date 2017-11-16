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
 * Models the "password" type of oauth token grant requests.
 * https://tools.ietf.org/html/rfc6749#section-1.3.3
 */
public class PasswordCredentialsGrantRequest extends AccessTokenGrantRequest {
	
	private final String username;
	private final String password;

	public PasswordCredentialsGrantRequest(final String username, 
						 final String password,
						 final String clientId, 
						 final String clientSecret,
						 final Collection<String> scopes) {
		super("password", clientId, clientSecret, scopes);
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
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
			propertyMap.put("username", "username");
			propertyMap.put("password", "password");
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
		return "PasswordCredentialsGrantRequest [username=" + username + ", password=" + password + ", getScopes()="
				+ getScopes() + ", getGrantType()=" + getGrantType() + ", getClientId()=" + getClientId()
				+ ", getClientSecret()=" + getClientSecret() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		PasswordCredentialsGrantRequest other = (PasswordCredentialsGrantRequest) obj;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}