package net.oauth2.client.http;

import java.io.IOException;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.client.OAuth2ProtocolException;

/**
 * An interface to implementations encapsulating the HTTP transport
 * communication to an Authorization Server Token Service, and the conversion
 * between the HTTP request/response payloads and this client's Java object
 * model for authorization grant's and access tokens.
 */
public interface TokenServiceHttpClient {

	/**
	 * The default URL path string to an authorization server token service with
	 * value "token".
	 */
	static String DEFAULT_PATH = "token";
	
	/**
	 * 
	 * @param path
	 *            The URL path to an OAuth token service. The default value is
	 *            "token" (for reference - {@link #DEFAULT_PATH}). Different
	 *            implementations may support either or both relative and
	 *            absolute paths.
	 * @param payload
	 *            An OAuth grant request for access or refresh token.
	 * @param <T>
	 *            Type of the returned token, extending {@link net.oauth2.Token}
	 * @return A valid Token that can be used by the client application for
	 *         requests to a Resource Server protected by the Authorization
	 *         Server that issued this Token.
	 * @throws IOException
	 *             thrown in case of networking or other HTTP errors
	 * @throws OAuth2ProtocolException
	 *             thrown in case of OAuth protocol errors.
	 */
	<T extends AccessToken> T post(String path, AccessTokenGrantRequest payload) throws IOException, OAuth2ProtocolException;

}