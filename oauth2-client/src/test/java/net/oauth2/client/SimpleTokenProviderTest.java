package net.oauth2.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import net.oauth2.AccessToken;

@RunWith(MockitoJUnitRunner.class)
public class SimpleTokenProviderTest {

	@Mock
	TokenService tokenService;

	@Test
	public void testGetToken() throws IOException {
		AccessToken token = new AccessToken("token","token_type", 0, "refresh_token", null);
		given(tokenService.fetch()).willReturn(token);
		SimpleTokenProvider tokenProvider = new SimpleTokenProvider(this.tokenService);
		
		AccessToken newToken = tokenProvider.get();
		
		assertEquals(token, newToken);
		verify(tokenService, only()).fetch();
		verifyNoMoreInteractions(tokenService);
	}
	
	@Test(expected=IOException.class)
	public void testGetTokenFailing() throws IOException {
		given(tokenService.fetch()).willThrow(new IOException());
		SimpleTokenProvider tokenProvider = new SimpleTokenProvider(this.tokenService);
		
		tokenProvider.get();
		
		verify(tokenService, only()).fetch();
		verifyNoMoreInteractions(tokenService);
	}
	
	@After
	public void afterTest(){
		Mockito.reset(this.tokenService);
	}
}
