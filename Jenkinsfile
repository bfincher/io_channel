def performRelease = false
def gradleOpts = "-s --build-cache -PlocalNexus=http://nexus3:8081/repository/public"
def buildCacheDir = ""

pipeline {
  agent { label "gradle-8.10-jdk11" }

  parameters {
    string(defaultValue: '', description: 'Extra Gradle Options', name: 'extraGradleOpts')
    booleanParam(name: 'majorRelease', defaultValue: false, description: 'Perform a major release')
    booleanParam(name: 'minorRelease', defaultValue: false, description: 'Perform a minor release')
    booleanParam(name: 'patchRelease', defaultValue: false, description: 'Perform a patch release')
    booleanParam(name: 'publish', defaultValue: false, description: 'Publish to nexus')
    string(name: 'baseBuildCacheDir', defaultValue: '/cache', description: 'Base build cache dir')
    string(name: 'buildCacheName', defaultValue: 'default', description: 'Build cache name')

  }
  
  
  stages {
    stage('Prepare') {
      steps {
        script {
          
          sh "wget http://nexus3:8081/repository/public/com/fincher/gradle-cache/0.0.1/gradle-cache-0.0.1.tgz -O /tmp/gradle-cache.tgz"
          sh "tar -zxf /tmp/gradle-cache.tgz --directory /tmp"

          buildCacheDir = sh(
              script: "/tmp/getBuildCache ${params.baseBuildCacheDir} ${params.buildCacheName}",
              returnStdout: true).trim()
              
          gradleOpts = gradleOpts + " --gradle-user-home " + buildCacheDir
          
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
            sh 'gradle prepareRelease ' + prepareReleaseOptions + ' ' + gradleOpts 
          }
          
        }
      }
    }
		
    stage('Build') {
      steps {
        sh 'gradle clean build ' + gradleOpts
      }
    }
    
    stage('Finalize') {
      when { expression { performRelease || params.publish } }
      steps {
        script {
          
          if (performRelease || params.publish ) {
            def publishParams = '-PpublishUsername=${publishUsername} -PpublishPassword=${publishPassword}'
            publishParams += ' -PpublishSnapshotUrl=http://nexus3:8081/repository/snapshots'
            publishParams += ' -PpublishReleaseUrl=http://nexus3:8081/repository/releases'
            withCredentials([usernamePassword(credentialsId: 'nexus.fincherhome.com', usernameVariable: 'publishUsername', passwordVariable: 'publishPassword')]) {
              sh "gradle publish  ${publishParams} ${gradleOpts}"
            }
          }

          if (performRelease) {
            withCredentials([sshUserPrivateKey(credentialsId: "bfincher_git_private_key", keyFileVariable: 'keyfile')]) {
			  sh 'gradle finalizeRelease -PsshKeyFile=${keyfile} ' + gradleOpts
            }
          }
          
        }
      }
    }
  }
  
  post {
    always {
      sh("/tmp/releaseBuildCache ${buildCacheDir}")
    }
  }
}

