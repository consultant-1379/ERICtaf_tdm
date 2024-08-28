package com.ericsson.cifwk.tdm.application.executions;

import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.DataSourceExecution;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DataSourceExecutionRepository extends BaseRepository<DataSourceExecution> {

    public static final String DATA_SOURCE_EXECUTIONS = "dataSourceExecutions";

    public DataSourceExecutionRepository() {
        super(DATA_SOURCE_EXECUTIONS, DataSourceExecution.class);
    }

    public List<DataSourceExecution> findByExecutionId(String executionId) {
        return find("{executionId:#}", executionId);
    }
}
