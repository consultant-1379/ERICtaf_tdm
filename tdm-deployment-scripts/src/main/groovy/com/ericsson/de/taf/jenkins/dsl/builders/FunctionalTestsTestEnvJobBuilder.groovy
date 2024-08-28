package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class FunctionalTestsTestEnvJobBuilder extends FunctionalTestsJobBuilder {

    final String mavenGoal

    FunctionalTestsTestEnvJobBuilder(String name,
                                     String description,
                                     String mavenGoal) {
        super(name, description)

        this.mavenGoal = mavenGoal
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        job.with {
            steps {
                Maven.goal delegate, mavenGoal
            }
        }
        return job
    }
}

