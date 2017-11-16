/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.gson;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import net.oauth2.AccessToken;

public class AccessTokenTypeAdapter implements JsonSerializer<AccessToken>, JsonDeserializer<AccessToken> {

	public static final GsonBuilder REGISTER(GsonBuilder builder){
		return builder.registerTypeAdapter(AccessToken.class, new AccessTokenTypeAdapter());
	}
	
	@Override
	public AccessToken deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		Type mapType = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, Object> map = ctx.deserialize(el, mapType);
		if(map!=null){
			if("".equals(map.get("scope"))){
				map.put("scope", null);
			}
			return new AccessToken(map);
		}
		return null;
	}

	@Override
	public JsonElement serialize(AccessToken token, Type type, JsonSerializationContext ctx) {
		if (token == null) {
			return null;
		}
		Map<String, Object> map = null;
		try {
			map = token.map();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
		Type mapType = new TypeToken<Map<String, String>>() {}.getType();
		return ctx.serialize(map, mapType);
	}

}
