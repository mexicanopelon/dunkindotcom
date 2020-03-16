#!/usr/bin/env groovy

def DEPLOYMENT_SCRIPT_BRANCH
def ARTIFACT_NAME
def ARTIFACT_VERSION
def ART_NAME = 'ddcom-ui'
def PUBLISHER_PORT = "4503"
def ARTIFACTORY_REPO = "ddcom-release-prod"

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
        choice(name: 'PUBLISHER', choices: ['PUB1', 'PUB2', 'PUB3', 'PUB4'], description: 'Deploy On')
        choice(name: 'SKIPPUBLISHER', choices: ['PUB1', 'PUB2', 'PUB3', 'PUB4'], description: 'Already Deployed On')
        string(defaultValue: "https://dunkindonuts.com", description: 'URL Check', name: 'URL')
        string(defaultValue: "content/dd/en.html", description: 'URL Value', name: 'URL_VAL')
    }

    stages {
        stage('CLEAN_WORKSPACE') {
            steps {
                cleanWs()
                sh 'printenv'
            }
        }
     
        stage('GET_DEPLOYMENT_FILES') {
            steps {
                script {
                    DEPLOYMENT_SCRIPT_BRANCH = sh (script: "echo ${GIT_BRANCH} | sed -e 's|origin/||g'",returnStdout: true).trim()
                    git branch: "${DEPLOYMENT_SCRIPT_BRANCH}",
                    url: "${GIT_URL}"
                }
            }
        }
        
        stage('GET_PUB_IP_PROPERTIESE') {
            steps {
                script {
                    def downloadSpec = """{
                        "files": [
                            {
                                "pattern": "${ARTIFACTORY_REPO}/${ART_NAME}/publisherip.propertise",
                                "target": "./"
                            }
                        ]
                    }"""
                    def server = Artifactory.server(ARTIFACTORY_SERVER)
                    server.download(downloadSpec)
                    sh "cat ${ART_NAME}/publisherip.propertise"
                }
            }
        }
        
        stage('GET_INVENTORY') {
            steps {
                script {
                    def downloadSpec = """{
                        "files": [
                            {
                                "pattern": "${ARTIFACTORY_REPO}/${ART_NAME}/inventory.ini",
                                "target": "./"
                            }
                        ]
                    }"""
                    def server = Artifactory.server(ARTIFACTORY_SERVER)
                    server.download(downloadSpec)
                    sh "cat ${ART_NAME}/inventory.ini"
                    ef versionSpec = """{
                        "files": [
                            {
                                "pattern": "${ARTIFACTORY_REPO}/${ART_NAME}/version.properties",
                                "target": "./"
                            }
                        ]
                    }"""
                    server.download(versionSpec)
                    sh "cat ${ART_NAME}/version.properties"
                }
            }
        }

        stage('GET_ARTIFACTS_VERSION') {
            steps {
                dir("${ART_NAME}") {
                    script {
                        ARTIFACT_VERSION = sh (script: "grep version= version.properties | sed 's/version=//g'",returnStdout: true).trim()
                        echo "Artifact Version is : ${ARTIFACT_VERSION}"
                    }
                }
            }
        }
        
        stage('DOWNLOAD_ARTIFACT'){
	        steps {
	            script {
                    def downloadSpec = """{
                        "files": [
                            {
                                "pattern": "${ARTIFACTORY_REPO}/${ART_NAME}/${ART_NAME}-${ARTIFACT_VERSION}*.zip",
                                "target": "./"
                            }
                        ]
                    }"""
                    def server = Artifactory.server(ARTIFACTORY_SERVER)
                    server.download(downloadSpec)
                }
            }
        }

        stage('DEPLOY_REST_PUBLISHER') {
            steps {
                dir("${ART_NAME}") {
                    script {
                        withCredentials([usernameColonPassword(credentialsId: 'ProdPubCurlPass', variable: 'PUBPASS')]){
                            def files = readFile('./publisherip.propertise').readLines()
                            for (int i = 1; i <= files.size(); i++) {
                                def PUB = "PUB$i"
                                if ("${PUB}" == "${params.SKIPPUBLISHER}") {
                                    echo "Already Deployed on ${PUB}"
                                } else {
                                    def PUBIP = sh (script: "grep $PUB= publisherip.propertise | sed 's/$PUB=//g' | sed 's/\"//g'",returnStdout: true).trim()
                                    echo "---------------Installing on ${PUB}----------------------------"
                                    sh "curl -u '$PUBPASS' -F file=@\"${ARTIFACT_NAME}\" -F force=true -F install=true http://${PUBIP}:${PUBLISHER_PORT}/crx/packmgr/service.jsp"
                                    sleep(time:150,unit:"SECONDS")
                                    timeout(time: 120, unit: 'SECONDS') {
                                        waitUntil {
                                            def response = httpRequest httpMode: 'GET',
                                            ignoreSslErrors: 'true',
                                            url: "http://${PUBIP}:${PUBLISHER_PORT}/${params.URL_VAL}",
                                            validResponseCodes: '200'
                                            echo 'Status: '+response.status
                                            def status = +response.status
                                            return (status == 200);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('CACHE_CLEAR_REST_DISPATCHER'){
            steps {
                script {
                    ansiColor('xterm') {
                        ansiblePlaybook( 
                            playbook: 'cache_clear.yaml',
                            inventory: "${ART_NAME}/inventory.ini",
                            limit: 'RestDispstcher',
                            credentialsId: 'ansibleDeploy',
                            disableHostKeyChecking: true,
                            colorized: true 
                        )
                    }
                }
            }
        }

        stage('AKAMAI_CLEAR_CLEAR') {
            steps{
                script {
                    sh "akamai purge --section prod invalidate --cpcode ${DONUTS_CP_CODE} ${NATION_CP_CODE}"
                }
            }
        }

        stage('START_HTTPD_SERVICE_REST_DISPATCHER') {
            steps {
                script {
                    ansiColor('xterm') {
	                    ansiblePlaybook( 
                            playbook: 'service.yaml',
                            inventory: "${ART_NAME}/inventory.ini", 
                            credentialsId: 'ansibleDeploy',
                            disableHostKeyChecking: true,
                            limit: 'RestDispstcher',
                            extras: '-e status="start"',
                            colorized: true
                        )
                    }
                }
            }
        }
    }
}
