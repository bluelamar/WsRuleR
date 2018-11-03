#!/bin/bash

ENTITY=$1
ID=$2


curl -v -X DELETE -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}

