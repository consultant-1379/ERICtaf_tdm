/*
Full API documentation:
https://jenkinsci.github.io/job-dsl-plugin/

Job DSL playground:
http://job-dsl.herokuapp.com/
*/

import static com.ericsson.de.taf.jenkins.dsl.Constants.*

import com.ericsson.de.taf.jenkins.dsl.builders.*
import javaposse.jobdsl.dsl.DslFactory

def mvnUnitTest = "clean install -Pserver -T 1C"
def mvnValidateCheckstyle ="clean validate -Pserver,checkstyle -DskipTests -fae -T 1C"
def mvnITest = "clean test -Pitest,server -Dspring.profiles.active=itest -T 1C"
def mvnClient = 'clean install -Pclient -T 1C'
def mvnFunctionalTests = 'clean package -Pacceptance -Dtaf.profiles=test -Dmaven.test.failure.ignore=false -Dsuitethreadpoolsize=1'
def mvnBackwardCompatibilityTests = "clean package -Pacceptance -Dtaf.profiles=test -Dsuites=tdm_prod_suite.xml -Dmaven.test.failure.ignore=false -Dsuitethreadpoolsize=1"
def mvnFunctionalTestsProduction = "clean package -Pacceptance -Dtaf.profiles=prod -Dsuites=tdm_prod_suite.xml -Dmaven.test.failure.ignore=false -Dsuitethreadpoolsize=1 -Dtdm.api.host=https://\$$DEPLOYMENT_PARAMETER_KEY/api/"
def mvnPushDocker = "-pl tdm-server docker:build -DpushImage"
def mvnBuildServer = "clean install -Pserver -DskipTests"
def mvnCleanInstall = "clean install -DskipTests"
def mvnChangeLog = 'com.ericsson.cifwk.taf:tafchangelog-maven-plugin:1.0.14:generate -X -e'

def releaseDescription = "Build flow for release"
def deployDescription = "Build flow for deploying to production"

def unitTests = 'Unit tests'
def iTests = 'Integration tests'
def checkStyles = 'validate Checkstyles'
def buildClient = 'Build Client'
def healthCheck = 'Run health Check on Test Env'
def rollback = 'Roll back the production environment to the previous version because of the health check failure'
def deployTest = 'Deploy to test Env'
def deployProd = 'Deploy to prod Env'
def deployStage = 'Deploy to stage Env'
def functionalTests = 'Run Functional Tests on test Env'
def backwardCompatibilityTests = 'Run the production functional tests using the currently live version of ' +
        'tdm-datasource against the latest version of TDM'
def functionalTestsProd = 'Run Functional Tests on production Env repeating every 10 mins'
def deployDockerSnapshots ='Deploy TDM app docker snapshot image to Artifactory'
def deployDockerRelease ='Deploy TDM app docker release image to Artifactory'
def changeLogDescription = 'Creates the changelog html page and uploads to taflanding'

//Gerrit flow
def aa = new GerritJobBuilder('AA-gerrit-unit-tests', unitTests, mvnUnitTest)
def ab = new GerritJobBuilder('AB-gerrit-integration-tests', iTests, mvnITest)
def ac = new GerritJobBuilder('AC-gerrit-CheckStyles', checkStyles, mvnValidateCheckstyle)
def ad = new ClientGerritJobBuilder('AD-gerrit-ClientUI', buildClient, mvnClient)
def ae = new SonarQubeGerritJobBuilder('AE-gerrit-sonar-qube')

//Build flow
def ba = new SimpleJobBuilder('BA-unit-tests', unitTests, mvnUnitTest)
def bb = new SimpleJobBuilder('BB-integration-tests', iTests, mvnITest)
def bc = new ClientJobBuilder('BC-ClientUI', buildClient, mvnClient)
def ca = new DeployDockerImages('CA-Deploy-Docker-images-snapshot', deployDockerSnapshots, mvnPushDocker, mvnBuildServer)
def da = new DeployToTestEnvBuilder('DA-Deploy-To-Test-Env', deployTest)
def db = new HealthCheckJobBuilder('DB-Test-Env-HealthCheck', healthCheck, 'health_check.sh')
def dc = new FunctionalTestsTestEnvJobBuilder('DC-Functional-Tests', functionalTests, mvnFunctionalTests)
def dd = new BackwardCompatibilityTestsJobBuilder('DD-Backward-Compatibility-Test', backwardCompatibilityTests, mvnBackwardCompatibilityTests)

def build = new MasterBuildFlowBuilder('B-build-flow', 'TDM-.*-flow',
        """\
        parallel(
            { build '${ba.name}' },
            { build '${bb.name}' },
            { build '${bc.name}' },
        )
        build '${ca.name}'
        build '${da.name}'
        build '${db.name}'
        build '${dc.name}'
        build '${dd.name}'
        """.stripIndent(), null)

