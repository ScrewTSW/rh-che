#!/bin/bash
echo "Starting chromedriver"
nohup chromedriver &
echo "Running Xvfb"
nohup /usr/bin/Xvfb :99 -screen 0 1920x1080x24 +extension RANDR > /dev/null 2>&1 &
echo "Preparing environment"

export CHE_INFRASTRUCTURE=openshift
export CHE_MULTIUSER=true
export RHCHE_SCREENSHOTS_DIR=${RHCHE_SCREENSHOTS_DIR:-"/home/fabric8/rh-che/functional-tests/target/screenshots"}
export RHCHE_OFFLINE_ACCESS_EXCHANGE=${RHCHE_OFFLINE_ACCESS_EXCHANGE:-"https://auth.openshift.io/api/token/refresh"}
export RHCHE_GITHUB_EXCHANGE=${RHCHE_GITHUB_EXCHANGE:-"https://auth.openshift.io/api/token?for=https://github.com"}
export RHCHE_OPENSHIFT_TOKEN_URL=${RHCHE_OPENSHIFT_TOKEN_URL:-"https://sso.openshift.io/auth/realms/fabric8/broker/openshift-v3/token"}
export RHCHE_HOST_URL=${RHCHE_HOST_URL:-"che.openshift.io"}
export RHCHE_HOST_PROTOCOL=${RHCHE_HOST_PROTOCOL:-"https"}
export RHCHE_HOST_FULL_URL="${RHCHE_HOST_PROTOCOL}://${RHCHE_HOST_URL}/"
export RHCHE_EXCLUDED_GROUPS=${RHCHE_EXCLUDED_GROUPS:-"github"}

docker network create --attachable -d bridge localnetwork
docker network connect localnetwork functional-tests-dep

if [[ "${RUN_LOCAL_CODE}" != "true" ]]; then
  echo "Running functional-tests from embedded sources."
  cd /home/fabric8/rh-che/
  else
  echo "Running functional-tests mounted to $(pwd)"
  cd /home/fabric8/che/
fi

echo "Running che-starter against ${RHCHE_HOST_FULL_URL}"

docker run -d -p 10000:10000 --name che-starter --network localnetwork \
  -e "GITHUB_TOKEN_URL=${RHCHE_GITHUB_EXCHANGE}" \
  -e "OPENSHIFT_TOKEN_URL=${RHCHE_OPENSHIFT_TOKEN_URL}" \
  -e "CHE_SERVER_URL=${RHCHE_HOST_FULL_URL}" \
  quay.io/openshiftio/almighty-che-starter:latest

echo "Running tests"

scl enable rh-maven33 rh-nodejs8 "mvn clean --projects functional-tests -Pfunctional-tests -B \
  -Dche.testuser.name=${RHCHE_ACC_USERNAME} \
  -Dche.testuser.email=${RHCHE_ACC_EMAIL} \
  -Dche.testuser.offline_token=${RHCHE_ACC_TOKEN} \
  -Dche.testuser.password=${RHCHE_ACC_PASSWORD} \
  -Dche.host=${RHCHE_HOST_URL} \
  -Dche.offline.to.access.token.exchange.endpoint=${RHCHE_OFFLINE_ACCESS_EXCHANGE} \
  -DexcludedGroups=${RHCHE_EXCLUDED_GROUPS} \
  -DcheStarterUrl=http://che-starter.localnetwork:10000 \
  -Dtests.screenshots_dir=${RHCHE_SCREENSHOTS_DIR} \
  test install"

echo "Grabbing che-starter logs"

docker logs che-starter > /home/fabric8/logs/che-starter.log
