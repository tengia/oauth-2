/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.databinding;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.oauth2.AccessToken;
import net.oauth2.ParametersMap;
import net.oauth2.ProtocolError;
import net.oauth2.client.http.DataBindingProvider;
import net.oauth2.client.http.FormEncodeDataBinding;

/**
 * A data binding provider implementation for serialization and deserialization 
 * of standard OAuth protocol payloads to and from the object model in this library.
 *
 */
public class SimpleDatabindingProvider implements DataBindingProvider<Object>{

	private static final Pattern objectEntriesPattern = Pattern.compile("\\{(.*?)\\}");
	
	@Override
	public Object raw() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void with(Object mapper) {
		throw new UnsupportedOperationException();
	}

	class Tuple {
		String key;
		Object value;
		public Tuple(String key, Object value) {
			this.key = key;
			this.value = value;
		}
		public String key(){return this.key;}
		public Object value(){return this.value;}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends AccessToken> T parseToken(String payload, Class<T> tokenType) throws IOException {
		if(!AccessToken.class.isAssignableFrom(tokenType))
			throw new IllegalArgumentException("Only AccessToken type is supported: " + tokenType);
		ParametersMap token = null;
		final Map<String, Object> parameters = new HashMap<>();
		Matcher matchPattern = objectEntriesPattern.matcher(payload);
		if (matchPattern.find()) {
			String params = matchPattern.group(1);
			String[] tupleStrings = params.split(",");
			parameters.putAll(Stream.of(tupleStrings)
								.map(this::parseTuple)
								.collect(Collectors.toMap(Tuple::key, Tuple::value)));
			token = new AccessToken(parameters);
		} else {
			throw new IllegalArgumentException("Expecting json object as top-level entry");
		}
		return (T) token;
	}
	
	private Tuple parseTuple(String tupleString){
		String[] kv = tupleString.split(":");
		if(kv.length<2)
			throw new IllegalArgumentException("Malformed JSON. Missing \":\" at " + tupleString);
		if(kv.length>2)
			throw new IllegalArgumentException("Malformed JSON. Too many  \":\" at " + tupleString + " The \":\" character is not supported within keys and values by this parser.");
		String key = kv[0].trim();
		if(key.startsWith("\"")){
			key= formatString(key);
		}
		Object value = null;
		String valueString = kv[1].trim();
		if("undefined".equals(valueString)){
			value = null;
		} else if(valueString.startsWith("\"")){
			//handle strings
			value = formatString(valueString);
		} else if(valueString.startsWith("[")){
			//handle arrays
			throw new IllegalArgumentException("Unsuported type. Arrays are not supported: " + valueString);
		} else if(valueString.endsWith("\"")){
			throw new IllegalArgumentException("malformed JSON. Strings must start with \".");
		} else {
			//handle numbers
			value = parseNumber(valueString);  
		}			
		if("scope".equals(key)){
			value = parseCollection((String)value);
		}   
		Tuple t = new Tuple(key, value);
		return t;
	}
	
	private String formatString(String value){
		if(value.startsWith("\"")){
			value = value.replaceFirst("\"", "");
			if(value.charAt(value.length()-1)!='"')
				throw new IllegalArgumentException("malformed JSON. Starts with \" but is missing the end \": \"" + value);
			value = value.substring(0, value.length()-1);
		} else {
			throw new IllegalArgumentException("malformed JSON. Strings must start with \".");
		}
		return value;
	}
	
	private Collection<String> parseCollection(String value){
		return FormEncodeDataBinding.CollectionDeserializer.parseDelimitedString(value, null, false);
	}
		
	/*public boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}*/

	private Long parseNumber(String value){
		Long number = null;
		try {  
			number = Long.parseLong(value);  
		} catch(NumberFormatException nfe) {  
		    throw new IllegalArgumentException("Malformed JSON. Not a number: " + value, nfe); 
		}  
		return number;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ProtocolError> T parseError(String payload, Class<T> errorType) throws IOException {
		if(!ProtocolError.class.isAssignableFrom(errorType))
			throw new IllegalArgumentException("Only ProtocolError type is supported: " + errorType);
		ParametersMap err = null;
		final Map<String, Object> parameters = new HashMap<>();
		Matcher matchPattern = objectEntriesPattern.matcher(payload);
		if (matchPattern.find()) {
			String params = matchPattern.group(1);
			String[] tupleStrings = params.split(",");
			parameters.putAll(Stream.of(tupleStrings)
								.map(this::parseTuple)
								.collect(Collectors.toMap(Tuple::key, Tuple::value)));
			err = new ProtocolError(parameters);
		} else {
			throw new IllegalArgumentException("Expecting json object as top-level entry");
		}
		return (T) err;
	}

}
