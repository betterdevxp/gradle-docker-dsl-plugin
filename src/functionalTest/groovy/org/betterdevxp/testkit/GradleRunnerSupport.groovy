package org.betterdevxp.testkit

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before

trait GradleRunnerSupport {

    File projectDir = new File("build/gradleRunnerTestDir")
    File buildFile = new File(projectDir, "build.gradle")
    GradleRunner runner

    @Before
    def setupGradleRunner() {
        projectDir.deleteDir()
        projectDir.mkdirs()
        new File(projectDir, "settings.gradle").text = ""
        runner = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(projectDir)
    }

    BuildResult run(String... args) {
        if ((args as List).contains("-s") == false) {
            args = args + "-s"
        }
        runner.withArguments(args).build()
    }

    BuildResult runAndFail(String ... args) {
        if ((args as List).contains("-s") == false) {
            args = args + "-s"
        }
        runner.withArguments(args).buildAndFail()
    }

}
