/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.javase;

import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.mockito.Mockito;

public class HttpConnectionTestMock {

	public static interface ConnectionHandler<T extends HttpURLConnection> {
		public void handle(String urlSpec, URL urlContext, T connection) throws IOException;
	}
	
	public static <T extends HttpURLConnection> T mockConnection(String urlSpec, URL urlContext, ConnectionHandler<T> handler, Class<T> urlConnecitonClass) throws IOException{
		final T huc = Mockito.mock(urlConnecitonClass);
		handler.handle(urlSpec, urlContext, huc);
		/*java.net.URL is final so we can't mock it directly. But we can inject a mock connection using its constructor stream handler argument.*/
		URLStreamHandler stubURLStreamHandler = new URLStreamHandler() {
			@Override
			protected URLConnection openConnection(URL u) throws IOException {
				return huc;
			}
		};
		if(urlContext == null)
			urlContext = new URL("https://localhost");
		URL url = new URL(urlContext, urlSpec, stubURLStreamHandler);
		when(huc.getURL()).thenReturn(url);
		return huc;
	}
	
	public static <T extends HttpURLConnection> T mockConnection(String urlSpec, URL urlCtx, Class<T> urlConnecitonClass, String responseContent, Charset charset, boolean isErrorStream, OutputStream out, int responseCode, Map<String, String> responseHeaders) throws IOException{
		final Charset _charset = charset == null? StandardCharsets.UTF_8 : charset;
		final OutputStream _out = (out == null)? new ByteArrayOutputStream() : out;
		T huc = mockConnection(urlSpec, urlCtx, new ConnectionHandler<T>() {

			@Override
			public void handle(String urlSpec, URL urlContext, T connection) throws IOException {
				if (responseContent != null) {
					InputStream stream = new ByteArrayInputStream(responseContent.getBytes(_charset));
					if(!isErrorStream)
						when(connection.getInputStream()).thenReturn(stream);
					else
						when(connection.getErrorStream()).thenReturn(stream);
				}
				when(connection.getOutputStream()).thenReturn(_out);
				when(connection.getResponseCode()).thenReturn(responseCode);
				if(responseHeaders!=null){
					for (Entry<String, String> header : responseHeaders.entrySet()) {
						when(connection.getHeaderField(header.getKey())).thenReturn(header.getValue());	
					}					
				}
			}
		}, urlConnecitonClass);
		return huc;
	}
	
}
