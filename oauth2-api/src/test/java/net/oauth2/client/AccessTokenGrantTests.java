package net.oauth2.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.AuthorizationCodeGrantRequest;
import net.oauth2.ClientCredentialsGrantRequest;
import net.oauth2.PasswordCredentialsGrantRequest;
import net.oauth2.RefreshTokenGrantRequest;

public class AccessTokenGrantTests {

	@Test
	public void testAccessTokenGrantRequest() {
		String grantType = "grant-type";
		String clientId = "client-id";
		String clientSecret = "client-secret";
		Collection<String> scopes = new ArrayList<>();
		scopes.add("scope");
		AccessTokenGrantRequest req = new AccessTokenGrantRequest(grantType, clientId, clientSecret, scopes);
		assertEquals(grantType, req.getGrantType());
		assertEquals(clientId, req.getClientId());
		assertEquals(clientSecret, req.getClientSecret());
		assertEquals(scopes, req.getScopes());
	}
	
	@Test
	public void testRefreshTokenGrantRequest() {
		String grantType = "refresh_token";
		String clientId = "client-id";
		String clientSecret = "client-secret";
		String refreshToken = "refresh-token";
		Collection<String> scopes = new ArrayList<>();
		scopes.add("scope");
		RefreshTokenGrantRequest req = new RefreshTokenGrantRequest(refreshToken, clientId, clientSecret, scopes);
		assertEquals(grantType, req.getGrantType());
		assertEquals(clientId, req.getClientId());
		assertEquals(clientSecret, req.getClientSecret());
		assertEquals(refreshToken, req.getRefreshToken());
		assertEquals(scopes, req.getScopes());
	}
		
	@Test
	public void testClientCredentialsGrantRequest() {
		String grantType = "client_credentials";
		String clientId = "client-id";
		String clientSecret = "client-secret";
		Collection<String> scopes = new ArrayList<>();
		scopes.add("scope");
		ClientCredentialsGrantRequest req = new ClientCredentialsGrantRequest(clientId, clientSecret, scopes);
		assertEquals(grantType, req.getGrantType());
		assertEquals(clientId, req.getClientId());
		assertEquals(clientSecret, req.getClientSecret());
		assertEquals(scopes, req.getScopes());
	}
	
	@Test
	public void testAuthorizationCodeGrantRequest() {
		String grantType = "authorization_code";
		String code = "code";
		String clientId = "client-id";
		String clientSecret = "client-secret";
		String redirectUrl= "";
		Collection<String> scopes = new ArrayList<>();
		scopes.add("scope");
		AuthorizationCodeGrantRequest req = new AuthorizationCodeGrantRequest(code, clientId, clientSecret, redirectUrl, scopes);
		assertEquals(grantType, req.getGrantType());
		assertEquals(code, req.getCode());
		assertEquals(clientId, req.getClientId());
		assertEquals(clientSecret, req.getClientSecret());
		assertEquals(redirectUrl, req.getRedirectUri());
		assertEquals(scopes, req.getScopes());
	}
	
	@Test
	public void testPasswordCredentialsGrantRequest() {
		String grantType = "password";
		String username = "code";
		String password= "pass";
		String clientId = "client-id";
		String clientSecret = "client-secret";
		Collection<String> scopes = new ArrayList<>();
		scopes.add("scope");
		PasswordCredentialsGrantRequest req = new PasswordCredentialsGrantRequest(username, password, clientId, clientSecret, scopes);
		assertEquals(grantType, req.getGrantType());
		assertEquals(username, req.getUsername());
		assertEquals(clientId, req.getClientId());
		assertEquals(password, req.getPassword());
		assertEquals(clientSecret, req.getClientSecret());
		assertEquals(scopes, req.getScopes());
	}
	
	
	public static class TestPojo extends AccessTokenGrantRequest{
		public TestPojo(String grantType, String clientId, String clientSecret, Collection<String> scope) {
			super(grantType, clientId, clientSecret, scope);
		}
		public long getLongPrimitive(){
			return 123L;
		}
		public Collection<String> getCollection(){
			Collection<String> c = new ArrayList<>();
			c.add("collectionvalue");
			return c;
		}
		@Override
		public Map<String, Object> map() throws Exception {
			Map<String, Object> _m = new HashMap<>(super.map());
			_m.put("long_prop", this.getLongPrimitive());
			_m.put("collection_prop", this.getCollection());
			return _m;
		}
	}

	@Test
	public void testAsMap() throws Exception{
		TestPojo tp = new TestPojo("test", "abc", "secret", null);
		Map<String, Object> map = tp.map();
		assertEquals("test", map.get("grant_type"));
		assertTrue(((Long)map.get("long_prop")).longValue() == 123L);
		assertEquals(map.get("collection_prop"), tp.getCollection());
	}
	
}
