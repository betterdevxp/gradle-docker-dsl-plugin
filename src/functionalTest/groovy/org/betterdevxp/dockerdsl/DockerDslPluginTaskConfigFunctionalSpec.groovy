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
        assert result.output.contains('starting container process caused: exec: \\"thisshouldfail\\"')
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
    finalizedBy stopTest

    targetContainerId startTest.getContainerId()

    onNext { container ->
        container.networkSettings.ports.bindings.forEach { exposedPort, bindings ->
            logger.quiet "PortBinding: $exposedPort.port -> ${bindings.first().hostPortSpec}"
        }
    }
}
''')

        when:
        BuildResult result = run("inspectTest")
        println result.output

        then:
        assert result.output.contains("PortBinding: 9124 -> 9123")
        assert result.output.contains("PortBinding: 9126 -> 9125")
    }

}
