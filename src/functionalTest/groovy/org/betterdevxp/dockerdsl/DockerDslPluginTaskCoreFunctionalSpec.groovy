package org.betterdevxp.dockerdsl

import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification

class DockerDslPluginTaskCoreFunctionalSpec extends Specification implements DockerDslPluginSupport {

    def setup() {
        initTestContainer()
        run("destroyTest")
    }

    def "pull should pull the image and skip if image already pulled"() {
        when:
        BuildResult result = run("pullTest")

        then:
        assert result.output.contains("Pulling image 'alpine:latest'")

        when:
        result = run("pullTest")

        then:
        assert result.output.contains("Task :pullTest SKIPPED")
    }

    def "destroy should remove the image and skip if image does not exist"() {
        given:
        run("pullTest")

        when:
        BuildResult result = run("destroyTest")

        then:
        assert result.output.contains("Removing image with ID 'alpine:latest'")

        when:
        result = run("destroyTest")

        then:
        assert result.output.contains("Task :destroyTest SKIPPED")
    }

    def "create should create the container and skip if container already exists"() {
        given:
        run("pullTest")

        when:
        BuildResult result = run("createTest")

        then:
        assert result.output.contains("Created container with ID 'test'")

        when:
        result = run("createTest")

        then:
        assert result.output.contains("Task :createTest SKIPPED")
    }

    def "start should start the container and skip if container already started"() {
        given:
        run("createTest")

        when:
        BuildResult result = run("startTest")

        then:
        assert result.output.contains("Starting container with ID 'test'")

        when:
        result = run("startTest")

        then:
        assert result.output.contains("Task :startTest SKIPPED")
    }

    def "stop should stop the container and skip if container not started"() {
        given:
        run("startTest")

        when:
        BuildResult result = run("stopTest")

        then:
        assert result.output.contains("Stopping container with ID 'test'")

        when:
        result = run("stopTest")

        then:
        assert result.output.contains("Task :stopTest SKIPPED")
    }

    def "remove should remove the container and skip if container does not exist"() {
        given:
        run("createTest")

        when:
        BuildResult result = run("removeTest")

        then:
        assert result.output.contains("Removing container with ID 'test'")

        when:
        result = run("removeTest")

        then:
        assert result.output.contains("Task :removeTest SKIPPED")
    }

}
