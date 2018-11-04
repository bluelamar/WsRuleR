#!/bin/bash

ENTITY="db"
NAME="database-1"
DATA="{\"name\":\"${NAME}\"}"

echo "Create a database called: $NAME"

# ex: {"id":"428fd6ce","name":"database-1"}

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

echo "Got response: $RC : $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to create component $NAME"
  exit $RC
fi

DB1=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`

echo "$NAME has id: $DB1"

echo "Now lets look at the database we created:"
ID=$DB1
# {"id":"46e30597","name":"db-1"}
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}

# Create another db
echo "-----------------"
echo "-----------------"
NAME="database-2"
DATA="{\"name\":\"${NAME}\"}"
echo "Create another database called: $NAME"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

echo "Got response: $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to create component $NAME"
  exit $RC
fi

DB2=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`

echo "$NAME has id: $DB2"

# Create an env
#
echo "-----------------"
echo "-----------------"
ENTITY="env"
NAME="environment-1"
DATA="{\"name\":\"${NAME}\"}"

echo "Create an environment called: $NAME"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

echo "Got response: $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to create component env=$NAME"
  exit $RC
fi

ENV1=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`

echo "$NAME has id: $ENV1"

# Create a link for db1 to env
#
echo "-----------------"
echo "-----------------"
ENTITY="db"
DLINK=$DB1
PARENT=$ENV1
DATA="{\"data_link\":\"${DLINK}\",\"parent\":\"${PARENT}\"}"

# ex: {"id":"5e624995","data_link":"46e30597","parent":"dd52054"}
echo "Create a link between env=$ENV1 and db=$DB1:"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY} -d "${DATA}"`
RC=$?

echo "Got response: $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to create link component for parent $PARENT"
  exit $RC
fi

DBLINK1=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`

echo "env->db1 Link has id: $DBLINK1"

# Create a link for db2 to env
#
echo "-----------------"
echo "-----------------"
ENTITY="db"
DLINK=$DB2
PARENT=$ENV1
DATA="{\"data_link\":\"${DLINK}\",\"parent\":\"${PARENT}\"}"

echo "Create a link between env=$ENV1 and db2=$DB2:"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY} -d "${DATA}"`
RC=$?

echo "Got response: $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to create link component for parent $PARENT"
  exit $RC
fi

DBLINK2=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`

echo "env->db2 Link has id: $DBLINK2"
echo "Lets get $DBLINK2:"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY}/${DBLINK2}

# Get children of env
#
echo "-----------------"
echo "-----------------"
ENTITY="env"
ID=$ENV1
echo "Look for the children of the environment $ENV1:"
RET=`curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/children/${ID}`
RC=$?

echo "Children - expect $DB1=database-1 and $DB2=database-2: $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to get child components for parent $ENV1"
  exit $RC
fi

# Delete env
#
echo "-----------------"
echo "-----------------"
echo "Lets delete the environment $ENV1:"
RET=`curl -v -X DELETE -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}`
RC=$?

echo "Delete of env1=$ENV1 - expect 204: $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to delete component for env=$ENV1"
  exit $RC
fi

# Get children of env again
#
echo "-----------------"
echo "-----------------"
echo "Look for the children of the deleted environment $ENV1 again - should be none:"
RET=`curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/children/${ID}`
RC=$?

echo "Children - expect 200 with empty list: $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to determine no child components for env=$ID"
  exit $RC
fi

# Get db1 - should exist
#
echo "-----------------"
echo "-----------------"
ENTITY="db"
ID=$DB1
echo "Get db1=$DB1"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}
RC=$?
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to get component for db1=$ID"
  exit $RC
fi

# Get db2 - should exist
#
echo "-----------------"
echo "-----------------"
ID=$DB2
echo "Get db2=$DB2:"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}
RC=$?
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to get component for db2=$ID"
  exit $RC
fi

# Delete db1
#
echo "-----------------"
echo "-----------------"
ID=$DB1
echo "Delete db1=$DB1:"
RET=`curl -v -X DELETE -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}`
RC=$?

echo "Expect 204: $RET"
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to delete component for db1=$ID"
  exit $RC
fi

# Get db1 - should not exist
#
echo "-----------------"
echo "-----------------"
echo "Get db1=$DB1 - expect 404:"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}
RC=$?
echo "-----------------"
if [ $RC -ne 0 ]; then
  echo "Failed to determine no component for db1=$ID"
  exit $RC
fi

# Create env2
#
echo "-----------------"
echo "-----------------"

# Create link for env2->db2
#
echo "-----------------"
echo "-----------------"

# Get children for env2 - should show db2
#
echo "-----------------"
echo "-----------------"

# Delete link for env2->db2
#
echo "-----------------"
echo "-----------------"

# Get children for env2 - should be no children
#
echo "-----------------"
echo "-----------------"

# Get link for env2-db2 - should be no link
#
echo "-----------------"
echo "-----------------"

# Get db2 - should exist
#
echo "-----------------"
echo "-----------------"

# Get env2 - should exist
#
echo "-----------------"
echo "-----------------"

# Delete db2
#
echo "-----------------"
echo "-----------------"

# Get db2 - should not exist
#
echo "-----------------"
echo "-----------------"

# Delete env2
#
echo "-----------------"
echo "-----------------"

# Get env2 - should not exist
#
echo "-----------------"
echo "-----------------"

