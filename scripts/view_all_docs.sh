#!/bin/bash

curl -c cdbcookies -H "Accept: application/json" -H "Content-Type: application/x-www-form-urlencoded"  http://localhost:5984/_session -X POST -d "name=wsruler&password=oneringtorule"

echo "GET all db docs:"
curl -v --cookie "cdbcookies" http://localhost:5984/wsdb/_all_docs

echo "GET all env docs:"
curl -v --cookie "cdbcookies" http://localhost:5984/wsenv/_all_docs

echo "GET all repo docs:"
curl -v --cookie "cdbcookies" http://localhost:5984/wsrepo/_all_docs

echo "GET all ws docs:"
curl -v --cookie "cdbcookies" http://localhost:5984/ws/_all_docs

echo "GET all disrsvc_groups":
curl -v --cookie "cdbcookies" http://localhost:5984/dirsvc_groups/_all_docs

echo "GET all disrsvc_owners":
curl -v --cookie "cdbcookies" http://localhost:5984/dirsvc_owners/_all_docs

echo "GET all disrsvc_links":
curl -v --cookie "cdbcookies" http://localhost:5984/dirsvc_links/_all_docs

