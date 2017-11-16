/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.ClientCredentialsGrantRequest;
import net.oauth2.RefreshTokenGrantRequest;
import net.oauth2.client.http.TokenServiceHttpClient;

@RunWith(MockitoJUnitRunner.class)
public class OAuthTokenServiceDelegateTest {

	@Mock
	TokenServiceHttpClient client;
	
	@Test
	public void testFetch() throws OAuth2ProtocolException, IOException {
		AccessTokenGrantRequest grant = new AccessTokenGrantRequest("testtype", "testclient", "testsecret", null);
		AccessToken _token = new AccessToken("123", "bearer", 123L, "456", null); 
		given(client.post("token", grant)).willReturn(_token);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(grant, client);
		
		AccessToken token = svc.fetch();
		
		assertEquals(_token, token);
		RefreshTokenGrantRequest expectedRefreshGrant = RefreshTokenGrantRequest.renew(grant, token.getRefreshToken(), null);
		assertEquals(expectedRefreshGrant, svc.getRefreshTokenGrantRequest());
		verify(client, only()).post("token", grant);
		verifyNoMoreInteractions(client);
	}
	
	@Test
	public void testFetchWithNoRefreshToken() throws OAuth2ProtocolException, IOException {
		AccessTokenGrantRequest grant = new AccessTokenGrantRequest("testtype", "testclient", "testsecret", null);
		AccessToken _token = new AccessToken("123", "bearer", 123L, null, null); 
		given(client.post("token", grant)).willReturn(_token);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(grant, client);
		
		AccessToken token = svc.fetch();
		
		assertEquals(_token, token);
		assertNull(svc.getRefreshTokenGrantRequest());
		verify(client, only()).post("token", grant);
		verifyNoMoreInteractions(client);
	}
	
	@Test
	public void testFetchReturnsNull() throws OAuth2ProtocolException, IOException {
		AccessTokenGrantRequest grant = new AccessTokenGrantRequest("testtype", "testclient", "testsecret", null);
		given(client.post("token", grant)).willReturn(null);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(grant, client);
		
		AccessToken token = svc.fetch();
		
		assertNull(token);
		assertNull(svc.getRefreshTokenGrantRequest());
		verify(client, only()).post("token", grant);
		verifyNoMoreInteractions(client);
	}

	@Test
	public void testRefresh() throws OAuth2ProtocolException, IOException {
		AccessTokenGrantRequest accessGrant = new AccessTokenGrantRequest("testtype", "testclient", "testsecret", null);
		RefreshTokenGrantRequest refreshGrant = RefreshTokenGrantRequest.renew(accessGrant, "456", null);
		AccessToken _token = new AccessToken("123", "bearer", 123L, "789", null); 
		given(client.post("token", refreshGrant)).willReturn(_token);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(accessGrant, client); 
		svc.setRefreshTokenGrantRequest(refreshGrant);
		AccessToken token = svc.refresh("456");
		
		assertEquals(_token, token);
		verify(client, only()).post("token", refreshGrant);
		verifyNoMoreInteractions(client);
	}
	
	@Test
	public void testRefreshWithNullArgument() throws OAuth2ProtocolException, IOException {
		AccessTokenGrantRequest accessGrant = new AccessTokenGrantRequest("testtype", "testclient", "testsecret", null);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(accessGrant, client);
		try{
			svc.refresh(null);
		} catch(IllegalArgumentException e){
			assertEquals("refreshToken is null", e.getMessage());
		} finally {
			verifyNoMoreInteractions(client);		
		}
	}
	
	@Test
	public void testRefreshBeforeFetch() throws OAuth2ProtocolException, IOException {
		AccessTokenGrantRequest accessGrant = new AccessTokenGrantRequest("testtype", "testclient", "testsecret", null);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(accessGrant, client);
		try{ 
			svc.refresh("test");
		} catch(IllegalStateException e){
			assertEquals("No refresh token grant initialized. Either authroization server does not support refreshing tokens or fetchToken was never invoked on this instance prior ot invoking refresh.", e.getMessage());
		} finally {
			verifyNoMoreInteractions(client);	
		}
	}
	
	@Test
	public void testRefreshGotTokenWithNoRefreshString() throws OAuth2ProtocolException, IOException {
		AccessTokenGrantRequest accessGrant = new AccessTokenGrantRequest("testtype", "testclient", "testsecret", null);
		RefreshTokenGrantRequest refreshGrant = RefreshTokenGrantRequest.renew(accessGrant, "456", null);
		AccessToken _token = new AccessToken("123", "bearer", 123L, null, null); 
		given(client.post("token", refreshGrant)).willReturn(_token);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(accessGrant, client); 
		svc.setRefreshTokenGrantRequest(refreshGrant);
		AccessToken token = svc.refresh("test");
		
		assertEquals(_token, token);
		verify(client, only()).post("token", refreshGrant);
		verifyNoMoreInteractions(client);
	}
	
	@Test
	public void testRefreshReturnsNull() throws OAuth2ProtocolException, IOException {
		AccessTokenGrantRequest accessGrant = new AccessTokenGrantRequest("testtype", "testclient", "testsecret", null);
		RefreshTokenGrantRequest refreshGrant = RefreshTokenGrantRequest.renew(accessGrant, "456", null); 
		given(client.post("token", refreshGrant)).willReturn(null);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(accessGrant, client); 
		svc.setRefreshTokenGrantRequest(refreshGrant);
		AccessToken token = svc.refresh("456");
		
		assertNull(token);
		assertEquals(refreshGrant, svc.getRefreshTokenGrantRequest());
		verify(client, only()).post("token", refreshGrant);
		verifyNoMoreInteractions(client);
	}
	
