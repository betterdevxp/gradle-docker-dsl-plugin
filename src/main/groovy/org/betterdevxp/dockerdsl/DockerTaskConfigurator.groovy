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

    void registerTasksAndConfigureDependencies(Project project, ContainerConfig config) {
        DockerLifecycleTaskProviderSet taskProviders = registerTasks(project, config)
        configureDependencies(taskProviders)
    }

    private DockerLifecycleTaskProviderSet registerTasks(Project project, ContainerConfig config) {
        DockerLifecycleTaskFactory factory = new DockerLifecycleTaskFactory(project, config)
        DockerLifecycleTaskProviderSet taskProviders = new DockerLifecycleTaskProviderSet()
        taskProviders.pullImage = factory.registerPullImageTask()
        taskProviders.destroyImage = factory.registerDestroyImageTask()
        taskProviders.createContainer = factory.registerCreateContainerTask()
        taskProviders.startContainer = factory.registerStartContainerTask()
        taskProviders.stopContainer = factory.registerStopContainerTask()
        taskProviders.removeContainer = factory.registerRemoveContainerTask()
        taskProviders.refreshContainer = factory.registerContainerTask("refresh", Task)
        taskProviders
    }

    private void configureDependencies(DockerLifecycleTaskProviderSet taskProviders) {
        taskProviders.createContainer.configure {
            dependsOn(taskProviders.pullImage)
            mustRunAfter(taskProviders.removeContainer)
        }
        taskProviders.startContainer.configure {
            dependsOn(taskProviders.createContainer)
            mustRunAfter(taskProviders.stopContainer)
            mustRunAfter(taskProviders.removeContainer)
        }
        taskProviders.removeContainer.configure {
            dependsOn(taskProviders.stopContainer)
        }
        taskProviders.destroyImage.configure {
            dependsOn(taskProviders.removeContainer)
        }
        taskProviders.refreshContainer.configure {
            dependsOn(taskProviders.removeContainer)
            dependsOn(taskProviders.startContainer)
        }
    }


    private static class DockerLifecycleTaskProviderSet {

        TaskProvider<DockerPullImage> pullImage
        TaskProvider<DockerRemoveImage> destroyImage
        TaskProvider<DockerCreateContainer> createContainer
        TaskProvider<DockerStartContainer> startContainer
        TaskProvider<DockerStopContainer> stopContainer
        TaskProvider<DockerRemoveContainer> removeContainer
        TaskProvider<Task> refreshContainer

    }

}
