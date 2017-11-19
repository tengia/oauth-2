[![Build Status](https://travis-ci.org/tengia/oauth-2.svg?branch=master)](https://travis-ci.org/tengia/oauth-2)

# oauth2-client

An OAuth2 client implementation, that doesn't have strong oppinion about the technologies you should be using in your own project and kindly hides the boilerplate and resilience concerns around OAuth authentication so you can focus on your core business. 

## Why?

It comes with out-of-the-box standard Java implementations for all it needs to operate, but also features optional modules with the most popular, edge technologies you will most likely want to use or you are already using. And it's easily extendable for more!
when you de reducing the boilerplate code you would normally need to take care of, built with simplicity, resilience and transparency in mind. 

The first and very obvious reason is that along with the positives that libraries, such as  [Spring Security OAuth](http://static.springsource.org/spring-security/oauth/) for example bring, they also drag a number of dependencies that your project might not be happy with. Contrary to that, this client library is built exclusively with standard Java SE. In fact, the decision to add a dependency to OkHttpClient3 for example was solely for the convenience that it brings along and to target a technology base that so far was not widely addressed directly. So if you come from the lightweight clients angle that might be just the client for you.

Another benefit of choosing this client that stands out, is that it takes simplicity to a next level, encapsulating the whole machinery for fetching, refreshing, fault tolerance behind a simple <code>getToken</code> method, for all a server-side client application cares is to always have a valid token at hand. You are free to choose the best strategy to handle tokens for your use case using the built-in implementations or roll your own and integrate it in the framework.

Even better, using this client library is that behind its simplistic interface it can make full use of the OAuth2 infrastructure and automatically refresh tokens for you based on the <code>expire_in</code> property of the token payload. It can be configured for advanced fault-tolerance strategies and react in case of specific exceptions and retry in certain conditions. Employing this arsenal we can build resilient token management service, while keeping all it all transparent to the using code. And you get that out-of-the-box.
 

## Usage

This client is built around several key concepts - AccessTokenGrantRequest, TokenService, TokenProvider and AccessToken. 
A TokenService is responsible for fetching and refreshing AccessTokens as per the AccessTokenGrantRequest type it is setup for. 
A TokenProvider is responsible for delivering a valid AccessToken, making use of a TokenService.

The TokenServices implement two methods: <code>fetch</code> and <code>refresh</code>.

Get access token with client credentials grant flow 
<pre><code>
	
	String url = "https://myoauth2host/tokenendpoint";
	String userId = "my_user_id";
	String password = "my_password";
	String clientId = "my_client_id";
	String clientSecret = "my_client_secret";
	Collection<String> scopes = null;//default. add if any
	
	//1. initialize the grant request for your chosen flow
	ClientCredentialsGrantRequest grantRequest = new ClientCredentialsGrantRequest(clientId, clientSecret, scopes);
	//2. initialize a token service instance with that grant
	TokenService tokenService = new DefaultTokenService(url, grantRequest, userId, password); 
	//3. initialize token provider using the service 
	TokenProvider tokenProvider = new SimpleTokenProvider(tokenService);
	//4. start fetching tokens
	AccessToken token = tokenProvider.getToken();
	
</code></pre>

This piece of code will fetch a new token each time the getToken method is invoked on the tokenProvider instance.
To change that behavior and switch to reusing and auto-refreshing tokens, we need to change from using SimpeTokenProvider to TokenRefreshService:
      
<pre><code>	

	TokenRefreshService<AccessToken> tokenProvider = new TokenRefreshService<>(tokenService);
	tokenProvider.start();
	AccessToken token = tokenProvider.getToken();
	tokenProvider.stop();
	
</code></pre>	

Now, we've started a background job (hence the start method) that will fetch tokens, continuously monitor their expiry state and refresh them as required, minimizing networking to the bare minimum until the job is cancelled. The job can be fine tuned in its behavior in many aspects, including strategy on token fetch failure. The default strategy is no-retry, i.e. fail fast.

If we wanted to change from using Client Credentials grant we could use any other of the out-of-the-box provided classes extending AccessTokenGrantRequest corresponding to the OAuth2 standard flows, or implement our own if we are dealing with something custom.
The flows supported out of the box are client credentials, password, authorization code and refresh token (used internally for refreshing  tokens as intended by the protocol)

## Setup 

#### Setting up different grant types

##### Client Credentials
<pre><code>
	
	String clientId = "my_client_id";
	String clientSecret = "my_client_secret";
	Collection<String> scopes = null;//default. add if any

	ClientCredentialsGrantRequest grantRequest = new ClientCredentialsGrantRequest(clientId, clientSecret, scopes);
	
</code></pre>	

##### Authorization Code

<pre><code>
	
	String code = "123";
	String redirectUrl = "https://myurl.org"
	String clientId = "my_client_id";
	String clientSecret = "my_client_secret";
	Collection<String> scopes = null;//default. add if any

	AuthorizationCodeGrantRequest grantRequest = new AuthorizationCodeGrantRequest(code, clientId, clientSecret, redirectUrl, scopes);
	
</code></pre>	


##### Password Credentials

<pre><code>
	
	String username = "my_username";
	String password = "my_password"
	String clientId = "my_client_id";
	String clientSecret = "my_client_secret";
	Collection<String> scopes = null;//default. add if any

	PasswordCredentialsGrantRequest grantRequest = new PasswordCredentialsGrantRequest(username, password, clientId, clientSecret, scopes);
	
</code></pre>	

#### Setting up TokenRefreshService

##### schedule, refreshBeforeExpireThreshold
The token refresh service can operate in autonomous, reliable, self-tuning manner, relying on the OAuth infrastructure or in standard alarm-clock-like mode.

To use it in its default, **autonomous mode**, the OAuth tokens provided by the Token service need to feature the standard **expire\_in** (measured in seconds) property. The token service job wakes up at regular intervals to check if the expire period of the token it takes care of has passed and it needs to refresh it. The default wake up interval is 50 minutes. That is, the first check will be 50 minutes after the last refresh or fetch of a token. That leaves a 10 minutes window for the refresh operation to complete. Note that the behavior in this time window is function of the refreshBeforeExpireThreshold parameter and the token's expire\_in property, so these three need to be tuned to play together correctly. 
For example, if the refreshBeforeExpireThreshold is set to 15 minutes, and the expire_in property is 3600 seconds (1 hour), when the service wakes up to check if the token expired and decide if it should trigger refresh at the 50th minute from the moment it fetched the last token, it will be up to the refreshBeforeExpireThreshold to instruct it to go on and refresh or not. If it's set to 0, the service will see that there are still 15 minutes to go and fall asleep. In 15 minutes though, the tokens it will provide will be expired. However, if the refreshBeforeExpireThreshold is set to 15 minutes, the service will note that waking up at the 50th minute it's at the threshold of 15 minutes to expire of 1 hour and will trigger update immediately. That will make the refresh schedule moving, but reliable. Consider that it takes some time for networking or several attempts due to unreliable service. The 5 minutes difference are the resilience factor that will make these disruptions transparent to the using code.

To fallback to the more predictable, yet not that reliable refresh **alarm clock**-like schedule, you simply need to tune the wake up interval to match the expire_in property and the refreshBeforeExpireThreshold to 0. In that manner, the service will wake up at fixed intervals (e.g. each hour) with the possibility to deliver expired tokens during the period of refreshing the existing ones.

##### retry policy
Should the refresh attempt fail, the service can employ a retry policy to decide how to proceed. The default is NoRetryPolicy and it will grant the service one refresh attempt, after which it will exit and wait its next scheduled run. Exceptions will be logged but not considered. Another option is to use the MinimalRetryPolicy. It will make up to three sequential attempts to contact the token service for refreshing the token with a period between the attempts of 1 minute. You can create your own policies implementing RetryPolicy for fine tuning the behavior on failed refresh attempts. The easiest way to start is by using either of the supplied implementations as an example. You are to control three parameters - maximum number of retires, period between retries and behavior on exception occurrence. Concerning the latter, you will decide if it's ok to proceed retrying, or it makes no sense to go on with further attempts based on the exception provided as argument to the callback function you will implement. 

## Extensibility

OAuth has been designed with extensibility in mind, which is mostly addresses the tokens payload. For tokens with additional token properties you need to extend the AccessToken base class and make sure it is serializable to/from JSON by a mapping provider.
This library comes with two implementations out-of-the-box for the most popular object mappers for JSON and Java - Jackson and GSON. Should you need another, use them a prototype for building it.
For token services with more specific requirements, implement a TokenServiceHttpClient or leverage one of the existing (for standard java, okhttp3 or Apache httpcomponents). 

## Customization
A primary concern in designing this library has been its customization capabilities. Unlike most of the implementations out there that encapsulate their key components for good, this one provides both for using as-is or for plugging into your existing project and reusing the components that you already use. It has been designed not to carry additional dependencies but make it easy to reuse your http client, authentication methods or object mapper.

The standard has some built-in extensibility but it is quite often the case that real life OAuth2 server provider implementations go beyond that and deviate from the standard significantly. In fact, one of the most significant uses of OAuth2 is social login, i.e. authentication, something which the protocol specification explicitly states it is not intended for.

To adapt the client to specific services, even OAuth 1.0 or of a different technology type, such as custom token services, you need to implement a custom TokenService. Adhering to the TokenService interface allows it to be used with the supplied TokenProvider implementations or your own.

Further, while authentication requirements are specified in the standard, the precise mechanisms are left out of scope and off-the-band. This is precisely why the concrete HTTP client used to communicate with the token service is exposed and can be supplied externally, potentially configured for the specific authentication protocol required by this token service. On the other hand, there is a default client configured to handle basic authentication, which is a pretty common scenario so you can always skip this part if it suits you.
