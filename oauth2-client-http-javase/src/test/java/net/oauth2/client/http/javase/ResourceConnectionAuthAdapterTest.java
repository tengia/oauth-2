/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.javase;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.AccessToken;
import net.oauth2.client.http.ResourceOAuthHeader;
import net.oauth2.client.http.javase.HttpConnectionTestMock.ConnectionHandler;
import net.oauth2.client.http.javase.conn.HttpsUrlConnectionFactory;

@RunWith(MockitoJUnitRunner.class)
public class ResourceConnectionAuthAdapterTest {
	
	@Mock
	private HttpsUrlConnectionFactory connectionFactory;

	@Test
	public void testAdapt() throws IOException {

		HttpsURLConnection conn = HttpConnectionTestMock.mockConnection("https://test.org", null,  new ConnectionHandler<HttpsURLConnection>() {
			public void handle(String urlSpec, URL urlContext, HttpsURLConnection connection) throws IOException {/*NO OP*/}
		}, HttpsURLConnection.class);					
		when(connectionFactory.connection(any(URL.class))).thenReturn(conn);
		new ResourceConnectionAuthAdapter().adapt(conn, new AccessToken("test", "bearer", 1234L, null, null));
		verify(conn, times(1)).setRequestProperty(ResourceOAuthHeader.HTTP_HEADER_NAME_AUTHORIZATION,"bearer test");
	}

}
