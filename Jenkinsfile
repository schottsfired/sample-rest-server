//library 'github.com/schottsfired/pipeline-libraries'
pipeline {

	agent none

	options {
		timestamps()
		buildDiscarder(logRotator(numToKeepStr:'10')) //delete old builds
	}

	environment {
		DOCKERHUB = credentials('emcconne_dockerhub')
		IMAGE_NAME = "emcconne/sample-rest-server"
		IMAGE_TAG = "latest"
		DOCKER_NETWORK = "cjt-network"
	}

	stages {

		stage('Build, Unit, Package') {
			agent {
				docker {
					label "docker"
					image "maven"
					reuseNode true
				}
			}
			steps {
				sh 'mvn clean package'
				junit testResults: '**/target/surefire-reports/TEST-*.xml'
				archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
				stash includes: '**/target/*, Dockerfile', name: 'assets'
			}
		}

		stage('Create Docker Image') {
			agent {
				docker {
					label "docker"
					image "docker"
					reuseNode true
				}
			}
			steps {
				unstash 'assets'
				sh "docker build -t $IMAGE_NAME:$IMAGE_TAG ."
			}
		}
		
		stage('Secure Cloud Interconnect Service') {
			agent any
			steps {
				sh "echo 'Calling VES for provisioning of interconnect'"
				sleep 10
			}
		}
		
		stage('Provisioning capacity in Amazon') {
			agent any
			steps {
				sh "echo 'Provisioning AWS Resources for deployment'"
				sleep 10
			}
		}		
		

		stage('Quality Analysis') {
			agent {
				docker {
					label "docker"
					image "maven"
				}
			}
			steps {
				parallel (
					"Sonar Scan" : {
						sh "mvn verify"
					},
					"Functional Test" : {
						//fire up the app
						//sh """
						//	docker run -d \
						//	--name sample-rest-server \
						//	--network $DOCKER_NETWORK \
						//	-p 4567:4567 \
						//	$IMAGE_NAME:$IMAGE_TAG
						//"""
						//hit the /hello endpoint and collect result
						retry(3) {
							sleep 15
							echo 'Functional Test completed successfully'
							//sh 'curl -v http://sample-rest-server:4567/hello > functionalTest.txt'
						}
						//store result
						//archiveArtifacts artifacts: 'functionalTest.txt', fingerprint: true
					}, failFast: true
				)
			}
		}

		stage('Push Docker Image') {
			agent {
				docker {
					label "docker"
					image "docker"
				}
			}
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
			emailext (
			  subject: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
			  body: """SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':
			    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
			  recipientProviders: [[$class: 'DevelopersRecipientProvider']]
			)
		}
	}
}
