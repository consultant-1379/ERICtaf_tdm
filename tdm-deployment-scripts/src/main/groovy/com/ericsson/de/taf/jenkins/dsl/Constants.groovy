package com.ericsson.de.taf.jenkins.dsl

class Constants {

    static final String PROJECT_NAME = 'TDM'

    static final String JOBS_PREFIX = "${PROJECT_NAME}"
    static final String GIT_PROJECT = 'OSS/com.ericsson.cifwk.taf.testdatamanagement/ERICtaf_tdm'

    static final String JOBS_MODULE = 'tdm-deployment-scripts'
    static final String JOBS_DIRECTORY = 'jenkins'
    static final String JOBS_PATH = "${JOBS_MODULE}/${JOBS_DIRECTORY}"

    static final String SLAVE_TAF_MAIN = 'taf_main_slave'
    static final String SLAVE_DOCKER_POD_H = 'FEM119_POD_H_docker_build_slave'

    static final String JDK_1_8 = 'JDK 1.8.0_25'
    static final String JDK_1_8_DOCKER = 'JDK 1.8 Docker Slave'
    static final String JDK_SYSTEM = '(System)'
    static final String GERRIT_SERVER = 'gerrit.ericsson.se'
    static final String GERRIT_CENTRAL = '${GERRIT_CENTRAL}' // resolves to 'ssh://gerrit.ericsson.se:29418'
    static final String GERRIT_MIRROR = '${GERRIT_MIRROR}' // resolves to 'ssh://gerritmirror.lmera.ericsson.se:29418'
    static final String GERRIT_BRANCH = '${GERRIT_BRANCH}'

    static final String GERRIT_REFSPEC = '${GERRIT_REFSPEC}'
    static final String GIT_URL = "${GERRIT_CENTRAL}/${GIT_PROJECT}"
    static final String GIT_REMOTE = 'origin'
    static final String GIT_BRANCH = 'master'

    static final List<String> PROD_ENVIRONMENTS = ['seliius20759.seli.gic.ericsson.se', 'seliius20777.seli.gic.ericsson.se']
    static final List<String> PROD_HC_ENVIRONMENTS = ['taf-tdm-prod.seli.wh.rnd.internal.ericsson.com', 'taf-tdmpassive.seli.wh.rnd.internal.ericsson.com']

    static final List<String> STAGE_ENVIRONMENTS = ['seliius20775.seli.gic.ericsson.se']
    static final List<String> STAGE_HC_ENVIRONMENTS = ['taf-tdmstage.seli.wh.rnd.internal.ericsson.com']

    public static final String DEPLOYMENT_PARAMETER_KEY = "deployment"
}
