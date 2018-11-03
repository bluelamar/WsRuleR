#!/bin/bash

ENTITY=$1
ID=$2

# {"id":"46e30597","name":"db-1"}

curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}

