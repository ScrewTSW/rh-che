#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

set -x
set -e
set +o nounset

/usr/sbin/setenforce 0

yum install --assumeyes git

#export CUSTOM_CHE_SERVER_FULL_URL=${RH_CHE_AUTOMATION_SERVER_DEPLOYMENT_URL}
#export OSIO_USERNAME=${RH_CHE_AUTOMATION_CHE_PREVIEW_USERNAME}
#export OSIO_PASSWORD=${RH_CHE_AUTOMATION_CHE_PREVIEW_PASSWORD}
#echo "OSIO_USERNAME=${OSIO_USERNAME}" >> ./jenkins-env
#echo "OSIO_PASSWORD=${OSIO_PASSWORD}" >> ./jenkins-env
#echo "CUSTOM_CHE_SERVER_FULL_URL=${CUSTOM_CHE_SERVER_FULL_URL}" >> ./jenkins-env

#echo "Downloading che-functional-tests repo"

#git -c http.sslVerify=false clone https://github.com/redhat-developer/che-functional-tests.git
#cp ./jenkins-env ./che-functional-tests/jenkins-env
#mv ./artifacts.key ./che-functional-tests/artifacts.key
#cd ./che-functional-tests

#echo "Downloading done."
echo "Running functional tests against ${CUSTOM_CHE_SERVER_FULL_URL}"

#DO_NOT_REBASE=true ./cico/cico_run_EE_tests.sh ./cico/config_rh_che_automated

mkdir /rhche-logs/
# TODO: Add account email into vault, change almighty job
# TODO: Split RH_CHE_AUTOMATION_SERVER_DEPLOYMENT_URL into PROTOCOL and HOST_URL
# -e "RHCHE_ACC_EMAIL=<email>" \
docker run --name functional-tests-dep --privileged \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v /rhche-logs:/root/logs \
           -e "RHCHE_SCREENSHOTS_DIR=/root/logs/screenshots" \
           -e "RHCHE_ACC_USERNAME=${RH_CHE_AUTOMATION_CHE_PREVIEW_USERNAME}" \
           -e "RHCHE_ACC_PASSWORD=${RH_CHE_AUTOMATION_CHE_PREVIEW_PASSWORD}" \
           -e "CHE_OSIO_AUTH_ENDPOINT=https://auth.prod-preview.openshift.io" \
           -e "RHCHE_HOST_PROTOCOL=https" \
           -e "RHCHE_HOST_URL=che.prod-preview.openshift.io" \
           quay.io/openshiftio/rhchestage-rh-che-functional-tests-dep