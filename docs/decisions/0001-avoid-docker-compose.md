#  Orient around individual containers rather than sets of containers

## Context and Problem Statement

When a build requires multiple containers, how to best orchestrate those containers?

## Considered Options

* Use Docker Compose
* Use Docker

## Decision Outcome

Chosen option: "Use Docker", because

* Compose requires the configuration of the complete set of containers but often times we will want to create small, 
  individual plugins for a container type (e.g. Mongo, Postgres, RabbitMQ, etc) which defines the default 
  configuration (ports, environment variables, startup scripts, etc) such that simply applying the plugin is all 
  that is required, the majority of the time


