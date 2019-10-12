pipeline {

	agent any
	
	stages {
		stage('Build') {
			steps {
				sh 'gradle --no-daemon clean build -x checkstyleMain -x checkstyleTest'
			}
		}
		
		stage('Checkstyle') {
		    steps {
		        sh 'gradle --no-daemon checkstyleMain checkstyleTest'
		    }
		}
		
		stage('Publish') {
		    steps {
		        sh 'gradle --no-daemon publishToMavenLocal'
		    }
		}
	}
}
