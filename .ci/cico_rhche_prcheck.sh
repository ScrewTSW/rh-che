#!/usr/bin/env bash
# Copyright (c) 2018 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

export PR_CHECK_BUILD="true"
export BASEDIR=$(pwd)
export DEV_CLUSTER_URL=https://devtools-dev.ext.devshift.net:8443/
export OC_VERSION=3.10.85

eval "$(./env-toolkit load -f jenkins-env.json -r \
        ^DEVSHIFT_TAG_LEN$ \
        ^QUAY_ \
        ^KEYCLOAK \
        ^BUILD_NUMBER$ \
        ^JOB_NAME$ \
        ^ghprbPullId$ \
        ^RH_CHE)"

source ./config
# Provides methods:
#   checkAllCreds
#   installDependencies
#   archiveArtifacts
source .ci/prepare_env_utils.sh

echo "Checking credentials:"
checkAllCreds
echo "Installing dependencies:"
#installDependencies
yum install docker --assumeyes
systemctl start docker

export PROJECT_NAMESPACE=prcheck-${RH_PULL_REQUEST_ID}
export DOCKER_IMAGE_TAG="${RH_TAG_DIST_SUFFIX}"-"${RH_PULL_REQUEST_ID}"

mkdir logs

#echo "Running ${JOB_NAME} PR: #${RH_PULL_REQUEST_ID}, build number #${BUILD_NUMBER}"
#.ci/cico_build_deploy_test_rhche.sh

RH_CHE_AUTOMATION_SERVER_DEPLOYMENT_URL="che.prod-preview.openshift.io"

docker run --name functional-tests-dep --privileged \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v /root/payload/logs:/root/logs \
		-e "RHCHE_SCREENSHOTS_DIR=/root/logs/screenshots" \
		-e "RHCHE_ACC_USERNAME=${RH_CHE_AUTOMATION_CHE_PREVIEW_USERNAME}" \
		-e "RHCHE_ACC_PASSWORD=${RH_CHE_AUTOMATION_CHE_PREVIEW_PASSWORD}" \
		-e "RHCHE_ACC_EMAIL=${RH_CHE_AUTOMATION_CHE_PREVIEW_EMAIL}" \
		-e "CHE_OSIO_AUTH_ENDPOINT=https://auth.prod-preview.openshift.io" \
		-e "RHCHE_OPENSHIFT_TOKEN_URL=https://sso.prod-preview.openshift.io/auth/realms/fabric8/broker" \
		-e "RHCHE_HOST_PROTOCOL=https" \
		-e "RHCHE_HOST_URL=$RH_CHE_AUTOMATION_SERVER_DEPLOYMENT_URL" \
		-e "TEST_SUITE=simpleTestSuite.xml" \
		quay.io/openshiftio/rhchestage-rh-che-functional-tests-dep
RESULT=$?

archiveArtifacts

if [[ ${RESULT} == 0 ]]; then
	echo "Tests result: SUCCESS"
	exit 0
else
	echo "Tests result: FAILURE"
	exit 1
fi
