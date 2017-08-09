package net.oauth2.client;

import java.io.IOException;

import net.oauth2.AccessToken;

public class SimpleTokenProvider implements TokenProvider{

	private final TokenService tokenService;
	
	public SimpleTokenProvider(TokenService tokenService) {
		this.tokenService = tokenService;
	}
	
	/**
	 * Fetches a Token from a TokenService upon each invocation.  
	 */
	@Override
	public <T extends AccessToken> T get() throws OAuth2ProtocolException, IOException {
		T token = tokenService.fetch();
		return token;
	}
	
}
