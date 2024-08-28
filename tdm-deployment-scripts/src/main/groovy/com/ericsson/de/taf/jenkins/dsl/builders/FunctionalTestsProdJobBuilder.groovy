package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.DEPLOYMENT_PARAMETER_KEY
import static com.ericsson.de.taf.jenkins.dsl.Constants.JDK_1_8
import static com.ericsson.de.taf.jenkins.dsl.Constants.PROD_ENVIRONMENTS
import static com.ericsson.de.taf.jenkins.dsl.Constants.SLAVE_TAF_MAIN

class FunctionalTestsProdJobBuilder extends FunctionalTestsJobBuilder {

    final String mavenGoal

    FunctionalTestsProdJobBuilder(String name,
                                  String description,
                                  String mavenGoal) {
        super(name, description)

        this.mavenGoal = mavenGoal
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        job.with {
            concurrentBuild()
            steps {
                shell("chmod +x tdm-deployment-scripts/jenkins/shell/checkout_version.sh")
                shell("tdm-deployment-scripts/jenkins/shell/checkout_version.sh \$$DEPLOYMENT_PARAMETER_KEY")
                Maven.goal delegate, mavenGoal
            }
            parameters {
                choiceParam(DEPLOYMENT_PARAMETER_KEY, PROD_ENVIRONMENTS)
            }
            label(SLAVE_TAF_MAIN)
            jdk(JDK_1_8)
            publishers {
                buildDescription('',"\$$DEPLOYMENT_PARAMETER_KEY")
            }
        }
        return job
    }
}

