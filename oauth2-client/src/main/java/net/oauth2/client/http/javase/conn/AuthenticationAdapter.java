package net.oauth2.client.http.javase.conn;

import javax.net.ssl.HttpsURLConnection;

public interface AuthenticationAdapter {

	void adapt(HttpsURLConnection connection);

}