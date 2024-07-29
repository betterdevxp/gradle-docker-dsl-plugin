package org.betterdevxp.dockerdsl

import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerRemoveImage
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class DockerLifecycleTaskFactory(private val project: Project, private val config: ContainerConfig) {

    companion object {
        const val LIFECYCLE_GROUP = "Docker Container Lifecycle"
    }

    fun <T: Task> registerImageTask(action: String, type: Class<T>): TaskProvider<T>  {
        return registerDockerTask(action, "image", type)
    }

    fun <T: Task> registerContainerTask(action: String, type: Class<T>): TaskProvider<T> {
        return registerDockerTask(action, "container", type)
    }

    private fun <T: Task> registerDockerTask(action: String, containerOrImage: String, type: Class<T>): TaskProvider<T> {
        val taskName = "${action}${config.displayName.capitalize()}"
        val taskProvider = project.tasks.register(taskName, type) {
            it.group = LIFECYCLE_GROUP
            it.description = "${action.capitalize()} the ${config.displayName} ${containerOrImage}"
        }
        return taskProvider
    }

    fun registerPullImageTask(): TaskProvider<DockerPullImage> {
        val taskProvider = registerImageTask("pull", DockerPullImage::class.java)
        taskProvider.configure { task ->
            task.image.set(config.imageName)

            task.onlyIf {
                DockerApiUtils.isImageLocal(task.dockerClient, config.imageName) == false
            }
        }
        return taskProvider
    }

    fun registerDestroyImageTask(): TaskProvider<DockerRemoveImage> {
        val taskProvider = registerImageTask("destroy", DockerRemoveImage::class.java)
        taskProvider.configure { task ->
            task.imageId.set(config.imageName)

            task.onlyIf {
                DockerApiUtils.isImageLocal(task.dockerClient, config.imageName)
            }
        }
        return taskProvider
    }

    fun registerCreateContainerTask(): TaskProvider<DockerCreateContainer> {
        val taskProvider = registerContainerTask("create", DockerCreateContainer::class.java)
        taskProvider.configure { task ->
            task.imageId.set(config.imageName)
            task.containerName.set(config.name)
            if (config.args.isEmpty() == false) {
                task.cmd.set(config.args)
            }
            if (config.portBindings.isEmpty() == false) {
                task.hostConfig.portBindings.set(config.portBindings)
            }
            if (config.env.isEmpty() == false) {
                task.envVars.set(config.env)
            }

            task.onlyIf {
                DockerApiUtils.isContainerCreated(task.dockerClient, config.name) == false
            }
        }
        return taskProvider
    }

    fun registerStartContainerTask(): TaskProvider<DockerStartContainer> {
        val taskProvider = registerContainerTask("start", DockerStartContainer::class.java)
        taskProvider.configure { task ->
            task.containerId.set(config.name)

            task.onlyIf {
                DockerApiUtils.isContainerRunning(task.dockerClient, config.name) == false
            }
        }
        return taskProvider
    }

    fun registerStopContainerTask(): TaskProvider<DockerStopContainer> {
        val taskProvider = registerContainerTask("stop", DockerStopContainer::class.java)
        taskProvider.configure { task ->
            task.containerId.set(config.name)
            task.waitTime.set(config.stopWaitTime)

            task.onlyIf {
                DockerApiUtils.isContainerRunning(task.dockerClient, config.name)
            }
        }
        return taskProvider
    }

    fun registerRemoveContainerTask(): TaskProvider<DockerRemoveContainer>  {
        val taskProvider = registerContainerTask("remove", DockerRemoveContainer::class.java)
        taskProvider.configure { task ->
            task.containerId.set(config.name)

            task.onlyIf {
                DockerApiUtils.isContainerCreated(task.dockerClient, config.name)
            }
        }
        return taskProvider
    }

}