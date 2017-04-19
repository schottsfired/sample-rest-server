pipeline {

	agent any

  options {
    buildDiscarder(logRotator(numToKeepStr:'10')) // Keep the 10 most recent builds
  }

	environment {
		SONAR = credentials('sonar')
	}

	stages {

		stage('Build') {
      steps {
        sh 'mvn clean package site'
				junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
      }
		}

		stage('Quality Analysis') {
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
				def BUILD_NUMBER = ${env.BUILD_NUMBER}
        sh """
					docker build \
					-t sample-rest-service:0.0.$BUILD_NUMBER \
					.
				"""
    	}
		}
	}
}
