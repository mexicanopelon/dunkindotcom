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
                    return ["test1","test2"] as List
                    ["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git"].execute()
                    return ["test3","test4"] as List
                    //def workspace = this.binding.jenkinsProject.workspace
                    def workspace = "/Users/cdelapaz/.jenkins/workspace/try_again3"

                    return ["test5","test6"] as List
                    return ["${workspace}","test"] as List
                    def inputFile = new File("${workspace}/dunkindotcom/dev-properties.json")
                    def data = new JsonSlurper().parseFile(inputFile, 'UTF-8')

                    data.servers_list.each{ 
                        serverGroup =  it.keySet()
                    }

                    ///return serverGroup as List
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