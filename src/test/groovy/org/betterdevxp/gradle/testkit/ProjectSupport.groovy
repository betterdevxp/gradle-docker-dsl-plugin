package org.betterdevxp.gradle.testkit

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

trait ProjectSupport {

    private File projectDir = new File("build/gradlePluginTestOutputDir/${UUID.randomUUID()}")
    private Project theProject

    String getProjectName() {
        "root"
    }

    File getBuildFile() {
        projectDir.mkdirs()
        new File(projectDir, "build.gradle")
    }

    Project getProject() {
        if (theProject == null) {
            theProject = createProject()
        }
        theProject
    }

    Project createProject() {
        ProjectBuilder.builder()
                .withName("${projectName}-project")
                .withProjectDir(projectDir)
                .build()
    }

    ProjectValidator getProjectValidator() {
        new ProjectValidator(project)
    }

    /**
     * For some reason, evaluate does not show up in IDEA code completion so provide a delegate method
     */
    void evaluateProject() {
        project.evaluate()
    }

}
