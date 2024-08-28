#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/local_ruleset.yaml"
def ci_ruleset = "ci/common_ruleset2.0.yaml"

try {
    stage('Helm Deps Install') {
        sh "${bob} -r ${ruleset} helm-install-deps"
    }
} catch (e) {
    throw e
}