package net.oauth2.client.retrofit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.client.retrofit.TokenEndpoint;
import okhttp3.OkHttpClient;

@RunWith(MockitoJUnitRunner.class)
public class TokenEndpointTest {

	@Mock OkHttpClient client;
	
	@Test
	public void testBuilder() {
		String url = "http://localhost";
		TokenEndpoint.Builder builder = TokenEndpoint.builder().baseUrl(url).client(this.client);
		
		assertNotNull(builder.url);
		assertNotNull(builder.client);
		
		TokenEndpoint te = builder.build();

		assertNotNull(te);
	}
	
	@Test
	public void testBuilderDefaultClient() {
		String url = "http://localhost";
		TokenEndpoint.Builder builder = TokenEndpoint.builder().baseUrl(url);
		
		TokenEndpoint te = builder.build();
		
		assertNull(builder.client);
		assertNotNull(te);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testBuilderNoBaseUrlInvocation() {
		TokenEndpoint te = TokenEndpoint.builder().build();
		assertNotNull(te);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBuilderInvalidUrlFail() {
		String url = "abc";
		TokenEndpoint te = TokenEndpoint.builder().baseUrl(url).client(this.client).build();
		assertNotNull(te);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBuilderNullInBaseUrlInvocation() {
		String url = null;
		TokenEndpoint te = TokenEndpoint.builder().baseUrl(url).client(this.client).build();
		assertNotNull(te);
	}

}
