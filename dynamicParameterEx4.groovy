#!/usr/bin/env groovy

def DEPLOYMENT_SCRIPT_BRANCH
def ARTIFACT_NAME
def ARTIFACT_VERSION
def ART_NAME = 'ddcom-ui'
def PUBLISHER_PORT = "4503"
def ARTIFACTORY_REPO = "ddcom-release-prod"
def serverGroup

properties([
    parameters([
        [
            $class: 'ChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: '', 
            filterable: false, 
            name: 'PUBLISHER', 
            randomName: 'choice-parameter-21337077649621572',
            script: [
                $class: 'GroovyScript', 
                fallbackScript: [
                    classpath: [], sandbox: false, script: ''
                ], 
                script: [
                    classpath: [], sandbox: false, script: 
                    '''
                    import groovy.json.JsonSlurper

                    ["rm", "-Rf", "/tmp/dunkindotcom"].execute()
                    ["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git", "/tmp/dunkindotcom"].execute()

                    def inputFile = new File("/tmp/dunkindotcom/dev-properties.json")
                    def data = new JsonSlurper().parseFile(inputFile, 'UTF-8')

                    def serverGroup = []
                    data.servers_list.each{ 
                        serverGroup =  it.keySet()
                    }

                    return serverGroup as List
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