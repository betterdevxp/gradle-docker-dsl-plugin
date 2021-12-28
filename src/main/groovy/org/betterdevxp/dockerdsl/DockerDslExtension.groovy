package org.betterdevxp.dockerdsl

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class DockerDslExtension {

    static final String NAME = "dockerdsl"

    private Project project

    DockerDslExtension(Project project) {
        this.project = project
    }

    void container(Closure closure) {
        ContainerConfig container = new ContainerConfig()
        ConfigureUtil.configure(closure, container)
        new DockerRemoteTaskFactory(project, container).createTasksAndInitializeDependencies()
    }

}
