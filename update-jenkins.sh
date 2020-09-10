#!/bin/bash 

# Download jenkins war file

set -o pipefail

# jenkins version 
JENKINS_VERSION=2.236

# jenkins.war checksum, download will be validated using it
JENKINS_SHA=5bb075b81a3929ceada4e960049e37df5f15a1e3cfc9dc24d749858e70b48919

# Can be used to customize where jenkins.war get downloaded from
JENKINS_URL=https://repo.jenkins-ci.org/public/org/jenkins-ci/main/jenkins-war/${JENKINS_VERSION}/jenkins-war-${JENKINS_VERSION}.war

curl -fsSL ${JENKINS_URL} -o jenkins/jenkins.war 
