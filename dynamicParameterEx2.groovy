#!/usr/bin/env groovy
import groovy.json.JsonSlurper

def DEPLOYMENT_SCRIPT_BRANCH
def ARTIFACT_NAME
def ARTIFACT_VERSION
def ART_NAME = 'ddcom-ui'
def PUBLISHER_PORT = "4503"
def ARTIFACTORY_REPO = "ddcom-release-prod"
def serverGroup

/*
["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git"].execute()

def inputFile = new File("./dunkindotcom/dev-properties.json")
def data = new JsonSlurper().parseFile(inputFile, 'UTF-8')

data.servers_list.each{ 
    serverGroup = it.keySet()
    print(serverGroup)
}
*/

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
                    classpath: [], sandbox: true, script: 
                    '''
                    // Find relevant AMIs based on their name
                    def sout = new StringBuffer(), serr = new StringBuffer()
                    def proc = '/usr/bin/aws ec2 describe-instances --profile=dunkindev --region us-east-1 --filter Name=name,Values=swi --query Reservations[*].Instances[*].PrivateIpAddress --output text'.execute()
                    proc.consumeProcessOutput(sout, serr)
                    proc.waitForOrKill(10000)
                    return sout.tokenize()
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