#!/bin/bash

ENTITY=$1
NAME=$2
ID=$3
DATA="{\"id\":\"$ID\",\"name\":\"${NAME}\"}"

# {"id":"428fd6ce","name":"database-1"}

RET=`curl -v -X PUT -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID} -d "${DATA}"`

echo "Got response: $RET"

echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g" > /tmp/${ENTITY}_${NAME}

cat /tmp/${ENTITY}_${NAME}

