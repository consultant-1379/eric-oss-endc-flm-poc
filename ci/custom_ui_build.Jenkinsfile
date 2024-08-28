#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/ui_local_ruleset.yaml"

stage('UI Build') {
    script {
        sh "${bob} -r ${ruleset} build"
    }
}