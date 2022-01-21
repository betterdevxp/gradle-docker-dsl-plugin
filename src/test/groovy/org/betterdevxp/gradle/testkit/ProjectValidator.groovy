package org.betterdevxp.gradle.testkit

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.internal.tasks.DefaultTaskDependency

class ProjectValidator {

    private Project project

    ProjectValidator(Project project) {
        this.project = project
    }

    boolean assertPluginApplied(String pluginName) {
        assert project.plugins.getPlugin(pluginName) != null
        true
    }

    List<String> getDependencyNamesForTask(String taskName) {
        Task task = project.tasks.getByName(taskName)
        task.taskDependencies.getDependencies(task).toList().collect { it.name }
    }

    boolean assertTaskDependency(String taskName, String... expectedDependencyNames) {
        expectedDependencyNames.each { String expectedDependencyName ->
            assertTaskDependency(taskName, expectedDependencyName)
        }
        true
    }

    boolean assertTaskDependency(String taskName, String expectedDependencyName) {
        List<String> dependencyNames = getDependencyNamesForTask(taskName)
        assert dependencyNames.contains(expectedDependencyName)
        "Expected task ${taskName} to declare dependency on ${expectedDependencyName}, actual dependencies: ${dependencyNames}"
        true
    }

    boolean assertNoTaskDependency(String taskName, String expectedMissingDependencyName) {
        List<String> dependencyNames = getDependencyNamesForTask(taskName)
        assert !dependencyNames.contains(expectedMissingDependencyName)
        "Expected task ${taskName} to NOT declare dependency on ${expectedMissingDependencyName}, actual dependencies: ${dependencyNames}"
        true
    }

    boolean assertTasksDefined(String... names) {
        names.each { String name ->
            try {
                project.tasks.named(name)
            } catch (UnknownTaskException ex) {
                assert false: "No task defined named ${name}"
            }
        }
        true
    }

    boolean assertTasksNotDefined(String... names) {
        names.each { String name ->
            try {
                project.tasks.named(name)
                assert false: "Task defined named ${name}"
            } catch (UnknownTaskException ex) {
            }
        }
        true
    }

    boolean assertTaskMustRunAfter(String taskName, String expectedMustRunAfterName) {
        assert getMustRunAfterTaskNames(taskName).contains(expectedMustRunAfterName)
        true
    }

    boolean assertTaskMustRunAfterNotDefined(String taskName, String expectedMustRunAfterName) {
        assert getMustRunAfterTaskNames(taskName).contains(expectedMustRunAfterName) == false
        true
    }

    private List<String> getMustRunAfterTaskNames(String taskName) {
        Task task = project.tasks.getByName(taskName)
        List<String> mustRunAfterTaskNames = (task.getMustRunAfter() as DefaultTaskDependency).getMutableValues().collect {
            if (it instanceof String) {
                return it
            } else if (it instanceof Task) {
                return it.name
            } else {
                throw new IllegalStateException("Unknown value=${it} of type=${it.class}")
            }
        }
        mustRunAfterTaskNames
    }

}
