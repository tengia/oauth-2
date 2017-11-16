/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.databinding;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Test;

import net.oauth2.AccessToken;
import net.oauth2.ProtocolError;
import net.oauth2.ProtocolErrorType;

public class SimpleDatabindingProviderTest {

	@Test
	public void testParseToken() throws IOException {
		String tokenJSON = "{ \"access_token\": \"test_token\", \"token_type\": \"bearer\", \"expires_in\": 123, \"refresh_token\": \"test_refresh_token\", \"scope\": \"read write\"}";
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		AccessToken token = p.parseToken(tokenJSON, AccessToken.class);
		assertEquals("test_token", token.getAccessToken());
		assertEquals("bearer", token.getTokenType());
		assertEquals(123L, token.getExpiresIn());
		assertEquals("test_refresh_token", token.getRefreshToken());
		Collection<String> scopes = new LinkedList<String>(); 
		Collections.addAll(scopes, "read", "write");
		assertEquals(scopes, token.getScopes());
	}
	
	@Test
	public void testParseTokenJsonTopLevelNotObject() throws IOException {
		String tokenJSON = "abc";
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		try{
			p.parseToken(tokenJSON, AccessToken.class);		
		} catch (IllegalArgumentException e){
			assertEquals("Expecting json object as top-level entry", e.getMessage());
		}
	}
	
	@Test
	public void testParseTokenJsonMalformedTupleNoDelimiter() throws IOException {
		String tokenJSON = "{\"a\" 1}";
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		try{
			p.parseToken(tokenJSON, AccessToken.class);		
		} catch (IllegalArgumentException e){
			assertEquals("Malformed JSON. Missing \":\" at \"a\" 1", e.getMessage());
		}
	}
	
	@Test
	public void testParseTokenJsonArrayValuesNotSupported() throws IOException {
		String tokenJSON = "{\"a\": []}";
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		try{
			p.parseToken(tokenJSON, AccessToken.class);		
		} catch (IllegalArgumentException e){
			assertEquals("Unsuported type. Arrays are not supported: []", e.getMessage());
		}
	}
	
	@Test
	public void testParseTokenJsonMalformedStringNoStart() throws IOException {
		String tokenJSON = "{\"a\": 1\"}";
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		try{
			p.parseToken(tokenJSON, AccessToken.class);		
		} catch (IllegalArgumentException e){
			assertEquals("malformed JSON. Strings must start with \".", e.getMessage());
		}
	}
	
	@Test
	public void testParseTokenJsonMalformedNotANumber() throws IOException {
		String tokenJSON = "{\"a\": 1a}";
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		try{
			p.parseToken(tokenJSON, AccessToken.class);		
		} catch (IllegalArgumentException e){
			assertEquals("Malformed JSON. Not a number: 1a", e.getMessage());
		}
	}
	
	@Test
	public void testParseTokenJsonMalformedStringNoClosing() throws IOException {
		String tokenJSON = "{\"a\": \"1}";
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		try{
			p.parseToken(tokenJSON, AccessToken.class);		
		} catch (IllegalArgumentException e){
			assertEquals("malformed JSON. Starts with \" but is missing the end \": \"1", e.getMessage());
		}
	}
	
	@Test
	public void testParseTokenJsonMalformedTupleTooManyDelimiters() throws IOException {
		String tokenJSON = "{\"a\": 1:}";
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		try{
			p.parseToken(tokenJSON, AccessToken.class);		
		} catch (IllegalArgumentException e){
			assertEquals("Malformed JSON. Too many  \":\" at \"a\": 1: The \":\" character is not supported within keys and values by this parser.", e.getMessage());
		}
	}

	@Test
	public void testParseError() throws IOException {
		SimpleDatabindingProvider p = new SimpleDatabindingProvider();
		String errstr = "{ \"error\": \"invalid_client\", \"error_description\": \"descr\", \"state\": \"login\"}";
		ProtocolError err = p.parseError(errstr, ProtocolError.class);
		assertEquals("invalid_client", err.getError());
		assertEquals(ProtocolErrorType.InvalidClient, err.getErrorType());
		assertEquals("descr", err.getDescription());
		assertEquals("login", err.getState());
	}


	@Test(expected=UnsupportedOperationException.class)
	public void testRaw() {
		new SimpleDatabindingProvider().raw();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testWith() {
		new SimpleDatabindingProvider().with(new Object());
	}
}
