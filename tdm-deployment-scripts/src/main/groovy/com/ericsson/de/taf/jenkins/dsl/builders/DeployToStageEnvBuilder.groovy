package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.DEPLOYMENT_PARAMETER_KEY
import static com.ericsson.de.taf.jenkins.dsl.Constants.STAGE_ENVIRONMENTS

class DeployToStageEnvBuilder extends DeployJobBuilder {

    DeployToStageEnvBuilder(String name,
                            String description) {
        super(name, description)
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            parameters {
                choiceParam(DEPLOYMENT_PARAMETER_KEY, STAGE_ENVIRONMENTS)
            }
            steps {
                shell("chmod +x tdm-deployment-scripts/jenkins/docker/stage/deploy.sh")
                shell("chmod +x tdm-deployment-scripts/jenkins/docker/stage/run.sh")
                shell("""
                        version=\$(git describe --abbrev=0);
                        tdm-deployment-scripts/jenkins/docker/stage/deploy.sh \$version \$$DEPLOYMENT_PARAMETER_KEY""")
            }

        }
    }
}
