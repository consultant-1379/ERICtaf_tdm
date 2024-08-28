package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.BuildFlowJob

import static com.ericsson.de.taf.jenkins.dsl.Constants.DEPLOYMENT_PARAMETER_KEY
import static com.ericsson.de.taf.jenkins.dsl.Constants.PROD_ENVIRONMENTS

class HealthCheckBuildFlowBuilder extends AbstractJobBuilder {

    static final String DESCRIPTION = "Build flow Health Check for prodcution Env"
    final String buildFlowText

    HealthCheckBuildFlowBuilder(String name , String buildFlowText) {
        super(name, DESCRIPTION)

        this.buildFlowText = buildFlowText
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            (delegate as BuildFlowJob).buildFlow buildFlowText

            parameters {
                choiceParam(DEPLOYMENT_PARAMETER_KEY, PROD_ENVIRONMENTS)
            }
            triggers {
                scm("H/10 * * * *")
            }
            publishers {
                buildDescription('',"\$$DEPLOYMENT_PARAMETER_KEY")
            }
        }
    }

    @Override
    Job create(DslFactory factory) {
        factory.buildFlowJob name
    }
}
