#!/usr/bin/env groovy
import groovy.json.JsonSlurper

def DEPLOYMENT_SCRIPT_BRANCH
def ARTIFACT_NAME
def ARTIFACT_VERSION
def ART_NAME = 'ddcom-ui'
def PUBLISHER_PORT = "4503"
def ARTIFACTORY_REPO = "ddcom-release-prod"
def serverGroup

["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git"].execute()
["ls -la"].execute()

def inputFile = new File("./dunkindotcom/dev-properties-1.json")
def data = new JsonSlurper().parseFile(inputFile, 'UTF-8')

data.servers_list.each{ 
    serverGroup = it.keySet()
    print(serverGroup)
}

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }

    environment {
        ARTIFACTORY_SERVER = 'Dunkin_artifactory'
    }

    parameters { 
        choice(name: 'PUBLISHER', choices: "${serverGroup}", description: 'Deploy On')
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