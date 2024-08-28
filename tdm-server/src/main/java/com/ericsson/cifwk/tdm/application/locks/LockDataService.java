package com.ericsson.cifwk.tdm.application.locks;

import com.ericsson.cifwk.tdm.api.model.Lock;
import com.ericsson.cifwk.tdm.api.model.LockType;
import com.ericsson.cifwk.tdm.application.executions.DataSourceExecutionService;
import com.ericsson.cifwk.tdm.infrastructure.mapping.MapperFacadeProvider;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceExecution;
import com.ericsson.cifwk.tdm.presentation.exceptions.LockException;
import com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException.verifyFound;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 18/02/2016
 */
@Service
public class LockDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockDataService.class);

    @Autowired
    private DataSourceExecutionService dataSourceExecutionService;

    @Autowired
    private LockRepository lockRepository;

    @Autowired
    private HazelcastInstance cacheManager;

    @Autowired
    private MapperFacadeProvider mapperFacadeProvider;

    public Lock createLock(Lock lock) {

        String jobId = lock.getJobId();
        String dataSourceId = lock.getDataSourceExecution().getDataSourceId();
        checkNotNull(jobId, "Job ID should not be null");
        checkNotNull(dataSourceId, "Data source ID should not be null");

        IMap<String, Object> sync = cacheManager.getMap("dataSourceLocks");
        sync.lock(jobId); // acquire lock safely in a distributed environment
        try {
            List<String> existingLock = checkExistingLock(dataSourceId, lock.getType(),
                    jobId);
            MapperFacade mapperFacade = mapperFacadeProvider.mapperFacade();
            com.ericsson.cifwk.tdm.model.Lock realLock =
                    mapperFacade.map(lock, com.ericsson.cifwk.tdm.model.Lock.class);

            doLock(realLock, existingLock);
            return mapperFacade.map(realLock, Lock.class);
        } finally {
            sync.unlock(jobId);
        }
    }

    private void doLock(com.ericsson.cifwk.tdm.model.Lock lock, List<String> existingLock) {
        DataSourceExecution executionRecord = null;
        try {
            executionRecord = dataSourceExecutionService.createExecutionRecord(lock.getDataSourceExecution(),
                    existingLock);
            int limit = executionRecord.getLimit();
            int numberOfRecords = executionRecord.getRecords().size();
            validateRecords(limit, numberOfRecords);

            lockRepository.insert(lock);
        } catch (LockException e) {
            throw new LockException(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Failed to create lock", e);
            if (executionRecord != null) {
                dataSourceExecutionService.delete(executionRecord);
            }
            lockRepository.delete(lock);

            throw new LockException("Failed to create lock", e);
        }
    }

    private static void validateRecords(int limit, int numberOfRecords) {
        if (numberOfRecords == 0 || (limit != -1 && numberOfRecords != limit)) {
            throw new LockException("Could not obtain enough records. Found " + numberOfRecords + " records of "
                    + limit);
        }
    }

    private List<String> checkExistingLock(String dataSourceId, LockType lockType, String jobId) {
        List<com.ericsson.cifwk.tdm.model.Lock> existingLocks = lockRepository.findByDataSourceId(dataSourceId, jobId);

        List<String> exclusiveLocks = existingLocks
                .stream()
                .filter(existingLock -> LockType.EXCLUSIVE.equals(existingLock.getType()))
                .flatMap(locks -> locks.getDataSourceExecution().getRecords().stream())
                .map(DataRecordEntity::getId)
                .collect(Collectors.toList());

        if (LockType.EXCLUSIVE.equals(lockType)) {
            List<String> sharedLocks = existingLocks
                    .stream()
                    .filter(existingLock -> LockType.SHARED.equals(existingLock.getType()))
                    .flatMap(locks -> locks.getDataSourceExecution().getRecords().stream())
                    .map(DataRecordEntity::getId)
                    .collect(Collectors.toList());

            exclusiveLocks.addAll(sharedLocks);
        }

        return exclusiveLocks;
    }

    public void releaseLock(String lockId) {
        com.ericsson.cifwk.tdm.model.Lock lock = verifyFound(lockRepository.findById(lockId));
        lockRepository.delete(lock);
    }

    public void expireLocks() {
        List<com.ericsson.cifwk.tdm.model.Lock> locks = lockRepository.findAll();
        locks.forEach(lock -> expireLock(lock));
    }

    private void expireLock(com.ericsson.cifwk.tdm.model.Lock lock) {
        long expirationTime = lock.getCreateTime().getTime() + lock.getTimeoutSeconds() * 1000;
        if (expirationTime < System.currentTimeMillis()) {
            try {
                releaseLock(lock.getId());
                LOGGER.info("Lock {} at execution {} timed out after {} seconds", lock.getId(),
                        lock.getDataSourceExecution().getExecutionId(), lock.getTimeoutSeconds());
            } catch (NotFoundException e) {
                // lock already released
            }
        }
    }
}
