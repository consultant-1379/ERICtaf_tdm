package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import com.ericsson.de.taf.jenkins.dsl.utils.Maven

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class ChangeLogBuilder extends FreeStyleJobBuilder {
    final String mavenGoal
    ChangeLogBuilder(String name,
                    String description,
                     String mavenGoal ) {
        super(name, description)

        this.mavenGoal = mavenGoal
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            scm {
                git {
                    remote {
                        name 'gc'
                        url "${GERRIT_CENTRAL}/${GIT_PROJECT}"
                    }
                    branch GIT_BRANCH
                    extensions {
                        cleanBeforeCheckout()
                    }
                }
            }
            jdk(JDK_SYSTEM)
            label(SLAVE_TAF_MAIN)
            steps{
                Maven.goal delegate, mavenGoal
                shell('cd ${PWD}/target\n' +
                        'targetDir=/proj/PDU_OSS_CI_TAF/taflanding/tdmrelease\n' +
                        'rm -f ${targetDir}/changelog.html\n' +
                        'cp changelog.html ${targetDir}/changelog.html')
            }
        }
    }
}