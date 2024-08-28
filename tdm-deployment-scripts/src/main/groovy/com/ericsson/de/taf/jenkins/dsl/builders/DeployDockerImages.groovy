package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import static com.ericsson.de.taf.jenkins.dsl.Constants.*

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class DeployDockerImages extends FreeStyleJobBuilder {

    final String mavenGoal

    final String mvnBuild

    DeployDockerImages(String name,
                       String description,
                       String mavenGoal,
                       String mvnBuildGoal) {
        super(name, description)

        this.mavenGoal = mavenGoal
        this.mvnBuild = mvnBuildGoal
    }

    String readFile(String path, def dslFactory) {
        return dslFactory.readFileFromWorkspace('tdm-deployment-scripts/jenkins/shell/' +"${path}")
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            scm {
                git {
                    remote {
                        name GIT_REMOTE
                        url "${GERRIT_MIRROR}/${GIT_PROJECT}"
                    }
                    branch GIT_BRANCH
                    extensions {
                        cleanAfterCheckout()
                    }
                }
            }
            jdk(JDK_SYSTEM)
            label(SLAVE_DOCKER_POD_H)

            steps {
                copyArtifacts('TDM-BC-ClientUI') {
                    includePatterns('tdm-ui-client/dist/**/*')
                    targetDirectory('copied')
                    buildSelector {
                        upstreamBuild {
                            allowUpstreamDependencies()
                            fallbackToLastSuccessful()
                        }
                    }
                }

                shell(readFile('copy_frontend_artifacts.sh', factory))

                Maven.goal delegate, mvnBuild

                shell('docker pull frolvlad/alpine-oraclejdk8:slim')

                Maven.goal delegate, mavenGoal
            }

        }
    }
}
