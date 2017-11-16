/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.jackson;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class WhitespaceDelimitedScopeDeserializer extends StdDeserializer<Collection<String>>{
	
	private static final long serialVersionUID = 456504895806428666L;
	
	protected WhitespaceDelimitedScopeDeserializer(JavaType valueType) {
		super(valueType);
	}
	
	public WhitespaceDelimitedScopeDeserializer() {
		super(TypeFactory.defaultInstance().constructCollectionType(Collection.class, String.class));
	}

	@Override
	public Collection<String> deserialize(JsonParser parser, DeserializationContext ctxt)throws IOException, JsonProcessingException {
		String enumerationStr = parser.getText();
		String[] enumerationArr = enumerationStr.split("\\s+");
		Collection<String> list = Collections.unmodifiableCollection(Arrays.asList(enumerationArr));
		return list;
	}
	
	public static final SimpleModule REGISTER(SimpleModule module){
		JavaType type = TypeFactory.defaultInstance().constructCollectionType(Collection.class, String.class);
		WhitespaceDelimitedScopeDeserializer deser = new WhitespaceDelimitedScopeDeserializer(type);		
		module.addDeserializer(Collection.class, deser);
		return module;
	}
	
}