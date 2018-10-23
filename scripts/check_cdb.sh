#!/bin/bash

echo "Get the root /:"
curl http://localhost:5984/

echo "Get the utils index.html:"
curl http://localhost:5984/_utils/index.html

echo "Get the utils verify install:"
curl http://localhost:5984/_utils/index.html#verifyinstall

AUTHHDR="Authorization:"
#AUTHUSER="wsruler"
AUTHPWD="oneringtorule"
AUTHUSER="admin"
AUTHPWD="mysecretpassword"
#AUTHVAL="Basic " +btoa(username + ":" + password"
AUTHVAL=`echo "${AUTHUSER}:${AUTHPWD}" | base64`
AUTHSTR="Basic ${AUTHVAL}"

# WORKS
curl -v -H "Accept: application/json" -H "Content-Type: application/x-www-form-urlencoded"  http://localhost:5984/_session -X POST -d "name=wsruler&password=oneringtorule"
#POST /_session HTTP/1.1
#Accept: application/json
#Content-Length: 24
#Content-Type: application/x-www-form-urlencoded
#Host: localhost:5984
#
#name=root&password=relax

exit 0

COOKIE="AuthSession=d3NydWxlcjo1QkNFQTZCMjo3Hyf5CvRgjcMLazq6rQMrkksYnw; Version=1; Path=/; HttpOnly"


curl -c cdbcookies -H "Accept: application/json" -H "Content-Type: application/x-www-form-urlencoded"  http://localhost:5984/_session -X POST -d "name=wsruler&password=oneringtorule"

echo "Get all the dbs:"
# ex: []
# curl --cookie "$COOKIE" http://localhost:5984/_all_dbs
curl --cookie "cdbcookies" http://localhost:5984/_all_dbs

echo "Get the list of nodes:"
# Returns a list of nodes
# ex: {"all_nodes":["nonode@nohost"],"cluster_nodes":["nonode@nohost"]}
#curl --cookie "$COOKIE" http://localhost:5984/_membership
curl --cookie "cdbcookies" http://localhost:5984/_membership

#echo "Put a new node:"
#curl --cookie "cdbcookies" -X PUT "http://localhost:5984/_nodes/node2@111.222.333.444" -d {}
 
#echo "Get the list of nodes again:"
#curl --cookie "cdbcookies" http://localhost:5984/_membership

echo "Get the config for node nonode:"
#curl http://localhost:5984/_node/{node-name}/_config	
curl --cookie "cdbcookies" "http://localhost:5984/_node/nonode@nohost/_config"	


curl http://localhost:5984/_uuids

# Health check endpoint
curl http://localhost:5984/_up


exit 0

# Checks the database existence
curl http://localhost:5984/{db} -X HEAD

# Returns the database information
curl http://localhost:5984/{db}

# Creates a new document with generated ID if _id is not specified
curl http://localhost:5984/{db} -X POST

# Creates a new database
curl http://localhost:5984/{db} -X PUT

# Deletes an existing database
curl http://localhost:5984/{db} -X DELETE

curl http://localhost:5984/{db}/_all_docs	

# Returns a built-in view of all documents in this database
curl http://localhost:5984/{db}/_all_docs

curl http://localhost:5984/{db}/_all_docs -X POST

