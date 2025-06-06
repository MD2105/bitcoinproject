pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKERHUB_USER = "${DOCKERHUB_CREDENTIALS_USR}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build & Test Backend') {
            steps {
                dir('bitcoin-price') {
                    sh './mvnw clean verify'
                }
            }
        }
        stage('Docker Build & Push Backend') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        dir('bitcoin-price') {
                            def backendImage = docker.build("${DOCKERHUB_USER}/bitcoin-backend:latest")
                            backendImage.push()
                        }
                    }
                }
            }
        }
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }
        stage('Docker Build & Push Frontend') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        dir('frontend') {
                            def frontendImage = docker.build("${DOCKERHUB_USER}/bitcoin-frontend:latest")
                            frontendImage.push()
                        }
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
                // Add your deployment commands here (e.g., kubectl apply ...)
            }
        }
    }
}