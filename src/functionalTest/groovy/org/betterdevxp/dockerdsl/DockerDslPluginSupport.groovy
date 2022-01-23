package org.betterdevxp.dockerdsl

import org.betterdevxp.testkit.GradleRunnerSupport


trait DockerDslPluginSupport extends GradleRunnerSupport {

    void initTestContainer(String dsl = null) {
        buildFile.text = """
plugins {
    id('org.betterdevxp.docker-dsl')
}
"""
        if (dsl != null) {
            buildFile << dsl
        } else {
            buildFile << """
dockerdsl {
    container {
        name "test"
        imageName "alpine:latest"
        args "sleep", "10"
        stopWaitTime 1
    }
}
"""
        }
    }

}
