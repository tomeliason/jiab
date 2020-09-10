# jenkins_in_a_box - image derived from official image 

For GBU consumption, this image represents a Jenkins instance based upon the official Jenkins image.

Reference Implementation for the Jenkins In A Box pattern described here: https://gbuconfluence.us.oracle.com/display/CDSMS/IP-JIAB+-+Jenkins+in+a+Box 

Jenkins In A Box reference implementation benefits include:

* a containerized Jenkins that can be deployed quickly to orchestrate a set of servers
* defined method for configuring pre-defined (specific, approved versions) components, including:
  * Jenkins - define/retrieve a specific version
  * plugins - define/retrieve a specific set and specific versions
* defined method for seeding the Jenkins instance with a set of pre-defined pipelines, read from files 

## Why pre-defined/downloaded Jenkins, plugins and pipelines?

* the target environment may not have the necessary connectivity to retrieve plugins, Jenkins, or the pipepline code
* there may be pre-approval required (such as CSSAP) to execute/use this software in a cloud service environment, and tht pre-approval may include specific Jenkins, plugin versions and pipelines
* to achieve the intent of Jenkins-in-a-box, a completely self-contained, containerized orchestration tool
 
## Changes from the official Jenkins image include:

* based on Oracle linux base image
* fixups for yum dependencies
* added an init.groovy file to perform some initial user setup
* configured plugins to be pre-configured (configure using plugins.txt and retrieve using update-plugins.sh)
* configured Jenkins to be pre-configured (configure update-jenkins.sh and retrieve)
* configured the jobDSL plugin to seed a Jenkins pipeline, that injects a set of jobDSL defined pipelines

## Getting Started 

1. Prerequisites

   * Oracle Linux 
      * Oracle Linux, 7-slim image is used as the base image
   
   * Docker
      * Your build host and intended host needs to have Docker installed to build and run the Docker image

   * Pipelines 
      * Your pipeline code can be pre-configured and pre-installed in the Jenkins instance, this project will seed Jenkins pipelines in jobDSL format, when the groovy files are place in the 'jobDSL' directory

   * A directory for a Docker volume used by Jenkins
      * To persist the Jenkins configuration, use a Docker volume to retain the file system for jenkins_home over container and system restart

2. Clone this git repo to your local machine 

3. Configure the init.groovy file with your desired user/credential 

   * The default 'admin' credential is defined in the init.groovy file 

4. Configure and retrieve a set of plugins, using plugins.txt

   * each line, list a plugin and optionally its version, such as
    ```
    blueocean:2.3.1
    ```
   * optionally, execute update-plugins.sh to retrieve the specific plugins (note that the Makefile will execute this script as part of the build process)

5. Configure and retrieve a Jenkins war file, using update-jenkins.sh

   * update the JENKINS_VERSION variable, such as
    ```
    JENKINS_VERSION=2.236
    ```
   * optionally, execute update-jeknins.sh to retrieve the Jenkins war file (note that the Makefile will execute this script as part of the build process)

6. Configure and retrieve your pipelines as jobDSL

   * note that a prerequisite is defining, coding, and testing your pipelines and formatting them for consumption by jobDSL (groovy-defined wrapper for a pipeline)
   * place your jobDSL files in the jobDSL directory, either from a repo, server or filesystem
   * these jobDSL files will be loaded into the Jenkins container instance

7. Use the Makefile to build the Jenkins container image

   * note that you can execute the steps in the makefile manually if you choose
   * execute 'make build' to build the jiab:0.1 image
   * 'docker images' should display the newly-built jiab:0.1 image

8. Execute the jiab:0.1 docker image as a container

   ```
   docker run -d --name jiab-latest --user root -p 8080:8080 -p 50000:50000 -v /u01/docker_volumes/jiab:/var/jenkins_home jiab:0.1
   ```
   * When running the image: 
      * -d indicates run in daemon monde
      * -p maps port 8080 external to 8080 internal, and 50000 external to 50000 internal (for runners)
      * -v maps local directory to the /var/jenkins_home directory
      * presently, the container runs as root; with some additional work with users, volumes and permissions it should be able to run as jenkins user

   * Note that the Jenkins in the instance will restart after initial configuration - it make take a few minutes to initialize, configure and seed the pipeline jobs into Jenkins

9. Access the running Jenkins instance within the docker image 
   * http://localhost:8080/jenkins - use the credentials configured in init.groovy 
   * The Admin/Config job is the seed job, at Jenkins creation time, it loads pipelines supplied as jobDSL format
   * Example jobs include:
      * helloworld.groovy - a simple hello world pipeline
      * executeoncompute.groovy - a pipeline that can execute ssh on an OCI compute host
         * mtierhosts.txt 
         * mtier.crt


# jenkins_in_a_box 

Note that the official image (where this project was clone from) is located: https://github.com/jenkinsci/docker

