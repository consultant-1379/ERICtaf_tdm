package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class HealthCheckWithRollBackTriggerJobBuilder extends HealthCheckJobBuilder {

    HealthCheckWithRollBackTriggerJobBuilder(String name,
                                             String description,
                                             String script
                          ) {
        super(name, description, script)
    }


    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        job.with {
            publishers {
                downstreamParameterized {
                    trigger('TDM-EG-Prod-Rollback'){
                        condition('UNSTABLE_OR_WORSE')
                        parameters {
                            currentBuild()
                        }
                    }
                }
            }
        }
        return job
    }
}
