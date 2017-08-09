package net.oauth2.client.http.javase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commons.io.IOs;
import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.ProtocolError;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.http.DataBindingProvider;
import net.oauth2.client.http.FormEncodeDataBinding;
import net.oauth2.client.http.FormEncodeDataBinding.CollectionSerializer;
import net.oauth2.client.http.javase.conn.AuthenticationAdapter;
import net.oauth2.client.http.javase.conn.BasicAuthenticationAdapter;
import net.oauth2.client.http.javase.conn.HttpLoggingFormatter;
import net.oauth2.client.http.javase.conn.HttpsUrlConnectionFactory;
import net.oauth2.client.http.TokenServiceHttpClient;

public class HttpsURLConnectionClientAdapter implements TokenServiceHttpClient {

	protected static final Logger LOGGER = LoggerFactory.getLogger(HttpsURLConnectionClientAdapter.class);

	private final URL baseSeviceUrl;
	private final HttpLoggingFormatter logFormat;
	private final HttpsUrlConnectionFactory connectionFactory;
	@SuppressWarnings("rawtypes")
	private final Class tokenClass;
	private final DataBindingProvider<?> dataBindingProvider;
	
	private static final FormEncodeDataBinding grantRequestFormEncoder = new FormEncodeDataBinding()
			.with("scope", new CollectionSerializer<Collection<String>>());//TODO

	public <T extends AccessToken> HttpsURLConnectionClientAdapter(URL baseSeviceUrl,
			HttpsUrlConnectionFactory connectionFactory, HttpLoggingFormatter logFormat,
			DataBindingProvider<?> dataBindingProvider, Class<T> tokenClass) {
		this.connectionFactory = connectionFactory;
		this.logFormat = logFormat;
		this.dataBindingProvider = dataBindingProvider;
		this.tokenClass = tokenClass;
		this.baseSeviceUrl = baseSeviceUrl;
	}

	public static final class Builder {

		URL baseSeviceUrl;
		HttpLoggingFormatter logFormatter;
		ConnectionFactory connectionFactory;
		Proxy proxy;
		DataBindingProvider<?> dataBindingProvider;
		Class<?> tokenClass;

		public Builder() {
		}

		public Builder baseUrl(String url) throws MalformedURLException {
			if(url == null)
				throw new IllegalArgumentException("url is null");
			this.baseSeviceUrl = new URL(url);
			return this;
		}

		public Builder logFormatter(HttpLoggingFormatter logFormatter) {
			this.logFormatter = logFormatter;
			return this;
		}

		public Builder basicAuthentication(String username, String password) {
			if (this.connectionFactory != null)
				throw new IllegalStateException("connectionFactory has already been set");
			AuthenticationAdapter authenticationAdapter = new BasicAuthenticationAdapter(username, password);
			this.connectionFactory = new ConnectionFactory(this.proxy, authenticationAdapter);
			return this;
		}

		public Builder withProxy(Proxy proxy) {
			this.proxy = proxy;
			return this;
		}

		public Builder connectionFactory(ConnectionFactory connectionFactory) {
			if(connectionFactory == null)
				throw new IllegalArgumentException("connectionFactory is null");
			this.connectionFactory = connectionFactory;
			return this;
		}

		public <T extends AccessToken> Builder tokenClass(Class<T> tokenClass) {
			if(tokenClass == null)
				throw new IllegalArgumentException("tokenClass is null");
			this.tokenClass = tokenClass;
			return this;
		}

		public Builder mapper(DataBindingProvider<?> dataBindingProvider) {
			if(dataBindingProvider == null)
				throw new IllegalArgumentException("dataBindingProvider is null");
			this.dataBindingProvider = dataBindingProvider;
			return this;
		}

		@SuppressWarnings("unchecked")
		public <T extends AccessToken> HttpsURLConnectionClientAdapter build() throws MalformedURLException {
			if (this.baseSeviceUrl == null)
				throw new IllegalStateException("baseUrl is required but never invoked");
			if (this.connectionFactory == null)
				throw new IllegalStateException("connectionFactory is required but never invoked");
			if (this.dataBindingProvider == null)
				throw new IllegalStateException("databinding provider is not set");
			if (this.logFormatter == null)
				this.logFormatter = new HttpLoggingFormatter();
			if (this.tokenClass == null)
				this.tokenClass = (Class<T>) AccessToken.class;

			return new HttpsURLConnectionClientAdapter(this.baseSeviceUrl, this.connectionFactory, this.logFormatter,
					this.dataBindingProvider, (Class<T>) this.tokenClass);
		}
	}

