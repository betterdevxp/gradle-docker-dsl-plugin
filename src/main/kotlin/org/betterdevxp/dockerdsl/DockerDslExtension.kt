package org.betterdevxp.dockerdsl

import groovy.lang.Closure
import org.gradle.api.Project

open class DockerDslExtension(val project: Project) {

    private val configurator = DockerTaskConfigurator()

    companion object {
        const val NAME = "dockerdsl"
    }

    fun container(closure: Closure<*>) {
        val container = ContainerConfig()
        project.configure(container, closure)
        configurator.registerTasksAndConfigureDependencies(project, container)
    }

}