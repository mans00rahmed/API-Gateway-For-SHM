pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh './mvnw clean verify'
                    } else {
                        bat 'mvnw.cmd clean verify'
                    }
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                        id: 'api-gateway-coverage',
                        name: 'API Gateway Coverage'
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    script {
                        if (isUnix()) {
                            sh './mvnw sonar:sonar -Dsonar.projectKey=api-gateway -Dsonar.projectName="API Gateway"'
                        } else {
                            bat 'mvnw.cmd sonar:sonar -Dsonar.projectKey=api-gateway -Dsonar.projectName="API Gateway"'
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            echo 'API Gateway pipeline finished.'
        }
        success {
            echo 'API Gateway build successful.'
        }
        failure {
            echo 'API Gateway pipeline failed.'
        }
    }
}
