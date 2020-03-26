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

def GetParamList() {
    //["rm", "-Rf", "/tmp/dunkindotcom"].execute()
    //sleep(1000)
    //["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git", "/tmp/dunkindotcom"].execute()
    //sleep(5000)

    //def inputFile = new File("/tmp/dunkindotcom/tagsProperties.json")
    def inputFile = new File("/tmp/tagsProperties.json") << new URL ("https://raw.githubusercontent.com/mexicanopelon/dunkindotcom/master/tagsProperties.json").getText()
    def data = new JsonSlurper().parseFile(inputFile, 'UTF-8')

    def options = []
    def envs = []

    data.Environment.each{
        envs = it.keySet()
        envs.each{
            env = it.toString()
            options.add("----- ${env} -----")
            serverGroup = data.Environment.getAt("$it").ServerGroup
            serverGroup.each{
                it.each{
                    it.each{
                        it.keySet().each {
                            options.add("${it}")
                            //envMap.put("'${it}'","'${env}'")
                            
                        }
                    }
                }
            }
        }
    }

    return options as List
}

return GetParamList()
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