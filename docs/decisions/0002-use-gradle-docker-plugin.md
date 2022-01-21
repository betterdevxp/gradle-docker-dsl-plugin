#  Use https://github.com/bmuschko/gradle-docker-plugin for backing implementation

## Context and Problem Statement

We want an easy interface for the manipulation of docker containers but do not want to re-implement (and maintain) the wheel.
Of several existing docker integration options, which to select?

## Decision Drivers

* Actively maintained with decent user base
* Good (or at least decent) documentation

## Considered Options

* https://github.com/bmuschko/gradle-docker-plugin
* https://github.com/palantir/gradle-docker (docker-run)
* https://github.com/Gelangweilte-Studenten/gradle-docker-tests
* https://github.com/docker-java/docker-java

## Decision Outcome

Chosen option: "https://github.com/bmuschko/gradle-docker-plugin", details below

## Pros and Cons of the Options 

### https://github.com/bmuschko/gradle-docker-plugin

* Good, well-established (since 2014) and continues to be maintained
* Good, excellent documentation (javadocs, user guide, changelog, etc)
* Good, plugin consists of a series of tasks for image/container manipulation, allowing for great implementation flexibility
* Good, tasks exists for virtually all docker operations
* Bad, no support for DSL, will need to be created and maintained

### https://github.com/palantir/gradle-docker (docker-run)

* Good, well-established (since 2016) and continues to be maintained
* Good, the plugin already supports a DSL
* Bad, only allows operations (create/run/stop/etc) on a single container per gradle module

### https://github.com/liferay/liferay-portal/tree/master/modules/sdk/gradle-plugins-app-docker

* Good, fits the target use case perfectly (run docker as part of commit stage automated tests) 
* Good, tasks exist for some docker operations
* Bad, new with little adoption/community
* Bad, assumes image already exist or will be built by project - no facility for pulling images

### https://github.com/docker-java/docker-java

Unlike the other options which are existing gradle plugins, this is a Docker client API.

* Good, the ultimate in flexibility
* Bad, documentation is not great
* Bad, would basically be re-implementing chunks of https://github.com/bmuschko/gradle-docker-plugin
