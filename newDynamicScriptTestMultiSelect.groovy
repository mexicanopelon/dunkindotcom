#!/usr/bin/env groovy
import groovy.json.JsonSlurperClassic

def envMap2 = [:]

List GetParamList() {
    ["rm", "-Rf", "/tmp/dunkindotcom"].execute()
    ["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git", "/tmp/dunkindotcom"].execute()
    sleep(5)

    def inputFile = new File("/tmp/dunkindotcom/tagsProperties.json")
    def data = new JsonSlurperClassic().parseFile(inputFile, 'UTF-8')

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
                            envMap2.put("'${it}'","'${env}'")
                            
                        }
                    }
                }
            }
        }
    }

    return options as List
}

LinkedHashMap getEnvMap() {
    ["rm", "-Rf", "/tmp/dunkindotcom"].execute()
    ["git", "clone", "git@github.com:mexicanopelon/dunkindotcom.git", "/tmp/dunkindotcom"].execute()
    sleep(5)

    def inputFile = new File("/tmp/dunkindotcom/tagsProperties.json")
    def data = new JsonSlurperClassic().parseFile(inputFile, 'UTF-8')
    def envMap = [:]
    data.Environment.each{
        it.keySet().each{
            env = it.toString()
            serverGroup = data.Environment.getAt("$it").ServerGroup
            serverGroup.each{
                it.each{
                    it.each{
                        it.keySet().each {
                            envMap.put("'${it}'","'${env}'")
                            
                        }
                    }
                }
            }
        }
    }

    return envMap
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
                    println getEnvMap()
                    println envMap2
                }
            }
        }   
    }
}