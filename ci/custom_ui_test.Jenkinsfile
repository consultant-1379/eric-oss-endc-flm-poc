#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/local_ruleset.yaml"
def ui_ruleset = "ci/ui_local_ruleset.yaml"

stage('Custom Test') {
     script {
        sh "${bob} -r ${ruleset} test"
    }
}

stage('UI Test') {
     script {
        ansiColor('xterm') {
            sh "${bob} -r ${ui_ruleset} test"
        }
    }
}