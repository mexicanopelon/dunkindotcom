#!/usr/bin/groovy
import com.google.common.collect.Sets;
import hudson.AbortException;
import hudson.Functions;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jenkins.model.CauseOfInterruption;
import jenkins.model.InterruptedBuildAction;

def SLACK_CHANNEL
def COLOUR_MAP = ['SUCCESS': 'good', 'FAILURE': 'danger', 'UNSTABLE': 'danger', 'ABORTED': 'danger']
def getBuildUser() {
    node {
        wrap([$class: 'BuildUser']) {
            GET_BUILD_USER = sh ( script: 'echo "${BUILD_USER}"', returnStdout: true).trim()
        }
        return GET_BUILD_USER
    }
}
def GetPub(){
    node{
        git branch: "common",
        url: "git@dbuslnxgithub01.dunkinbrands.corp:dunkindonuts/ddcom_CICD.git"
        def props = readJSON file: 'tagsProperties.json', returnPojo: true
        def options = []
        def count = '1';
        props.Environment.get(0)."${params.Environment}".get(0).ServerGroup.get(0).each{ key, value ->
            echo "PUB$count"
            options.add("PUB$count")
        count++
        }
        return options as List
    }
}
pipeline {
    agent any
    options {
        buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '5'))
    }
    parameters {
        choice(name: 'Environment', choices: ['Dev', 'Qa', 'Stag', 'Prod'], description: 'Deploy On')
        choice(name: 'Publisher', choices: GetPub(), description: 'Publisher Deploy On')
        choice(name: 'UpdateBuild', choices: ['False', 'True'], description: 'Update the job')
    }
    environment {
        BUILD_USER = ''
    }
    stages {
        stage('UPDATE_JOB'){
            when {
                expression {
                    params.UpdateBuild == 'True'
                }
            }
            steps{
                script{
                    echo "Build Updated"
                    throw new FlowInterruptedException(Result.SUCCESS)
                }
            }
        }
        stage('CLEAN_WORKSPCE') {
            steps {
                cleanWs()
            }
        }
        stage('GET_DEPLOYMENT_PROP') {
            steps {
                script {
                    echo "${params.Publisher}"
                    BUILD_USER = getBuildUser()
                    echo "${BUILD_USER}"
                }
            }
        }
    }
}