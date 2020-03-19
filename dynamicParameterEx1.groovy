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

def inputFile = new File("./dunkindotcom/dev-properties.json")
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

    stages {
        stage("Release scope") {
            steps {
                script {
                    // Prepare a list and write to file
                    sh "echo \"patch\nminor\nmajor\" > ${WORKSPACE}/list"

                    // Load the list into a variable
                    env.LIST = readFile (file: "${WORKSPACE}/list")

                    // Show the select input
                    env.RELEASE_SCOPE = input message: 'User input required', ok: 'Release!',
                            parameters: [choice(name: 'RELEASE_SCOPE', choices: env.LIST, description: 'What is the release scope?')]
                }
                echo "Release scope selected: ${env.RELEASE_SCOPE}"
            }
        }

        stage('BUILD') {
            steps {
                script{
                    sh "echo HELLO WORLD!!!"
                }
            }
        }   
    }
}