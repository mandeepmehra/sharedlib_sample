def call(Map config  ){

  pipeline{
    agent any
    options{
         buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    environment {
        QA_PORT               = "${config.port_QA}"
        PROD_PORT             = "${config.port_Prod}"
        APP_NAME              = "${config.appName}"
        SRC_DIR               = "${config.srcBaseDir}"
        HELM_CHART            ="${config.helmChartName}"
        HELM_CHART_VER        = "${env.BUILD_NUMBER}"
        DTR_URL               = "${config.dockerRepoName}"
	      DTR_CREDS             = 'dockerhub'
        DTR_NAMESPACE         = 'ge-poc'
        IMAGE_NAME            = "${config.dockerImageName}"
        BUILD_NUMBER          = "${env.BUILD_NUMBER}"
        IMAGE_TAG             = "${DTR_URL}/${IMAGE_NAME}:${BUILD_NUMBER}"
	      DOCKER_IMAGE          = ''
        SKIP_INTEGRATION_TEST = "${config.skipIntegrationTest}"
        ARTIFACTORY_USER      = ''
        ARTIFACTORY_PASS      = ''
    }
    stages{
        stage ('Print Metadata'){
            steps{
                echo sh(returnStdout: true, script: 'env')
            }
        }
        stage ('Pull dependencies'){
           parallel{
               stage ('npm dependencies') {
                 steps {
                    dir("${SRC_DIR}"){
                      sh 'npm install'
                    }
                 }
               }
               stage ('chart dependencies') {
                 steps {
                 checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'helmcharts']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'GitLabCreds', url: 'https://gitlab.com/xebia-devops/xebia-helmcharts']]]}
               }
           }
        }

        stage ('Verify code quality'){
          parallel{
            stage ('Unit Testing') {
              steps{
                  // download all the necessary packages
                  dir("${SRC_DIR}"){
                    sh 'npm test'
                  }
              }
            }
            stage ('Static Code Analysis'){
              steps{
                dir("${SRC_DIR}"){
                  script {
                    sh "sonar-scanner -Dsonar.host.url=http://sonarqube:9000"
                  }
                }
              }
            }
        
          }
        }

        stage ('Build Docker Image and Package chart'){
          parallel {
            stage ('Build Image'){
              steps{
		            dir("${SRC_DIR}"){
    		          script {
		                DOCKER_IMAGE = docker.build IMAGE_TAG
		              }
                }
             }
            }
            stage ('Create helm package') {
              steps {
                dir("helmcharts"){
                  sh 'helm package ${HELM_CHART} --version=${HELM_CHART_VER} -u --app-version ${BUILD_NUMBER}'
                }
              }
            }  
          }
        }

        stage ('Scan Image'){
           steps{
            scanImageUT "${IMAGE_TAG}", "${SCANNER_CLIENT_IP}", "${SCANNER_HOST}", "${SCANNER_PORT}"
           }
        }

        stage ('Publish Image & Package'){
          parallel {
            stage ('Publish Image'){
              steps{
                script {
                  docker.withRegistry( '', DTR_CREDS ) {
                    DOCKER_IMAGE.push()
                  }
                }
              }
            }
            stage ('Publish helm package') {
              steps {
                dir("helmcharts"){
                  sh 'helm repo update'
             
                  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId:'artifactorycreds', usernameVariable: 'ARTIFACTORY_USER', passwordVariable: 'ARTIFACTORY_PASS']]) {
                    sh "curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_PASS} -T ${HELM_CHART}-${HELM_CHART_VER}.tgz ${ARTIFACTORY_URL}/helm/${HELM_CHART}-${HELM_CHART_VER}.tgz"
                  }
                }
              }
            }
          }
        }

	      stage ('Cleanup Image'){
           steps {
             sh "docker rmi $IMAGE_TAG"
	        }
        }

        stage ('Deploy to QA'){
            steps{
                 sh 'helm repo update'
                 sh "helm upgrade --install ${APP_NAME}-qa  --set buildNumber=$BUILD_NUMBER,image.tag=$BUILD_NUMBER,image.repository=${DTR_URL}/${IMAGE_NAME},service.nodePort=$QA_PORT --namespace qa helm/${APP_NAME} --version ${HELM_CHART_VER}"
            }
        }

        stage ('Run Integration Tests'){
          when {
            expression {
                return SKIP_INTEGRATION_TEST == 'false';
            }
          }
            steps{
                sh 'echo TBD'
            }
        }

        stage ('Deploy to Production'){
          when{
            environment name: 'GIT_BRANCH', value: 'origin/master'
          }
          steps{
                sh 'helm repo update'
                sh "helm upgrade --install ${APP_NAME}-prod  --set buildNumber=$BUILD_NUMBER,image.tag=$BUILD_NUMBER,image.repository=${DTR_URL}/${IMAGE_NAME},service.nodePort=$PROD_PORT --namespace prod helm/${APP_NAME} --version ${HELM_CHART_VER}"
          }
        }

        stage ('Business Validation'){
          when{
            environment name: 'GIT_BRANCH', value: 'origin/master'
          }
          steps{
                script {
                  env.RELEASE_SUCCESS = input message: 'Is Release successful?',
                      parameters: [choice(name: 'Release is a success', choices: 'no\nyes', description: 'Choose "no" if you want to rollback the deployment')]
                }
          }
        }
        stage ('Rollback production'){
          when{
            allOf {
              environment name: 'RELEASE_SUCCESS', value: 'no'
              environment name: 'GIT_BRANCH', value: 'origin/master'
            }
          }
          steps{
                sh 'helm repo update'
                sh "helm rollback ${APP_NAME}-prod 0"
          }
        }
    }

    post {  
         failure {  
             mail bcc: '', body: "<br>Project: ${env.JOB_NAME} <br>Build Number: ${env.BUILD_NUMBER} <br> URL : ${env.BUILD_URL}", cc: '', charset: 'UTF-8', from: '', mimeType: 'text/html', replyTo: '', subject: "ERROR CI: Project name -> ${env.JOB_NAME}", to: "mandeep.mehra@xebia.com";  
         }  
         changed {  
           mail bcc: '', body: "<br>Project: ${env.JOB_NAME} <br>Build Number: ${env.BUILD_NUMBER} <br> URL : ${env.BUILD_URL}", cc: '', charset: 'UTF-8', from: '', mimeType: 'text/html', replyTo: '', subject: "Build Status Changed: Project name -> ${env.JOB_NAME}", to: "mandeep.mehra@xebia.com";      
         }  
     }  

  }
}
