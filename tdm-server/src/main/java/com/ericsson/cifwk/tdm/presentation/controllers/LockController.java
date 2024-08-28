package com.ericsson.cifwk.tdm.presentation.controllers;

import java.util.List;

import com.ericsson.cifwk.tdm.application.datasources.DataSourceService;
import com.ericsson.cifwk.tdm.application.locks.LockDataService;
import com.ericsson.cifwk.tdm.api.model.Lock;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: include API versioning
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 10/02/2016
 */

@RestController
@RequestMapping("/api/locks")
public class LockController {

    @Autowired
    LockDataService lockDataService;

    @Autowired
    DataSourceService dataSourceService;

    @RequestMapping(method = RequestMethod.POST)
    public Lock createDataRecordLock(@RequestBody Lock lock, @RequestHeader(value = "User-Agent",
            defaultValue = "Rest") String userAgent) {
        dataSourceService.addRequestToMetrics(lock.getDataSourceExecution().getDataSourceId(), userAgent);
        return lockDataService.createLock(lock);
    }

    /**
     *
     * @param lockId
     * @return
     * @deprecated
     */
    @Deprecated
    @RequestMapping(value = "/{lockId}/records", method = RequestMethod.GET)
    public List<DataRecordEntity> getDataRecordsForLock(@PathVariable("lockId") String lockId) {
        return dataSourceService.getRecordsForLock(lockId);
    }

    @RequestMapping(value = "/{lockId}", method = RequestMethod.DELETE)
    public void releaseLock(@PathVariable("lockId") String lockId) {
        lockDataService.releaseLock(lockId);
    }
}
