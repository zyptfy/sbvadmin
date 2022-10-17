pipeline {
    /*
     *agent {
     *   docker {
     *       image 'maven:3.8.1-adoptopenjdk-11'
     *       args '-v /root/.m2:/root/.m2'
     *  }
     *}
     */
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Restart') {
            steps {
                sh 'chown -R tomcat:tomcat /root/.jenkins/workspace/sbvadmin/adminserver/admin-web/target'
                sh 'systemctl restart sbvadmin'
            }
        }
    }
}