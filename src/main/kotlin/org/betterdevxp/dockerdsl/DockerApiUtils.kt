package org.betterdevxp.dockerdsl

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.Image

class DockerApiUtils {

    companion object {

        private fun findImage(client: DockerClient, imageName: String): Image? {
            val images = client.listImagesCmd().exec()
            return images.find {
                it.repoTags?.contains(imageName) ?: false
            }
        }

        fun isImageLocal(client: DockerClient, imageName: String) : Boolean {
            return findImage(client, imageName) != null
        }

        private fun findContainer(client: DockerClient, containerName: String) : Container? {
            val containers = client.listContainersCmd()
                .withShowAll(true)
                .exec()
            return containers.find { it.names.contains("/$containerName") }
        }

        fun isContainerCreated(client: DockerClient, containerName: String) : Boolean {
            return findContainer(client, containerName) != null
        }

        fun isContainerRunning(client: DockerClient, containerName: String) : Boolean {
            val container = findContainer(client, containerName)
            return container?.state == "running"
        }

    }
}