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

    private DockerBatchLifecycleTaskProviderSet batchTaskProviders
    private Map<String, DockerLifecycleTaskProviderSet> imageNameToLifecycleTaskProviderSet = [:]

    void registerTasksAndConfigureDependencies(Project project, ContainerConfig config) {
        DockerLifecycleTaskProviderSet taskProviders = registerTasks(project, config)
        taskProviders.configureDependencies()
        configureBulkDependencies(project, taskProviders)
        imageNameToLifecycleTaskProviderSet[config.imageName] = taskProviders
    }

    private DockerLifecycleTaskProviderSet registerTasks(Project project, ContainerConfig config) {
        DockerLifecycleTaskFactory factory = new DockerLifecycleTaskFactory(project, config)
        DockerLifecycleTaskProviderSet taskProviders = new DockerLifecycleTaskProviderSet()
        taskProviders.createContainer = factory.registerCreateContainerTask()
        taskProviders.startContainer = factory.registerStartContainerTask()
        taskProviders.stopContainer = factory.registerStopContainerTask()
        taskProviders.removeContainer = factory.registerRemoveContainerTask()
        taskProviders.refreshContainer = factory.registerContainerTask("refresh", Task)

        DockerLifecycleTaskProviderSet imageTaskProviders = imageNameToLifecycleTaskProviderSet[config.imageName]
        if (imageTaskProviders == null) {
            taskProviders.pullImage = factory.registerPullImageTask()
            taskProviders.destroyImage = factory.registerDestroyImageTask()
        } else {
            taskProviders.pullImage = imageTaskProviders.pullImage
            taskProviders.destroyImage = imageTaskProviders.destroyImage
        }
        taskProviders
    }

    private void configureBulkDependencies(Project project, DockerLifecycleTaskProviderSet dockerLifecycleTaskProviderSet) {
        if (imageNameToLifecycleTaskProviderSet.size() > 0) {
            if (batchTaskProviders == null) {
                batchTaskProviders = registerBatchTasks(project)
                DockerLifecycleTaskProviderSet initialTaskProviderSet = imageNameToLifecycleTaskProviderSet.values()[0]
                batchTaskProviders.addDependenciesToBatchTasks(initialTaskProviderSet)
            }
            batchTaskProviders.addDependenciesToBatchTasks(dockerLifecycleTaskProviderSet)
        }
    }

    private DockerBatchLifecycleTaskProviderSet registerBatchTasks(Project project) {
        DockerBatchLifecycleTaskProviderSet batchProviders = new DockerBatchLifecycleTaskProviderSet()
        batchProviders.destroyAllImages = registerBatchTask(project, "destroyAllImages", "Destroy all images")
        batchProviders.startAllContainers = registerBatchTask(project, "startAllContainers", "Start all containers")
        batchProviders.stopAllContainers = registerBatchTask(project, "stopAllContainers", "Stop all containers")
        batchProviders.removeAllContainers = registerBatchTask(project, "removeAllContainers", "Remove all containers")
        batchProviders.refreshAllContainers = registerBatchTask(project, "refreshAllContainers", "Refresh all containers")
        batchProviders
    }

    private TaskProvider registerBatchTask(Project project, String taskName, String taskDescription) {
        TaskProvider taskProvider = project.tasks.register(taskName, Task) {
            group = DockerLifecycleTaskFactory.LIFECYCLE_GROUP
            description = taskDescription
        }
        taskProvider
    }


    private static class DockerLifecycleTaskProviderSet {

        TaskProvider<DockerPullImage> pullImage
        TaskProvider<DockerRemoveImage> destroyImage
        TaskProvider<DockerCreateContainer> createContainer
        TaskProvider<DockerStartContainer> startContainer
        TaskProvider<DockerStopContainer> stopContainer
        TaskProvider<DockerRemoveContainer> removeContainer
        TaskProvider<Task> refreshContainer

        void configureDependencies() {
            createContainer.configure {
                dependsOn(pullImage)
                mustRunAfter(removeContainer)
            }
            startContainer.configure {
                dependsOn(createContainer)
                mustRunAfter(stopContainer)
                mustRunAfter(removeContainer)
            }
            removeContainer.configure {
                dependsOn(stopContainer)
            }
            destroyImage.configure {
                dependsOn(removeContainer)
            }
            refreshContainer.configure {
                dependsOn(removeContainer)
                dependsOn(startContainer)
            }
        }

    }

    private static class DockerBatchLifecycleTaskProviderSet {

        TaskProvider<Task> destroyAllImages
        TaskProvider<Task> startAllContainers
        TaskProvider<Task> stopAllContainers
        TaskProvider<Task> removeAllContainers
        TaskProvider<Task> refreshAllContainers

        void addDependenciesToBatchTasks(DockerLifecycleTaskProviderSet dockerLifecycleTaskProviderSet) {
            destroyAllImages.configure {
                dependsOn(dockerLifecycleTaskProviderSet.destroyImage)
            }
            startAllContainers.configure {
                dependsOn(dockerLifecycleTaskProviderSet.startContainer)
            }
            stopAllContainers.configure {
                dependsOn(dockerLifecycleTaskProviderSet.stopContainer)
            }
            removeAllContainers.configure {
                dependsOn(dockerLifecycleTaskProviderSet.removeContainer)
            }
            refreshAllContainers.configure {
                dependsOn(dockerLifecycleTaskProviderSet.refreshContainer)
            }
        }

    }

}
