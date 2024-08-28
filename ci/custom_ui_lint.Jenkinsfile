#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/ui_local_ruleset.yaml"

stage('UI Lint') {
    script {
        sh "${bob} -r ${ruleset} lint"
    }
}