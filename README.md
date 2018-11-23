# WsRuleR
Demo of Workspace website: Workspace Environment, Repository and DB Relationships

## Design

The API is documented in docs/WsRuler_Interface.pdf

The server uses CouchDB as the backing store. This DB supports a schema-free
document model with full support for JSON documents. Further the DB is accessed via a REST API.

The service leverages off of abstraction via interfaces to allow implementations
for various features. The implementations are specified via System properties.
All have defaults.

An interface (RestConnection) defines a REST API to backend services.
Implementations of RestConnection are:

   * CdbRestConnection : API that communicates with the CouchDB
   * DsRestConnection : API that communicates with the virtual Directory Service

Although the DsRestConnection is just the API for comm with the Directory Service,
the actual implementation is a simple extension of the CdbRestConnection.

A connection pool (ConnPool) to manage the collection of RestConnections is defined.
The implementation of the pool is a very simple queue pool (QueueConnPool).
The WsRuler service requests a connection by service name and the pool clones a 
connection object for that service type.

Along with creating a new connection, login or authentication is required.
So there is the ConnLoginFactory interface that defines the API a connection
uses to get its credentials for a target service.
The simple implementation for the login factory is CdbConnCredFactory.

There are several entities stored in the DB as well as associated entities returned
to or sent by clients. See the API document for description of these entities.

There is also a factory (IdFactory) for creating unique ID's for the various entities.
This package includes an implementation called ShortIdFactory. This factory
will create 4 byte ID's by hashing a given entity object with a random salt.
The hash function used is Fowler–Noll–Vo. A simple but effective algorithm
for which to create hashes.
I added the random salt to ensure that if multiple users defined entities of
the same type using the same name, they will get a unique ID due to the added salt.
The ShortIdFactory can be configured to create 8 byte ID's.


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

* wsruler.idfactoryclass : Name of ID factory class : Default is "org.bluelamar.wsruler.ShortIdFactory" - Fowler–Noll–Vo hash implementation
* wsruler.id_size : Values are number of bits "4" or "8" : Default is "4"


## Running

1. Startup the database in a terminal window. It could take up to 20 seconds to initialize.

$ ./scripts/run_cdb.sh

You will know its ready when you see it starts printing to the console.
Note the datbase can be blown away at anytime by shutting down the database
and then deleting the directory: cdb
*run_cdb.sh* will recreate the directory.

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

