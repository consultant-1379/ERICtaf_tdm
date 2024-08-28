package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class ClientGerritJobBuilder extends GerritJobBuilder {

    final String mavenGoal

    ClientGerritJobBuilder(String name,
                           String description,
                           String mavenGoal) {
        super(name, description, mavenGoal)
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)

        job.with {

            label(SLAVE_DOCKER_POD_H)
            jdk(JDK_SYSTEM)
        }
        return job
    }
}
