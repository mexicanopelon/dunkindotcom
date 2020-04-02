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
def GetPub(){
    node{
        git branch: "master",
        url: "git@github.com:mexicanopelon/dunkindotcom.git"
        def props = readJSON file: 'tagsProperties.json', returnPojo: true
        def options = []
        def count = '1';
        props.Environment.get(0)."${params.Environment}".get(0).ServerGroup.get(0).each{ key, value ->
            echo "PUB$count"
            options.add("PUB$count")
        count++
        }
        return options as List
    }
}
pipeline {
    agent any
    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }
    parameters {
        choice(name: 'Environment', choices: ['Dev', 'Qa', 'Stag', 'Prod'], description: 'Deploy On')
        choice(name: 'Publisher', choices: GetPub(), description: 'Publisher Deploy On')
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
                    throw new FlowInterruptedException(Result.SUCCESS)
                }
            }
        }
        stage('CLEAN_WORKSPCE') {
            steps {
                cleanWs()
            }
        }
        stage('GET_DEPLOYMENT_PROP') {
            steps {
                script {
                    echo "${params.Publisher}"
                    BUILD_USER = getBuildUser()
                    echo "${BUILD_USER}"
                }
            }
        }
    }
}