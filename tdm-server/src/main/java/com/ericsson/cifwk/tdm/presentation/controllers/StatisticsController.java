package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.StatisticsObject;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceService;
import com.ericsson.cifwk.tdm.application.user.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private DataSourceService dataSourceService;

    @GetMapping("/users")
    public ResponseEntity getUsers() {
        List<StatisticsObject> loggedOnUsers = userSessionService.getLoggedOnUsers();
        return new ResponseEntity<>(loggedOnUsers, HttpStatus.OK);
    }

    @GetMapping("/dataSources")
    public ResponseEntity getDataSources(@RequestParam(value = "type",
            required = false,
            defaultValue = "Browser") String type) {
        List<StatisticsObject> dataSources = dataSourceService.getDataSourceRequests(type);
        return new ResponseEntity<>(dataSources, HttpStatus.OK);
    }
}
