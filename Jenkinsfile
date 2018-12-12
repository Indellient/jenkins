pipeline {
  agent any
  stages {
    stage('scm') {
      steps {
        git(branch: 'master', url: 'https://github.com/jmery/jenkins.git')
      }
    }
    stage('build') {
      steps {
        habitat(task: 'build', directory: '.', origin: '${env.HAB_ORIGIN}')
      }
    }
  }
  environment {
    HAB_NOCOLORING = 'true'
    HAB_BLDR_URL = 'https://bldr.habitat.sh/'
    HAB_ORIGIN = 'jmery'
  }
}