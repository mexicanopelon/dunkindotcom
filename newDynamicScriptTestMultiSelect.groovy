#!/usr/bin/env groovy
import groovy.json.JsonSlurper

List GetParamList() {
    ["rm", "-Rf", "dunkindotcom/"].execute()
    ["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git"].execute()
    sleep(5000)

    def inputFile = new File("./dunkindotcom/tagsProperties.json")
    def data = new JsonSlurper().parseFile(inputFile, 'UTF-8')

    def options = []
    def envs = []

    data.Environment.each{
        envs = it.keySet()
        envs.each{
            options.add("----- ${it} -----")
            serverGroup = data.Environment.getAt("$it").ServerGroup
            serverGroup.each{
                it.each{
                    it.each{
                        it.keySet().each {
                            options.add("${it}")
                        }
                    }
                }
            }
        }
    }

    return options as List
}

properties([
    parameters([
        choice(name: 'PARAM', choices: GetParamList().join('\n'), description: 'Choice'),
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