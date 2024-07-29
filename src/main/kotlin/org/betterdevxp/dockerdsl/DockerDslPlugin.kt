package org.betterdevxp.dockerdsl

import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class DockerDslPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply(DockerRemoteApiPlugin::class.java)
        project.extensions.create(DockerDslExtension.NAME, DockerDslExtension::class.java, project)
    }

}