	@Test
	public void testRefreshTokenScopesExactMatch() throws OAuth2ProtocolException, IOException {
		Collection<String> requestedScope = new ArrayList<>(2);
		requestedScope.add("read");
		requestedScope.add("write");
		
		AccessTokenGrantRequest grantRequest = new ClientCredentialsGrantRequest("test-client", "test-secret", requestedScope);
		AccessToken returned_token = new AccessToken("test-token", null, 123L, "refresh", requestedScope);
		RefreshTokenGrantRequest refreshGrant = new RefreshTokenGrantRequest("refresh", grantRequest.getClientId(), grantRequest.getClientSecret(), requestedScope);
		given(this.client.post(TokenService.DEFAULT_URL_PATH, refreshGrant)).willReturn(returned_token);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(grantRequest, client); 
		svc.setRefreshTokenGrantRequest(refreshGrant);
		AccessToken token = svc.refresh("refresh");
		
		assertNotNull(token);
		assertEquals(refreshGrant, svc.getRefreshTokenGrantRequest());
		verify(client, only()).post("token", refreshGrant);
		verifyNoMoreInteractions(client);
	}
	
	@Test
	public void testRefreshTokenScopesAnyOrder() throws OAuth2ProtocolException, IOException {
		Collection<String> requestedScope = new ArrayList<>(2);
		requestedScope.add("read");
		requestedScope.add("write");
		Collection<String> receivedScope = new ArrayList<>(1);
		receivedScope.add("write");
		receivedScope.add("read");
		
		AccessTokenGrantRequest grantRequest = new ClientCredentialsGrantRequest("test-client", "test-secret", requestedScope);
		AccessToken returned_token = new AccessToken("test-token", null, 123L, "refresh", receivedScope);
		RefreshTokenGrantRequest refreshGrant = new RefreshTokenGrantRequest("refresh", grantRequest.getClientId(), grantRequest.getClientSecret(), requestedScope);
		given(this.client.post(TokenService.DEFAULT_URL_PATH, refreshGrant)).willReturn(returned_token);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(grantRequest, client); 
		svc.setRefreshTokenGrantRequest(refreshGrant);
		AccessToken token = svc.refresh("refresh");
		
		assertNotNull(token);
		assertTrue(refreshGrant.getScopes().containsAll(svc.getRefreshTokenGrantRequest().getScopes()));
		verify(client, only()).post("token", refreshGrant);
		verifyNoMoreInteractions(client);
	}
	
	@Test
	public void testRefreshTokenScopesLessFail() throws OAuth2ProtocolException, IOException {
		Collection<String> requestedScope = new ArrayList<>(2);
		requestedScope.add("read");
		requestedScope.add("write");
		Collection<String> receivedScope = new ArrayList<>(1);
		receivedScope.add("read");
		
		AccessTokenGrantRequest grantRequest = new ClientCredentialsGrantRequest("test-client", "test-secret", requestedScope);
		AccessToken returned_token = new AccessToken("test-token", null, 123L, "refresh", receivedScope);
		RefreshTokenGrantRequest refreshGrant = new RefreshTokenGrantRequest("refresh", grantRequest.getClientId(), grantRequest.getClientSecret(), requestedScope);
		given(this.client.post(TokenService.DEFAULT_URL_PATH, refreshGrant)).willReturn(returned_token);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(grantRequest, client); 
		svc.setRefreshTokenGrantRequest(refreshGrant);
		try{
			AccessToken token = svc.refresh("refresh");
			fail("expected IllegalStateException that was never thrown");
		} catch(IllegalStateException e){}
		
		verify(client, only()).post("token", refreshGrant);
		verifyNoMoreInteractions(client);
	}
	
	@Test
	public void testRefreshTokenSameSizeDifferentSocpesFail() throws OAuth2ProtocolException, IOException {
		Collection<String> requestedScope = new ArrayList<>(2);
		requestedScope.add("read");
		requestedScope.add("write");
		Collection<String> receivedScope = new ArrayList<>(1);
		receivedScope.add("_read_");
		receivedScope.add("_write_");
		
		AccessTokenGrantRequest grantRequest = new ClientCredentialsGrantRequest("test-client", "test-secret", requestedScope);
		AccessToken returned_token = new AccessToken("test-token", null, 123L, "refresh", receivedScope);
		RefreshTokenGrantRequest refreshGrant = new RefreshTokenGrantRequest("refresh", grantRequest.getClientId(), grantRequest.getClientSecret(), requestedScope);
		given(this.client.post(TokenService.DEFAULT_URL_PATH, refreshGrant)).willReturn(returned_token);
		
		OAuthTokenServiceDelegate<AccessToken> svc = new OAuthTokenServiceDelegate<>(grantRequest, client); 
		svc.setRefreshTokenGrantRequest(refreshGrant);
		try{
			AccessToken token = svc.refresh("refresh");
			fail("expected IllegalStateException that was never thrown");
		} catch(IllegalStateException e){}
		
		verify(client, only()).post("token", refreshGrant);
		verifyNoMoreInteractions(client);
	}

}
