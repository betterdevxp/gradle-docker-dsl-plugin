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

class DockerTaskConfigurator {

    static final String LIFECYCLE_GROUP = "Docker Container Lifecycle"

    private Project project
    private ContainerConfig config

    private TaskProvider<DockerPullImage> pullImageTaskProvider
    private TaskProvider<DockerRemoveImage> destroyImageTaskProvider
    private TaskProvider<DockerCreateContainer> createContainerTaskProvider
    private TaskProvider<DockerStartContainer> startContainerTaskProvider
    private TaskProvider<DockerStopContainer> stopContainerTaskProvider
    private TaskProvider<DockerRemoveContainer> removeContainerTaskProvider
    private TaskProvider<Task> refreshContainerTaskProvider

    DockerTaskConfigurator(Project project, ContainerConfig config) {
        this.project = project
        this.config = config
    }

    void registerTasksAndConfigureDependencies() {
        registerTasks()
        configureDependencies()
    }

    private void registerTasks() {
        registerPullImageTask()
        registerDestroyImageTask()
        registerCreateContainerTask()
        registerStartContainerTask()
        registerStopContainerTask()
        registerRemoveContainerTask()
        registerRefreshContainerTask()
    }

    private void configureDependencies() {
        createContainerTaskProvider.configure {
            dependsOn(pullImageTaskProvider)
            mustRunAfter(removeContainerTaskProvider)
        }
        startContainerTaskProvider.configure {
            dependsOn(createContainerTaskProvider)
            mustRunAfter(stopContainerTaskProvider)
            mustRunAfter(removeContainerTaskProvider)
        }
        removeContainerTaskProvider.configure {
            dependsOn(stopContainerTaskProvider)
        }
        destroyImageTaskProvider.configure {
            dependsOn(removeContainerTaskProvider)
        }
        refreshContainerTaskProvider.configure {
            dependsOn(removeContainerTaskProvider)
            dependsOn(startContainerTaskProvider)
        }
    }

    private <T extends Task> TaskProvider<T> registerImageTask(String action, Class<T> type) {
        registerDockerTask(action, "image", type)
    }

    private <T extends Task> TaskProvider<T> registerContainerTask(String action, Class<T> type) {
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

    private void registerPullImageTask() {
        pullImageTaskProvider = registerImageTask("pull", DockerPullImage)
        pullImageTaskProvider.configure {
            image.set(config.imageName)

            onlyIf {
                DockerApiUtils.isImageLocal(dockerClient, config.imageName) == false
            }
        }
    }

    private void registerDestroyImageTask() {
        destroyImageTaskProvider = registerImageTask("destroy", DockerRemoveImage)
        destroyImageTaskProvider.configure {
            imageId.set(config.imageName)

            onlyIf {
                DockerApiUtils.isImageLocal(dockerClient, config.imageName)
            }
        }
    }

    private void registerCreateContainerTask() {
        createContainerTaskProvider = registerContainerTask("create", DockerCreateContainer)
        createContainerTaskProvider.configure {
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
    }

    private void registerStartContainerTask() {
        startContainerTaskProvider = registerContainerTask("start", DockerStartContainer)
        startContainerTaskProvider.configure {
            containerId.set(config.name)

            onlyIf {
                DockerApiUtils.isContainerRunning(dockerClient, config.name) == false
            }
        }
    }

    private void registerStopContainerTask() {
        stopContainerTaskProvider = registerContainerTask("stop", DockerStopContainer)
        stopContainerTaskProvider.configure {
            containerId.set(config.name)
            waitTime.set(config.stopWaitTime)

            onlyIf {
                DockerApiUtils.isContainerRunning(dockerClient, config.name)
            }
        }
    }

    private void registerRemoveContainerTask() {
        removeContainerTaskProvider = registerContainerTask("remove", DockerRemoveContainer)
        removeContainerTaskProvider.configure {
            containerId.set(config.name)

            onlyIf {
                DockerApiUtils.isContainerCreated(dockerClient, config.name)
            }
        }
    }

    private void registerRefreshContainerTask() {
        refreshContainerTaskProvider = registerContainerTask("refresh", Task)
    }

}
