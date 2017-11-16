/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.javase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.ProtocolError;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.http.DataBindingProvider;
import net.oauth2.client.http.javase.conn.HttpLoggingFormatter;
import net.oauth2.client.http.javase.conn.HttpsUrlConnectionFactory;

@RunWith(MockitoJUnitRunner.class)
public class HttpsURLConnectionClientAdapterTest {

	@Mock
	private HttpsUrlConnectionFactory connectionFactory;
	@Mock
	private HttpLoggingFormatter logFormatter;
	@Mock
	private DataBindingProvider<?> dataBindingProvider;

	final static String payload = "{"
			 + "	\"access_token\": 1234,"
			 + "	\"expiresIn\": 3600,"
			 + "	\"refresh_token\":456,"
			 + "	\"token_type\": \"Bearer\","
			 + "	\"geolocation\": \"custom\""
			 + "}";
	final static AccessToken token = new AccessToken("1234", "Bearer", 3600L, "456", Arrays.asList(new String[]{"custom"}));
	
	final static String errorPayload = "{"
			 + "	\"error\": \"invalid_token\","
			 + "	\"error_description\": \"descr\""
			 + "}";
	final static ProtocolError err = new ProtocolError("invalid_token", "descr", null, null);
	
	@Before
	public void setUp() throws Exception {}

	@Test
	public void testPost() throws IOException {
		HttpsURLConnection conn = HttpConnectionTestMock.mockConnection("test", null, HttpsURLConnection.class, payload, null, false, null, 200, null);
		when(connectionFactory.connection(any(URL.class))).thenReturn(conn);
		when(dataBindingProvider.parseToken(payload, AccessToken.class)).thenReturn(token);
		
		HttpsURLConnectionClientAdapter httpAdapter = new HttpsURLConnectionClientAdapter(new URL("https://localhost"), connectionFactory, logFormatter, dataBindingProvider, AccessToken.class);
		
		AccessToken _token = httpAdapter.post("token", new AccessTokenGrantRequest("", "", "", null));
		
		assertNotNull(_token);
		assertEquals(token, _token);
	}
	
	@Test
	public void testPostError() throws IOException {
		
		URL url = new URL("https://localhost");
		Map<String, String> responseHeaders  = new HashMap<>();
		responseHeaders.put("Content-Type", "application/json");
		HttpsURLConnection conn = HttpConnectionTestMock.mockConnection("test", null, HttpsURLConnection.class, errorPayload, null, true, null, 400, responseHeaders);
		when(connectionFactory.connection(any(URL.class))).thenReturn(conn);
		when(dataBindingProvider.parseError(errorPayload, ProtocolError.class)).thenReturn(err);
		
		HttpsURLConnectionClientAdapter httpAdapter = new HttpsURLConnectionClientAdapter(url, connectionFactory, logFormatter, dataBindingProvider, AccessToken.class);
		
		try {
			httpAdapter.post("token", new AccessTokenGrantRequest("", "", "", null));
			fail("OAuth2ProtocolException exception expected to be thrown but it was not");
		} catch(OAuth2ProtocolException e) {
			assertEquals(err, e.getError());	
		}
		
	}
}
