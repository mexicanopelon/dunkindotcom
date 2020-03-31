#!/usr/bin/env groovy
import groovy.json.JsonSlurperClassic

def GetBranchList() {
    def getTags = "git ls-remote --heads --tags https://github.com/mexicanopelon/dunkindotcom.git".execute().text

    return getTags.readLines().collect { 
        it.split()[1].replaceAll('refs/heads/', '').replaceAll('refs/tags/', '') 
    }
}

properties([
    parameters([
        choice(name: 'Branch', choices: GetBranchList().join('\n'), description: 'Please Choose a Branch'),
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
                    println getEnvMap()
                }
            }
        }   
    }
}