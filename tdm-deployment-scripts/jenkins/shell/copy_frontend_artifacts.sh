#!/bin/sh
set -e
echo "Copy front end to the right 'tdm-ui-client/dist' directory"

mkdir -p tdm-ui-client/dist
mv copied/tdm-ui-client/dist/* tdm-ui-client/dist
