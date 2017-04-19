pipeline {

	agent any

  options {
    buildDiscarder(logRotator(numToKeepStr:'10')) // Keep the 10 most recent builds
  }

	stages {

		stage('Build') {
      steps {
        sh 'mvn clean package site'
				junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
      }
		}

		stage('Quality Analysis') {
			environment {
				SONAR = credentials('sonar')
			}
      steps {
        parallel (
              "Integration Test" : {
                  // sh 'mvn verify'
                  echo 'Run integration tests here...'
              },
              "Sonar Scan" : {
                  sh "mvn sonar:sonar -Dsonar.host.url=http://sonar.beedemo.net:9000 -Dsonar.organization=$SONAR_USR -Dsonar.login=$SONAR_PSW"
              }, failFast: true
        )
      }
		}

	  stage('Build & Push Docker Image') {
			when {
				branch 'master'
			}
      steps {
				env.SHORT_COMMIT = sh(returnStdout: true, script: "git rev-parse HEAD | cut -c1-7").trim()
        sh """
					docker build -t sample-rest-service:${BUILD_NUMBER}-${SHORT_COMMIT} .
					docker push sample-rest-service:${BUILD_NUMBER}-${SHORT_COMMIT}
				"""
    	}
		}

		stage('Deploy') {
			steps {
				echo "doing the deploy.."
			}
		}
	}
}
