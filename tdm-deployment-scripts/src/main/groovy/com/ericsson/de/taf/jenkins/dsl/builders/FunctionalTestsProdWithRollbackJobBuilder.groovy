package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class FunctionalTestsProdWithRollbackJobBuilder extends FunctionalTestsProdJobBuilder {

    FunctionalTestsProdWithRollbackJobBuilder(String name,
                                              String description,
                                              String mavenGoal) {
        super(name, description, mavenGoal)
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        job.with {
            publishers {
                downstreamParameterized {
                    trigger('TDM-EF-Prod-Rollback'){
                        condition('UNSTABLE_OR_WORSE')
                        parameters {
                            currentBuild()
                        }
                    }
                }
            }
        }
    }
}

