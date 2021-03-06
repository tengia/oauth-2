/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
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
