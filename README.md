[![main Actions Status](https://github.com/betterdevxp/gradle-docker-dsl-plugin/workflows/gradle-build/badge.svg)](https://github.com/betterdevxp/gradle-docker-dsl-plugin/actions)

# gradle-docker-dsl-plugin

Gradle plugin for managing Docker containers, specifically in the context of the local and CI build/test lifecycle.

This plugin provides a handy DSL for the excellent [gradle-docker-plugin](https://github.com/bmuschko/gradle-docker-plugin).  
The primary use case is to facilitate testing, either local or as part of a CI build.  As such, the tasks are oriented
around pulling images and container lifecycle rather than building and pushing images.

### Installation

Apply the plugin using standard gradle convention

plugins {
    id ("org.betterdevxp.dockerdsl") version "0.1.0"
}

### Usage

Given a container definition, gradle tasks are dynamically created to pull the image, create the container, start the 
container, stop the container, remove the container, and destroy the image.

For example, given the following container definition...
```gradle
dockerdsl {
    container {
        name "postgres"
        imageName "postgres:9.4"
        publish "5432:5432"
        env "POSTGRES_USER=postgres"
        env "POSTGRES_PASSWORD=postgres"
    }
}
```

The following tasks will be created:

* pullPostgres - pulls the postgres image
* createPostgres - creates the postgres container, depends on pullPostgres
* startPostgres - starts the postgres container, depends on createPostgres
* stopPostgres - stops the postgres container
* removePostgres - removes the postgres container, depends on stopPostgres
* destroyPostgres - destroys the postgres container, depends on removePostgres

Task dependencies are defined such that any prerequisite tasks are executed.  In addition, a task will only execute
if it needs to in order to fulfill its function.  For example, if the task `startPostgres` is executed but the 
image has not been pulled, the following tasks will be executed - `pullPostgres`, `createPostgres`, `startPostgres`.
If the `startPostgres` is executed again, nothing will happen since the container is already started.

#### Bulk tasks

If more than one container is defined per project, the following additional tasks will be created.

* startAllContainers
* stopAllContainers
* removeAllContainers
* refreshAllContainers
* destroyAllImages
