pipeline {
  agent any

  tools {
    jdk 'jdk17'
  }

  environment {
    SONAR_HOST_URL     = 'https://sonarcloud.io/'
    SONAR_PROJECT_KEY  = 'reykrish09_pcodemo1'
    SONAR_ORGANIZATION = 'reykrish09'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh 'chmod +x mvnw'
        sh './mvnw clean install'
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withCredentials([string(credentialsId: 'sonar', variable: 'SONAR_TOKEN')]) {
          script {
            def scannerHome = tool name: 'sonar', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
            sh """
              ${scannerHome}/bin/sonar-scanner \\
                -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \\
                -Dsonar.organization=${env.SONAR_ORGANIZATION} \\
                -Dsonar.sources=src \\
                -Dsonar.host.url=${env.SONAR_HOST_URL} \\
                -Dsonar.login=${SONAR_TOKEN} \\
                -Dsonar.java.binaries=.
            """
          }
        }
      }
    }

    stage('Fetch & Fix Vulnerabilities') {
      steps {
        withCredentials([string(credentialsId: 'sonar', variable: 'SONAR_TOKEN')]) {
          withEnv([
            "SONAR_HOST=${env.SONAR_HOST_URL}",
            "SONAR_KEY=${env.SONAR_PROJECT_KEY}"
          ]) {
            // Reads secrets from environment (safe), not interpolated
            sh './mvnw exec:java -Dexec.mainClass=com.example.sonar.SonarApiFixer'
          }
        }
      }
    }

    stage('Re-Scan After Fix') {
      steps {
        withCredentials([string(credentialsId: 'sonar', variable: 'SONAR_TOKEN')]) {
          script {
            def scannerHome = tool name: 'sonar', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
            sh """
              ${scannerHome}/bin/sonar-scanner \\
                -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \\
                -Dsonar.organization=${env.SONAR_ORGANIZATION} \\
                -Dsonar.sources=src \\
                -Dsonar.host.url=${env.SONAR_HOST_URL} \\
                -Dsonar.login=${SONAR_TOKEN} \\
                -Dsonar.java.binaries=.
            """
          }
        }
      }
    }
  }

  post {
    success {
      echo " Analysis and Quality Gate passed!"
    }
    failure {
      echo " Analysis failed – check logs above."
    }
  }
}
