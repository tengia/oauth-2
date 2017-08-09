package net.oauth2.clients.github;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

import net.oauth2.AuthorizationCodeGrantRequest;
import net.oauth2.client.OAuthTokenServiceDelegate;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.SimpleTokenProvider;
import net.oauth2.client.TokenProvider;
import net.oauth2.client.http.TokenServiceHttpClient;
import net.oauth2.client.http.javase.HttpsURLConnectionClientAdapter;

/**
 * <p>
 * The sample shows how to obtain a valid token for using Google APIs.
 * </p>
 * <p>
 * <B>Prerequisites.</B><BR>
 * You will need a valid OAuth 2.0 client registered in Github Developer
 * Settings > OAuth Applications at https://github.com/settings/applications.
 * <br>
 * More specifically, you will need 'Client ID', 'Client Secret', 'Authorization
 * callback URL' exactly as specified in the registered application.<br>
 * Additionally, you will need also API scopes.
 * </p>
 * <p>
 * <B>Using the sample.</B><BR>
 * To trigger a token request the sample needs a couple of parameters from your
 * client and apis that you will use with the token (specified above).<br>
 * You can supply them as environment variables and run the java program or you
 * can supply them interactively in the command line. Mixing the two options is
 * also possible, i.e. supply part of the parameters as environment variables
 * and another part interactively.<br>
 * The names of the environment variables that you can use to supply the
 * parameters are:
 * <ul>
 * <li><B>GITHUB_API_CLIENT_ID</B>: The client ID you received from GitHub when
 * you registered an OAuth Application in Developer Settings in your account.
 * </li>
 * <li><B>GITHUB_API_CLIENT_SECRET</B>: The client Secret</li>
 * <li><B>GITHUB_API_REDIRECT_URI</B>: The Authorization callback URL</li>
 * <li><B>GITHUB_API_SCOPES</B>: A space delimited list of scopes.</li>
 * <li><B>GITHUB_API_AUTHORIZATION_CODE</B>: The authorization code for this
 * client if you have already obtained it by some means</li>
 * </ul>
 * Note, that if you do not provide authorization code via environment variable,
 * the sample will prompt you for any missing parameters required to form a
 * valid request for the code and generate it for you. Use it to obtain and
 * supply the code.
 * </p>
 * <p>
 * <b>Reference</b><br>
 * https://developer.github.com/apps/building-integrations/setting-up-and-registering-oauth-apps/about-authorization-options-for-oauth-apps/
 * </p>
 */
public class GithubClient {

	public static void main(String[] args) throws OAuth2ProtocolException, IOException {
		String clientId = System.getenv().get("GITHUB_API_CLIENT_ID");
		String clientSecret = System.getenv().get("GITHUB_API_CLIENT_SECRET");
		String scopesString = System.getenv().get("GITHUB_API_SCOPES");
		String redirectUri = System.getenv().get("GITHUB_API_REDIRECT_URI");
		String code = System.getenv().get("GITHUB_API_AUTHORIZATION_CODE");

		if (code == null || clientId == null || clientSecret == null || scopesString == null)
			System.out.println(
					"* Consult with the Google API console to supply the parameters that will be required on the next lines.");

		Scanner s = new Scanner(System.in);

		if (clientId == null) {
			System.out.print("Client ID: ");
			clientId = formatString(s.nextLine());
		}
		if (redirectUri == null) {
			System.out.print("Redirect URI: ");
			redirectUri = formatString(s.nextLine());
		}
		if (scopesString == null) {
			System.out.print("API scopes (provide as single space delimited list): ");
			scopesString = formatString(s.nextLine());
		}

		if (code == null) {
			String authCodeRequest = MessageFormat.format(
					"http://github.com/login/oauth/authorize?client_id={0}&redirect_uri={1}&scope={2}", clientId,
					redirectUri, scopesString);

			System.out.println(
					"Copy this request to a browser, provide consent when asked and finally copy the value of \"code\" request parameter from the redirect url. The code will be required.");
			System.out.println();
			System.out.println(authCodeRequest);
			System.out.println();

			System.out.print("Authorization code: ");
			code = s.nextLine();
		}

		if (clientSecret == null) {
			System.out.print("Client Secret: ");
			clientSecret = formatString(s.nextLine());
		}

		Collection<String> scopes = null;
		if (scopesString != null)
			scopes = parseScopes(scopesString);

		/* Create a TokenProvider and get a token with it */
		/*
		 * Note: Github's response to token requests is non standard so we will
		 * augment a little our databinding provider to conform to this. See
		 * GithubDatabindingProvider
		 */
		String tokenServiceBaseUrl = "https://github.com/login/oauth/";
		TokenServiceHttpClient client = new HttpsURLConnectionClientAdapter.Builder().baseUrl(tokenServiceBaseUrl)
				.basicAuthentication(clientId, clientSecret).mapper(new GithubDatabindingProvider()).build();
		AuthorizationCodeGrantRequest grant = new AuthorizationCodeGrantRequest(code, clientId, clientSecret,
				redirectUri, scopes);
		TokenProvider provider = new SimpleTokenProvider(new OAuthTokenServiceDelegate<>(grant, client, "access_token"));

		System.out.println(provider.get());

		s.close();
	}

	static Collection<String> parseScopes(String scopesString) {
		String[] enumerationArr = scopesString.split("\\s+");
		Collection<String> list = Collections.unmodifiableCollection(Arrays.asList(enumerationArr));
		return list;
	}

	static String formatString(String val) {
		return val == null ? "" : val;
	}

}
