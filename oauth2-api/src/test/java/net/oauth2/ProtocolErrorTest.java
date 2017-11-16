/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import net.oauth2.ProtocolError;

public class ProtocolErrorTest {

	@Test(expected=IllegalArgumentException.class)
	public void testProtocolErrorNull() throws URISyntaxException {
		String error = null;
		ProtocolError err = new ProtocolError(error, "", new URI(""), "");
		assertNull(err.getError());
		assertNull(err.getDescription());
		assertNull(err.getState());
		assertNull(err.getUri());
	}
	
	@Test
	public void testProtocolError() throws URISyntaxException {
		String error = "err";
		String descr = "descr";
		String state = "state";
		URI uri = new URI("");
		ProtocolError err = new ProtocolError(error, descr, uri, state);
		String errString = err.getError();
		assertEquals(error, errString);
		assertEquals(descr, err.getDescription());
		assertEquals(state, err.getState());
		assertEquals(uri, err.getUri());
	}
	
	@Test
	public void testProtocolErrorType() throws URISyntaxException {
		String invalidClientString =  ProtocolErrorType.valueOf("InvalidClient").toString();
		assertEquals("invalid_client", invalidClientString);
	}

}
