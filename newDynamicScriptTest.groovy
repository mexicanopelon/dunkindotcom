#!/usr/bin/env groovy
import groovy.json.JsonSlurper

List JustTest() {
    ["rm", "-Rf", "/tmp/dunkindotcom"].execute()
    ["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git", "/tmp/dunkindotcom"].execute()

    def inputFile = new File("/tmp/dunkindotcom/dev-properties.json")
    def data = new JsonSlurper().parseFile(inputFile, 'UTF-8')

    def serverGroup = []
    data.servers_list.each{ 
        serverGroup =  it.keySet()
    }

    return serverGroup as List
}

properties([
    parameters([
        choice(name: 'PARAM', choices: JustTest().join('\n'), description: 'Choice'),
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