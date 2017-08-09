package net.oauth2.clients.twitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

import net.oauth2.ClientCredentialsGrantRequest;
import net.oauth2.client.OAuthTokenServiceDelegate;
import net.oauth2.client.OAuth2ProtocolException;
import net.oauth2.client.SimpleTokenProvider;
import net.oauth2.client.TokenProvider;
import net.oauth2.client.http.TokenServiceHttpClient;
import net.oauth2.client.http.databinding.SimpleDatabindingProvider;
import net.oauth2.client.http.javase.HttpsURLConnectionClientAdapter;

/**
 * <P>
 * The sample shows how to obtain a valid token for using Google APIs.
 * </P>
 * </p>
 * <B>Prerequisites.</B><BR>
 * You will need a valid OAuth 2.0 client registered in Google API console at
 * https://console.developers.google.com.<div>More specifically, you will need
 * 'client id', 'client secret', 'redirect uri' exactly as specified in the
 * console section 'Credentials' (download as json if you are missing the
 * redirect url).</div><div>Additionally, you will need also API scopes for the
 * APIs enabled in the console that you intend to use with the token you will be
 * requesting.</div>
 * </p>
 * <p>
 * <B>Using the sample.</B><BR>
 * To trigger a token request the sample needs a couple of parameters from your
 * client and apis that you will use with the token (specified above).<div>You
 * can supply them as environment variables and run the java program or you can
 * supply them interactively in the command line. Mixing the two options is also
 * possible, i.e. supply part of the parameters as environment variables and
 * another part interactively.</div><div>The names of the environment variables
 * that you can use to supply the parameters are:
 * <ul>
 * <li><B>GOOGLE_API_CLIENT_ID</B>: The client ID obtained from the API Console.</li>
 * <li><B>GOOGLE_API_CLIENT_SECRET</B>: The client Secret obtained from the API Console</li>
 * <li><B>GOOGLE_API_SCOPES</B>: A valid scope for the api that you will call</li>
 * </ul>
 * </div> <div>Note, that if you do not provide authorization code via
 * environment variable, the sample will prompt you for any missing parameters
 * required to form a valid request for the code and generate it for you. Use
 * it to obtain and supply the code.</div>
 * </p>
 * <p>
 * <b>Reference</b><br>
 * https://dev.twitter.com/oauth/application-only
 * </p>
 */
public class TwitterClient {

	public static void main(String[] args) throws OAuth2ProtocolException, IOException {
		String clientId = System.getenv().get("TWITTER_API_CLIENT_ID");
		String clientSecret = System.getenv().get("TWITTER_API_CLIENT_SECRET");
		String scopesString = System.getenv().get("TWITTER_API_SCOPES");

		if (clientId == null || clientSecret == null || scopesString == null)
			System.out.println(
					"* Consult with the Twitter API console to supply the parameters that will be required on the next lines.");

		Scanner s = new Scanner(System.in);

		if (clientId == null) {
			System.out.print("Client ID: ");
			clientId = formatString(s.nextLine());
		}
		if (scopesString == null) {
			System.out.print("API scopes (provide as single space delimited list): ");
			scopesString = formatString(s.nextLine());
		}
		if (clientSecret == null) {
			System.out.print("Client Secret: ");
			clientSecret = formatString(s.nextLine());
		}

		Collection<String> scopes = null;
		if (scopesString != null)
			scopes = parseScopes(scopesString);

		/*Create a TokenProvider and get a token with it.*/
		/*Experiment with different clients (apache, okhttp3), or object mappers (Gson, Jackson)*/
		String tokenServiceBaseUrl = "https://api.twitter.com/oauth2/";
		TokenServiceHttpClient client = new HttpsURLConnectionClientAdapter.Builder().baseUrl(tokenServiceBaseUrl)
				.basicAuthentication(clientId, clientSecret).mapper(new SimpleDatabindingProvider()).build();
		ClientCredentialsGrantRequest grant = new ClientCredentialsGrantRequest(clientId, clientSecret, scopes);
		TokenProvider provider = new SimpleTokenProvider(new OAuthTokenServiceDelegate<>(grant, client));

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
