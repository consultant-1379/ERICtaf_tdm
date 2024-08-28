package com.ericsson.cifwk.tdm.client.services;

import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
public interface LoginService {

    @GET("login")
    Call<AuthenticationStatus> status();

    @POST("login")
    Call<AuthenticationStatus> login(@Body UserCredentials credentials);

    @DELETE("login")
    Call<AuthenticationStatus> logout();
}
