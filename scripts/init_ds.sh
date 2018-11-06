#!/bin/bash 

# run this script once after first creation of the DB

# init 3 dbs for the DS service
# dirsvc_groups : contains the owner-group objects
# dirsvc_links : links for owner to group
# dirsvc_owners : owner objects

# get a session cookie
curl -c cdbcookies -H "Accept: application/json" -H "Content-Type: application/x-www-form-urlencoded"  http://localhost:5984/_session -X POST -d "name=wsruler&password=oneringtorule"

# create the db's
curl -v --cookie "cdbcookies" http://localhost:5984/dirsvc_groups -X PUT

curl -v --cookie "cdbcookies" http://localhost:5984/dirsvc_links -X PUT

curl -v --cookie "cdbcookies" http://localhost:5984/dirsvc_owners -X PUT

# populate with our dummy data

# group: name

# Creates a new document with generated ID if _id is not specified:
curl -H "Content-Type: application/json" http://localhost:5984/dirsvc_groups -X POST -d '{"_id":"admins","name":"admins"}'

curl -H "Content-Type: application/json" http://localhost:5984/dirsvc_groups -X POST -d '{"_id":"users","name":"users"}'

# owner: name + enail_address
curl -H "Content-Type: application/json" http://localhost:5984/dirsvc_owners -X POST -d '{"_id":"stan","name":"stan","email_address":"stan@marvel.com"}'

curl -H "Content-Type: application/json" http://localhost:5984/dirsvc_owners -X POST -d '{"_id":"pie","name":"magnum","email_address":"magnum@hawaii.com"}'

curl -H "Content-Type: application/json" http://localhost:5984/dirsvc_owners -X POST -d '{"_id":"cash","name":"johny","email_address":"music@nashville.com"}'

# link: parent + data_link
curl -H "Content-Type: application/json" http://localhost:5984/dirsvc_links -X POST -d '{"_id":"hawaii","parent":"users","data_link":"pie"}'

curl -H "Content-Type: application/json" http://localhost:5984/dirsvc_links -X POST -d '{"_id":"nashville","parent":"users","data_link":"cash"}'

curl -H "Content-Type: application/json" http://localhost:5984/dirsvc_links -X POST -d '{"_id":"comics","parent":"admins","data_link":"stan"}'

