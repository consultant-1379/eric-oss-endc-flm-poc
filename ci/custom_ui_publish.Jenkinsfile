#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/ui_local_ruleset.yaml"

stage('UI Publish') {
    script {
        sh "${bob} -r ${ruleset} publish"
    }
}