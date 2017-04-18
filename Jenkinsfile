pipeline {

	agent any

  options {
    buildDiscarder(logRotator(numToKeepStr:'10')) // Keep the 10 most recent builds
  }

	environment {
		SONAR = credentials('sonar')
	}

	stages {

		stage('Build - Pull Request') {
			when {
				expression { (BRANCH_NAME.startsWith("PR")) }
      }
      steps {
        sh 'mvn test'
      }
		}

		stage('Build - Development') {
			when {
        branch 'development'
      }
      steps {
        sh 'mvn clean package site'
      }
		}

		stage('Build - Master') {
			when {
        branch 'master'
      }
      steps {
        sh 'mvn clean package site'
      }
		}

		stage('Quality Analysis') {
			when {
        branch 'master'
      }
      steps {
        parallel (
              "integrationTests" : {
                  // sh 'mvn verify'
                  echo 'Run integration tests here...'
              },
              "sonarAnalysis" : {
                  sh "mvn sonar:sonar -Dsonar.host.url=https://sonarqube.com -Dsonar.organization=$SONAR_USR -Dsonar.login=$SONAR_PSW"
              }, failFast: true
        )
      }
		}

		stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        echo 'Here is where we deploy our code!'
    	}
		}
	}
}
