package com.ericsson.de.taf.jenkins.dsl.builders

import com.ericsson.de.taf.jenkins.dsl.utils.Git
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

abstract class FunctionalTestsJobBuilder extends FreeStyleJobBuilder {

    FunctionalTestsJobBuilder(String name,
                              String description) {
        super(name, description)
    }

    @Override
    Job build(DslFactory factory) {
        def job = super.build(factory)
        job.with {
            scm {
                Git.simple delegate
            }
            label(SLAVE_DOCKER_POD_H)
            jdk(JDK_1_8_DOCKER)
            publishers {
                archiveTestNG('**/test-output/testng-results.xml')
                mailer('Hyderabad.EricssonTAF@tcs.com', true, false)

            }
            wrappers {
                timeout {
                    elastic(150, 3, 5)
                }
            }
            publishers {
                allureReportPublisher {
                    config {
                        jdk('')
                        commandline('')
                        resultsPattern('target/allure-results')
                        properties {
                            propertyConfig {
                                key('allure.issues.tracker.pattern')
                                value('http://taftm.lmera.ericsson.se/#tm/viewTC/%s')
                            }
                            propertyConfig {
                                key('allure.issues.tracker.pattern')
                                value('http://jira-oss.lmera.ericsson.se/browse/%s')
                            }
                        }
                        reportBuildPolicy('ALWAYS')
                        includeProperties(true)
                    }
                }
            }
        }
        return job
    }
}
