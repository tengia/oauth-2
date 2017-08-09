package net.oauth2.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OAuth2ObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	public OAuth2ObjectMapper() {
		registerModule(new OAuth2Module());
	}

	public OAuth2ObjectMapper(ObjectMapper src) {
		super(src);
		registerModule(new OAuth2Module());
	}
	
}
