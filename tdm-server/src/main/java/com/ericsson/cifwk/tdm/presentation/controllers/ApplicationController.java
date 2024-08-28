package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.ApplicationInfo;
import com.ericsson.cifwk.tdm.api.model.IntegrationsInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {

    @Value("${info.build.version}")
    private String version;

    @Value("${info.build.name}")
    private String name;

    @Value("${info.build.artifact}")
    private String artifactId;

    @Value("${remote.tce.url.app}")
    private String tceAppUrl;

    @GetMapping
    public ApplicationInfo getLatestVersionOfSchedule() {
        return new ApplicationInfo(version, name, artifactId);
    }

    @GetMapping("/integrations")
    public IntegrationsInfo getIntegrations() {
        return new IntegrationsInfo(tceAppUrl);
    }

    @GetMapping("/version")
    public String getVersion() {
        return version;
    }
}
