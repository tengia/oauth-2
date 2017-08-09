package net.oauth2.client.http;

import net.oauth2.AccessToken;

public class ResourceOAuthHeader {

	private static final String HTTP_HEADER_AUTHORIZATION_VALUE_PATTERN = "%s %s";
	
	public static final String HTTP_HEADER_NAME_AUTHORIZATION = "Authorization";
	
	public static String format(AccessToken token){
		return String.format(HTTP_HEADER_AUTHORIZATION_VALUE_PATTERN, token.getTokenType(), token.getAccessToken());
	}

}
