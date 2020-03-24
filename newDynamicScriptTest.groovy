#!/usr/bin/env groovy

List JustTest() {
        List xxx = ['a','b']
        return xxx
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