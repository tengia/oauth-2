package net.oauth2.client.http.apache.httpcomponents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.localserver.LocalServerTestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.ProtocolError;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.http.DataBindingProvider;

@RunWith(MockitoJUnitRunner.class)
public class ApacheHttpClientAdapterTest {

	@Mock DataBindingProvider<?> mapper;

	static LocalServerTestBase base;
	static HttpHost host;
	
	final static  String payload = "{"
			 + "	\"access_token\": 1234,"
			 + "	\"expiresIn\": 3600,"
			 + "	\"refresh_token\":456,"
			 + "	\"token_type\": \"Bearer\","
			 + "	\"geolocation\": \"custom\""
			 + "}";
	
	final static String errorPayload = "{"
			 + "	\"error\": \"invalid_token\","
			 + "	\"error_description\": \"descr\""
			 + "}";
	
	@BeforeClass
	public static void setUp() throws Exception {
		base = new LocalServerTestBase(){
			@Override
			public void setUp() throws Exception {
				super.setUp();
				this.serverBootstrap.registerHandler("/token", new HttpRequestHandler() {
					@Override
					public void handle(HttpRequest request, HttpResponse response, HttpContext context)
							throws HttpException, IOException {
						String content = payload;
						InputStream in = new ByteArrayInputStream(content.getBytes());
						response.setEntity(new InputStreamEntity(in, content.length(), ContentType.APPLICATION_JSON));
					}					
				});
				this.serverBootstrap.registerHandler("/token/error", new HttpRequestHandler() {
					@Override
					public void handle(HttpRequest request, HttpResponse response, HttpContext context)
							throws HttpException, IOException {
						String content = errorPayload;
						InputStream in = new ByteArrayInputStream(content.getBytes());
						response.setStatusCode(400);
						response.setEntity(new InputStreamEntity(in, content.length(), ContentType.APPLICATION_JSON));
					}					
				});
				this.serverBootstrap.registerHandler("/token/othererror", new HttpRequestHandler() {
					@Override
					public void handle(HttpRequest request, HttpResponse response, HttpContext context)
							throws HttpException, IOException {
						String content = "tralala";
						InputStream in = new ByteArrayInputStream(content.getBytes());
						response.setStatusCode(500);
						response.setEntity(new InputStreamEntity(in, content.length(), ContentType.DEFAULT_TEXT));
					}					
				});
			}
		};
		
		base.setUp();
		host = base.start();
	}

	@Test
	public void testPost() throws IOException {

				
		AccessToken _myToken = new AccessToken("1234", "Bearer", 3600L, "456", Arrays.asList(new String[]{"cutom"}));
		when(mapper.parseToken(anyString(), eq(AccessToken.class))).thenReturn(_myToken);
		
		ApacheHttpClientAdapter adapter = new ApacheHttpClientAdapter(new URL(host.toURI()), "", "", this.mapper, AccessToken.class);
		
		AccessToken myToken = adapter.post("token", new AccessTokenGrantRequest("", "", "", null));
		
		assertEquals(_myToken, myToken);
	}
	
	@Test
	public void testPostProtocolException() throws IOException {
			
		ProtocolError err = new ProtocolError("invalid_token", "descr", null, null);
		when(mapper.parseError(errorPayload, ProtocolError.class)).thenReturn(err);
		
		ApacheHttpClientAdapter adapter = new ApacheHttpClientAdapter(new URL(host.toURI()), "", "", this.mapper, AccessToken.class);
		
		try{
			adapter.post("token/error", new AccessTokenGrantRequest("", "", "", null));
			fail("exception expected to be thrown but it was not");
		} catch (IOException e){
			assertTrue(e instanceof OAuth2ProtocolException);
			OAuth2ProtocolException protocolException = (OAuth2ProtocolException)e;
			assertEquals(err, protocolException.getError());
		}

	}
	
	@Test
	public void testPostOtherException() throws IOException {
			
		ApacheHttpClientAdapter adapter = new ApacheHttpClientAdapter(new URL(host.toURI()), "", "", this.mapper, AccessToken.class);
		
		try{
			adapter.post("token/othererror", new AccessTokenGrantRequest("", "", "", null));
			fail("exception expected to be thrown but it wasn not");
		} catch (IOException e){
			assertTrue(!(e instanceof OAuth2ProtocolException));
		}

	}

	@AfterClass
	public static void tearDown() throws Exception {
		base.shutDown();
	}

}
