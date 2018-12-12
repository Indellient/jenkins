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
        habitat(task: 'build', directory: '.', origin: '${HAB_ORIGIN}')
      }
    }
    /* stage('upload') {
      steps {
        withCredentials(bindings: [string(credentialsId: 'jmery-depot-token', variable: 'HAB_AUTH_TOKEN')]) {
          habitat(task: 'upload', lastBuildFile: '${workspace}/results/last_build.env', bldrUrl: '${HAB_BLDR_URL}', authToken: '${HAB_AUTH_TOKEN}')
        }

      }
    } */
  }
  environment {
    HAB_NOCOLORING = 'true'
    HAB_BLDR_URL = 'https://bldr.habitat.sh/'
    HAB_ORIGIN = 'jmery'
  }
}