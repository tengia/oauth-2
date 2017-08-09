package net.oauth2.clients.github;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.oauth2.AccessToken;
import net.oauth2.AuthorizationCodeGrantRequest;
import net.oauth2.client.AutoRenewingTokenProvider;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.OAuthTokenServiceDelegate;
import net.oauth2.client.http.TokenServiceHttpClient;
import net.oauth2.client.http.javase.HttpsURLConnectionClientAdapter;

public class GithubTokensJobClient {

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
		final AutoRenewingTokenProvider<AccessToken> provider = new AutoRenewingTokenProvider<>(new OAuthTokenServiceDelegate<>(grant, client, "access_token"));
		AtomicInteger i = new AtomicInteger(0);
		provider.schedule(0.1).attach((newToken, oldToken)->{
			System.out.println(newToken);
			i.incrementAndGet();
			if(i.intValue()>3)
				provider.stop(false);
		}).start();
		try {
			TimeUnit.SECONDS.sleep(60);
		} catch (InterruptedException e) {}
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
