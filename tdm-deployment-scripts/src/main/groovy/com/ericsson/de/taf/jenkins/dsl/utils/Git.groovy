package com.ericsson.de.taf.jenkins.dsl.utils

import com.ericsson.de.taf.jenkins.dsl.builders.GitBuilder
import javaposse.jobdsl.dsl.helpers.ScmContext

import static com.ericsson.de.taf.jenkins.dsl.Constants.GERRIT_BRANCH
import static com.ericsson.de.taf.jenkins.dsl.Constants.GERRIT_REFSPEC

final class Git {

    private Git() {
    }

    static def simple(ScmContext scm) {
        new GitBuilder().build(scm)
    }

    static def gerrit(ScmContext scm) {
        new GitBuilder(
                remoteRefspec: GERRIT_REFSPEC,
                gitBranch: GERRIT_BRANCH,
                addGerritTrigger: true
        ).build(scm)
    }
}
