#!/bin/bash
set -e

if [ $# -ne 2 ]
then
  echo "This script must be run with exactly two arguments, the host to deploy tdm on and the version of TDM to deploy."
  echo "example: ./deploy.sh ERICtaf_tdm-10.0.73 taftdm.lmera.ericsson.se"
  exit 1
fi

VERSION=$1
USER=tafuser
PASSWORD="${tafuser_password}"
HOST=$2
SCRIPT_DIR=$(dirname $0)
TDM_PATH=/home/tafuser/tmp/tdm
SSHPASS=/proj/PDU_OSS_CI_TAF/tools/sshpass-1.05/sshpass

TDM_VERSION=${VERSION/*-/}

echo "# Deploy TDM v.${TDM_VERSION}"

if [ -z "${tafuser_password}" ]; then
  echo "# tafuser_password was not provided for accessing ${HOST}"
  exit 1
fi

if ! ssh-keygen -F ${HOST} > /dev/null; then
    echo "adding keys of ${HOST} to .ssh/known_hosts"
    ssh-keyscan -H ${HOST} >> ~/.ssh/known_hosts
fi

echo "# Fix to get cAdvisor up and running (https://github.com/google/cadvisor/issues/1444)"
${SSHPASS} -p "${PASSWORD}" ssh ${USER}@${HOST} \
    "echo ${PASSWORD} | sudo -S -s /bin/bash -c 'sudo mount -o remount,rw \"/sys/fs/cgroup\"'"

if ${SSHPASS} -p "${PASSWORD}" ssh ${USER}@${HOST} \
    "echo ${PASSWORD} | sudo -S -s /bin/bash -c 'sudo ln -s /sys/fs/cgroup/cpu,cpuacct /sys/fs/cgroup/cpuacct,cpu'"; then
    echo "# symbolic link created"
else
    echo "# symbolic link command failed. It is presumed that the link already exists"
fi

echo "# Create '${TDM_PATH}' scripts directory on target environment at ${HOST}"
${SSHPASS} -p "${PASSWORD}" ssh ${USER}@${HOST} sudo mkdir -p ${TDM_PATH}

sed -i "s/\${TDM_VERSION}/${TDM_VERSION}/g" ${SCRIPT_DIR}/docker-compose.yml

echo "# Uploading deployment artifacts"
${SSHPASS} -p "${PASSWORD}" \
    scp -p ${SCRIPT_DIR}/run.sh ${SCRIPT_DIR}/docker-compose.yml ${USER}@${HOST}:${TDM_PATH}

echo "# Running deployment commands"
${SSHPASS} -p "${PASSWORD}" \
    ssh ${USER}@${HOST} /bin/bash << EOF

  export TDM_VERSION=${TDM_VERSION}
  export COMPOSE_FILE=${TDM_PATH}/docker-compose.yml
  cd ${TDM_PATH}
  ${TDM_PATH}/run.sh

EOF
