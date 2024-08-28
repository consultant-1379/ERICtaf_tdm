package com.ericsson.cifwk.tdm.application.executions;

import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.Execution;
import org.springframework.stereotype.Repository;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 17/02/2016
 */
@Repository
public class ExecutionRepository extends BaseRepository<Execution> {

    public static final String EXECUTIONS_COLLECTION = "executions";

    public ExecutionRepository() {
        super(EXECUTIONS_COLLECTION, Execution.class);
    }
}
