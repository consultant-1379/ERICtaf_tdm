#!/bin/sh
set -e
check() {
    echo "verify $1"
    CODE=$(curl -sL -w "%{http_code}\\n" "$1" -o /dev/null)
    if test $CODE -ne 200; then
        exit 1
    fi
}

check "http://atvts3406.athtem.eei.ericsson.se:8888/"
check "http://atvts3406.athtem.eei.ericsson.se:8888/api/application"
