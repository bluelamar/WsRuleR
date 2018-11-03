#!/bin/bash

ENTITY=$1
DLINK=$2
PARENT=$3
DATA="{\"data_link\":\"${DLINK}\",\"parent\":\"${PARENT}\"}"

# {"id":"5e624995","data_link":"46e30597","parent":"dd52054"}

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY} -d "${DATA}"`

echo "Got response: $RET"

echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g" > /tmp/${ENTITY}_${DLINK}_${PARENT}

cat /tmp/${ENTITY}_${DLINK}_${PARENT}

