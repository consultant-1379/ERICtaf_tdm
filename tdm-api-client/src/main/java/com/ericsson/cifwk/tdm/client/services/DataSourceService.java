package com.ericsson.cifwk.tdm.client.services;

import java.util.List;

import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;

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
public interface DataSourceService {

    @GET("datasources")
    Call<List<DataSourceIdentity>> getDataSourceIdentities();

    @GET("datasources/latest")
    Call<DataSourceIdentity> getLatestIdentityByContextAndName(@Query("context") String contextId,
                                                                     @Query("name") String name,
                                                                     @Query("approved") boolean approved);
    @POST("datasources")
    Call<DataSourceIdentity> createDataSource(@Body DataSource execution);

    @GET("datasources/labels/{label}/contexts/{contextId}")
    Call<DataSourceIdentity> getDataSourceByLabel(@Path("label") String label, @Path("contextId") String contextId);

    @GET("datasources/{datasourceId}")
    Call<DataSourceIdentity> getDataSourceById(@Path("datasourceId") String datasourceId,
                                               @Query("approved") boolean approved);

    @GET("datasources/{dataSourceId}/versions/{version}")
    Call<DataSourceIdentity> getDataSourceByIdAndVersion(@Path("dataSourceId") String dataSourceId,
                                                         @Path("version") String version);

    @GET("datasources/{datasourceId}/versions/{version}/records")
    Call<List<DataRecord>> getRecords(@Path("datasourceId") String datasourceId,
                                      @Path("version") String version,
                                      @Query("columns") String columns,
                                      @Query("predicates") List<String> filter);

}
