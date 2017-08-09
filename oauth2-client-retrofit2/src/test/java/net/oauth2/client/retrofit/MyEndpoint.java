package net.oauth2.client.retrofit;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface MyEndpoint{
	@POST
	@FormUrlEncoded
	Call<MyToken> getAccessToken(@Url String endpointUrl, @FieldMap Map<String, Object> formFields);
}