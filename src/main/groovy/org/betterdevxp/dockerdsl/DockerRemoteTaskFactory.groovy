package org.betterdevxp.dockerdsl

import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerRemoveImage
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class DockerRemoteTaskFactory {

    static final String LIFECYCLE_GROUP = "Docker Container Lifecycle"

    private Project project
    private ContainerConfig config
    private DockerApiUtils apiUtils

    private TaskProvider<DockerPullImage> pullImageTaskProvider
    private TaskProvider<DockerRemoveImage> destroyImageTaskProvider
    private TaskProvider<DockerCreateContainer> createContainerTaskProvider
    private TaskProvider<DockerStartContainer> startContainerTaskProvider
    private TaskProvider<DockerStopContainer> stopContainerTaskProvider
    private TaskProvider<DockerRemoveContainer> removeContainerTaskProvider

    DockerRemoteTaskFactory(Project project, ContainerConfig config) {
        this.project = project
        this.config = config
        this.apiUtils = new DockerApiUtils()
    }

    void createTasksAndInitializeDependencies() {
        createTasks()
        initializeDependencies()
    }

    private void createTasks() {
        pullImageTaskProvider = createPullImageTask()
        destroyImageTaskProvider = createDestroyImageTask()
        createContainerTaskProvider = createCreateContainerTask()
        startContainerTaskProvider = createStartContainerTask()
        stopContainerTaskProvider = createStopContainerTask()
        removeContainerTaskProvider = createRemoveContainerTask()
    }

    private void initializeDependencies() {
        createContainerTaskProvider.configure {
            dependsOn(pullImageTaskProvider)
        }
        startContainerTaskProvider.configure {
            dependsOn(createContainerTaskProvider)
        }

        removeContainerTaskProvider.configure {
            dependsOn(stopContainerTaskProvider)
        }
        destroyImageTaskProvider.configure {
            dependsOn(removeContainerTaskProvider)
        }
    }

    private String getTaskName(String taskType) {
        "${taskType}${config.displayName.capitalize()}"
    }

    private String getImageDescription(String action) {
        "${action.capitalize()} the ${config.displayName} image"
    }

    private String getContainerDescription(String action) {
        "${action.capitalize()} the ${config.displayName} container"
    }

    private TaskProvider<DockerPullImage> createPullImageTask() {
        String taskName = getTaskName("pull")
        project.tasks.register(taskName, DockerPullImage) {
            group = LIFECYCLE_GROUP
            description = getImageDescription("pull")
            image.set(config.imageName)

            onlyIf {
                apiUtils.isImageLocal(dockerClient, config.imageName) == false
            }
        }
    }

    private TaskProvider<DockerRemoveImage> createDestroyImageTask() {
        String taskName = getTaskName("destroy")
        project.tasks.register(taskName, DockerRemoveImage) {
            group = LIFECYCLE_GROUP
            description = getImageDescription("destroy")
            imageId.set(config.imageName)

            onlyIf {
                apiUtils.isImageLocal(dockerClient, config.imageName)
            }
        }
    }

    private TaskProvider<DockerCreateContainer> createCreateContainerTask() {
        String taskName = getTaskName("create")
        project.tasks.register(taskName, DockerCreateContainer) {
            group = LIFECYCLE_GROUP
            description = getContainerDescription("create")
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
                apiUtils.isContainerCreated(dockerClient, config.name) == false
            }
        }
    }

    private TaskProvider<DockerStartContainer> createStartContainerTask() {
        String taskName = getTaskName("start")
        project.tasks.register(taskName, DockerStartContainer) {
            group = LIFECYCLE_GROUP
            description = getContainerDescription("start")
            containerId.set(config.name)

            onlyIf {
                apiUtils.isContainerRunning(dockerClient, config.name) == false
            }
        }
    }

    private TaskProvider<DockerStopContainer> createStopContainerTask() {
        String taskName = getTaskName("stop")
        project.tasks.register(taskName, DockerStopContainer) {
            group = LIFECYCLE_GROUP
            description = getContainerDescription("stop")
            containerId.set(config.name)
            waitTime.set(config.stopWaitTime)

            onlyIf {
                apiUtils.isContainerRunning(dockerClient, config.name)
            }
        }
    }

    private TaskProvider<DockerRemoveContainer> createRemoveContainerTask() {
        String taskName = getTaskName("remove")
        project.tasks.register(taskName, DockerRemoveContainer) {
            group = LIFECYCLE_GROUP
            description = getContainerDescription("remove")
            containerId.set(config.name)

            onlyIf {
                apiUtils.isContainerCreated(dockerClient, config.name)
            }
        }
    }

}
