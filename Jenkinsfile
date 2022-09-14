def performRelease = false
def gradleOpts = "-s --build-cache -PlocalNexus=https://nexus.fincherhome.com/nexus/content/groups/public"

pipeline {
  agent any

  parameters {
    string(defaultValue: '', description: 'Extra Gradle Options', name: 'extraGradleOpts')
    booleanParam(name: 'majorRelease', defaultValue: false, description: 'Perform a major release')
    booleanParam(name: 'minorRelease', defaultValue: false, description: 'Perform a minor release')
    booleanParam(name: 'patchRelease', defaultValue: false, description: 'Perform a patch release')
    booleanParam(name: 'publish', defaultValue: false, description: 'Publish to nexus')

  }

  tools {
    jdk 'jdk17'
  }
  
  stages {
    stage('Prepare') {
      steps {
        script {
          
          def releaseOptionCount = 0;
          def prepareReleaseOptions = "";
          
          if (params.majorRelease) {
            performRelease = true
            prepareReleaseOptions = "--releaseType MAJOR"
            releaseOptionCount++
          }
          if (params.minorRelease) {
            performRelease = true
            prepareReleaseOptions = "--releaseType MINOR"
            releaseOptionCount++
          }
          if (params.patchRelease) {
            performRelease = true
            prepareReleaseOptions = "--releaseType PATCH"
            releaseOptionCount++
          }

          if (releaseOptionCount > 1) {
            error("Only one of major, minor, or patch release options can be selected")
          }                         
  
          if (!params.extraGradleOpts.isEmpty()) {
            gradleOpts = gradleOpts + " " + extraGradleOpts
          }

          sh "git config --global user.email 'brian@fincherhome.com' && git config --global user.name 'Brian Fincher'"
          
          if (performRelease) {
            sh './gradlew prepareRelease ' + prepareReleaseOptions + ' ' + gradleOpts 
          }
          
        }
      }
    }
		
    stage('Build') {
      steps {
        sh './gradlew clean build ' + gradleOpts     
      }
    }
    
    stage('Finalize') {
      when { expression { performRelease || params.publish } }
      steps {
        script {
          
          if (performRelease || params.publish ) {
            def publishParams = '-PpublishUsername=${publishUsername} -PpublishPassword=${publishPassword}'
            publishParams += ' -PpublishSnapshotUrl=https://nexus.fincherhome.com/nexus/content/repositories/snapshots'
            publishParams += ' -PpublishReleaseUrl=https://nexus.fincherhome.com/nexus/content/repositories/releases'
            withCredentials([usernamePassword(credentialsId: 'nexus.fincherhome.com', usernameVariable: 'publishUsername', passwordVariable: 'publishPassword')]) {
              sh "./gradlew publish  ${publishParams} -s --build-cache -PlocalNexus=https://nexus.fincherhome.com/nexus/content/groups/public"
            }
          }

          if (performRelease) {
            withCredentials([sshUserPrivateKey(credentialsId: "bfincher_git_private_key", keyFileVariable: 'keyfile')]) {
			  sh './gradlew finalizeRelease -PsshKeyFile=${keyfile} ' + gradleOpts
            }
          }
          
        }
      }
    }
  }
}

