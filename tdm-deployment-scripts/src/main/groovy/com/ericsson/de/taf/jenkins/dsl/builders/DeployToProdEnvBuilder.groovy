package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class DeployToProdEnvBuilder extends DeployJobBuilder {

    DeployToProdEnvBuilder(String name,
                           String description) {
        super(name, description)
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            parameters {
                choiceParam(DEPLOYMENT_PARAMETER_KEY, PROD_ENVIRONMENTS)
            }
            steps {
                shell("chmod +x tdm-deployment-scripts/jenkins/docker/production/deploy.sh")
                shell("chmod +x tdm-deployment-scripts/jenkins/docker/production/run.sh")
                shell("""
                        version=\$(git describe --abbrev=0);
                        tdm-deployment-scripts/jenkins/docker/production/deploy.sh \$version \$$DEPLOYMENT_PARAMETER_KEY""")
            }
        }
    }
}
