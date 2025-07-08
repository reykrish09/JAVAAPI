pipeline {
  agent any

  tools {
    jdk 'jdk17'
    // sonar will be dynamically loaded
  }

  environment {
    SONAR_HOST_URL      = 'https://sonarcloud.io/'
    SONAR_AUTH_TOKEN    = credentials('sonar') // Jenkins secret ID
    SONAR_PROJECT_KEY   = 'reykrish09_pcodemo1'
    SONAR_ORGANIZATION  = 'reykrish09'
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
        script {
          def scannerHome = tool name: 'sonar', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
          sh """
            ${scannerHome}/bin/sonar-scanner \
              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
              -Dsonar.organization=${SONAR_ORGANIZATION} \
              -Dsonar.sources=src \
              -Dsonar.host.url=${SONAR_HOST_URL} \
              -Dsonar.login=${SONAR_AUTH_TOKEN} \
              -Dsonar.java.binaries=.
          """
        }
      }
    }

    stage('Fetch & Fix Vulnerabilities') {
      steps {
        sh """
          ./mvnw exec:java \
            -Dexec.mainClass=com.example.sonar.SonarApiFixer \
            -Dexec.args='${SONAR_HOST_URL} ${SONAR_AUTH_TOKEN} ${SONAR_PROJECT_KEY}'
        """
      }
    }

    stage('Re-Scan After Fix') {
      steps {
        script {
          def scannerHome = tool name: 'sonar', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
          sh """
            ${scannerHome}/bin/sonar-scanner \
              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
              -Dsonar.organization=${SONAR_ORGANIZATION} \
              -Dsonar.sources=src \
              -Dsonar.host.url=${SONAR_HOST_URL} \
              -Dsonar.login=${SONAR_AUTH_TOKEN} \
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
      echo "Analysis failed – check logs above."
    }
  }
}