	@Override
	public <T extends AccessToken> T post(String urlPath, AccessTokenGrantRequest grantRequest) throws IOException, OAuth2ProtocolException {
		// construct request path
		URL url = null;
		if (urlPath != null)
			url = new URL(this.baseSeviceUrl, urlPath);
		else
			url = this.baseSeviceUrl;

		// encode grant for www.form-encode entity payload
		String formPayload = grantRequestFormEncoder.encode(grantRequest, null);

		// get a new connection
		HttpsURLConnection connection = this.connectionFactory.connection(url);

		// http communication
		String responsePayload = this.httpPostForm(connection, formPayload);

		// bind object payload to java object model
		@SuppressWarnings("unchecked")
		T token = (T) this.dataBindingProvider.parseToken(responsePayload, this.tokenClass);

		return token;
	}

	private String httpPostForm(HttpsURLConnection connection, String formPayload) throws OAuth2ProtocolException, IOException {
		OutputStream out = null;
		InputStream in = null;
		String response = "";
		String errorDetails = "";
		try {
			// Post payload
			LOGGER.debug(logFormat.formatRequest(connection, formPayload));
			out = connection.getOutputStream();
			IOs.produce(out, formPayload);

			// Get Response
			int httpCode = connection.getResponseCode();
			if (httpCode < 400) { 
				// handle success
				in = connection.getInputStream();
				response = IOs.consume(in);
			} else {
				// handle errors
				final InputStream errorDetailsStream = connection.getErrorStream();
				errorDetails = IOs.consume(errorDetailsStream);
				String contentType = connection.getHeaderField("Content-Type");
				int idx = contentType.indexOf(";");
				if(idx > -1){
					contentType = contentType.substring(0, idx);
				}
				this.handleProtocolError(httpCode, errorDetails, contentType);
			}
		} finally {
			if (out != null)
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					/* ignore */}
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					/* ignore */}
			if (connection != null)
				connection.disconnect();
			LOGGER.debug(logFormat.formatResponse(connection, response, errorDetails));
		}
		return response;
	}

	protected void handleProtocolError(int code, String responsePayload, String contentType) throws IOException {
    	String errorMsg = "Token post request failed";
    	IOException ex = null;
		if (responsePayload == null || responsePayload.length() < 1 || contentType == null || !"application/json".equals(contentType)) {
			ex = new IOException(errorMsg);
		} else {
			if(contentType!=null && responsePayload!=null){
				String subtype = contentType.substring(contentType.indexOf("/")+1, contentType.length());
				if(subtype.startsWith("json")){
					ProtocolError error = this.dataBindingProvider.parseError(responsePayload, ProtocolError.class);
					ex = new OAuth2ProtocolException(error);
					errorMsg = String.format("%s. [%s]: %s", errorMsg, error.getError(), error.getDescription());	
				} else {
					errorMsg = responsePayload; //really?
				}
			}
		}
		//catch all
		if(ex == null)
			ex = new IOException(errorMsg);
		LOGGER.error(errorMsg);
		throw ex;
	}
	
	static class ConnectionFactory implements HttpsUrlConnectionFactory {
		private final Proxy proxy;
		private final AuthenticationAdapter authenticationAdapter;

		ConnectionFactory() {
			this.proxy = null;
			this.authenticationAdapter = null;
		}

		ConnectionFactory(final Proxy proxy, AuthenticationAdapter authenticationAdapter) {
			this.proxy = proxy;
			this.authenticationAdapter = authenticationAdapter;
		}

		@Override
		public HttpsURLConnection connection(URL url) throws IOException {
			HttpsURLConnection connection = null;
			if (this.proxy != null)
				connection = (HttpsURLConnection) url.openConnection(this.proxy);
			else
				connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");

			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			if (this.authenticationAdapter != null)
				this.authenticationAdapter.adapt(connection);

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);

			return connection;
		}

	}

/*	public static void main(String[] args) throws IOException {
		HttpsURLConnectionClientAdapter adapter = new HttpsURLConnectionClientAdapter(
				new URL("https://oauthasservices-a25bdd9cf.hana.ondemand.com/oauth2/api/v1/"), 
				new ConnectionFactory(null, new BasicAuthenticationAdapter("794e1695-35ad-3adb-8f2b-047a269f4f22", "abcd1234")), 
				new HttpLoggingFormatter(), new JacksonDataBindingProvider(), AccessToken.class);
		AccessToken token = adapter.post("token", new ClientCredentialsGrantRequest("794e1695-35ad-3adb-8f2b-047a269f4f22", "abcd1234", null));
		System.out.println(token);
	}*/
}