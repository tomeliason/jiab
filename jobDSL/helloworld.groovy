pipelineJob("helloworld") {
	description()
	keepDependencies(false)
	definition {
		cps { script(
"""pipeline {
   agent any

   stages {
      stage('Hello') {
         steps {
            echo 'Hello World'
         }
      }
   }
}"""	)
    sandbox()	}
	}
	disabled(false)
}