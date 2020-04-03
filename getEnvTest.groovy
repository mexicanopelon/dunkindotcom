#!/usr/bin/groovy
def COLOUR_MAP = ['SUCCESS': 'good', 'FAILURE': 'danger', 'UNSTABLE': 'danger', 'ABORTED': 'danger']

/*
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
        git branch: "common",
        url: "git@github.com:mexicanopelon/dunkindotcom.git"
        def props = readJSON file: 'tagsProperties.json', returnPojo: true
        def options = []
        def count = '1';
        props.Environment.get(0).Dev.get(0).ServerGroup.get(0).each{ key, value ->
            echo "PUB$count"
            options.add("PUB$count")
        count++
        }
        return options as List
    }
}
*/

def GetEnv(){
    node{
        git branch: "common",
        url: "git@github.com:mexicanopelon/dunkindotcom.git"
        def props = readJSON file: 'tagsProperties.json', returnPojo: true
        def options = []
        def count = '1';
        //return ['env']
        //return props.Environment.get(0).each.get(0) as List

        props.Environment.get(0).Dev.get(0).ServerGroup.get(0).each{ key, value ->
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
        choice(name: 'Environment', choices: GetEnv(), description: 'Deploy On')
        choice(name: 'Publisher', choices: "Pub1, Pub2", description: 'Publisher Deploy On')
        choice(name: 'UpdateBuild', choices: ['False', 'True'], description: 'Update the job')
    }
    environment {
        BUILD_USER = ''
    }
    stages {
        stage('UPDATE'){
            when {
                expression {
                    params.UpdateBuild == 'True'
                }
            }
            steps{
                script{
                    echo "Build Updated"
                }
            }
        }
    }
}