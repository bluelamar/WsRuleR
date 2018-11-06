#!/bin/bash

check_ok_status() {
  RC=$1
  MSG=$2
  if [ $RC -ne 0 ]; then
    echo $MSG
    exit $RC
  fi
}

check_ret_status() {
  EXPECT=$1
  RET=$2
  sleep 1
  echo "$RET" | grep "$EXPECT"
  RC=$?
  if [ $RC -ne 0 ]; then
    echo "Failed: Expected $EXPECT: $RET"
    exit $RC
  fi
}


echo "================="
ENTITY="db"
NAME="database-1"
DATA="{\"name\":\"${NAME}\"}"

echo "Create a database called: $NAME - expect 200"

# ex: {"id":"428fd6ce","name":"database-1"}

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create db component $NAME"

DB1=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$DB1" $RET
echo "Got response: $RET"
echo "-----------------"

echo "$NAME has id: $DB1"

echo "Now lets look at the database we created:"
ID=$DB1
# {"id":"46e30597","name":"db-1"}
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}

# Create another db
echo "-----------------"
echo "================="
NAME="database-2"
DATA="{\"name\":\"${NAME}\"}"
echo "Create another database called: $NAME"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create db component $NAME"
echo "Got response: $RET"
echo "-----------------"

DB2=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$DB2" $RET

echo "$NAME has id: $DB2"

# Create an env
#
echo "-----------------"
echo "================="
ENTITY="env"
NAME="environment-1"
ENV1_NAME=$NAME
DATA="{\"name\":\"${NAME}\"}"

echo "Create an environment called: $NAME"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create env=$NAME"
echo "Got response: $RET"
echo "-----------------"

ENV1=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$ENV1" $RET

echo "$NAME has id: $ENV1"

# Create a link for db1 to env
#
echo "-----------------"
echo "================="
ENTITY="db"
DLINK=$DB1
PARENT=$ENV1
DATA="{\"data_link\":\"${DLINK}\",\"parent\":\"${PARENT}\"}"

# ex: {"id":"5e624995","data_link":"46e30597","parent":"dd52054"}
echo "Create a link between env=$ENV1 and db=$DB1:"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create link db1=$DLINK to env=$PARENT"
echo "Got response: $RET"
echo "-----------------"

DBLINK1=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$DBLINK1" $RET

echo "env->db1 Link has id: $DBLINK1"

# Create a link for db2 to env
#
echo "-----------------"
echo "================="
ENTITY="db"
DLINK=$DB2
PARENT=$ENV1
DATA="{\"data_link\":\"${DLINK}\",\"parent\":\"${PARENT}\"}"

echo "Create a link between env=$ENV1 and db2=$DB2:"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create link db2=$DLINK to env=$PARENT"
echo "Got response: $RET"
echo "-----------------"

DBLINK2=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$DBLINK2" $RET

echo "env->db2 Link has id: $DBLINK2"
echo "Lets get $DBLINK2:"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY}/${DBLINK2}

# Get children of env
#
echo "-----------------"
echo "================="
ENTITY="env"
ID=$ENV1
echo "Look for the children of the environment $ENV1:"
RET=`curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/children/${ID}`
RC=$?

echo "Children - expect $DB1=database-1 and $DB2=database-2: $RET"
echo "-----------------"
check_ok_status $RC "Failed to get children for env=$ID"
check_ret_status "database-1" "$RET"
check_ret_status "database-2" "$RET"

# Change name of the env
#
echo "-----------------"
echo "================="
NAME="obsolete-env"
DATA="{\"id\":\"$ENV1\",\"name\":\"${NAME}\"}"
echo "Change the name of environment $ENV1=$ENV1_NAME to $NAME:"

RET=`curl -v -X PUT -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ENV1} -d "${DATA}"`
RC=$?

echo "Expect 204: $RET"
echo "-----------------"
check_ok_status $RC "Failed to update env=$ID"

# View our new name for env
#
echo "-----------------"
echo "================="
echo "Lets view the environment $ENV1:"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ENV1}


# Delete env
#
echo "-----------------"
echo "================="
echo "Lets delete the environment $ENV1:"
RET=`curl -v -X DELETE -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}`
RC=$?

check_ok_status $RC "Failed to delete env=$ID"
echo "Delete of env1=$ENV1 - expect 204: $RET"
echo "-----------------"

# Get children of env again
#
echo "-----------------"
echo "================="
echo "Look for the children of the deleted environment $ENV1 again - should be none:"
RET=`curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/children/${ID}`
RC=$?

check_ok_status $RC "Get children for deleted env=$ID"
echo "-----------------"
check_ret_status "\[\]" "$RET"
echo "Children - expect 200 with empty list: $RET"
echo "-----------------"

# Get db1 - should exist
#
echo "-----------------"
echo "================="
ENTITY="db"
ID=$DB1
echo "Get db1=$DB1"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}
RC=$?
check_ok_status $RC "Get db1=$ID"

