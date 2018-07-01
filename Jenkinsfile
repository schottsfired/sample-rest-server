library 'github.com/schottsfired/pipeline-libraries'
pipeline {

	agent any

	options {
		timestamps()
		buildDiscarder(logRotator(numToKeepStr:'5')) //delete old builds
		ansiColor('xterm')
	}

	environment {
		DOCKERHUB = credentials('dockerhub')
		IMAGE_NAME = "schottsfired/sample-rest-server"
		IMAGE_TAG = dockerImageTag()
		DOCKER_NETWORK = "cjt-network"
	}

	stages {
		stage('Build, Unit, Package') {
			steps {
				withMaven(mavenOpts: '-Djansi.force=true') {
				    sh 'mvn clean package -Dstyle.color=always'
			    }
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
						withMaven(publisherStrategy: 'EXPLICIT', mavenOpts: '-Djansi.force=true') {
				    		sh 'mvn sonar:sonar -Dsonar.host.url=http://sonar:9000 -Dstyle.color=always'
			    		}
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
					}
				)
			}
		}

		stage('Publish Docs, Push Docker Image') {
			when {
				branch 'master'
			}
			steps {
				parallel (
					"Publish Docs" : {
						withMaven(publisherStrategy: 'EXPLICIT', mavenOpts: '-Djansi.force=true') {
							sh 'mvn site -Dstyle.color=always'
						}
						publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'target/site', reportFiles: 'index.html', reportName: 'API Documentation', reportTitles: ''])
					},
					"Push Docker Image" : {
						sh """
							docker login -u $DOCKERHUB_USR -p $DOCKERHUB_PSW
							docker push $IMAGE_NAME:$IMAGE_TAG
						"""
					}
				)
			}
		}
	}

	post {
		always {
			dockerNuke(IMAGE_NAME, IMAGE_TAG)
		}
	}
}
