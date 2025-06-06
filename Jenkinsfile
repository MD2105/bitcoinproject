pipeline {
    agent any

    environment {
        // Prepend the Homebrew bin directory so npm/node are found
        PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKERHUB_USER        = "${DOCKERHUB_CREDENTIALS_USR}"
    }

    stages {
        stage('Check Docker Access') {
            steps {
                sh 'docker version'
            }
        }
        stage('Checkout') {
            steps { checkout scm }
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
                    sh 'docker context use default || true'
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
                    // Now npm should be on PATH
                    sh 'echo $PATH'
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }
        stage('Docker Build & Push Frontend') {
            steps {
                script {
                    sh 'docker context use default || true'
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
            }
        }
    }
}