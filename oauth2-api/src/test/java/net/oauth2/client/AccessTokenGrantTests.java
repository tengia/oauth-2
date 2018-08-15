/*
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies
 * this distribution, and is available at
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.AuthorizationCodeGrantRequest;
import net.oauth2.ClientCredentialsGrantRequest;
import net.oauth2.PasswordCredentialsGrantRequest;
import net.oauth2.RefreshTokenGrantRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
        String redirectUrl = "";
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
        String password = "pass";
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

    @Test
    public void testAsMap() throws Exception {
        Collection<String> scope = new ArrayList<>();
        scope.add("a");
        scope.add("b");
        TestPojo tp = new TestPojo("test", "abc", "secret", scope);
        Map<String, Object> map = tp.map();
        assertEquals("test", map.get("grant_type"));
        assertEquals(123L, ((Long) map.get("long_prop")).longValue());
        assertEquals(map.get("collection_prop"), tp.getCollection());
        assertEquals("a b", map.get("scope"));
    }

    @Test
    public void testToString() {
        AccessTokenGrantRequest grantRequest = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        assertEquals("AccessTokenGrantRequest [grant_type=grant_type, client_id=client_id, client_secret=client_secret, scope=null]", grantRequest.toString());
    }

    @Test
    public void testEqualsIsReflexive() {
        AccessTokenGrantRequest grantRequest = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        // must be reflexive: for any non-null reference value x, x.equals(x) must return true
        assertEquals(grantRequest, grantRequest);
        // more than one invocation of hashCode() on the same object will return the same integer
        assertEquals(grantRequest.hashCode(), grantRequest.hashCode());
    }

    @Test
    public void testEqualsIsSymmetric() {
        AccessTokenGrantRequest grantRequest1 = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        AccessTokenGrantRequest grantRequest2 = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);

        // must be symmetric: for any non-null reference values x and y, x.equals(y) must return true if and only if y.equals(x) returns true
        assertTrue(grantRequest1.equals(grantRequest2) && grantRequest1.equals(grantRequest2));
        //Invocation of hashCode on equal objects produces the same integer
        assertEquals(grantRequest1.hashCode(), grantRequest2.hashCode());
    }

    @Test
    public void testEqualsIsTransitive() {
        AccessTokenGrantRequest grantRequest1 = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        AccessTokenGrantRequest grantRequest2 = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        AccessTokenGrantRequest grantRequest3 = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);

        // must be transitive: for any non-null reference value x, y and z, if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) must return true
        assertTrue(grantRequest1.equals(grantRequest2) && grantRequest2.equals(grantRequest3) && grantRequest1.equals(grantRequest3));
        // Invocation of hashCode on equal objects produces the same integer
        assertTrue(grantRequest1.hashCode() == grantRequest2.hashCode() && grantRequest1.hashCode() == grantRequest3.hashCode());
    }

    @Test
    public void testEqualsIsConsistent() {
        AccessTokenGrantRequest grantRequest1 = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        AccessTokenGrantRequest grantRequest2 = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);

        // must be consistent: for any non-null reference values x and y, multiple invocations of x.equals(y) must return the same result if no information used in equals
        // comparison test is modifed
        assertTrue(grantRequest1.equals(grantRequest2) && grantRequest1.equals(grantRequest2));
    }

    @Test
    public void testEqualsIsFalseForNullArguments() {
        AccessTokenGrantRequest grantRequest = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        //handling null arguments: for any non-null reference value x, x.equals(null) must return false
        assertNotEquals(null, grantRequest);
    }

    @Test
    public void testEqualsIsFalseForDifferentTypeArguments() {
        AccessTokenGrantRequest grantRequest = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        //test on equality with different type will return false
        assertNotEquals(grantRequest, new Object());
    }

    @Test
    public void testEqualsIsFalseOnNotEqualCfgObjects() {

        AccessTokenGrantRequest grantRequest1 = new AccessTokenGrantRequest("grant_type", "client_id", "client_secret", null);
        AccessTokenGrantRequest grantRequest2 = new AccessTokenGrantRequest("grant_type1", "client_id1", "client_secret1", null);

        // objects are not equal
        assertNotEquals(grantRequest1, grantRequest2);
        // Invocation of hashCode on not equal objects produces different results
        assertTrue(grantRequest1.hashCode() != grantRequest2.hashCode());
    }

    public static class TestPojo extends AccessTokenGrantRequest {
        TestPojo(String grantType, String clientId, String clientSecret, Collection<String> scope) {
            super(grantType, clientId, clientSecret, scope);
        }

        @Override
        public Map<String, Object> map() throws Exception {
            Map<String, Object> _m = new HashMap<>(super.map());
            _m.put("long_prop", this.getLongPrimitive());
            _m.put("collection_prop", this.getCollection());
            return _m;
        }

        long getLongPrimitive() {
            return 123L;
        }

        Collection<String> getCollection() {
            Collection<String> c = new ArrayList<>();
            c.add("collectionvalue");
            return c;
        }
    }

}
