/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2.client;

import java.io.IOException;

import net.oauth2.ProtocolError;

/**
 * Exception wrapper for OAuth protocol error payloads
 *
 */
public class OAuth2ProtocolException extends IOException {

	private static final long serialVersionUID = 3867699761347758500L;

	private ProtocolError error;
	
	public OAuth2ProtocolException(ProtocolError error) {
		super(error !=null ? (error.getError() + " - " + error.getDescription()) : null);
		this.error = error;
	}	
	
	public ProtocolError getError() {
		return error;
	}

}
