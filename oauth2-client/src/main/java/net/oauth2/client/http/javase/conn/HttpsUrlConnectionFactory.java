package net.oauth2.client.http.javase.conn;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public interface HttpsUrlConnectionFactory {

	HttpsURLConnection connection(URL url) throws IOException;
	
}
