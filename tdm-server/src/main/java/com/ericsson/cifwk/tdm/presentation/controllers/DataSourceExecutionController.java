package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.model.DataSourceExecution;
import com.ericsson.cifwk.tdm.application.executions.DataSourceExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 16/02/2016
 */
@RestController
@RequestMapping("/api/datasource-executions")
public class DataSourceExecutionController {

    @Autowired
    DataSourceExecutionService dataSourceExecutionService;

    @RequestMapping(method = RequestMethod.POST)
    public DataSourceExecution createDataSourceExecutionRecord(@RequestBody DataSourceExecution dataSourceExecution) {
        return dataSourceExecutionService.createExecutionRecord(dataSourceExecution, new ArrayList<>());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<DataSourceExecution> getDataSourceExecutionById(@PathVariable("id") String id) {
        Optional<DataSourceExecution> maybeExecution = dataSourceExecutionService.findById(id);
        if (maybeExecution.isPresent()) {
            return new ResponseEntity<>(maybeExecution.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<DataSourceExecution> findDataSourceExecutions(@RequestParam("executionId") String executionId) {
        return dataSourceExecutionService.findByExecutionId(executionId);
    }
}
