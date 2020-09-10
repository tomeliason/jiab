pipelineJob("executeoncompute") {
	description()
	keepDependencies(false)
	definition {
		cps { script(
"""
def remote = [:]
remote.name = 'test'
remote.host = 'test'
remote.user = 'oracle'
remote.password = 'password'
remote.allowAnyHosts = true

def props

pipeline {
   agent any

   stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
                echo 'list workspace'
                sh 'ls -al'
            }
        }
        stage('copy files') {
            steps {
                sh 'cp /usr/share/jenkins/ref/parameters/* . ' 
            }
        }
         stage('connect server') {
            steps {
                script {
                    def r = [:]
                    props = readJSON file: 'mtierhosts.txt'
                    for(element in props) {
                        println element
                        r.allowAnyHosts = true
                        r.name = element.name
                        r.host = element.address 
                        r.user = element.user
                        r.identityFile = env.WORKSPACE + "/" + element.identityFile
                        sshCommand remote: r, command: "ls -lrt"
                        sshCommand remote: r, command: 'for i in {1..5}; do echo -n "Loop $i "; date ; sleep 1; done'
                    }
                }
               sshCommand remote: remote, command: "ls -lrt"
               sshCommand remote: remote, command: 'for i in {1..5}; do echo -n \"Loop \$i \"; date ; sleep 1; done'
            }
         }
      }
   }
"""	)
    sandbox()	}
	}
	disabled(false)
}