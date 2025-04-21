package org.betterdevxp.dockerdsl

import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification

class DockerDslPluginTaskConfigFunctionalSpec extends Specification implements DockerDslPluginSupport {

    def setup() {
        initTestContainer()
        runner.withArguments("removeTest", "pullTest").build()
    }

    def "create should apply args to created container"() {
        given:
        initTestContainer("""
dockerdsl {
    container {
        name "test"
        imageName "alpine:latest"
        args "thisshouldfail"
    }
}
""")

        when:
        BuildResult result = runAndFail("startTest")

        then:
        assert result.output.contains('error during container init: exec: \\"thisshouldfail\\"')
    }

    def "stopWaitTime should stop the container within the specified time"() {
        given:
        initTestContainer("""
dockerdsl {
    container {
        name "test"
        imageName "alpine:latest"
        args "sleep", "5"
        stopWaitTime 2
    }
}

project.ext.start = null
stopTest.doFirst {
    project.ext.start = System.currentTimeMillis()
}
stopTest.doLast {
    long taskTime = System.currentTimeMillis() - project.ext.start
    if (taskTime < 2000 || taskTime > 5000) {
        throw new GradleException("stopWaitTime not respected, taskTime=" + taskTime)
    }
}
""")

        when:
        run("startTest", "stopTest")

        then:
        notThrown(Exception)
    }

    def "should apply port configuration"() {
        given:
        initTestContainer('''
import com.bmuschko.gradle.docker.tasks.container.DockerInspectContainer

dockerdsl {
    container {
        name "test"
        imageName "alpine:latest"
        args "sleep", "10"
        stopWaitTime 1

        portBinding "9123:9124"
        portBinding 9125, 9126
    }
}

createTest.exposePorts('tcp', [9124, 9126])

task inspectTest(type: DockerInspectContainer) {
    dependsOn startTest

    targetContainerId startTest.getContainerId()

    onNext { container ->
        container.networkSettings.ports.bindings.forEach { exposedPort, bindings ->
            logger.quiet "PortBinding: $exposedPort.port -> ${bindings.first().hostPortSpec}"
        }
    }
}
''')

        when:
        BuildResult result = run("inspectTest", "stopTest")

        then:
        assert result.output.contains("PortBinding: 9124 -> 9123")
        assert result.output.contains("PortBinding: 9126 -> 9125")
    }

    def "should apply environment configuration"() {
        given:
        initTestContainer('''
import com.bmuschko.gradle.docker.tasks.container.DockerLogsContainer

dockerdsl {
    container {
        name "test"
        imageName "alpine:latest"
        args "env"

        envVar "KEY1 = value1"
        envVar "KEY2", "value2"
        envVars (["KEY3": "value3", "KEY4": "value4"])
    }
}

task logTest(type: DockerLogsContainer) {
    dependsOn startTest

    targetContainerId startTest.getContainerId()
    follow = true
    tailAll = true
}
''')

        when:
        BuildResult result = run("logTest", "stopTest")

        then:
        assert result.output.contains("KEY1=value1")
        assert result.output.contains("KEY2=value2")
        assert result.output.contains("KEY3=value3")
        assert result.output.contains("KEY4=value4")
    }

    def "should add task descriptions"() {
        given:
        initTestContainer("""
dockerdsl {
    container {
        name "test"
        imageName "alpine:latest"
    }
}
""")

        when:
        BuildResult result = run("task")
        
        then:
        assert result.output.contains("pullTest - Pull the test image")
        assert result.output.contains("destroyTest - Destroy the test image")
        assert result.output.contains("createTest - Create the test container")
        assert result.output.contains("startTest - Start the test container")
        assert result.output.contains("stopTest - Stop the test container")
        assert result.output.contains("removeTest - Remove the test container")
    }

}
