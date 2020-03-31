#!/usr/bin/env groovy

def DEPLOYMENT_SCRIPT_BRANCH
def ARTIFACT_NAME
def ARTIFACT_VERSION

properties([
    parameters([
        [
            $class: 'ChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Please Choose a Valid Publisher', 
            filterable: false, 
            name: 'Branch Name', 
            randomName: 'choice-parameter-21337077649621572',
            script: [
                $class: 'GroovyScript', 
                fallbackScript: [
                    classpath: [], sandbox: false, script: ''
                ], 
                script: [
                    classpath: [], sandbox: false, script: 
                    '''
def getTags = "git ls-remote --heads --tags https://github.com/mexicanopelon/dunkindotcom.git".execute().text

return getTags.readLines().collect { 
   it.split()[1].replaceAll('refs/heads/', '').replaceAll('refs/tags/', '') 
}
                    '''
                ]
            ]
        ]
    ])
])

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }

    environment {
        ARTIFACTORY_SERVER = 'Dunkin_artifactory'
    }

    stages {
        stage('BUILD') {
            steps {
                script{
                    sh "echo HELLO WORLD!!!"
                    sh "echo WORKSPACE is ${env.WORKSPACE}"
                }
            }
        }   
    }
}