#!/usr/bin/env groovy
import groovy.json.JsonSlurper

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
            name: 'Release', 
            randomName: 'choice-parameter-21337077649621572', 
            script: [
                $class: 'GroovyScript', 
                fallbackScript: [
                    classpath: [], sandbox: true, script: ''
                ], 
                script: [
                    classpath: [], sandbox: false, script: 
                    '''
                    import groovy.json.JsonSlurper

                    def inputFile = new File("/tmp/devprops.json") << new URL ("https://raw.githubusercontent.com/mexicanopelon/dunkindotcom/master/dev-properties.json").getText()
                    def data = new JsonSlurper().parseFile(inputFile, 'UTF-8')

                    def serverGroup = []
                    data.servers_list.each{ 
                    serverGroup = it.keySet()
                    print(serverGroup)
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
                }
            }
        }   
    }
}