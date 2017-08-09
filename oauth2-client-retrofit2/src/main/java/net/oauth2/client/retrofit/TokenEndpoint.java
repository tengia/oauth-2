package net.oauth2.client.retrofit;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * https://tools.ietf.org/html/rfc6749#section-3.2
 *
 */
public interface TokenEndpoint {

	String DEFAULT_URL_PATH = "token";
	
	/**
	 * Provides access tokens for clients authenticated with basic authentication scheme.
	 * 
	 * https://tools.ietf.org/html/rfc6749#section-2.3.1
	 * 
	 * @param endpointUrl
	 * @param fields
	 * 
	 * @return a Call to the Token endpoint that may be used as invoked synchronously (using execute method) or asynchronously (using enque method)
	 */
	@POST
	@FormUrlEncoded
	Call<String> getAccessToken(@Url String endpointUrl, @FieldMap Map<String, Object> formFields);

	/**
	 * 
	 * https://tools.ietf.org/html/rfc6749#section-6
	 * 
	 * @param endpointUrl
	 * @param fields
	 * @return
	 */
	@POST
	@FormUrlEncoded
	Call<String> refreshToken(@Url String endpointUrl, @FieldMap Map<String, Object> formFields);
	

	static Builder builder(){
		return new Builder();
	}
	
	static class Builder {
		
		String url;
		OkHttpClient client;
		
		Builder(){}
		
		/**
		 * Mandatory
		 * @param url
		 * @return
		 */
		public Builder baseUrl(String url){
			if(url==null)
				throw new IllegalArgumentException("baseUrl cannot be null");
			else
				try {
					new URL(url);
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException("Malformed baseUrl", e);
				}
			this.url = url;
			return this;
		}
		
		/**
		 * Optional. Will use default OkHttpClient configuration if omitted.
		 * @param client
		 * @return
		 */
		public Builder client(OkHttpClient client){
			this.client = client;
			return this;
		}
		
		public <T> T build(Class<T> serviceClass){
			if(this.url==null)
				throw new IllegalStateException("Cannot invoke build prior to setting baseUrl");

			Retrofit.Builder retrofitServiceFactoryBuilder = new Retrofit.Builder()
					.baseUrl(this.url)
					.addConverterFactory(ScalarsConverterFactory.create());
			
			if(this.client!=null)
				retrofitServiceFactoryBuilder.client(this.client);
			
			Retrofit retrofitServiceFactory = retrofitServiceFactoryBuilder.build();				
			T tokenEndpointDelegate = retrofitServiceFactory.create(serviceClass);
			
			return tokenEndpointDelegate;
		}
		
		public TokenEndpoint build(){
			return build(TokenEndpoint.class);
		}
	}
}
