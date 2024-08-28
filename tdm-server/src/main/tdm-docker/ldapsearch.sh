#!/bin/sh
ldapsearch -LLL -x -h enmteagat -p 389 -b 'ou=People,dc=agat,dc=enm,dc=org' '(objectclass=*)' uid | grep uid: | awk '{print $2}'