/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.retrofit;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.oauth2.AccessToken;

public class MyToken extends AccessToken {
	private String geolocation;

	@JsonCreator
	public MyToken(@JsonProperty("access_token")final String accessToken,
				 @JsonProperty("expires_in")final long expiresIn, 
				 @JsonProperty("refresh_token")final String refreshToken, 
				 @JsonProperty("scope") final Collection<String> scope,
				 @JsonProperty("geolocation")final String geolocation) {
		super(accessToken, "Bearer", expiresIn, refreshToken, scope);
		this.geolocation = geolocation;
	}
	@JsonProperty("geolocation")
	public String getGeolocation() {
		return geolocation;
	}
}