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
        dir('bitcoin-price') {
            sh "echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin"
            sh "docker build -t $DOCKERHUB_CREDENTIALS_USR/bitcoin-backend:latest ."
            sh "docker push $DOCKERHUB_CREDENTIALS_USR/bitcoin-backend:latest"
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
        stage('Docker Build & Push Backend') {
    steps {
        dir('bitcoin-price') {
            sh "echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin"
            sh "docker build -t $DOCKERHUB_CREDENTIALS_USR/bitcoin-backend:latest ."
            sh "docker push $DOCKERHUB_CREDENTIALS_USR/bitcoin-backend:latest"
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