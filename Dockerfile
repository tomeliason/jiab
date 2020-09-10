FROM oraclelinux:7-slim

LABEL maintainer="tom.eliason@oracle.com" 

# updated yum command from apt, install required packages for jenkins
RUN yum update -y && yum install -y git vi vim curl dpkg java java-devel unzip which && yum install -y oracle-epel-release-el7 oracle-release-el7 && yum install -y git-lfs && yum clean all
ENV JAVA_HOME /etc/alternatives/jre_openjdk

ARG user=jenkins
ARG group=jenkins
ARG uid=1000
ARG gid=1000
ARG http_port=8080
ARG agent_port=50000
ARG JENKINS_HOME=/var/jenkins_home
ARG REF=/usr/share/jenkins/ref

ENV JENKINS_HOME $JENKINS_HOME
ENV JENKINS_SLAVE_AGENT_PORT ${agent_port}
ENV REF $REF

# Jenkins is run with user `jenkins`, uid = 1000
# If you bind mount a volume from the host or a data container,
# ensure you use the same uid
RUN mkdir -p $JENKINS_HOME \
  && chown ${uid}:${gid} $JENKINS_HOME \
  && groupadd -g ${gid} ${group} \
  && useradd -d "$JENKINS_HOME" -u ${uid} -g ${gid} -m -s /bin/bash ${user}

# Jenkins home directory is a volume, so configuration and build history
# can be persisted and survive image upgrades
VOLUME $JENKINS_HOME

# $REF (defaults to `/usr/share/jenkins/ref/`) contains all reference configuration we want
# to set on a fresh new installation. Use it to bundle additional plugins
# or config file with your custom jenkins Docker image.
RUN mkdir -p ${REF}/init.groovy.d
COPY init.groovy ${REF}/init.groovy.d/

RUN chown -R ${user} "$JENKINS_HOME" "$REF"

# for main web interface:
EXPOSE ${http_port}

# will be used by attached slave agents:
EXPOSE ${agent_port}

ENV COPY_REFERENCE_FILE_LOG $JENKINS_HOME/copy_reference_file.log

USER ${user}

COPY jenkins/jenkins.war /usr/share/jenkins/jenkins.war 
COPY jenkins-support /usr/local/bin/jenkins-support

# inject the seed DSL groovy script - this one creates the Admin/Configure pipeline, which loads from jobDSL
COPY dsl /usr/share/jenkins/ref/dsl

# inject the set of pipelines for this jenkins instance to run, from jobDSL
COPY jobDSL /usr/share/jenkins/ref/jobDSL

# inject the jenkins startup script
COPY jenkins.sh /usr/local/bin/jenkins.sh

# copy pre-approved plugins directory
COPY plugins /usr/share/jenkins/ref/plugins 

# copy parameters to run jobs, such as host definitions, login methods
COPY parameters /usr/share/jenkins/ref/parameters

RUN echo -n "2.236" > "$JENKINS_HOME"/jenkins.install.InstallUtil.lastExecVersion

ENTRYPOINT ["/usr/local/bin/jenkins.sh"]
