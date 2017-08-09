package net.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * https://tools.ietf.org/html/rfc6749#section-1.3
 */
public class AccessTokenGrantRequest implements ParametersMap{

	private final String grant_type;
	private final String client_id;
	private final String client_secret;
	private Collection<String> scope;

	public AccessTokenGrantRequest(final String grantType, final String clientId, final String clientSecret,
			final Collection<String> scope) {
		this.grant_type = grantType;
		this.client_id = clientId;
		this.client_secret = clientSecret;
		this.scope = scope;
	}

	public Collection<String> getScopes() {
		return scope;
	}

	public final void setScopes(Collection<String> scopes) {
		this.scope = scopes;
	}

	public String getGrantType() {
		return grant_type;
	}

	public String getClientId() {
		return client_id;
	}

	public String getClientSecret() {
		return client_secret;
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
			propertyMap.put("grantType", "grant_type");
			propertyMap.put("clientId", "client_id");
			propertyMap.put("clientSecret", "client_secret");
			propertyMap.put("scopes", "scope");
			propertyMap = Collections.unmodifiableMap(propertyMap);
		}
		return propertyMap;
	}

	@Override
	public Map<String, Object> map() throws Exception {
		Map<String, Object> grant = BeanUtils.asMap(this, getPropertyMap());
		return grant;
	}

	@Override
	public String toString() {
		return "AccessTokenGrantRequest [grant_type=" + grant_type + ", client_id=" + client_id + ", client_secret="
				+ client_secret + ", scope=" + scope + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((client_id == null) ? 0 : client_id.hashCode());
		result = prime * result + ((client_secret == null) ? 0 : client_secret.hashCode());
		result = prime * result + ((grant_type == null) ? 0 : grant_type.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccessTokenGrantRequest other = (AccessTokenGrantRequest) obj;
		if (client_id == null) {
			if (other.client_id != null)
				return false;
		} else if (!client_id.equals(other.client_id))
			return false;
		if (client_secret == null) {
			if (other.client_secret != null)
				return false;
		} else if (!client_secret.equals(other.client_secret))
			return false;
		if (grant_type == null) {
			if (other.grant_type != null)
				return false;
		} else if (!grant_type.equals(other.grant_type))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

}