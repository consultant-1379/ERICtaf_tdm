#!/bin/sh
set -e

if [ $# -ne 1 ]
then
  echo "This script must be run with exactly one argument, the host to health check."
  exit 1
fi
HOST=$1

check() {
    echo "verify $1"
    CODE=$(curl -sL -w "%{http_code}\\n" "$1" -o /dev/null)
    if test $CODE -ne 200; then
        exit 1
    fi
}

check "https://${HOST}/"
check "https://${HOST}/api/application"
