pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm  // This will populate the GIT_COMMIT and other Git-related environment variables
            }
        }
        stage('Build Image') {
            steps {
                sh 'docker build . -t=shift-left-test'
            }
        }
        stage('Shift Left Scan') {
            steps {
                shiftLeftScan(
                        dockerImage: 'shift-left-test',
                        commit: "${GIT_COMMIT}",
                        branch: "${GIT_BRANCH}",
                        repo: "${GIT_URL}",
                )
            }
        }
        stage('Publish Image') {
            steps {
                sh 'echo publish image here'
            }
        }
        stage('Shift Left Publish Event') {
            steps {
                shiftLeftEvent(
                        eventType: 'IMAGE_PUBLISH',
                        dockerImage: 'shift-left-test',
                        commit: "${GIT_COMMIT}",
                        branch: "${GIT_BRANCH}",
                        repo: "${GIT_URL}",
                )
            }
        }
        stage('Deploy Image') {
            steps {
                sh 'echo deploy image here'
            }
        }
        stage('Shift Left Deploy Event') {
            steps {
                shiftLeftEvent(
                        eventType: 'IMAGE_DEPLOY',
                        dockerImage: 'shift-left-test',
                        commit: "${GIT_COMMIT}",
                        branch: "${GIT_BRANCH}",
                        repo: "${GIT_URL}",
                )
            }
        }

        stage ("Test Existing Images") {
            steps {
                shiftLeftEvent(
                        dockerImage: 'ubuntu:20.04',
                        commit: "${GIT_COMMIT}",
                        branch: "${GIT_BRANCH}",
                        repo: "${GIT_URL}",
                )
                shiftLeftEvent(
                        dockerImage: 'ubuntu:24.04',
                        commit: "${GIT_COMMIT}",
                        branch: "${GIT_BRANCH}",
                        repo: "${GIT_URL}",
                )
            }
        }
    }
}
