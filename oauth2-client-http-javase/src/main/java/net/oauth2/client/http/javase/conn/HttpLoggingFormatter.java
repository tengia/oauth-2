/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client.http.javase.conn;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpLoggingFormatter {
	
	private static final String NEWLINE = System.getProperty("line.separator");

	public HttpLoggingFormatter() {

	}

	public String formatRequest(HttpsURLConnection connection, String requestPayload) {
		StringBuilder log = new StringBuilder();
		log.append("--> ");

		final String requestLine = connection.getRequestMethod() + " " + connection.getURL().toString();
		log.append(requestLine);
		log.append(NEWLINE);
		
		final String requestHeaders = headers(connection.getRequestProperties());
		log.append(requestHeaders);
		log.append(NEWLINE);

		if (requestPayload != null && requestPayload.trim().length() > 0) {
			log.append(requestPayload);
		}

		return log.toString();
	}

	public String formatResponse(HttpsURLConnection connection, String responsePayload, String errorDetails) {
		StringBuilder log = new StringBuilder();
		log.append("<-- ");

		final String responseStatusLine = getStatusLine(connection);
		log.append(responseStatusLine);
		log.append(NEWLINE);

		final String responseHeaders = headers(connection.getHeaderFields());
		log.append(responseHeaders);
		log.append(NEWLINE);

		if (responsePayload != null && responsePayload.trim().length() > 0) {
			log.append(responsePayload);
			log.append(NEWLINE);
		}

		if (errorDetails != null && errorDetails.trim().length() > 0) {
			log.append(errorDetails);
			log.append(NEWLINE);
		}

		return log.toString();
	}

	private String headers(final Map<String, List<String>> headers) {
		final StringBuilder headersList = new StringBuilder();
		for (final Map.Entry<String, List<String>> header : headers.entrySet()) {
			if (header.getKey() == null || header.getValue() == null) {
				continue;
			}
			headersList.append(header.getKey()).append(":").append(header.getValue().toString()).append(NEWLINE);
		}
		return headersList.toString();
	}

	private String getStatusLine(final HttpURLConnection connection) {
		String responseStatusLine = null;
		if (connection.getHeaderFields() != null) {
			if (connection.getHeaderFields().get(null) != null) {
				final List<String> statusLineList = connection.getHeaderFields().get(null);
				if (statusLineList.size() > 0) {
					responseStatusLine = statusLineList.get(0);
				}
			}
		}
		if (responseStatusLine == null) {
			try {
				responseStatusLine = "HTTP/1.0 " + connection.getResponseCode() + " " + connection.getResponseMessage();
			} catch (final IOException e) {
				responseStatusLine = "Cannot retrieve status line from this connection reponse";
			}
		}
		return responseStatusLine;
	}

}
