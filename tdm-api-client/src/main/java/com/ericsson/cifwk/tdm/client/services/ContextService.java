package com.ericsson.cifwk.tdm.client.services;

import com.ericsson.cifwk.tdm.api.model.Context;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ContextService {
    @GET("contexts")
    Call<Context> getContextByName(@Query("name") String name);

    @GET("contexts")
    Call<Context> getContextByPath(@Query("path") String path);
}
