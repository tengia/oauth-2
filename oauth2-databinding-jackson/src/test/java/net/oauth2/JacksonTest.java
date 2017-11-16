/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import net.oauth2.jackson.CommaDelimitedScopeDeserializer;
import net.oauth2.jackson.WhitespaceDelimitedScopeDeserializer;

@RunWith(MockitoJUnitRunner.class)
public class JacksonTest {

	Collection<String> scopes = new ArrayList<>(3);	
	
	@Before
	public void before() {
		scopes.add("a");
		scopes.add("b");
		scopes.add("c");		
	}
	
	@Mock JsonParser parser;
	@Mock DeserializationContext deserializationCtx;
	
	@Test
	public void testWhitespaceDelimitedScopeDeserializer() throws IOException {
		WhitespaceDelimitedScopeDeserializer deserializer = new WhitespaceDelimitedScopeDeserializer();
		given(this.parser.getText()).willReturn("scopeA scopeB");		
		Collection<String> scopes = deserializer.deserialize(this.parser, this.deserializationCtx);
		assertNotNull(scopes);
		assertTrue(scopes.size()==2);
		assertTrue("", scopes.contains("scopeA"));
		assertTrue("", scopes.contains("scopeB"));
		verify(this.parser).getText();
	}
	
	@Test
	public void testCommaDelimitedScopeDeserializer() throws IOException {
		CommaDelimitedScopeDeserializer deserializer = new CommaDelimitedScopeDeserializer();
		given(this.parser.getText()).willReturn("scopeA, scopeB  ");
		Collection<String> scopes = deserializer.deserialize(this.parser, this.deserializationCtx);
		assertNotNull(scopes);
		assertTrue(scopes.size()==2);
		assertTrue("", scopes.contains("scopeA"));
		assertTrue("", scopes.contains("scopeB"));		
		verify(this.parser).getText();
	}
	
	@After
	public void after() {
		scopes.clear();
		Mockito.reset(this.parser);
	}

}
