/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
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