//Release flow
def release = new ReleaseJobBuilder('EA-release')
def changeLog = new ChangeLogBuilder('EB-Changelog', changeLogDescription, mvnChangeLog)
def deployDocker = new DeployReleasedDockerImages('EC-Deploy-Docker-images', deployDockerRelease, mvnPushDocker,
        mvnCleanInstall)
def deployToProduction = new DeployToProdEnvBuilder('ED-Deploy-To-Production', deployProd)
def prodHealthCheck = new HealthCheckWithRollBackTriggerJobBuilder('EE-Prod-HealthCheck', healthCheck,'health_check_prod.sh')
def prodFuncTest = new FunctionalTestsProdWithRollbackJobBuilder("EF-Prod-Functional-Tests", functionalTestsProd, mvnFunctionalTestsProduction)
def rollbackJobBuilder = new RollbackProdEnvJobBuilder('EG-Prod-Rollback', rollback)
//Other
def deployToStage = new DeployToStageEnvBuilder('HA-Deploy-To-Stage', deployStage)

def deployToProdBuildFlowDSL = """
        build( '${deployToProduction.name}', $DEPLOYMENT_PARAMETER_KEY: params['$DEPLOYMENT_PARAMETER_KEY'])
        build( '${prodHealthCheck.name}', $DEPLOYMENT_PARAMETER_KEY: params['$DEPLOYMENT_PARAMETER_KEY'])
        build( '${prodFuncTest.name}', $DEPLOYMENT_PARAMETER_KEY: params['$DEPLOYMENT_PARAMETER_KEY'])
        """.stripIndent()

def deployToStageBuildFlowDSL = """
        build( '${deployToStage.name}', $DEPLOYMENT_PARAMETER_KEY: params['$DEPLOYMENT_PARAMETER_KEY'])
        build( '${prodHealthCheck.name}', $DEPLOYMENT_PARAMETER_KEY: params['$DEPLOYMENT_PARAMETER_KEY'])
        build( '${prodFuncTest.name}', $DEPLOYMENT_PARAMETER_KEY: params['$DEPLOYMENT_PARAMETER_KEY'])
        """.stripIndent()

def buildReleaseFlow = new BuildFlowBuilder('E-release-flow', releaseDescription,
        """\
        build '${release.name}'
        build '${changeLog.name}'
        build '${deployDocker.name}'
        $deployToStageBuildFlowDSL
        """.stripIndent(), 'TDM-.*-flow', STAGE_ENVIRONMENTS)


def deployToProdFlow = new BuildFlowBuilder('G-deploy-Prod-flow', deployDescription,
        deployToProdBuildFlowDSL, 'TDM-.*-flow', PROD_ENVIRONMENTS)

def deployToStageFlow = new BuildFlowBuilder('H-deploy-Stage-flow', deployDescription,
        deployToStageBuildFlowDSL, 'TDM-.*-flow', STAGE_ENVIRONMENTS)

def fa = new HealthCheckJobBuilder('FA-Prod-HealthCheck', healthCheck, 'health_check_prod.sh')
def fb = new FunctionalTestsProdJobBuilder('FB-Functional-Tests-Prod', functionalTestsProd,
        mvnFunctionalTestsProduction)

String healthCheckBuildFlow = "parallel ("
PROD_HC_ENVIRONMENTS.each {
        healthCheckBuildFlow += """{\
                build( "${fa.name}", $DEPLOYMENT_PARAMETER_KEY: "$it")
                build( "${fb.name}", $DEPLOYMENT_PARAMETER_KEY: "$it")
                },""".stripIndent()
}
int count = 0
STAGE_HC_ENVIRONMENTS.each {
        count++
        healthCheckBuildFlow+="""{\
                build( "${fa.name}", $DEPLOYMENT_PARAMETER_KEY: "$it")
                build( "${fb.name}", $DEPLOYMENT_PARAMETER_KEY: "$it")
                }""".stripIndent()
        if(count < STAGE_ENVIRONMENTS.size()){
                healthCheckBuildFlow+=","
        }}
healthCheckBuildFlow+=")"
def buildF = new HealthCheckBuildFlowBuilder('F-build-HealthCheck-Prod-flow', healthCheckBuildFlow)


[aa, ab, ac, ad, ae, ba, bb, bc, ca, da, db, dc, dd, build, release, changeLog, deployDocker, deployToProduction,
 prodHealthCheck,
 rollbackJobBuilder, buildReleaseFlow, fa, fb, buildF, deployToProdFlow, prodFuncTest, deployToStage,
 deployToStageFlow]*.build(this as DslFactory)

