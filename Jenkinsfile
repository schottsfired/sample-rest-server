pipeline {

	agent any

	/*tools {
		maven 'Maven3'
  }*/

  options {
    buildDiscarder(logRotator(numToKeepStr:'10')) // Keep the 10 most recent builds
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
                  sh 'mvn sonar:sonar -Dsonar.host.url=https://sonarqube.com -Dsonar.organization=cvitter-github -Dsonar.login=58ebe76411b8bef9fe0730201f583109005c505d'
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
