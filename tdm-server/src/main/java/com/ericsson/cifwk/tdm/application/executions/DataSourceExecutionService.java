package com.ericsson.cifwk.tdm.application.executions;

import com.ericsson.cifwk.tdm.application.datasources.DataSourceService;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceExecution;
import com.ericsson.cifwk.tdm.model.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;

@Service
public class DataSourceExecutionService {

    private static final String ERROR_NOT_EXIST = "Datasource with id %s doesn't exist";

    @Autowired
    private DataSourceExecutionRepository dataSourceExecutionRepository;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private ExecutionRepository executionRepository;

    public DataSourceExecution createExecutionRecord(DataSourceExecution execution,
            List<String> lockedRecords) {

        Execution entity = executionRepository.findById(execution.getExecutionId());
        checkArgument(entity != null, ERROR_NOT_EXIST, execution.getDataSourceId());

        List<DataRecordEntity> records = dataSourceService.findDataRecordEntitiesByVersion(
                execution.getDataSourceId(), execution.getVersion(), execution.getPredicates(), execution.getColumns());

        int limit = execution.getLimit();

        getRecords(execution, lockedRecords, records, limit);

        dataSourceExecutionRepository.insert(execution);

        return execution;
    }

    private static void getRecords(final DataSourceExecution execution, final List<String> lockedRecords,
            final List<DataRecordEntity> records, final int limit) {
        if (lockedRecords.isEmpty()) {
            if (limit == -1) {
                records.forEach(r -> execution.getRecords().add(r));
            } else {
                records.stream().limit(limit).forEach(r -> execution.getRecords().add(r));
            }
        } else {
            getNonLockedRecords(execution, lockedRecords, records, limit);
        }
    }

    private static void getNonLockedRecords(final DataSourceExecution execution,
            final List<String> lockedRecords, final List<DataRecordEntity> records, final int limit) {
        int recordLimit = limit;
        if (recordLimit == -1) {
            records.forEach(r -> {
                if (!lockedRecords.contains(r.getId())) {
                    execution.getRecords().add(r);
                }
            });
        } else {
            for (DataRecordEntity recordEntity: records) {
                if (!lockedRecords.contains(recordEntity.getId()) && recordLimit != 0) {
                    execution.getRecords().add(recordEntity);
                    recordLimit--;
                }
            }

        }
    }

    public void delete(DataSourceExecution executionRecord) {
        dataSourceExecutionRepository.removeById(executionRecord.getId());
    }

    public Optional<DataSourceExecution> findById(String dataSourceExecutionId) {
        return ofNullable(dataSourceExecutionRepository.findById(dataSourceExecutionId));
    }

    public List<DataSourceExecution> findByExecutionId(String executionId) {
        return dataSourceExecutionRepository.findByExecutionId(executionId);
    }
}
