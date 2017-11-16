/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.gson;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.oauth2.AccessToken;
import net.oauth2.ProtocolError;
import net.oauth2.client.http.DataBindingProvider;

public class GsonDataBindingProvider implements DataBindingProvider<Gson> {

	public static GsonBuilder DEFAULT_BUILDER;
	static {
		GsonBuilder builder = new GsonBuilder();
		WhitespaceDelimitedCollectionTypeAdapterFactory.REGISTER(builder);
		AccessTokenTypeAdapter.REGISTER(builder);
		ProtocolErrorTypeAdapter.REGISTER(builder);
		DEFAULT_BUILDER = builder;
	}

	private static final Gson DEFAULT_MAPPER;
	static {
		DEFAULT_MAPPER = DEFAULT_BUILDER.create();
	}
	
	private Gson mapper;
	
	public GsonDataBindingProvider() {
		this(DEFAULT_MAPPER);
	}
	
	public GsonDataBindingProvider(GsonBuilder builder) {
		this(builder.create());
	}
	
	public GsonDataBindingProvider(final Gson mapper) {
		this.mapper = mapper;
	}

	@Override
	public <T extends AccessToken> T parseToken(String payload, Class<T> tokenType) throws JsonParseException, IOException {
		T token = this.mapper.fromJson(payload, tokenType);
		return token;
	}

	@Override
	public <T extends ProtocolError> T parseError(String payload, Class<T> errorType) throws JsonParseException,  IOException {
		T error = this.mapper.fromJson(payload, errorType);
		return error;
	}

	@Override
	public Gson raw() {
		return this.mapper;
	}

	@Override
	public void with(Gson mapper) {
		this.mapper = mapper;
	}

}
