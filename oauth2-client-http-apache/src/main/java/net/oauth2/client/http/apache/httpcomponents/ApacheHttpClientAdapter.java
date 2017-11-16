/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.apache.httpcomponents;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.oauth2.AccessToken;
import net.oauth2.AccessTokenGrantRequest;
import net.oauth2.ProtocolError;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.http.DataBindingProvider;
import net.oauth2.client.http.FormEncodeDataBinding;
import net.oauth2.client.http.TokenServiceHttpClient;

/**
 * A OAuth Token Service HTTP client adapter based on Apache HTTP Client (org.apache.httpcomponents.HttpClient).
 *
 */
public class ApacheHttpClientAdapter implements TokenServiceHttpClient {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientAdapter.class);
	
	private URL baseUrl;
	private CloseableHttpClient httpclient;
	
	private DataBindingProvider<?> dataBindingProvider;
	@SuppressWarnings("rawtypes")
	private Class tokenClass;

	@SuppressWarnings("unchecked")
	public <T extends AccessToken> ApacheHttpClientAdapter(URL baseUrl, String username, String password, DataBindingProvider<?> dataBindingProvider, Class<T> tokenClass) {
		this.baseUrl = baseUrl;
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		this.httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
		this.dataBindingProvider = dataBindingProvider;
		if (tokenClass == null)
			tokenClass = (Class<T>) AccessToken.class;
		this.tokenClass = tokenClass;
	}

	@Override
	public <T extends AccessToken> T post(String urlPath, AccessTokenGrantRequest grantRequest) throws IOException {
		//construct request path
		URL url = null;
		if(urlPath!=null)
			url = new URL(this.baseUrl, urlPath);
		else
			url = this.baseUrl;
		
		//encode grant for www.form-encode entity payload
		List<NameValuePair> formPayload = formEncodeGrant(grantRequest);

		//http communication
		String responsePayload = this.httpPostForm(url, formPayload);
		
		//bind object payload to java object model 
		@SuppressWarnings("unchecked")
		T token = (T) this.dataBindingProvider.parseToken(responsePayload, this.tokenClass);
		
		return token;
	}
	
	private String httpPostForm(URL url, List<NameValuePair> formPayload) throws IOException, OAuth2ProtocolException {
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formPayload, Consts.UTF_8);

		HttpPost httpPostMethod = new HttpPost(url.toExternalForm());
		httpPostMethod.setEntity(entity);
		
		HttpResponse response = this.httpclient.execute(httpPostMethod);
		
		String responsePayload = null;
		try {
		    HttpEntity responseEntity = response.getEntity();
		    if (responseEntity != null) {
		        long len = responseEntity.getContentLength();
		        if (len != -1 && len < 2048) {
		            responsePayload = EntityUtils.toString(responseEntity);
		        } else {
		        	//TODO: as per apache guidelines for secure handling of large responses (although we don't expect large response here)
		        }
		        //Protocol errors should be in the range [400-500). OAuth2 is just too permissive to reliably infer if it's protocol or other error based on the code...
				if (response.getStatusLine().getStatusCode() > 399) {
		        	this.handleProtocolError(response,  responsePayload);
		        }
		    }
		} finally {
			if(response instanceof CloseableHttpResponse){
				((CloseableHttpResponse)response).close();
			}
		}
		return responsePayload;
	}
	
	private static List<NameValuePair> formEncodeGrant(AccessTokenGrantRequest grant){
		final List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		try {
			grant.map()
			.entrySet().stream().forEach((e)->{
				String key = e.getKey();
				String val = null;
				if (e.getValue() != null) {
					if(String.class.isAssignableFrom(e.getValue().getClass())){
						val = (String) e.getValue();
					} else {
						if(key == "scope"){
							@SuppressWarnings("unchecked")
							Collection<String> scopes = (Collection<String>) e.getValue();
							val = FormEncodeDataBinding.CollectionSerializer.formatToDelimitedString(scopes);
						} else {
							throw new RuntimeException("Unsupported type for form encoding: " + e.getValue().getClass());
						}
					}
					try {
						if(val!=null){
							val = URLEncoder.encode(val, StandardCharsets.UTF_8.name());
							formparams.add(new BasicNameValuePair(key, val));
						}
					} catch (UnsupportedEncodingException uee) {
						/* ignore */
						throw new RuntimeException(uee);
					}				
				}
			});
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot encode this AccessTokenGrantRequest to application/x-www-form-urlencoded string", e);
		}
		return formparams;
	}

	private void handleProtocolError(HttpResponse response, String responsePayload) throws IOException{
    	String errorMsg = "Token post request failed";
    	String contentTypeString = null;
    	if(response.containsHeader("Content-Type")){
    		contentTypeString = response.getFirstHeader("Content-Type").getValue();
			int idx = contentTypeString.indexOf(";");
			if(idx > -1){
				contentTypeString = contentTypeString.substring(0, idx);
			}
    	}
    	IOException ex = null;
		if (responsePayload == null || responsePayload.length() < 1 || contentTypeString == null || !"application/json".equals(contentTypeString)) {
			ex = new IOException(errorMsg);
		} else {
			if(contentTypeString!=null && responsePayload!=null){
				String subtype = contentTypeString.substring(contentTypeString.indexOf("/")+1, contentTypeString.length());
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
	
	
/*	public static void main(String[] args) throws OAuth2ProtocolException, IOException {
		String clientId = "794e1695-35ad-3adb-8f2b-047a269f4f22";
		String clientSecret = "abcd1234";
		String user = clientId;
		String pass = clientSecret;
		
		String url = "https://oauthasservices-a25bdd9cf.hana.ondemand.com/oauth2/api/v1/";
		
		TokenServiceHttpClient client = new ApacheHttpClientAdapter(new URL(url), user, pass,null, AccessToken.class);
		
		ClientCredentialsGrantRequest cc = new ClientCredentialsGrantRequest(clientId, clientSecret, null);
		DefaultTokenService<AccessToken> dt = new DefaultTokenService<>(cc, client);
		AccessToken token = dt.fetchToken();
		System.out.println(token);
	}*/
}
