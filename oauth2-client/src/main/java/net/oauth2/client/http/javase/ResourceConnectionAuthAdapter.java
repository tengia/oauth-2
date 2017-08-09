package net.oauth2.client.http.javase;

import javax.net.ssl.HttpsURLConnection;

import net.oauth2.AccessToken;
import net.oauth2.client.http.ResourceOAuthHeader;

public class ResourceConnectionAuthAdapter {

	public ResourceConnectionAuthAdapter() {}
	
	public <T extends AccessToken> void adapt(HttpsURLConnection connection, T token) {
		String headerValue = ResourceOAuthHeader.format(token);
		connection.setRequestProperty(ResourceOAuthHeader.HTTP_HEADER_NAME_AUTHORIZATION, headerValue);
	}

}
