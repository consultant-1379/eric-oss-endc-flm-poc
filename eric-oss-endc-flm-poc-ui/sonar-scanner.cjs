require('dotenv').config();
const scanner = require('sonarqube-scanner');

scanner(
  {
    serverUrl: process.env.SONAR_HOST_URL,
    token: process.env.SONAR_AUTH_TOKEN,
    options: {
      'sonar.login': process.env.SONAR_AUTH_TOKEN,
      'sonar.projectKey': process.env.SONAR_PROJECT_KEY || 'com.ericsson.oss.predev:eric-oss-endc-flm-poc-ui',
      'sonar.projectName': process.env.SONAR_PROJECT_NAME || 'eric-oss-endc-flm-poc-ui',
      'sonar.projectVersion': process.env.SONAR_PROJECT_VERSION || '1.0',
      'sonar.sources': process.env.SONAR_SOURCES || 'src',
      'sonar.tests': process.env.SONAR_SOURCES || 'test',
      'sonar.language': process.env.SONAR_LANGUAGE || 'js',
      'sonar.sourceEncoding': process.env.SONAR_SOURCE_ENCODING || 'UTF-8',
    },
  },
  () => process.exit()
);