pipeline {
    agent any

    environment {
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
                script {
                    echo 'Deploying with Docker Compose...'
                    dir('deploy') {
                        sh 'docker compose pull'
                        sh 'docker compose up -d'
                    }

                    // -- Kubernetes alternative (uncomment to use) --
                     echo 'Deploying to Kubernetes...'
                    dir('k8s') {
                         sh 'kubectl apply -f deployment.yaml'
                         sh 'kubectl rollout status deployment/bitcoin-backend'
                         sh 'kubectl rollout status deployment/bitcoin-frontend'
                     }
                }
            }
        }
    }
}
