package net.oauth2.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.oauth2.AccessToken;
import net.oauth2.ProtocolError;
import net.oauth2.client.http.DataBindingProvider;

public class JacksonDataBindingProvider implements DataBindingProvider<ObjectMapper> {

	private static final ObjectMapper DEFAULT_MAPPER = new OAuth2ObjectMapper(
			new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true));
	
	private ObjectMapper mapper;
	
	public JacksonDataBindingProvider() {
		this(DEFAULT_MAPPER);
	}
	
	public JacksonDataBindingProvider(final ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public <T extends AccessToken> T parseToken(String payload, Class<T> tokenType) throws JsonParseException, JsonMappingException, IOException {
		T token = (T) this.mapper.readValue(payload, tokenType);
		return token;
	}

	@Override
	public <T extends ProtocolError> T parseError(String payload, Class<T> errorType) throws JsonParseException, JsonMappingException, IOException {
		T error = (T) this.mapper.readValue(payload, errorType);
		return error;
	}

	
	@Override
	public ObjectMapper raw() {
		return this.mapper;
	}

	@Override
	public void with(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	protected ObjectMapper getMapper() {
		return mapper;
	}

}
