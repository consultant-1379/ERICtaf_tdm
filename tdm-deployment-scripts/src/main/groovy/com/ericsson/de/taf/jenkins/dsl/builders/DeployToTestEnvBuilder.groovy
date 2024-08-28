package com.ericsson.de.taf.jenkins.dsl.builders

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class DeployToTestEnvBuilder extends FreeStyleJobBuilder {

    DeployToTestEnvBuilder(String name,
                           String description) {
        super(name, description)
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
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
            jdk(JDK_SYSTEM)
            label(SLAVE_DOCKER_POD_H)
            wrappers {
                maskPasswordsBuildWrapper {
                    varPasswordPairs {
                        varPasswordPair {
                            var('HOST_PASSWORD')
                            password('shroot')
                        }
                    }
                }
            }
            steps {
                shell("chmod +x tdm-deployment-scripts/jenkins/docker/test/deploy.sh")
                shell("chmod +x tdm-deployment-scripts/jenkins/docker/test/run.sh")
                shell("tdm-deployment-scripts/jenkins/docker/test/deploy.sh")
            }
        }
    }
}
