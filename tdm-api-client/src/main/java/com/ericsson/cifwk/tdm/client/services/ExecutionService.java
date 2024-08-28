package com.ericsson.cifwk.tdm.client.services;

import com.ericsson.cifwk.tdm.api.model.Execution;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
public interface ExecutionService {

    @POST("executions")
    Call<Execution> startExecution(@Body Execution execution);

    @PATCH("executions/{executionId}")
    Call<Execution> finishExecution(@Path("executionId") String executionId);

    @GET("executions/{executionId}")
    Call<Execution> getExecutionById(@Path("executionId") String executionId);
}
