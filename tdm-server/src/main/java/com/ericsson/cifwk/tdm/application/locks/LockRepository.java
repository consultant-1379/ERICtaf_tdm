package com.ericsson.cifwk.tdm.application.locks;

import com.ericsson.cifwk.tdm.api.model.LockType;
import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.Lock;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 17/02/2016
 */
@Repository
public class LockRepository extends BaseRepository<Lock> {

    public static final String LOCKS_COLLECTION = "locks";

    public LockRepository() {
        super(LOCKS_COLLECTION, Lock.class);
    }

    @Override
    public void insert(Lock lock) {
        lock.setCreateTime(new Date());
        super.insert(lock);
    }

    public void delete(Lock lock) {
        if (lock.getId() != null) {
            removeById(lock.getId());
        }
    }

    public List<Lock> findByDataSourceId(String dataSourceId, String jobId) {
        return find("{'dataSourceExecution.dataSourceId':#, 'jobId':# }", dataSourceId, jobId);
    }

    public List<Lock> findByDataSourceIdAndTestwarePackage(String dataSourceId, String testwarePackage) {
        return find("{'dataSourceExecution.dataSourceId':#, 'dataSourceExecution.testwarePackage'}",
                dataSourceId, testwarePackage);
    }

    public Lock findOneByDataSourceIdAndLockType(String dataSourceId, LockType lockType) {
        return findOne("{'dataSourceExecution.dataSourceId': #, type: #}", dataSourceId, lockType);
    }
}
