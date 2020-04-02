#!/usr/bin/groovy
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException

def SLACK_CHANNEL
def COLOUR_MAP = ['SUCCESS': 'good', 'FAILURE': 'danger', 'UNSTABLE': 'danger', 'ABORTED': 'danger']
def getBuildUser() {
    node {
        wrap([$class: 'BuildUser']) {
            GET_BUILD_USER = sh ( script: 'echo "${BUILD_USER}"', returnStdout: true).trim()
        }
        return GET_BUILD_USER
    }
}

pipeline {
    agent any
    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }
    parameters {
        choice(name: 'Environment', choices: ['Dev', 'Qa', 'Stag', 'Prod'], description: 'Deploy On')
        choice(name: 'Publisher', choices: ['Pub1', 'PUb2'], description: 'Publisher Deploy On')
        choice(name: 'UpdateBuild', choices: ['False', 'True'], description: 'Update the job')
    }
    environment {
        BUILD_USER = ''
    }
    stages {
        stage('UPDATE_JOB'){
            when {
                expression {
                    params.UpdateBuild == 'True'
                }
            }
            steps{
                script{
                    echo "Build Updated"
                    //throw new FlowInterruptedException(Result.SUCCESS)
                    currentBuild.getRawBuild().getExecutor().interrupt(Result.SUCCESS)
                    sleep(1) 
                }
            }
        }
        stage('CLEAN_WORKSPCE') {
            steps {
                script{
                    sh "echo CLEAN_WORKSPACE RAN!!!"
                }
            }
        }
        stage('GET_DEPLOYMENT_PROP') {
            steps {
                script{
                    sh "echo GET_DEPLOYMENT_PROP RAN!!!"
                }
            }
        }
    }
}