# Get db2 - should exist
#
echo "-----------------"
echo "================="
ID=$DB2
echo "Get db2=$DB2:"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}
RC=$?
check_ok_status $RC "Get db2=$ID"

# Delete db1
#
echo "-----------------"
echo "================="
ID=$DB1
echo "Delete db1=$DB1:"
RET=`curl -v -X DELETE -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}`
RC=$?

check_ok_status $RC "Failed to delete db1=$ID"
echo "Expect 204: $RET"
echo "-----------------"

# Get db1 - should not exist
#
echo "-----------------"
echo "================="
echo "Get db1=$DB1 - expect 404:"
curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/${ID}
RC=$?
check_ok_status $RC "Failed to delete db1=$ID"
echo "-----------------"

# Create env2
#
echo "-----------------"
echo "================="
ENTITY="env"
NAME="environment-2"
DATA="{\"name\":\"${NAME}\"}"

echo "Create an environment called: $NAME"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create env=$NAME"
echo "Got response: $RET"
echo "-----------------"

ENV2=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$ENV2" "$RET"

echo "$NAME has id: $ENV2"

# Create link for env2->db2
#
echo "-----------------"
echo "================="
ENTITY="db"
DLINK=$DB2
PARENT=$ENV2
DATA="{\"data_link\":\"${DLINK}\",\"parent\":\"${PARENT}\"}"

echo "Create a link between env2=$ENV2 and db2=$DB2:"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create link db2=$DLINK to env2=$PARENT"
echo "Got response: $RET"
echo "-----------------"

DBLINK2=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$DBLINK2" "$RET"

# Get children for env2 - should show db2
#
echo "-----------------"
echo "================="
ENTITY="env"
ID=$ENV2
echo "Look for the children of the environment $ENV2:"
RET=`curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/children/${ID}`
RC=$?

check_ok_status $RC "Failed to get children for env2=$ID"
echo "Children - expect $DB2=database-2: $RET"
echo "-----------------"

check_ret_status "$DB2" "$RET"

# Create repo
#
echo "-----------------"
echo "================="
ENTITY="repo"
NAME="repo-1"
DATA="{\"name\":\"${NAME}\"}"

echo "Create a repo called: $NAME"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create repo=$NAME"
echo "Got response: $RET"
echo "-----------------"

REPO=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$REPO" "$RET"

echo "$NAME has id: $REPO"

# Create work space
#
echo "-----------------"
echo "================="
ENTITY="ws"
NAME="ws-1"
GRPNAMES="[\"admins\",\"users\"]"
DATA="{\"name\":\"${NAME}\",\"groups\":${GRPNAMES}}"

echo "Create a ws called: $NAME"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create ws=$NAME"
echo "Got response: $RET"
echo "-----------------"

WS1=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$WS1" "$RET"

echo "$NAME has id: $WS1"

# Link env to work space
#
echo "-----------------"
echo "================="

ENTITY="env"
DLINK=$ENV2
PARENT=$WS1
DATA="{\"data_link\":\"${DLINK}\",\"parent\":\"${PARENT}\"}"

echo "Create a link between ws=$PARENT and env2=$DLINK:"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create link env2=$DLINK to ws=$PARENT"
echo "Got response: $RET"
echo "-----------------"

ENVLINK2=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$ENVLINK2" "$RET"

# Get children for work space
#
echo "-----------------"
echo "================="

ENTITY="ws"
ID=$WS1
echo "Look for the children of the workspace $WS1:"
RET=`curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/children/${ID}`
RC=$?

check_ok_status $RC "Failed to get children for ws=$ID"
echo "Children - expect $DB2=database-2: $RET"
echo "-----------------"

check_ret_status "$DB2" "$RET"

# Link repo to work space
#
echo "-----------------"
echo "================="

ENTITY="repo"
DLINK=$REPO
PARENT=$WS1
DATA="{\"data_link\":\"${DLINK}\",\"parent\":\"${PARENT}\"}"

echo "Create a link between ws=$PARENT and repo=$DLINK:"

RET=`curl -v -X POST -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/link/${ENTITY} -d "${DATA}"`
RC=$?

check_ok_status $RC "Failed to create link repo=$DLINK to ws=$PARENT"
echo "Got response: $RET"
echo "-----------------"

REPOLINK=`echo $RET | awk -F ":" '{print $2}' | awk -F "," '{print $1}' | sed "s/\"//g"`
check_ret_status "$REPOLINK" "$RET"

# Get children for work space again - should now contain the repo
#
echo "-----------------"
echo "================="

ENTITY="ws"
ID=$WS1
echo "Look for the children of the workspace $WS1:"
RET=`curl -v -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/v1/${ENTITY}/children/${ID}`
RC=$?

echo "Children - expect $REPO=repo-1: $RET"
echo "-----------------"


