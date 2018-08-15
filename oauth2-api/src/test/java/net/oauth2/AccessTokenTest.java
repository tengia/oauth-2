/*
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies
 * this distribution, and is available at
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenTest {

    private Collection<String> scopes = new ArrayList<>(3);

    @Before
    public void before() {
        scopes.add("a");
        scopes.add("b");
        scopes.add("c");
    }

    @Test
    public void testHasScope() {
        AccessToken token = new AccessToken(null, null, 0, null, scopes);
        boolean hasScope = token.hasScope("a");
        assertTrue(hasScope);
    }

    @Test
    public void testHasScopeNegative() {
        AccessToken token = new AccessToken(null, null, 0, null, scopes);
        boolean hasScope = token.hasScope("d");
        assertFalse(hasScope);
    }

    @Test
    public void testHasScopeNull() {
        AccessToken token = new AccessToken(null, null, 0, null, null);
        boolean hasScope = token.hasScope("a");
        assertFalse(hasScope);
    }

    @Test
    public void testHasScopeNullAndNegative() {
        AccessToken token = new AccessToken(null, null, 0, null, null);
        boolean hasScope = token.hasScope("d");
        assertFalse(hasScope);
    }

    @Test
    public void testMapConstructorLongExpiresIn() {
        Map<String, Object> map = new HashMap<>();
        map.put("expires_in", 1L);
        AccessToken token = new AccessToken(map);
        assertEquals(1L, token.getExpiresIn());
    }

    @Test
    public void testMapConstructorIntegerExpiresIn() {
        Map<String, Object> map = new HashMap<>();
        map.put("expires_in", 1);
        AccessToken token = new AccessToken(map);
        assertEquals(1L, token.getExpiresIn());
    }

    @Test
    public void testMapConstructorScopeCollection() {
        Map<String, Object> map = new HashMap<>();
        Collection<String> scopes = new ArrayList<>();
        scopes.add("a");
        scopes.add("b");
        map.put("scope", scopes);
        AccessToken token = new AccessToken(map);
        assertEquals(scopes, token.getScopes());
    }

    @Test
    public void testMapConstructorSpaceSeparatedScopesString() {
        Map<String, Object> map = new HashMap<>();
        Collection<String> scopes = new ArrayList<>();
        scopes.add("a");
        scopes.add("b");
        map.put("scope", "a b");
        AccessToken token = new AccessToken(map);
        assertEquals(scopes, token.getScopes());
    }

    @Test
    public void testGetters() {
        String tokenType = "OAuth";
        String tokenString = "token";
        long expiresIn = 3600;
        String refreshToken = "refresh-token";
        AccessToken token = new AccessToken(tokenString, tokenType, expiresIn, refreshToken, scopes);
        assertEquals(tokenType, token.getTokenType());
        assertEquals(tokenString, token.getAccessToken());
        assertEquals(expiresIn, token.getExpiresIn());
        assertEquals(refreshToken, token.getRefreshToken());
        assertEquals(this.scopes, token.getScopes());
    }

    @Test
    public void testToString() {
        String tokenType = "OAuth";
        String tokenString = "token";
        long expiresIn = 3600;
        String refreshToken = "refresh-token";
        AccessToken token = new AccessToken(tokenString, tokenType, expiresIn, refreshToken, scopes);
        String s = token.toString();
        assertEquals("AccessToken [accessToken=token, tokenType=OAuth, expiresIn=3600, refreshToken=refresh-token, scopes=[a, b, c]]", s);
    }

    @After
    public void after() {
        scopes.clear();
    }

}
