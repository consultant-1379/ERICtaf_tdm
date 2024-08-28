package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.DEPLOYMENT_PARAMETER_KEY
import static com.ericsson.de.taf.jenkins.dsl.Constants.JDK_SYSTEM
import static com.ericsson.de.taf.jenkins.dsl.Constants.SLAVE_TAF_MAIN
import static com.ericsson.de.taf.jenkins.dsl.Constants.GERRIT_CENTRAL
import static com.ericsson.de.taf.jenkins.dsl.Constants.GERRIT_MIRROR
import static com.ericsson.de.taf.jenkins.dsl.Constants.GIT_BRANCH
import static com.ericsson.de.taf.jenkins.dsl.Constants.GIT_PROJECT

class HealthCheckJobBuilder extends FreeStyleJobBuilder {

    final String script;

    HealthCheckJobBuilder(String name,
                          String description,
                          String script
                          ) {
        super(name, description)

        this.script = script;
    }


    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        job.with {
            concurrentBuild()
            scm {
                git {
                    remote {
                        name 'gm'
                        url "${GERRIT_MIRROR}/${GIT_PROJECT}"
                    }
                    remote {
                        name 'gc'
                        url "${GERRIT_CENTRAL}/${GIT_PROJECT}"
                    }
                    branch GIT_BRANCH
                    extensions {
                        cleanAfterCheckout()
                        disableRemotePoll()
                    }
                }
            }
            label(SLAVE_TAF_MAIN)
            jdk(JDK_SYSTEM)
            steps {
                shell("chmod +x tdm-deployment-scripts/jenkins/shell/${script}")
                shell("tdm-deployment-scripts/jenkins/shell/${script} \$$DEPLOYMENT_PARAMETER_KEY")
            }

            publishers {
                mailer('Hyderabad.EricssonTAF@tcs.com', true, false)
                buildDescription('',"\$$DEPLOYMENT_PARAMETER_KEY")
            }
        }
        return job
    }
}
