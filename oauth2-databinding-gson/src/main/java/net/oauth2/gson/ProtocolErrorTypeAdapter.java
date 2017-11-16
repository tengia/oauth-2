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

import net.oauth2.ProtocolError;

public class ProtocolErrorTypeAdapter implements JsonDeserializer<ProtocolError>, JsonSerializer<ProtocolError> {

	public static final GsonBuilder REGISTER(GsonBuilder builder){
		return builder.registerTypeAdapter(ProtocolError.class, new ProtocolErrorTypeAdapter());
	}
	
	@Override
	public JsonElement serialize(ProtocolError err, Type type, JsonSerializationContext ctx) {
		if (err == null) {
			return null;
		}
		Map<String, Object> map = null;
		try {
			map = err.map();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
		Type mapType = new TypeToken<Map<String, String>>() {}.getType();
		return ctx.serialize(map, mapType);
	}

	@Override
	public ProtocolError deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		Type mapType = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, Object> map = ctx.deserialize(el, mapType);
		if(map!=null){
			if("".equals(map.get("scope"))){
				map.put("scope", null);
			}
			return new ProtocolError(map);
		}
		return null;
	}

}
