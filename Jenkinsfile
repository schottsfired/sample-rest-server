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
			environment {
				DOCKERHUB = credentials('dockerhub')
			}
			when {
				branch 'master'
			}
			steps {
				sh """
					docker build -t schottsfired/sample-rest-service:${BUILD_NUMBER}-`git rev-parse HEAD` .
					docker login -u $DOCKERHUB_USR -p $DOCKERHUB_PSW
					docker push schottsfired/sample-rest-service:${BUILD_NUMBER}-`git rev-parse HEAD`
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
