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
        string(name: 'CODE_BRANCH', defaultValue: '', description: 'Branch Name')
        choice(name: 'PUBLISHER', choices: ['PUB1', 'PUB2', 'PUB3', 'PUB4'], description: 'Deploy On')
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
        
        stage('BUILD') {
            steps {
                dir('codebase') {
                    script {
                        sh 'ls -lrth'
                        sh 'mvn clean install -P dunkinProfile'
                        def uploadSpec = """{
                            "files": [
                                {
                                    "pattern": "${ART_NAME}/target/${ART_NAME}*.zip",
                                    "target": "${ARTIFACTORY_REPO}/${ART_NAME}/"
                                }
                            ]
                        }"""
                        def server = Artifactory.server(ARTIFACTORY_SERVER)
                        server.upload(uploadSpec)
                        ARTIFACT_NAME = sh (returnStdout: true, script: "ls ${ART_NAME}/target | grep \"${ART_NAME}*\"").trim()
                        ARTIFACT_VERSION = sh (returnStdout: true, script: "echo '${ARTIFACT_NAME}' | awk -F '-' '{print \$3}'").trim()
                        echo "Artifact Name is : ${ARTIFACT_NAME}"
                        echo "Artifact Version is : ${ARTIFACT_VERSION}"
                        writeFile file: 'version.properties', text: "version=${ARTIFACT_VERSION}"
                        def versionSpec = """{
                            "files": [
                                {
                                    "pattern": "./version.properties",
                                    "target": "${ARTIFACTORY_REPO}/${ART_NAME}/"
                                }
                            ]
                        }"""
                        server.upload(versionSpec)
                    }
                }
            }

            post {
                always {
                    dir('codebase') {
                        deleteDir()
                    }
                }
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
        
        stage('GET_PUBLISHER_IP') {
            steps {
                script {
                    def files = readFile('./publisherTag.properties').readLines()
                    for (int i = 1; i <= files.size(); i++) {
                        echo "Number of IP : $i"
                        sh "echo \"PUB$i=\$(aws ec2 describe-instances --filter \"Name=tag:Name,Values=`head -n$i ./publisherTag.properties | tail -1`\"  --region us-east-1  | jq .Reservations[].Instances[].NetworkInterfaces[].PrivateIpAddress)\" >> publisherip.propertise"
                    }
                    sh "cat publisherip.propertise"
                    def publisheripSpec = """{
                        "files": [
                            {
                                "pattern": "./publisherip.propertise",
                                "target": "${ARTIFACTORY_REPO}/${ART_NAME}/"
                            }
                        ]
                    }"""
                    def server = Artifactory.server(ARTIFACTORY_SERVER)
                    server.upload(publisheripSpec)
                    load "publisherip.propertise"
                }
            }
        }
        
        stage('GET_DISP_IP') {
            steps {
                script {
                    def files = readFile('./dispatcherTag.properties').readLines()
                    for (int i = 1; i <= files.size(); i++) {
                        echo "Number of IP : $i"
                        sh "echo \"DISP$i=\$(aws ec2 describe-instances --filter \"Name=tag:Name,Values=`head -n$i ./dispatcherTag.properties | tail -1`\"  --region us-east-1  | jq -r .Reservations[].Instances[].NetworkInterfaces[].PrivateIpAddress)\" >> dispatcherip.propertise"
                    }
                    sh "cat dispatcherip.propertise"
                }
            }
        }
        
        stage('CREATE_INVENTORY') {
            steps {
                script {
                    writeFile file: 'inventory.ini', text: '[Dispatcher]\n[RestDispstcher]\n'
                    def lines = readFile('./publisherip.propertise').readLines()
                    def count = '1';
                    while("$count" <= lines.size()) {
                        def DISPIP = sh (script: "grep DISP$count= dispatcherip.propertise | sed 's/DISP$count=//g' | sed 's/\"//g'",returnStdout: true).trim()
                        if (params.PUBLISHER == "PUB$count") {
                            echo "Same Publisher"
                            sh "sed -i \"/\\[Dispatcher\\]/a$DISPIP\" inventory.ini"
                        } else {
                            sh "echo $DISPIP >> inventory.ini"
                        }
                    count++;
                    }
                    sh "cat inventory.ini"
                    def inventorySpec = """{
                        "files": [
                            {
                                "pattern": "./inventory.ini",
                                "target": "${ARTIFACTORY_REPO}/${ART_NAME}/"
                            }
                        ]
                    }"""
                    def server = Artifactory.server(ARTIFACTORY_SERVER)
                    server.upload(inventorySpec)
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

        stage('HTTP_SERVICE_STOP') {
            ansiColor('xterm') {
                ansiblePlaybook(
                    playbook: 'service.yaml',
                    inventory: 'inventory.ini',
                    limit: 'Dispatcher',
                    credentialsId: 'ansibleDeploy',
                    disableHostKeyChecking: true,
                    extras: '-e status="stop"',
                    colorized: true
                )
            }
        }

        stage('DEPLOY_PUBLISHER') {
            steps {
                dir("${ART_NAME}") {
                    script {
                        withCredentials([usernameColonPassword(credentialsId: 'ProdPubCurlPass', variable: 'PUBPASS')]){                        
                            def PUB = "${params.PUBLISHER}"
                            def PUBIP = sh (script: "grep $PUB= publisherip.propertise | sed 's/$PUB=//g' | sed 's/\"//g'",returnStdout: true).trim()
                            echo "---------------Installing on ${PUBIP}----------------------------"
                            sh "curl -u '$PUBPASS' -F file=@\"${ARTIFACT_NAME}\" -F force=true -F install=true http://${PUBIP}:${PUBLISHER_PORT}/crx/packmgr/service.jsp"
                            sleep(time:120,unit:"SECONDS")
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

        stage('CACHE_CLEAR') {
            ansiColor('xterm') {
                ansiblePlaybook(
                    playbook: 'playbook.yaml',
                    inventory: 'inventory.ini',
                    limit: 'Dispatcher',
                    credentialsId: 'ansibleDeploy',
                    disableHostKeyChecking: true,
                    colorized: true
                )
            }
        }

        stage('AKAMAI_CLEAR') {
            sh "akamai purge --section production invalidate --cpcode ${DONUTS_CP_CODE} ${NATION_CP_CODE}"
        }

        stage('HTTP_SERVICE_START') {
            ansiColor('xterm') {
                ansiblePlaybook(
                    playbook: 'service.yaml',
                    inventory: 'inventory.ini',
                    limit: 'Dispatcher',
                    credentialsId: 'ansibleDeploy',
                    disableHostKeyChecking: true,
                    extras: '-e status="start"',
                    colorized: true
                )
            }
        }

        stage('STOP_HTTPD_SERVICE_REST_DISP') {
            steps {
                script {
                    ansiColor('xterm') {
	                    ansiblePlaybook( 
                            playbook: 'service.yaml',
                            inventory: 'inventory.ini', 
                            credentialsId: 'ansibleDeploy',
                            disableHostKeyChecking: true,
                            limit: 'RestDispstcher',
                            extras: '-e status="stop"',
                            colorized: true
                        )
                    }
                }
            }
        }

        stage('URL_STATUS'){
            steps {
                script {
                   timeout(time: 60, unit: 'SECONDS') {
                        waitUntil {
                            def response = httpRequest httpMode: 'GET',
                            ignoreSslErrors: 'true',
                            url: "${params.URL}",
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