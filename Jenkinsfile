pipeline {

	agent any

	options {
		buildDiscarder(logRotator(numToKeepStr:'10')) // Keep the 10 most recent builds
	}

	environment {
		SONAR = credentials('sonar')
		DOCKERHUB = credentials('dockerhub')
		IMAGE_NAME = "schottsfired/sample-rest-server"
		IMAGE_VERSION = "latest"
		DOCKER_NETWORK = "cjt-network"
	}

	stages {
		stage('Version') {
			steps {
				script {
					IMAGE_VERSION = 
						sh returnStdout: true,
							script: "echo ${BUILD_NUMBER}-`git rev-parse HEAD` | tr -d '\n'"
					echo "Set version to $IMAGE_VERSION"
				}
			}
		}

		stage('Build, Unit, Package') {
			steps {
				sh 'mvn clean package'
				junit testResults: '**/target/surefire-reports/TEST-*.xml'
				archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
			}
		}

		stage('Create Docker Image') {
			steps {
				sh "docker build -t $IMAGE_NAME:$IMAGE_VERSION ."
			}
		}

		stage('Quality Analysis') {
			steps {
				parallel (
					"Sonar Scan" : {
						sh "mvn sonar:sonar -Dsonar.host.url=http://sonar.beedemo.net:9000 -Dsonar.login=$SONAR"
					},
					"Functional Test" : {
						//fire up the app
						sh """
							docker run -d \
							--name sample-rest-server \
							--network $DOCKER_NETWORK \
							-p 4567:4567 \
							$IMAGE_NAME:$IMAGE_VERSION
						"""
						//hit the /hello endpoint and collect result
						retry(3) {
							sh 'curl -v http://sample-rest-server:4567/hello > functionalTest.txt'
							sleep 5
						}
						//store result
						archiveArtifacts artifacts: 'functionalTest.txt', fingerprint: true
					}, failFast: true
				)
			}
		}

		stage('Push Docker Image') {
			when {
				branch 'master'
			}
			steps {
				sh """
					docker login -u $DOCKERHUB_USR -p $DOCKERHUB_PSW
					docker push $IMAGE_NAME:$IMAGE_VERSION
				"""
			}
		}
	}

	post {
		always {
			//Stop sample-rest-server
			sh "docker ps -q --filter ancestor='$IMAGE_NAME:$IMAGE_VERSION' | xargs docker stop || true"
			//Remove the container
			sh "docker ps -a -q --filter ancestor='$IMAGE_NAME:$IMAGE_VERSION' | xargs docker rm || true"
			//Remove the image
			sh "docker images --format '{{.Repository}}:{{.Tag}}' | grep '^$IMAGE_NAME' | xargs docker rmi || true"
		}
	}
}
