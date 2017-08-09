package net.oauth2.client;

import java.io.IOException;

import net.oauth2.AccessToken;

/**
 * Functional capability for synchronous provisioning of Access Tokens.
 *
 */
public interface TokenProvider {

	/**
	 * Return token synchronously
	 * 
	 * @return
	 * @throws OAuth2ProtocolException
	 */
	<T extends AccessToken> T get() throws OAuth2ProtocolException, IOException;

}