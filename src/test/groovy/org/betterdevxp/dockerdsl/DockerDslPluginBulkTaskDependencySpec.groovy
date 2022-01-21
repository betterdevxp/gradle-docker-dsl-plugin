package org.betterdevxp.dockerdsl

import org.betterdevxp.gradle.testkit.ProjectSupport
import spock.lang.Specification

class DockerDslPluginBulkTaskDependencySpec extends Specification implements ProjectSupport {

    def setup() {
        project.plugins.apply(DockerDslPlugin.class)
    }

    private void initMultiContainer() {
        buildFile.text = """
dockerdsl {
    container {
        name "test"
        imageName "alpine:latest"
    }
}
dockerdsl {
    container {
        name "helloWorld"
        imageName "hello-world:linux"
    }
}
"""
    }

    def "destroyAllImages should depend on the individual destroy image tasks"() {
        given:
        initMultiContainer()

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("destroyAllImages", "destroyAlpineLatest")
        projectValidator.assertTaskDependency("destroyAllImages", "destroyHelloWorldLinux")
    }

    def "startAllContainers should depend on the individual startContainer tasks"() {
        given:
        initMultiContainer()

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("startAllContainers", "startTest")
        projectValidator.assertTaskDependency("startAllContainers", "startHelloWorld")
    }

    def "stopAllContainers should depend on the individual startContainer tasks"() {
        given:
        initMultiContainer()

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("stopAllContainers", "stopTest")
        projectValidator.assertTaskDependency("stopAllContainers", "stopHelloWorld")
    }

    def "removeAllContainers should depend on the individual removeContainer tasks"() {
        given:
        initMultiContainer()

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("removeAllContainers", "removeTest")
        projectValidator.assertTaskDependency("removeAllContainers", "removeHelloWorld")
    }

    def "refreshAllContainers should depend on the individual refreshContainer tasks"() {
        given:
        initMultiContainer()

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("refreshAllContainers", "refreshTest")
        projectValidator.assertTaskDependency("refreshAllContainers", "refreshHelloWorld")
    }

    def "bulk tasks should not exist if only one container is defined"() {
        given:
        buildFile.text = """
dockerdsl {
    container {
        name "test"
        imageName "alpine:latest"
    }
}
"""

        when:
        evaluateProject()

        then:
        projectValidator.assertTasksNotDefined("destroyAllImages", "startAllContainers",
                "stopAllContainers", "removeAllContainers", "refreshAllContainers")
    }

    private void initMultiContainerWithSameImage() {
        buildFile.text = """
dockerdsl {
    container {
        name "testOne"
        imageName "alpine:latest"
    }
}
dockerdsl {
    container {
        name "testTwo"
        imageName "alpine:latest"
    }
}
"""
    }

    def "destroyAllImages task should have a single dependency when multiple containers reference the same image"() {
        given:
        initMultiContainerWithSameImage()

        when:
        evaluateProject()

        then:
        List<String> dependencies = projectValidator.getDependencyNamesForTask("destroyAllImages")
        assert dependencies == ["destroyAlpineLatest"]
    }

    def "destroy image task should depend on all remove container tasks which depend on the image"() {
        given:
        initMultiContainerWithSameImage()

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("destroyAlpineLatest", "removeTestOne")
        projectValidator.assertTaskDependency("destroyAlpineLatest", "removeTestTwo")
    }

    def "each create task should depend on the same pull task if the container use the same image"() {
        given:
        initMultiContainerWithSameImage()

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("createTestOne", "pullAlpineLatest")
        projectValidator.assertTaskDependency("createTestTwo", "pullAlpineLatest")
    }

}
