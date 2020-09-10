#!groovy

/*
 * This script is designated for the init.groovy.d 
 * directory to be executed at startup time of the 
 * Jenkins instance. This script requires the jobDSL
 * Plugin. Tested with job-dsl:1.70
 */

import hudson.model.*
import jenkins.model.*
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import jenkins.install.InstallState
import jenkins.model.Jenkins
import hudson.model.Hudson
// import com.cloudbees.plugins.credentials.domains.Domain
// import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
// import com.cloudbees.plugins.credentials.CredentialsScope
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm


// Add deploy key for the centrally shared pipeline and configuration repository
// def domain = Domain.global()
// def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
// def keyFileContents = new File("/var/jenkins_home/jenkins-ssh-keys/deploy-key-shared-library").text
// def privateKey = new BasicSSHUserPrivateKey(
//   CredentialsScope.GLOBAL,
//   "deploy-key-shared-library",
//   "root",
//   new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(keyFileContents),
//   "",
//   "SSH key for shared-library"
// )
// store.addCredentials(domain, privateKey)

def instance = Jenkins.getInstance()

println("Wizard Check")

// Disable Wizards
println(Jenkins.instance.getSecurityRealm().getClass().getSimpleName())

if(Jenkins.instance.getSecurityRealm().getClass().getSimpleName() == 'HudsonPrivateSecurityRealm') {
    

    def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
    strategy.setAllowAnonymousRead(false)
    instance.setAuthorizationStrategy(strategy)   

    if (!instance.installState.isSetupComplete()) {
        println '--> Neutering SetupWizard'
        println instance.getInstallState() 
        InstallState is = new InstallState("INITIAL_SETUP_COMPLETED", true)
        is.initializeState()
        instance.setInstallState(is)
        println instance.getInstallState()
    }

    instance.save() 

    // configure an intial admin user with password 

    def setupUser = "admin"
    // new File("/var/jenkins_home/jenkins-basic-auth-credentials/default-setup-user").text.trim()
    def setupPass = "welcome1"
    // new File("/var/jenkins_home/jenkins-basic-auth-credentials/default-setup-password").text.trim()

    def hudsonRealm = new HudsonPrivateSecurityRealm(false)
    instance.setSecurityRealm(hudsonRealm)
    def user = instance.getSecurityRealm().createAccount(setupUser, setupPass)
    user.save()

    // configure jobDSL security, otherwise scripts loaded from files won't run
    def job_dsl_security = instance.getExtensionList('javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration')[0]
    if(job_dsl_security.useScriptSecurity) {
        job_dsl_security.useScriptSecurity = false
        println 'Job DSL script security has changed.  It is now disabled.'
        job_dsl_security.save()
    }
    else {
        println 'Nothing changed.  Job DSL script security already disabled.'
    }

    instance.save()
}

println("Configure jobDSL")

// Create the configuration pipeline from a jobDSL script
def jobDslScript = new File('/var/jenkins_home/dsl/ConfigurationAndSeedingPipelineDSL.groovy')
def workspace = new File('.')
def jobManagement = new JenkinsJobManagement(System.out, [:], workspace)
new DslScriptLoader(jobManagement).runScript(jobDslScript.text)

// Execute the initial configuration jobs
println("Execute initial configuraiton jobs")
Jenkins.instance.getAllItems(Job.class).each {
    println(it.fullName)
    it.scheduleBuild(1)
}

sleep(30000)
instance.doSafeRestart(null)