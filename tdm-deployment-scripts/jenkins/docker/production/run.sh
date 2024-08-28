#!/bin/bash
set -e

if [ $(sudo /usr/local/bin/docker-compose ps -q | wc -l) != 0 ]
then
    echo "# Stopping TDM services"
    sudo /usr/local/bin/docker-compose down
fi

echo tdm version ${TDM_VERSION}

echo "# Pulling latest cAdvisor service image"
sudo /usr/local/bin/docker-compose pull cAdvisor

no_tag=$(sudo docker images -qf "dangling=true");
if [ -n "$no_tag" ]; then
    echo "# Removing old TDM service images"
    echo "$no_tag"
    sudo docker rmi ${no_tag}
fi

echo "# Starting up TDM services"
echo running this compose file ${COMPOSE_FILE}
sudo /usr/local/bin/docker-compose up -d

