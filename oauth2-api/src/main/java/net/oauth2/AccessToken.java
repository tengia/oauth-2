package net.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * https://tools.ietf.org/html/rfc6749#section-4.1.4
 */
public class AccessToken implements ParametersMap {
	
	/**
	 * The access token string as issued by the authorization server.
	 * Required.
	 */
	private String accessToken;
	/**
	 * The type of token this is, typically just the string “bearer”.
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

	public AccessToken(final String accessToken, String tokenType, final long expiresIn, final String refreshToken, final Collection<String> scopes) {
		this.accessToken = accessToken;
		if (tokenType == null)
			tokenType = "Bearer";
		this.tokenType = tokenType;
		this.expiresIn = expiresIn;		
		this.refreshToken = refreshToken;
		this.scopes = scopes;
	}
	
	@SuppressWarnings("unchecked")
	public AccessToken(Map<String, Object> map) {
		if(map == null)
			throw new IllegalArgumentException("map is null");
		this.accessToken = (String) map.get("access_token");
		this.tokenType = (String) map.getOrDefault("token_type", "Bearer");
		Long val = (Long) map.get("expires_in");
		if(val!=null)
			this.expiresIn = Long.parseLong(String.valueOf(val));
		this.refreshToken = (String) map.get("refresh_token");
		this.scopes = (Collection<String>) map.get("scope");
	}

	public final String getTokenType() {
		return tokenType;
	}

	public final String getRefreshToken() {
		return refreshToken;
	}

	public final Collection<String> getScopes() {
		return scopes;
	}

	public final String getAccessToken() {
		return accessToken;
	}

	public final long getExpiresIn() {
		return expiresIn;
	}
	
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

	public Map<String, Object> map() throws Exception {
		Map<String, Object> grant = BeanUtils.asMap(this, getPropertyMap());
		return grant;
	}

}