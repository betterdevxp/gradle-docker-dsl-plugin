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

class DockerLifecycleTaskFactory {

    static final String LIFECYCLE_GROUP = "Docker Container Lifecycle"

    private Project project
    private ContainerConfig config

    DockerLifecycleTaskFactory(Project project, ContainerConfig config) {
        this.project = project
        this.config = config
    }

    def <T extends Task> TaskProvider<T> registerImageTask(String action, Class<T> type) {
        registerDockerTask(action, "image", type)
    }

    def <T extends Task> TaskProvider<T> registerContainerTask(String action, Class<T> type) {
        registerDockerTask(action, "container", type)
    }

    private <T extends Task> TaskProvider<T> registerDockerTask(String action, String containerOrImage, Class<T> type) {
        String taskName = "${action}${config.displayName.capitalize()}"
        TaskProvider<T> taskProvider = project.tasks.register(taskName, type) {
            group = LIFECYCLE_GROUP
            description = "${action.capitalize()} the ${config.displayName} ${containerOrImage}"
        }
        taskProvider
    }

    TaskProvider<DockerPullImage> registerPullImageTask() {
        TaskProvider<DockerPullImage> taskProvider = registerImageTask("pull", DockerPullImage)
        taskProvider.configure {
            image.set(config.imageName)

            onlyIf {
                DockerApiUtils.isImageLocal(dockerClient, config.imageName) == false
            }
        }
        taskProvider
    }

    TaskProvider<DockerRemoveImage> registerDestroyImageTask() {
        TaskProvider<DockerRemoveImage> taskProvider = registerImageTask("destroy", DockerRemoveImage)
        taskProvider.configure {
            imageId.set(config.imageName)

            onlyIf {
                DockerApiUtils.isImageLocal(dockerClient, config.imageName)
            }
        }
        taskProvider
    }

    TaskProvider<DockerCreateContainer> registerCreateContainerTask() {
        TaskProvider<DockerCreateContainer> taskProvider = registerContainerTask("create", DockerCreateContainer)
        taskProvider.configure {
            imageId.set(config.imageName)
            containerName.set(config.name)
            if (config.args.isEmpty() == false) {
                cmd.set(config.args)
            }
            if (config.portBindings.isEmpty() == false) {
                hostConfig.portBindings.set(config.portBindings)
            }
            if (config.env.isEmpty() == false) {
                envVars.set(config.env)
            }

            onlyIf {
                DockerApiUtils.isContainerCreated(dockerClient, config.name) == false
            }
        }
        taskProvider
    }

    TaskProvider<DockerStartContainer> registerStartContainerTask() {
        TaskProvider<DockerStartContainer> taskProvider = registerContainerTask("start", DockerStartContainer)
        taskProvider.configure {
            containerId.set(config.name)

            onlyIf {
                DockerApiUtils.isContainerRunning(dockerClient, config.name) == false
            }
        }
        taskProvider
    }

    TaskProvider<DockerStopContainer> registerStopContainerTask() {
        TaskProvider<DockerStopContainer> taskProvider = registerContainerTask("stop", DockerStopContainer)
        taskProvider.configure {
            containerId.set(config.name)
            waitTime.set(config.stopWaitTime)

            onlyIf {
                DockerApiUtils.isContainerRunning(dockerClient, config.name)
            }
        }
        taskProvider
    }

    TaskProvider<DockerRemoveContainer> registerRemoveContainerTask() {
        TaskProvider<DockerRemoveContainer> taskProvider = registerContainerTask("remove", DockerRemoveContainer)
        taskProvider.configure {
            containerId.set(config.name)

            onlyIf {
                DockerApiUtils.isContainerCreated(dockerClient, config.name)
            }
        }
        taskProvider
    }

}
