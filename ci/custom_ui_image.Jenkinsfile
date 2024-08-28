#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/ui_local_ruleset.yaml"

stage('UI Image') {
    script {
        sh "${bob} -r ${ruleset} image"
        sh "${bob} -r ${ruleset} image-dr-check"
    }
}