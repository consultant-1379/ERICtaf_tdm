package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.helpers.ScmContext

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

class GitBuilder {

    String remoteUrl = GIT_URL
    String remoteName = GIT_REMOTE
    String remoteRefspec = ''
    String gitBranch = GIT_BRANCH
    boolean addGerritTrigger = false

    def build(ScmContext scm) {
        scm.with {
            git {
                remote {
                    url remoteUrl
                    name remoteName
                    refspec remoteRefspec
                }
                branch gitBranch
                extensions {
                    cleanBeforeCheckout()
                    if (addGerritTrigger) {
                        choosingStrategy {
                            gerritTrigger()
                        }
                    }
                }
            }
        }
    }
}
