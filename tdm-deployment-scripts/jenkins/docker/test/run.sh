#!/bin/bash
set -e

if [ $(docker-compose ps -q | wc -l) != 0 ]; then
    echo "# Stopping all TDM containers"
    docker-compose down
fi

echo "# Pulling $TDM_VERSION TDM image"
docker-compose pull TDM_backend

no_tag=$(docker images -qf "dangling=true");
if [ -n "$no_tag" ]; then
    echo "# Removing old TDM images"
    echo "$no_tag"
    docker rmi ${no_tag}
fi

echo "# Starting up TDM service v.$TDM_VERSION"
docker-compose up -d

echo "# Status of Docker containers:"
docker ps --format "{{.Names}}: {{.Status}}"
