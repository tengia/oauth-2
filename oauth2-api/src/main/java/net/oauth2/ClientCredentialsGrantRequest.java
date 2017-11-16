/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2;

import java.util.Collection;

/**
 * Models the "client_credentials" type of oauth token grant requests.
 *
 */
public class ClientCredentialsGrantRequest extends AccessTokenGrantRequest {

	/**
	 * Constructs a "client_credentials" type of grant request from properties.
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @param scopes
	 */
	public ClientCredentialsGrantRequest(final String clientId,
			final String clientSecret,
			final Collection<String> scopes) {
		super("client_credentials", clientId, clientSecret, scopes);
	}

	@Override
	public String toString() {
		return "ClientCredentialsGrantRequest [getScopes()=" + getScopes() + ", getGrantType()=" + getGrantType()
				+ ", getClientId()=" + getClientId() + ", getClientSecret()=" + getClientSecret() + "]";
	}

}