package net.oauth2.client.http.javase.conn;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

public class BasicAuthenticationAdapter implements AuthenticationAdapter {
	
	private static final String HTTP_HEADER_NAME_AUTHORIZATION = "Authorization";
	private static final String HTTP_HEADER_AUTHORIZATION_VALUE_PATTERN = "Basic %s";
	private static final String BASIC_AUTH_PATTERN = "%s:%s";

	final String username;
	final String password;
	final String headerValue;

	public BasicAuthenticationAdapter(String username, String password) {
		this.username = username!=null?username:"";
		this.password = password!=null?password:"";
		String formattedValue = String.format(BASIC_AUTH_PATTERN, this.username, this.password);
		String encodedValue = Base64.getEncoder().encodeToString(formattedValue.getBytes(StandardCharsets.UTF_8));
		this.headerValue = String.format(HTTP_HEADER_AUTHORIZATION_VALUE_PATTERN, encodedValue);
	}

	/* (non-Javadoc)
	 * @see net.oauth2.http.AuthenticationAdapter#adapt(javax.net.ssl.HttpsURLConnection)
	 */
	@Override
	public void adapt(HttpsURLConnection connection){
		connection.setRequestProperty(HTTP_HEADER_NAME_AUTHORIZATION, this.headerValue);
	}
	
}
