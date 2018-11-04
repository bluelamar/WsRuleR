#!/bin/bash

ENTITY=$1
#NAME=$2

#curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/match/${ENTITY}/${NAME}

curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/all/$ENTITY

