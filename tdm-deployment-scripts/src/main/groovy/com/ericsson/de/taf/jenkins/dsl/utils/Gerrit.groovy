package com.ericsson.de.taf.jenkins.dsl.utils

import javaposse.jobdsl.dsl.ContextHelper
import javaposse.jobdsl.dsl.helpers.triggers.GerritEventContext
import javaposse.jobdsl.dsl.helpers.triggers.TriggerContext

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

final class Gerrit {

    private Gerrit() {
    }

    static def patchsetCreated(TriggerContext triggers) {
        gerritTrigger(triggers) {
            (delegate as GerritEventContext).patchsetCreated()
        }
    }

    static def refUpdated(TriggerContext triggers) {
        gerritTrigger(triggers) {
            (delegate as GerritEventContext).refUpdated()
        }
    }

    private static def gerritTrigger(TriggerContext triggers, Closure eventsClosure) {
        triggers.with {
            gerrit {
                events {
                    ContextHelper.executeInContext eventsClosure, delegate
                }
                project GIT_PROJECT, GIT_BRANCH

                configure {
                    it << serverName(GERRIT_SERVER)
                }
            }
        }
    }
}
