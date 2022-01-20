package org.betterdevxp.dockerdsl

import org.betterdevxp.gradle.testkit.ProjectSupport
import spock.lang.Specification

class DockerDslPluginBulkTaskDependencySpec extends Specification implements ProjectSupport {

    def setup() {
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
        project.plugins.apply(DockerDslPlugin.class)
    }

    def "destroyAllImages should depend on the individual destroy image tasks"() {
        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("destroyAllImages", "destroyTest")
        projectValidator.assertTaskDependency("destroyAllImages", "destroyHelloWorld")
    }

    def "startAllContainers should depend on the individual startContainer tasks"() {
        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("startAllContainers", "startTest")
        projectValidator.assertTaskDependency("startAllContainers", "startHelloWorld")
    }

    def "stopAllContainers should depend on the individual startContainer tasks"() {
        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("stopAllContainers", "stopTest")
        projectValidator.assertTaskDependency("stopAllContainers", "stopHelloWorld")
    }

    def "removeAllContainers should depend on the individual removeContainer tasks"() {
        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("removeAllContainers", "removeTest")
        projectValidator.assertTaskDependency("removeAllContainers", "removeHelloWorld")
    }

    def "refreshAllContainers should depend on the individual refreshContainer tasks"() {
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

}
