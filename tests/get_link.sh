#!/bin/bash

ENTITY=$1
ID=$2

# {"id":"5e624995","data_link":"46e30597","parent":"dd52054"}

curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY}/${ID}

