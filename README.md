# WsRuleR
Demo of Workspace website: Workspace Environment, Repository and DB Relationships

## Building

THere are 2 main components to be built. One is the database, and secondly
the server.
The database is CouchDB that runs in a *Docker* container.
The server is an embedded Jetty server that may be built with *Maven*.

To build the database, ensure you have the *prerequisite* of *Docker* on your 
system. Then run the following script:

$ ./scripts/bld_cdb.sh

Building the Docker image only has to be done once.

To build the server, ensure you have the *prerequisite* of *Maven* installed.
Then perform this command from the root of the directory:

$ mvn clean package install

Thats all there is to it.


### Prerequisites

*Docker:*

To install Docker on Mac, you can either use *brew* or follow these directions:
https://docs.docker.com/v17.12/docker-for-mac/install/#what-to-know-before-you-install

If you dont have Homebrew, then do the following:
 From https://brew.sh/

$ /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

$ brew doctor

$ brew analytics off

$ sudo brew install caskroom/cask/brew-cask

$ brew cask install docker

*Maven:*

Just get the gzipped tar file and unzip, follow easy instructions here:

https://maven.apache.org/install.html


## Configuration

There are defaults for all configuration parameters.
All configuration is via System properties.

* wsruler.server_port : Value set should is valid tcp port : Default is "8080"

This param defines the connection pool you wish to use:

* wsruler.connpoolfactoryclass : Default "org.bluelamar.wsruler.QueueConnPoolFactory"

These params are for connecting to the database:

* wsruler.db.url : List of 1 or more urls separated by a ';'. Default is "http://localhost:5984/"
* wsruler.dbconnloginfactory : Connection credential login factory. Default: "org.bluelamar.wsruler.CdbConnCredFactory"
* wsruler.dbconnclonerclass : Connection cloner creational pattern object. Default: "org.bluelamar.wsruler.RestConnection"

These params are for connecting to the Owners Group Directory Service:

* wsruler.ogrp.url : List of 1 or more urls separated by a ';'. Default is "http://localhost:5984/"
* wsruler.ogrp.connloginfactory : Connection credential login factory. Default: "org.bluelamar.wsruler.CdbConnCredFactory"
* wsruler.ogrp.connclonerclass : Connection cloner creational pattern object. Default: "org.bluelamar.wsruler.DsRestConnection"

These parameters refer to the building of unique ID's when the server creates
new objects in the database.

* wsruler.idfactoryclass : Name of ID factory class : Default is "org.bluelamar.wsruler.ShortIdFactory"
* wsruler.id_add_salt : Values are "true" or "false" : Default is "false"
* wsruler.id_size : Values are number of bits "4" or "8" : Default is "4"


## Running

1. Startup the database in a terminal window. It could take up to 20 seconds to initialize.

$ ./scripts/run_cdb.sh

You will know its ready when you see it starts printing to the console.
Then from a separate terminal initialize the database with the Directory
Service Owner Group data if you have not yet done so.
This simply pre-populates the DB with made-up groups with owners.

$ ./scripts/init_ds.sh

2. In a separate terminal start the server.

$ ./scripts/run_wsvr.sh

## Testing

There are multiple test scripts for various API supported by the server.
All are found in the tests/ subdirectory.

Run the following script to test all API:

$ ./tests/test_all.sh

