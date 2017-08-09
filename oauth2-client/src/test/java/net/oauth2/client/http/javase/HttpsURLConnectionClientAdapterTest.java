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
import java.net.HttpURLConnection;
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
		HttpsURLConnection conn = mockConnection("test", null, HttpsURLConnection.class, payload, null, false, null, 200, null);
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
		HttpsURLConnection conn = mockConnection("test", null, HttpsURLConnection.class, errorPayload, null, true, null, 400, responseHeaders);
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
	
	static interface ConnectionHandler<T extends HttpURLConnection> {
		public void handle(String urlSpec, URL urlContext, T connection) throws IOException;
	}

	private static <T extends HttpURLConnection> T mockConnection(String spec, URL urlContext, ConnectionHandler<T> handler, Class<T> urlConnecitonClass) throws IOException{
		final T huc = Mockito.mock(urlConnecitonClass);
		handler.handle(spec, urlContext, huc);
		/*java.net.URL is final so we can't mock it directly. But we can inject a mock connection using its constructor stream handler argument.*/
		URLStreamHandler stubURLStreamHandler = new URLStreamHandler() {
			@Override
			protected URLConnection openConnection(URL u) throws IOException {
				return huc;
			}
		};
		if(urlContext == null)
			urlContext = new URL("https://localhost");
		URL url = new URL(urlContext, spec, stubURLStreamHandler);
		when(huc.getURL()).thenReturn(url);
		return huc;
	}
	
	private <T extends HttpURLConnection> T mockConnection(String spec, URL urlCtx, Class<T> urlConnecitonClass, String responseContent, Charset charset, boolean isErrorStream, OutputStream out, int responseCode, Map<String, String> responseHeaders) throws IOException{
		final Charset _charset = charset == null? StandardCharsets.UTF_8 : charset;
		final OutputStream _out = (out == null)? new ByteArrayOutputStream() : out;
		T huc = mockConnection(spec, urlCtx, new ConnectionHandler<T>() {

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
