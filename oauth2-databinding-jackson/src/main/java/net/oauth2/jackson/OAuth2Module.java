/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.jackson;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class OAuth2Module extends SimpleModule{

	private static final long serialVersionUID = 1L;
	private static final String NAME = "OAuth2Module";
	private static final VersionUtil VERSION_UTIL = new VersionUtil() {};

	public OAuth2Module() {
		super(NAME, VERSION_UTIL.version());
		//register serializers
		WhitespaceDelimitedScopeSerializer.REGISTER(this);
		AccessTokenSerializer.REGISTER(this);
		ProtocolErrorSerializer.REGISTER(this);
		//register deserializers
		WhitespaceDelimitedScopeDeserializer.REGISTER(this);
		AccessTokenDeserializer.REGISTER(this);
		ProtocolErrorDeserializer.REGISTER(this);
	}
	
	public static final OAuth2Module INSTANCE(){
		OAuth2Module module = new OAuth2Module();
		return module;
	} 
}
