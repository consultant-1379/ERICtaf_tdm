package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class RollbackProdEnvJobBuilder extends DeployJobBuilder {

    RollbackProdEnvJobBuilder(String name,
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
                        commit=\$(git rev-list --tags --skip=1 --max-count=1); 
                        version=\$(git describe --abbrev=0 --tags \$commit);
                        tdm-deployment-scripts/jenkins/docker/production/deploy.sh \$version \$$DEPLOYMENT_PARAMETER_KEY""")
            }
            publishers {
                downstreamParameterized {
                    trigger('TDM-ED-Prod-HealthCheck, TDM-EE-Prod-Functional-Tests'){
                        condition('ALWAYS')
                        parameters {
                            currentBuild()
                        }
                    }
                }
            }
        }
    }
}
