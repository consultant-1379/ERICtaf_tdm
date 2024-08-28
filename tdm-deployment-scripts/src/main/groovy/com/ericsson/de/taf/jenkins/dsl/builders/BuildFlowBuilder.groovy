package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.BuildFlowJob

import static com.ericsson.de.taf.jenkins.dsl.Constants.DEPLOYMENT_PARAMETER_KEY

class BuildFlowBuilder extends AbstractJobBuilder {

    final String buildFlowText
    final String buildFlowJobName
    final List<String> choiceParameters

    BuildFlowBuilder(String name, String description,
                     String buildFlowText, String buildFlowJobName, List<String> choiceParameters) {
        super(name, description)
        this.buildFlowText = buildFlowText
        this.buildFlowJobName = buildFlowJobName
        this.choiceParameters = choiceParameters
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        job.with {
            (delegate as BuildFlowJob).buildFlow buildFlowText

            blockOn(buildFlowJobName) {
                blockLevel 'GLOBAL'
            }

            publishers {
                buildDescription('',"\$$DEPLOYMENT_PARAMETER_KEY")
            }
        }

        if (this.choiceParameters != null) {
            job.with {
                parameters {
                    choiceParam(DEPLOYMENT_PARAMETER_KEY, this.choiceParameters)
                }
            }
        }
        return job
    }

    @Override
    Job create(DslFactory factory) {
        factory.buildFlowJob name
    }
}
