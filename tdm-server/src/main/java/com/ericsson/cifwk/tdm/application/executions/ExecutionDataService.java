package com.ericsson.cifwk.tdm.application.executions;

import com.ericsson.cifwk.tdm.model.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 17/02/2016
 */
@Service
public class ExecutionDataService {

    @Autowired
    ExecutionRepository executionRepository;

    public Execution createNew(Execution execution) {
        execution.start();
        executionRepository.insert(execution);
        return execution;
    }

    public List<Execution> findAll() {
        return executionRepository.findAll();
    }

    public Optional<Execution> finishExecution(String executionId) {
        Optional<Execution> maybeExecution = findById(executionId);
        if (maybeExecution.isPresent()) {
            Execution execution = maybeExecution.get();
            execution.finish();
            executionRepository.update(execution);
            return Optional.of(execution);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Execution> findById(String executionId) {
        Execution byId = executionRepository.findById(executionId);
        if (byId != null) {
            return Optional.of(byId);
        }
        return Optional.empty();
    }

}
