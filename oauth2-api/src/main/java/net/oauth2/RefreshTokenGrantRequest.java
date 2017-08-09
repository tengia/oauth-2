package net.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RefreshTokenGrantRequest extends AccessTokenGrantRequest {

	protected String refreshToken;

	public RefreshTokenGrantRequest(final String refreshToken, final String clientId, final String clientSecret,
			final Collection<String> scopes) {
		super("refresh_token", clientId, clientSecret, scopes);
		this.refreshToken = refreshToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public static final RefreshTokenGrantRequest renew(final AccessTokenGrantRequest originalTokenRequestGrant,
			final String refreshToken, final Collection<String> scopes) {
		/*
		 * The requested scope MUST NOT include any scope not originally granted
		 * by the resource owner, and if omitted is treated as equal to the
		 * scope originally granted by the resource owner.
		 */
		if (scopes != null) {
			Collection<String> granted_scopes = originalTokenRequestGrant.getScopes();
			if (granted_scopes == null) {
				throw new IllegalArgumentException(
						"The requested scope includes scope not originally granted by the resource owner: " + scopes);
			}
			for (String refreshScope : scopes) {
				if (!granted_scopes.contains(refreshScope)) {
					throw new IllegalArgumentException(
							"The requested scope includes scope not originally granted by the resource owner: "
									+ refreshScope);
				}
			}
		}

		RefreshTokenGrantRequest refreshGrant = new RefreshTokenGrantRequest(refreshToken,
				originalTokenRequestGrant.getClientId(), originalTokenRequestGrant.getClientSecret(), scopes);
		return refreshGrant;
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
			propertyMap.put("refreshToken", "refresh_token");
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
		return "RefreshTokenGrantRequest [refreshToken=" + refreshToken + ", getScopes()=" + getScopes()
				+ ", getGrantType()=" + getGrantType() + ", getClientId()=" + getClientId() + ", getClientSecret()="
				+ getClientSecret() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
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
		RefreshTokenGrantRequest other = (RefreshTokenGrantRequest) obj;
		if (refreshToken == null) {
			if (other.refreshToken != null)
				return false;
		} else if (!refreshToken.equals(other.refreshToken))
			return false;
		return true;
	}
	

}