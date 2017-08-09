package net.oauth2;

import java.util.Collection;

public class ClientCredentialsGrantRequest extends AccessTokenGrantRequest {

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