#!/bin/bash
set -e

USER=root
PASSWORD="${HOST_PASSWORD}"
HOST=atvts2830.athtem.eei.ericsson.se
SCRIPT_DIR=$(dirname $0)
TDM_PATH=/var/tdm

echo "# Find latest TDM version"
TDM_VERSION=`$M2_HOME/bin/mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec`

if [ -z "${HOST_PASSWORD}" ]; then
  echo "# HOST_PASSWORD was not provided for accessing ${HOST}"
  exit 1
fi

if ! ssh-keygen -F ${HOST} > /dev/null; then
    echo "adding keys of ${HOST} to .ssh/known_hosts"
    ssh-keyscan -H ${HOST} >> ~/.ssh/known_hosts
fi

echo "# Create '${TDM_PATH}' scripts directory on target environment at ${HOST}"
sshpass -v -p "${PASSWORD}" \
    ssh ${USER}@${HOST} mkdir -p ${TDM_PATH}

echo "# Uploading deployment artifacts"
sshpass -p "${PASSWORD}" \
    scp -p ${SCRIPT_DIR}/run.sh ${SCRIPT_DIR}/docker-compose.yml ${USER}@${HOST}:${TDM_PATH}

echo "# Running deployment commands"
sshpass -p "${PASSWORD}" \
    ssh ${USER}@${HOST} /bin/bash << EOF

  export COMPOSE_FILE=${TDM_PATH}/docker-compose.yml
  export TDM_VERSION=${TDM_VERSION}
  ${TDM_PATH}/run.sh

EOF
