pipeline {
  agent any
  stages {
    stage('scm') {
      steps {
        git(branch: 'master', url: 'ttps://github.com/jmery/jenkins.git')
      }
    }
  }
  environment {
    HAB_NOCOLORING = 'true'
    HAB_BLDR_URL = '\'https://bldr.habitat.sh/\''
    HAB_ORIGIN = 'jmery'
  }
}