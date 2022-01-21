package org.betterdevxp.dockerdsl

import org.betterdevxp.gradle.testkit.ProjectSupport
import spock.lang.Specification

class DockerDslPluginSpec extends Specification implements ProjectSupport {

    def "should create container lifecycle tasks"() {
        given:
        project.buildFile.text = """
dockerdsl {
    container {
        name "postgres"
        imageName "postgres:latest"
        portBinding "5432:5432"
        envVar "POSTGRES_USER=postgres"
        envVar "POSTGRES_PASSWORD=postgres"
    }
}
"""

        and:
        project.pluginManager.apply(DockerDslPlugin)

        when:
        evaluateProject()

        then:
        projectValidator.assertTasksDefined("pullPostgresLatest", "createPostgres", "startPostgres",
                "stopPostgres", "removePostgres", "destroyPostgresLatest")
    }
}
