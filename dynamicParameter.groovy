#!/usr/bin/env groovy
import groovy.json.JsonSlurper

def DEPLOYMENT_SCRIPT_BRANCH
def ARTIFACT_NAME
def ARTIFACT_VERSION
def ART_NAME = 'ddcom-ui'
def PUBLISHER_PORT = "4503"
def ARTIFACTORY_REPO = "ddcom-release-prod"


["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git"].execute()

pipeline {
    agent any
    tools {
        maven 'Maven 3.2.3'
        jdk "Java 1.8.0_201"
    }
    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }

    environment {
        ARTIFACTORY_SERVER = 'Dunkin_artifactory'
    }

    parameters { 
        string(name: 'CODE_BRANCH', defaultValue: '', description: 'Branch Name')
        choice(name: 'PUBLISHER', choices: ['PUB1', 'PUB2', 'PUB3', 'PUB4'], description: 'Deploy On')
        string(defaultValue: "https://dunkindonuts.com", description: 'URL Check', name: 'URL')
        string(defaultValue: "content/dd/en.html", description: 'URL Value', name: 'URL_VAL')
    }

    stages {
        stage('SCM') {
            steps {
                script{
                    sh "mkdir codebase"
                    dir ('codebase') {
                        git branch: "${params.CODE_BRANCH}",
                        url: 'git@dbuslnxgithub01.dunkinbrands.corp:dunkindonuts/ddcom-aem.git'
                    }
                }
            }
        }   
    }
}