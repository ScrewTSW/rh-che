pipeline {
    agent { label 'osioche-crw-jenkins-node' }
    environment {
        USERNAME = ""
        PASSWORD = ""
    }
    stages {
        stage ("Prepairing environment") {
            steps {
                withCredentials([usernameColonPassword(credentialsId: '${MOUNT_VOLUME_ACCOUNT_CREDENTIALS_ID}', variable: 'USERPASS')]) {
                    script {
                        USERNAME = USERPASS.split(":")[0]
                        echo "$USERNAME"
                        PASSWORD = USERPASS.split(":")[1]
                    }
                }
            }
        }
        stage ("Running volume mount job") {
            steps {
                dir (".ci/mount-volume") {
                    sh """
                    ./run_test.sh -u ${USERNAME} -p ${PASSWORD} \
                                  -r https://api.starter-us-east-2a.openshift.com:443 \
                                  -v empty-volume -z mount_volume- -t 120 -i 5 
                    """
                }
            }
        }
    }
    
}