package org.betterdevxp.dockerdsl

import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerRemoveImage
import org.gradle.api.Project

class DockerRemoteTaskFactory {

    private Project project
    private ContainerConfig config
    private DockerApiUtils apiUtils

    private DockerPullImage pullImageTask
    private DockerRemoveImage removeImageTask
    private DockerCreateContainer createContainerTask
    private DockerStartContainer startContainerTask
    private DockerStopContainer stopContainerTask
    private DockerRemoveContainer removeContainerTask

    DockerRemoteTaskFactory(Project project, ContainerConfig config) {
        this.project = project
        this.config = config
        this.apiUtils = new DockerApiUtils()
    }

    void initialize() {
        createTasks()
        setTaskDependencies()
    }

    private void createTasks() {
        pullImageTask = createPullImageTask()
        removeImageTask = createRemoveImageTask()
        createContainerTask = createCreateContainerTask()
        startContainerTask = createStartContainerTask()
        stopContainerTask = createStopContainerTask()
        removeContainerTask = createRemoveContainerTask()
    }

    private void setTaskDependencies() {
        createContainerTask.dependsOn(pullImageTask)
        startContainerTask.dependsOn(createContainerTask)

        removeContainerTask.dependsOn(stopContainerTask)
        removeImageTask.dependsOn(removeContainerTask)
    }

    private String getTaskName(String taskType) {
        "${taskType}${config.displayName.capitalize()}"
    }

    private DockerPullImage createPullImageTask() {
        String taskName = getTaskName("pull")
        project.tasks.create(taskName, DockerPullImage) {
            image.set(config.imageName)

            onlyIf {
                apiUtils.isImageLocal(dockerClient, config.imageName) == false
            }
        }
    }

    private DockerRemoveImage createRemoveImageTask() {
        String taskName = getTaskName("destroy")
        project.tasks.create(taskName, DockerRemoveImage) {
            imageId.set(config.imageName)

            onlyIf {
                apiUtils.isImageLocal(dockerClient, config.imageName)
            }
        }
    }

    private DockerCreateContainer createCreateContainerTask() {
        String taskName = getTaskName("create")
        project.tasks.create(taskName, DockerCreateContainer) {
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

    private DockerStartContainer createStartContainerTask() {
        String taskName = getTaskName("start")
        project.tasks.create(taskName, DockerStartContainer) {
            containerId.set(config.name)

            onlyIf {
                apiUtils.isContainerRunning(dockerClient, config.name) == false
            }
        }
    }

    private DockerStopContainer createStopContainerTask() {
        String taskName = getTaskName("stop")
        project.tasks.create(taskName, DockerStopContainer) {
            containerId.set(config.name)
            waitTime.set(config.stopWaitTime)

            onlyIf {
                apiUtils.isContainerRunning(dockerClient, config.name)
            }
        }
    }

    private DockerRemoveContainer createRemoveContainerTask() {
        String taskName = getTaskName("remove")
        project.tasks.create(taskName, DockerRemoveContainer) {
            containerId.set(config.name)

            onlyIf {
                apiUtils.isContainerCreated(dockerClient, config.name)
            }
        }
    }

}
