package com.ericsson.de.taf.jenkins.dsl.builders

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

import static com.ericsson.de.taf.jenkins.dsl.utils.Maven.MAVEN_VERSION

class SonarQubeGerritJobBuilder extends GerritJobBuilder {

    private static final String DESCRIPTION_SUFFIX = 'as a part of Gerrit verification process'

    SonarQubeGerritJobBuilder(String name) {
        super(name, "Sonarqube ${DESCRIPTION_SUFFIX}", "clean org.jacoco:jacoco-maven-plugin:0.7.4" +
                ".201502262128:prepare-agent install -Pserver,acceptance -DskipTests")
    }

    @Override
    Job build(DslFactory factory) {
        super.build(factory).with {
            publishers {
                sonar {
                    installationName("SonarQube")
                    additionalProperties("-Dsonar.java.binaries=target/classes")
                    mavenInstallation MAVEN_VERSION
                }
            }
        }
    }
}
