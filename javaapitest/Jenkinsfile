pipeline {
  agent any

  tools {
    jdk 'jdk17'  // from second file
    // sonar tool will be dynamically loaded below in script
  }

  environment {
    SONAR_HOST_URL   = 'https://sonarcloud.io/'
    SONAR_AUTH_TOKEN = credentials('sonar')  // from second file
    SONAR_PROJECT_KEY = 'reykrish09_pcodemo1'
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
        sh './mvnw clean install'
      }
    }

    stage('SonarQube Analysis') {
      steps {
        script {
          def scannerHome = tool name: 'sonar', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
          sh """
            ${scannerHome}/bin/sonar-scanner \
              -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \
              -Dsonar.organization=${env.SONAR_ORGANIZATION} \
              -Dsonar.sources=src \
              -Dsonar.host.url=${env.SONAR_HOST_URL} \
              -Dsonar.login=${env.SONAR_AUTH_TOKEN} \
              -Dsonar.java.binaries=.
          """
        }
      }
    }

    stage('Fetch & Fix Vulnerabilities') {
      steps {
        sh './mvnw exec:java -Dexec.mainClass="com.example.sonar.SonarApiFixer"'
      }
    }

    stage('Re-Scan After Fix') {
      steps {
        script {
          def scannerHome = tool name: 'sonar', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
          sh """
            ${scannerHome}/bin/sonar-scanner \
              -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \
              -Dsonar.organization=${env.SONAR_ORGANIZATION} \
              -Dsonar.sources=src \
              -Dsonar.host.url=${env.SONAR_HOST_URL} \
              -Dsonar.login=${env.SONAR_AUTH_TOKEN} \
              -Dsonar.java.binaries=.
          """
        }
      }
    }
  }

  post {
    success {
      echo "Analysis and Quality Gate passed!"
    }
    failure {
      echo "Analysis failed – check logs."
    }
  }
}
