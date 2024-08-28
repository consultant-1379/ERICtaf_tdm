package com.ericsson.cifwk.tdm.client.services;

import java.util.List;

import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.Lock;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
public interface LockService {

    @POST("locks")
    Call<Lock> createDataRecordLock(@Body Lock lock);

    /**
     *
     * @param lockId
     * @return
     * @deprecated
     */
    @Deprecated
    @GET("locks/{lockId}/records")
    Call<List<DataRecord>> getDataRecordsForLock(@Path("lockId") String lockId);

    @DELETE("locks/{lockId}")
    Call<Void> releaseLock(@Path("lockId") String lockId);

}
