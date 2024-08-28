package com.ericsson.cifwk.tdm.client.services;

import com.ericsson.cifwk.tdm.api.model.DataSourceExecution;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
public interface DataSourceExecutionService {

    @POST("datasource-executions")
    Call<DataSourceExecution> createDataSourceExecutionRecord(@Body DataSourceExecution dataSource);

    @GET("datasource-executions/{id}")
    Call<DataSourceExecution> getDataSourceExecutionById(@Path("id") String dataSourceExecutionId);

    @GET("datasource-executions")
    Call<DataSourceExecution> findDataSourceExecutions(@Query("executionId") String executionId);

}
