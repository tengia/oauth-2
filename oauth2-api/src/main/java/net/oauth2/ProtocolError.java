package net.oauth2;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * https://tools.ietf.org/html/rfc6749#section-4.1.2.1
 */
public class ProtocolError implements ParametersMap{

	private final String error;
	private final String description;
	private final URI uri;
	private final String state;

	public ProtocolError(final String error,
			final String description, final URI uri,
			final String state) {
		if (error == null)
			throw new IllegalArgumentException("error is null");
		this.error = error;
		// optional parameters
		this.description = description;
		this.uri = uri;
		this.state = state;
	}

	public ProtocolError(Map<String, Object> map) {
		this.error = (String) map.get("error");
		this.description = (String) map.get("error_description");
		this.uri = (URI) map.get("uri");
		this.state = (String) map.get("state");  
	}

	public String getError() {
		return error;
	}

	public ProtocolErrorType getErrorType() {
		return ProtocolErrorType.parse(this.error);
	}

	public String getDescription() {
		return description;
	}

	public URI getUri() {
		return uri;
	}

	public String getState() {
		return state;
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
			propertyMap.put("error", "error");
			propertyMap.put("description", "error_description");
			propertyMap.put("uri", "error_uri");
			propertyMap.put("state", "state");
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
		return "ProtocolError [error=" + error + ", description=" + description + ", uri=" + uri + ", state=" + state
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		ProtocolError other = (ProtocolError) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

}
