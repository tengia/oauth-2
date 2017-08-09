package net.oauth2.client.http.okhttp3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.ProtocolError;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.http.DataBindingProvider;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(MockitoJUnitRunner.class)
public class OkHttpClientAdapterTest {

	@Mock DataBindingProvider<?> mapper;
	MockWebServer server;
	
	@Before
	public void before(){
		this.server = new MockWebServer();
	}
	
	@After
	public void after(){
		try {
			this.server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPost() throws IOException {

		String payload = "{"
				 + "	\"access_token\": 1234,"
				 + "	\"expiresIn\": 3600,"
				 + "	\"refresh_token\":456,"
				 + "	\"token_type\": \"Bearer\","
				 + "	\"geolocation\": \"custom\""
				 + "}";
		
		this.server = new MockWebServer();
		
		// Schedule some responses.
		server.enqueue(new MockResponse().setBody(payload));
		server.start();
		
		HttpUrl baseUrl = server.url("/token");
		String url = baseUrl.url().toExternalForm().substring(0,  baseUrl.url().toExternalForm().indexOf("token"));
		
		AccessToken _myToken = new AccessToken("testtoken", "testtpye", 123L, null, null);
		when(mapper.parseToken(payload, AccessToken.class)).thenReturn(_myToken);
		OkHttpClient client = new OkHttpClient.Builder().build();
		OkHttpClientAdapter<AccessToken> adapter = new OkHttpClientAdapter<>(url, client, mapper, AccessToken.class);
		
		AccessToken myToken = adapter.post("token", new AccessTokenGrantRequest("", "", "", null));
		
		assertEquals(_myToken, myToken);
	}

	@Test
	public void testHandleProtocolError() throws IOException {
		String payload = "{"
				 + "	\"error\": \"invalid_token\","
				 + "	\"error_description\": \"descr\""
				 + "}";
		
		this.server = new MockWebServer();
		
		// Schedule some responses.
		server.enqueue(new MockResponse().setResponseCode(400).addHeader("Content-Type", "application/json").setBody(payload));
		server.start();
		
		HttpUrl baseUrl = server.url("/token");
		String url = baseUrl.url().toExternalForm().substring(0,  baseUrl.url().toExternalForm().indexOf("token"));
		
		ProtocolError _myToken = new ProtocolError("invalid_token", "descr", null, null);
		when(mapper.parseError(payload, ProtocolError.class)).thenReturn(_myToken);
		OkHttpClient client = new OkHttpClient.Builder().build();
		OkHttpClientAdapter<AccessToken> adapter = new OkHttpClientAdapter<>(url, client, mapper, AccessToken.class);
		try {
			adapter.post("token", new AccessTokenGrantRequest("", "", "", null));
			fail("expected exception to be thrown");			
		} catch (Throwable t){
			assertTrue(t instanceof OAuth2ProtocolException);
			OAuth2ProtocolException ex = (OAuth2ProtocolException)t;
			assertEquals(_myToken, ex.getError());
		}
	}

}