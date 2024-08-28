#!/bin/sh
set -e

if [ $# -ne 1 ]
then
  echo "This script must be run with exactly one argument, the host to get the version from."
  exit 1
fi
deployment=$1

echo environment is $deployment
version=$(curl -X GET --header 'Accept: application/json' "https://$deployment/api/application" | awk -F "[\"\"]" '{print $4}' | awk 'FNR == 2 {print}')
echo version deployed is $version
tag=ERICtaf_tdm-$version
echo tag is $tag
git checkout $tag
