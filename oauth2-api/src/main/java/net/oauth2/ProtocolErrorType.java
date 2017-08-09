package net.oauth2;

public enum ProtocolErrorType {
	
	InvalidRequest("invalid_request"),
	InvalidClient("invalid_client"),
	InvalidGrant("invalid_grant"),
	UnauthorizedClient("unauthorized_client"),
	UnsupportedGrantType("unsupported_grant_type"),
	InvalidScope("invalid_scope"),
	
	AccessDenied("access_denied"),
	UnsupportedResponseType("unsupported_response_type"),
	ServerError("server_error"),
	TemporarilyUnavailable("temporarily_unavailable");
	
	final String type;
	private ProtocolErrorType(final String type){
		this.type = type;
	}
	@Override
	public String toString() {
		return this.type;
	}
	public String getType() {
		return type;
	}	
	public static ProtocolErrorType parse(String type){
		if (type == null || type.length() < 1)
			throw new IllegalArgumentException("invalid argument 'type'[" + type + "]");
		
		ProtocolErrorType errType = null;
		ProtocolErrorType[] types = ProtocolErrorType.values();
		for (ProtocolErrorType protocolErrorType : types) {
			String _type = protocolErrorType.getType();
			if(_type.equals(type)){
				errType = protocolErrorType;
				break;
			}
		}
		return errType;
	}  
}