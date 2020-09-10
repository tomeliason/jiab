#!groovy

folder('Admin') {
    description('Folder containing configuration and seed jobs')
}

pipelineJob("Admin/Configure") {
	description()
	keepDependencies(false)
	definition {
		cps { script (
"""node('master') {
  stage('Checkout') {
    // Clean workspace and checkout shared library repository on the jenkins master
    cleanWs()
    // checkout scm
  }

  stage('Configuration') {
    // remove the init script 

    sh('rm /var/jenkins_home/init.groovy.d/*.groovy')

    // set config file in master
    // sh('cp /var/jenkins_home/workspace/Admin/Configure/resources/config/configuration-as-code-plugin/jenkins.yaml /var/jenkins_home/jenkins.yaml')

    // run configuration from config file
    // load('resources/config/groovy/triggerConfigurationAsCodePlugin.groovy')

    // set public key for bootstrapping user
    // load('resources/config/groovy/userPublicKeys.groovy')

    // set the timezone
    // load('resources/config/groovy/timezone.groovy')
  }

  stage('Deploy Agent Networks') {
    //ansiColor('xterm') {
    //  sh('ln -sfn /var/jenkins_home/agent-bootstrapping-terraform-config/aws-agent-network.backend.config resources/terraform/aws/agent-network/')
    //  sh('ln -sfn /var/jenkins_home/agent-bootstrapping-terraform-config/aws-agent-network.tfvars resources/terraform/aws/agent-network/terraform.tfvars')
    //  sh('cd resources/terraform/ && make deploy-agent-network')
    //}
  }

  stage('Job Seeding') {
      sh(' pwd ')
      sh(' ls -al')
      sh(' ls -al /var/jenkins_home/dsl')
      sh(' ls -al /var/jenkins_home/jobDSL')
      sh(' ls -al ../../../jobDSL/*.groovy')
      sh(' cp /var/jenkins_home/jobDSL/*.groovy .')
    jobDsl(targets: '*.groovy', sandbox: false)
  }
}""" ) 
      sandbox()		}
	}
	disabled(false)
	configure {
		it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
			strategy {
				'daysToKeep'('-1')
				'numToKeep'('20')
				'artifactDaysToKeep'('-1')
				'artifactNumToKeep'('-1')
			}
		}
	}
}
