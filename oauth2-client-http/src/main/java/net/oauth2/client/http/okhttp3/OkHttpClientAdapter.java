package net.oauth2.client.http.okhttp3;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.ProtocolError;
import net.oauth2.RefreshTokenGrantRequest;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.http.DataBindingProvider;
import net.oauth2.client.http.FormEncodeDataBinding;
import net.oauth2.client.http.FormEncodeDataBinding.CollectionSerializer;
import net.oauth2.client.http.TokenServiceHttpClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpClientAdapter<T extends AccessToken> implements TokenServiceHttpClient {

	protected static final Logger LOGGER = LoggerFactory.getLogger(OkHttpClientAdapter.class);
	
	protected String serviceBaseUrl;
	protected RefreshTokenGrantRequest refreshTokenGrantRequest;
	protected OkHttpClient okHttpClient;
	private DataBindingProvider<?> dataBindingProvider;
	private Class<T> accessTokenClass;

	private static final FormEncodeDataBinding grantRequestFormEncoder = new FormEncodeDataBinding()
			.with("scope", new CollectionSerializer<Collection<String>>());//TODO
	
	@SuppressWarnings("unchecked")
	public OkHttpClientAdapter(final String serviceBaseUrl, final OkHttpClient httpClient, DataBindingProvider<?> dataBindingProvider, Class<T> accessTokenClass) {
		if (serviceBaseUrl == null)
			throw new IllegalArgumentException("serviceBaseUrl is null");
		if (httpClient == null)
			throw new IllegalArgumentException("httpClient is null");

		this.serviceBaseUrl = serviceBaseUrl;
		this.okHttpClient = httpClient;
		this.dataBindingProvider = dataBindingProvider;
		if (accessTokenClass == null)
			accessTokenClass = (Class<T>) AccessToken.class;
		this.accessTokenClass = accessTokenClass;
	}

	public static final MediaType WWW_FORM_ENCODED = MediaType.parse("application/x-www-form-encoded");
	public static final MediaType JSON = MediaType.parse("*/json");

	@SuppressWarnings("unchecked")
	@Override
	public T post(String urlPath, AccessTokenGrantRequest grantRequest) throws IOException {
		// construct request path
		if (urlPath == null)
			urlPath = DEFAULT_PATH;
		String requestUrl = String.format("%s%s", this.serviceBaseUrl, urlPath);

		//encode payload
		String payload = grantRequestFormEncoder.encode(grantRequest, null);
		
		//http comm
		RequestBody body = RequestBody.create(WWW_FORM_ENCODED, payload);
		Request request = new Request.Builder().url(requestUrl).post(body).build();
		
		Response response = this.okHttpClient.newCall(request).execute();
		
		T token = null;
		if(response.isSuccessful()){
			String responsePayload = response.body().string();
			// bind object payload to java object model
			token = (T) this.dataBindingProvider.parseToken(responsePayload, this.accessTokenClass);
		} else {
			this.handleProtocolError(response, "");
		}
		return token;
	}

	protected void handleProtocolError(Response response, String operationName) throws IOException {
		ProtocolError error = null;
		String errorMsg = "Access Token post request failed";
		IOException ex = null;
		String responseContentTypeSubtype = null;
		String contentTypeHeaderValue = response.header("Content-Type");
		if(contentTypeHeaderValue!=null)
			responseContentTypeSubtype = MediaType.parse(response.header("Content-Type")).subtype();
		if ((response.code() > 399) && responseContentTypeSubtype !=null && responseContentTypeSubtype.equals(JSON.subtype())) {
			String responseString = response.body().string();
			error = this.dataBindingProvider.parseError(responseString, ProtocolError.class);
			ex = new OAuth2ProtocolException(error);
			errorMsg = String.format("%s. [%s]: %s", errorMsg, error.getError(), error.getDescription());
		} else {
			ex = new IOException(errorMsg);
		}
		LOGGER.error(errorMsg);
		throw ex;
	}
	
/*	public static void main(String[] args) throws OAuth2ProtocolException, IOException {
		String clientId = "794e1695-35ad-3adb-8f2b-047a269f4f22";
		String clientSecret = "abcd1234";
		String user = clientId;
		String pass = clientSecret;
		
		String url = "https://oauthasservices-a25bdd9cf.hana.ondemand.com/oauth2/api/v1/";
		
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.authenticator(new Authenticator() {
			          @Override public Request authenticate(Route route, Response response) throws IOException {
			              String credential = Credentials.basic(user, pass);
			              return response.request().newBuilder()
			                  .header("Authorization", credential)
			                  .build();
			            }
			          }).build();
				
		OAuth2TokenServiceHttpClient<AccessToken> client = new OkHttpClientAdapter<>(url, okHttpClient, AccessToken.class);
		ClientCredentialsGrantRequest cc = new ClientCredentialsGrantRequest(clientId, clientSecret, null);
		DefaultTokenService<AccessToken> dt = new DefaultTokenService<>(cc, client);
		AccessToken token = dt.fetchToken();
		System.out.println(token);
	}*/
	
}
