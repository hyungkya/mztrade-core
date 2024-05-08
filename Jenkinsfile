pipeline {
    agent any

    stages {
        stage('Git Checkout') {
            steps {
                echo 'Checking Out...'
                git branch: 'master', credentialsId: 'github_personal_access_token', url: 'https://github.com/hyungkya/mztrade-core'
            }
        }
        
        stage('Setting Firebase Admin SDK key') {
            steps {
            	withCredentials([file(credentialsId: 'firebase_admin_sdk_key', variable: 'firebaseKey')]) {
                    script {
                        sh 'cp -f $firebaseKey src/main/resources/serviceAccountKey.json'
                    }
            	}
            }
        }
        
        stage('Setting Environment Variables') {
            steps {
            	withCredentials([file(credentialsId: 'env', variable: 'envFile')]) {
                    script {
                        sh 'cp -f $envFile .env'
                    }
            	}
            }
        }
        
        stage("Gradle Build") {
            steps {
                sh './gradlew clean build'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build . --platform linux/arm64 --file Dockerfile --tag mztrade/core:arm64'
                sh 'docker build . --platform linux/amd64 --file Dockerfile --tag mztrade/core:amd64'
            }
        }
        
        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker_personal_access_token', passwordVariable: 'password', usernameVariable: 'username')]){
                         sh 'echo ${password} | docker login -u ${username} --password-stdin'
                     }
                sh 'docker push mztrade/core:arm64'
                sh 'docker push mztrade/core:amd64'
            }
        }
        
        stage('Delete Docker Image') {
            steps {
                sh 'docker rmi mztrade/core:arm64'
                sh 'docker rmi mztrade/core:amd64'
            }
        }
        
        stage('Deploy over SSH') {
            steps {
                sshPublisher(
                    failOnError: true,
                    publishers: [
                        sshPublisherDesc(
                            configName: 'mztrade-core-dev',
                            verbose: true,
                            transfers: [
                                sshTransfer(
                                    cleanRemote: true,
                                    sourceFiles: 'mysql/initdb.d/*',
                                    removePrefix: 'mysql/initdb.d',
                                    remoteDirectory: './mysql/init/',
                                ),
                                sshTransfer(
                                    execCommand: 'sh deploy.sh'
                                )
                            ]
                        )
                    ]
                )
            }
        }
    }
}
