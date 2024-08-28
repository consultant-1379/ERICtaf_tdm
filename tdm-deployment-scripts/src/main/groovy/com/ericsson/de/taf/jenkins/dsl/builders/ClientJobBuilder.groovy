package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Git
import com.ericsson.de.taf.jenkins.dsl.utils.Maven
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class ClientJobBuilder extends FreeStyleJobBuilder {

    final String mavenGoal

    ClientJobBuilder(String name,
                     String description,
                     String mavenGoal) {
        super(name, description)

        this.mavenGoal = mavenGoal
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)

        job.with {

            scm {
                Git.simple delegate
            }

            label(SLAVE_DOCKER_POD_H)
            jdk(JDK_SYSTEM)

            steps {
                Maven.goal delegate, mavenGoal
            }

            publishers {
                archiveArtifacts('tdm-ui-client/dist/**/*')
            }
        }
        return job
    }
}
