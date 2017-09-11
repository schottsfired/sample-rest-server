pipeline {

	agent any

	options {
		buildDiscarder(logRotator(numToKeepStr:'10')) // Keep the 10 most recent builds
	}

	environment {
		SONAR = credentials('sonar')
		DOCKERHUB = credentials('dockerhub')
		IMAGE_NAME = "schottsfired/sample-rest-server"
		VERSION = "latest"
	}

	stages {
		stage('Version') {
			steps {
				script {
					VERSION = 
						sh returnStdout: true,
						script: "echo ${BUILD_NUMBER}-`git rev-parse HEAD`"
						.trim()
					echo "Set version to $VERSION"
				}
			}
		}

		stage('Build, Unit Test, Package') {
			steps {
				sh 'mvn clean package'
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
						sh "mvn sonar:sonar -Dsonar.host.url=http://sonar.beedemo.net:9000 -Dsonar.login=$SONAR"
					}, failFast: true
				)
			}
		}

		stage('Build & Push Docker Image') {
			when {
				branch 'master'
			}
			steps {
				sh """
					docker build -t schottsfired/sample-rest-service:$VERSION .
					docker login -u $DOCKERHUB_USR -p $DOCKERHUB_PSW
					docker push schottsfired/sample-rest-service:$VERSION
				"""
			}
		}

		stage('Deploy') {
			when {
				branch 'master'
			}
			steps {
				sh 'docker run -d -p 4567:4567 schottsfired/sample-rest-service:$VERSION'
				input 'Does http://localhost:4567/hello look good?'
			}
			post {
				always {
					sh 'docker stop $(docker ps -q --filter ancestor="schottsfired/sample-rest-service:$VERSION") || true'
				}
			}
		}
	}
}
