# Prerequisites

This section provides information on deploying the Test Data Management System (TDM) application to an external customer environment. 
In this mode Ldap, Email, CI Portal and Test Context
Editor have been mocked or removed.

There are 2 users defined
 - admin - has manager role access
 - user - has test engineer role access


# User Access
Each users**admin**and**user** have been given predefined username and password.
These are required in order to login.

Test Manager
 - username: admin
 - password: admin

Test Engineer
 - username: user
 - password: password

# Deployment
The docker compose file in this directory is an example of the production
environment setup.

It contains the following docker images:
- Test Data Management 
- Mongo Database 3.2
- cAdvisor

cAdvisor is a monitoring tool to monitor e.g. CPU/memory usage of both the host machine and docker containers. If not needed it can be removed.

In the docker compose file example the TDM version needs to be set as an environment variable. This makes it easier to upgrade.
But it can be hard coded.

````
version: "3"
services:
  TDM_backend:
    image: armdocker.rnd.ericsson.se/proj_taf_tdm/tdm-server:${TDM_VERSION}
    container_name: "backend"
    ports:
      - "80:8888"
    environment:
      - SPRING_PROFILES_ACTIVE=customer
    volumes:
      - /opt/store/volumes/tdm-logs:/var/log/tdm
    depends_on:
      - mongodb
    logging:
      options:
        max-size: "1g"
        max-file: "3"
  mongodb:
    image: mongo:3.2
    ports:
     - "27017:27017"
    volumes:
     - /tdmMongoDB/db:/data/db
  cAdvisor:
    image: google/cadvisor:latest
    container_name: "cAdvisor"
    stdin_open: true
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:rw
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
    tty: true
    ports:
      - "9090:8080"

````


To set TDM to run in customer mode the following environment variables must be passed:
**SPRING_PROFILES_ACTIVE=customer**

TDM will then run with certain featured switched off.

# Mongo Database
TDM requires a Mongo database version 3.2. Please note that other later versions may not work.

# System Specifications

| Specifications        | Description  |
|-----------------------|--------------|
| Operating System      | CENTS OS 7.4+|
| Docker version        | 17.04.0-ce   |
| Memory                | 4GB          |
| CPU Cores             | 2            |




