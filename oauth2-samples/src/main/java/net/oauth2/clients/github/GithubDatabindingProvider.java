package net.oauth2.clients.github;

import java.io.IOException;

import net.oauth2.AccessToken;
import net.oauth2.ProtocolError;
import net.oauth2.client.http.DataBindingProvider;
import net.oauth2.client.http.FormEncodeDataBinding;
import net.oauth2.client.http.FormEncodeDataBinding.CollectionDeserializer;

public class GithubDatabindingProvider implements DataBindingProvider<Object> {

	FormEncodeDataBinding formDataBinding = new FormEncodeDataBinding()
			.with("scope", new CollectionDeserializer<String>().unmodifiable(true));
	
	public GithubDatabindingProvider() {}

	@Override
	public <T extends AccessToken> T parseToken(String payload, Class<T> tokenType) throws IOException {
		return formDataBinding.from(payload, tokenType, null);
	}

	@Override
	public Object raw() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void with(Object mapper) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends ProtocolError> T parseError(String payload, Class<T> errorType) throws IOException {
		throw new UnsupportedOperationException();
	}
	
}
