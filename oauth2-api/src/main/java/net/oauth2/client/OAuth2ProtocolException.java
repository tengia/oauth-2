package net.oauth2.client;

import java.io.IOException;

import net.oauth2.ProtocolError;

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
