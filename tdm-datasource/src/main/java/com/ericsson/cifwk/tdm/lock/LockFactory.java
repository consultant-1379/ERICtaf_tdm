package com.ericsson.cifwk.tdm.lock;

import com.ericsson.cifwk.tdm.api.model.DataSourceExecution;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.Execution;
import com.ericsson.cifwk.tdm.api.model.Lock;
import com.ericsson.cifwk.tdm.client.TDMClient;
import com.ericsson.cifwk.tdm.datasource.DataSourceLockedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.IOException;
import java.util.UUID;

import static com.ericsson.cifwk.tdm.lock.LockProperties.TDM_LOCK_RETRY_INTERVAL_SEC;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         27/09/2017
 */
public class LockFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactory.class);

    private static final int STATUS_LOCKED = 423;
    private static final int BAD_REQUEST = 400;

    private static final String TDM_READ_ERROR = "Failed to read from Test Data Management server";

    private TDMClient tdmClient;
    private int waitTimeout;

    public LockFactory(TDMClient tdmClient) {
        this.tdmClient = tdmClient;
    }

    public Lock create(LockProperties configuration, LockAttributes lockAttributes) {
        Lock lock = new Lock();
        configuration.configure(lock);
        lock.setJobId(JobResolver.getJobId());
        lock.setType(lockAttributes.getLockType());

        DataSourceExecution execution = createExecution(lockAttributes.getDataSourceId(),
                lockAttributes.getTestwarePackage());
        execution.setLimit(lockAttributes.getLimit());
        execution.setPredicates(lockAttributes.getFilter());
        execution.setColumns(lockAttributes.getColumns());
        execution.setVersion(lockAttributes.getVersion());
        lock.setDataSourceExecution(execution);

        waitTimeout = configuration.getWaitTimeout();
        if (waitTimeout > 0) {
            lock = createLockWithRetry(lock, waitTimeout);
        } else {
            lock = createLock(lock);
        }
        return lock;
    }

    private Lock createLock(Lock lock) {
        long waitUntil = System.currentTimeMillis() + waitTimeout * 1000;
        while (true) {
            try {
                Response<Lock> response = tdmClient.getLockService().createDataRecordLock(lock).execute();
                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    String error = response.errorBody().string();
                    if (response.code() == STATUS_LOCKED) {
                        throw new DataSourceLockedException(error);
                    } else if (response.code() == BAD_REQUEST) {
                        throw new DataSourceLockedException(error);
                    } else {
                        throw new RuntimeException(error);
                    }
                }
            } catch (IOException e) {
                if (System.currentTimeMillis() > waitUntil) {
                    throw  new RuntimeException(TDM_READ_ERROR, e);
                }
                LOGGER.info("Unable to fetch Lock status of Data Source [{}], retrying in {} seconds",
                        lock.getDataSourceExecution().getDataSourceId(), TDM_LOCK_RETRY_INTERVAL_SEC);
                sleep();
            }
        }
    }


    private Lock createLockWithRetry(Lock lock, int waitTimeoutSeconds) {
        long waitUntil = System.currentTimeMillis() + waitTimeoutSeconds * 1000;
        while (true) {
            try {
                return createLock(lock);
            } catch (DataSourceLockedException lockedException) {
                if (System.currentTimeMillis() > waitUntil) {
                    throw lockedException;
                }
                LOGGER.info("Data Source [{}] is locked by other test, retrying in {} seconds",
                        lock.getDataSourceExecution().getDataSourceId(), TDM_LOCK_RETRY_INTERVAL_SEC);
                sleep();
            }
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(TDM_LOCK_RETRY_INTERVAL_SEC * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting on lock", e);
        }
    }

    private DataSourceExecution createExecution(DataSourceIdentity dataSourceId, String testwarePackage) {
        DataSourceExecution execution = new DataSourceExecution();
        execution.setDataSourceId(dataSourceId.getId());
        execution.setVersion(dataSourceId.getVersion());
        execution.setExecutionId(UUID.randomUUID().toString());
        execution.setTestwarePackage(testwarePackage);
        Response<Execution> response;
        try {
            response = tdmClient.getExecutionService().startExecution(new Execution()).execute();
        } catch (IOException e) {
            throw new RuntimeException(TDM_READ_ERROR, e);
        }
        if (response.isSuccessful()) {
            execution.setExecutionId(response.body().getId());
        } else {
            throw new RuntimeException("Failed to log test execution in Test Data Management system: " +
                    response.message());
        }
        return execution;
    }
}
