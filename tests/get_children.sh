#!/bin/bash

ENTITY=$1
ID=$2

curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/children/${ID}

