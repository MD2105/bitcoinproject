pipeline {
    agent any

    environment {
        // Ensure Jenkins can find Docker in its PATH (if you also set this in Global env-vars).
        PATH = "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKERHUB_USER        = "${DOCKERHUB_CREDENTIALS_USR}"
    }

    stages {
        stage('Check Docker Access') {
            steps {
                echo 'Checking Docker CLI access from Jenkins...'
                sh 'docker version'
            }
        }

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
                    // Switch to the "default" Docker context if possible; ignore errors if it doesn't exist.
                    sh 'docker context use default || true'

                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        dir('bitcoin-price') {
                            // Build & push the backend image
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
                    // Again, force the default context
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
                // Add your deployment commands here (e.g. kubectl apply, docker-compose, etc.)
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
    }
}