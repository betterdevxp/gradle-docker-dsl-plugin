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

    private class DockerLifecycleTaskProviderSet(
        val pullImage: TaskProvider<DockerPullImage>,
        val destroyImage: TaskProvider<DockerRemoveImage>,
        val createContainer: TaskProvider<DockerCreateContainer>,
        val startContainer: TaskProvider<DockerStartContainer>,
        val stopContainer: TaskProvider<DockerStopContainer>,
        val removeContainer: TaskProvider<DockerRemoveContainer>,
        val refreshContainer: TaskProvider<Task>
    ) {

        fun configureDependencies() {
            createContainer.configure {
                it.dependsOn(pullImage)
                it.mustRunAfter(removeContainer)
            }
            startContainer.configure {
                it.dependsOn(createContainer)
                it.mustRunAfter(stopContainer)
                it.mustRunAfter(removeContainer)
            }
            removeContainer.configure {
                it.dependsOn(stopContainer)
            }
            destroyImage.configure {
                it.dependsOn(removeContainer)
            }
            refreshContainer.configure {
                it.dependsOn(removeContainer)
                it.dependsOn(startContainer)
            }
        }

    }

    private class DockerBatchLifecycleTaskProviderSet(
        val destroyAllImages: TaskProvider<Task>,
        val startAllContainers: TaskProvider<Task>,
        val stopAllContainers: TaskProvider<Task>,
        val removeAllContainers: TaskProvider<Task>,
        val refreshAllContainers: TaskProvider<Task>
    ) {
        fun addDependenciesToBatchTasks(dockerLifecycleTaskProviderSet: DockerLifecycleTaskProviderSet) {
            destroyAllImages.configure {
                it.dependsOn(dockerLifecycleTaskProviderSet.destroyImage)
            }
            startAllContainers.configure {
                it.dependsOn(dockerLifecycleTaskProviderSet.startContainer)
            }
            stopAllContainers.configure {
                it.dependsOn(dockerLifecycleTaskProviderSet.stopContainer)
            }
            removeAllContainers.configure {
                it.dependsOn(dockerLifecycleTaskProviderSet.removeContainer)
            }
            refreshAllContainers.configure {
                it.dependsOn(dockerLifecycleTaskProviderSet.refreshContainer)
            }
        }

    }

    private var initialTaskProviders: DockerLifecycleTaskProviderSet? = null
    private var batchTaskProviders: DockerBatchLifecycleTaskProviderSet? = null

    fun registerTasksAndConfigureDependencies(project: Project, config: ContainerConfig) {
        val taskProviders = registerTasks(project, config)
        taskProviders.configureDependencies()
        configureBulkDependencies(project, taskProviders)
    }

    private fun registerTasks(project: Project, config: ContainerConfig): DockerLifecycleTaskProviderSet {
        val factory = DockerLifecycleTaskFactory(project, config)
        val taskProviders = DockerLifecycleTaskProviderSet(
            pullImage = factory.registerPullImageTask(),
            destroyImage = factory.registerDestroyImageTask(),
            createContainer = factory.registerCreateContainerTask(),
            startContainer = factory.registerStartContainerTask(),
            stopContainer = factory.registerStopContainerTask(),
            removeContainer = factory.registerRemoveContainerTask(),
            refreshContainer = factory.registerContainerTask("refresh", Task::class.java)
        )
        return taskProviders
    }

    private fun configureBulkDependencies(project: Project, dockerLifecycleTaskProviderSet: DockerLifecycleTaskProviderSet) {
        if (initialTaskProviders == null) {
            initialTaskProviders = dockerLifecycleTaskProviderSet
        } else {
            if (batchTaskProviders == null) {
                batchTaskProviders = registerBatchTasks(project)
                batchTaskProviders!!.addDependenciesToBatchTasks(initialTaskProviders!!)
            }
            batchTaskProviders!!.addDependenciesToBatchTasks(dockerLifecycleTaskProviderSet)
        }
    }

    private fun registerBatchTasks(project: Project): DockerBatchLifecycleTaskProviderSet {
        val batchProviders = DockerBatchLifecycleTaskProviderSet(
            destroyAllImages = registerBatchTask(project, "destroyAllImages", "Destroy all images"),
            startAllContainers = registerBatchTask(project, "startAllContainers", "Start all containers"),
            stopAllContainers = registerBatchTask(project, "stopAllContainers", "Stop all containers"),
            removeAllContainers = registerBatchTask(project, "removeAllContainers", "Remove all containers"),
            refreshAllContainers = registerBatchTask(project, "refreshAllContainers", "Refresh all containers")
        )
        return batchProviders
    }

    private fun registerBatchTask(project: Project, taskName: String, taskDescription: String): TaskProvider<Task> {
        val taskProvider = project.tasks.register(taskName, Task::class.java) {
            it.group = DockerLifecycleTaskFactory.LIFECYCLE_GROUP
            it.description = taskDescription
        }
        return taskProvider
    }

}