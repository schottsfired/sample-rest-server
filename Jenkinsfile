library 'github.com/schottsfired/pipeline-libraries'
pipeline {

	agent {
		docker {
			label "docker"
			image "maven"
		}
	}

	options {
		timestamps()
		buildDiscarder(logRotator(numToKeepStr:'10')) //delete old builds
	}

	environment {
		DOCKERHUB = credentials('emcconne_dockerhub')
		IMAGE_NAME = "emcconne/sample-rest-server"
		IMAGE_TAG = dockerImageTag()
		DOCKER_NETWORK = "cjt-network"
	}

	stages {
		stage('Build, Unit, Package') {
			steps {
				sh 'mvn clean package'
				junit testResults: '**/target/surefire-reports/TEST-*.xml'
				archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
			}
		}

		stage('Create Docker Image') {
			steps {
				sh "docker build -t $IMAGE_NAME:$IMAGE_TAG ."
			}
		}

		stage('Quality Analysis') {
			steps {
				parallel (
					"Sonar Scan" : {
						sh "mvn sonar:sonar -Dsonar.host.url=http://sonar:9000"
					},
					"Functional Test" : {
						//fire up the app
						sh """
							docker run -d \
							--name sample-rest-server \
							--network $DOCKER_NETWORK \
							-p 4567:4567 \
							$IMAGE_NAME:$IMAGE_TAG
						"""
						//hit the /hello endpoint and collect result
						retry(3) {
							sleep 2
							sh 'curl -v http://sample-rest-server:4567/hello > functionalTest.txt'
						}
						//store result
						archiveArtifacts artifacts: 'functionalTest.txt', fingerprint: true
					}, failFast: true
				)
			}
		}

		stage('Publish Docs') {
			when {
				branch 'master'
			}
			steps {
				sh 'mvn site:site'
				step([$class: 'JavadocArchiver', javadocDir: 'target/site/apidocs', keepAll: false])
			}
		}

		stage('Push Docker Image') {
			when {
				branch 'master'
			}
			steps {
				sh """
					docker login -u $DOCKERHUB_USR -p $DOCKERHUB_PSW
					docker push $IMAGE_NAME:$IMAGE_TAG
				"""
			}
		}
	}

	post {
		always {
			dockerNuke(IMAGE_NAME, IMAGE_TAG)
		}
	}
}
