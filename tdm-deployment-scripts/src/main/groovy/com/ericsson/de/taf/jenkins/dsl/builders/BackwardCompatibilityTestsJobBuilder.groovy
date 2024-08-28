package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class BackwardCompatibilityTestsJobBuilder extends FunctionalTestsJobBuilder {

    final String mavenGoal

    BackwardCompatibilityTestsJobBuilder(String name,
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
                shell("chmod +x tdm-deployment-scripts/jenkins/shell/checkout_version.sh")
                shell("tdm-deployment-scripts/jenkins/shell/checkout_version.sh taf-tdm-prod.seli.wh.rnd.internal.ericsson.com")
                Maven.goal delegate, mavenGoal
            }
        }
        return job
    }
}

