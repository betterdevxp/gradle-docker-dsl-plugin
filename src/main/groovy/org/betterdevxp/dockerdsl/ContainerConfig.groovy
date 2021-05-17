package org.betterdevxp.dockerdsl

import org.gradle.api.GradleException

class ContainerConfig {

    String name
    String displayName
    String imageName
    Integer stopWaitTime
    List<String> args = []
    List<String> portBindings = []
    Map<String, String> env = [:]

    Iterable<String> getPortBindings() {
        portBindings
    }

    void name(String name) {
        this.name = name
    }

    void displayName(String displayName) {
        this.displayName = displayName
    }

    void imageName(String imageName) {
        this.imageName = imageName
    }

    void stopWaitTime(Integer stopWaitTime) {
        this.stopWaitTime = stopWaitTime
    }

    void args(String ... args) {
        this.args = args as List
    }

    String getDisplayName() {
        displayName ?: toCamelCase(name)
    }

    private String toCamelCase(String text) {
        text.replaceAll("(_|-)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() })
    }

    void portBinding(String portBinding) {
        portBindings << portBinding
    }

    void portBinding(int hostPort, int containerPort) {
        portBinding("${hostPort}:${containerPort}")
    }

    void portBindings(String ... portBindingArray) {
        portBindings.addAll(portBindingArray)
    }

    void envVar(String keyAndValue) {
        String[] keyAndValueArray = keyAndValue.split(/\s*=\s*/)
        if (keyAndValueArray.length != 2) {
            throw new GradleException("Expecting input of form 'key=value', was '${keyAndValue}'")
        }
        env[keyAndValueArray[0]] = keyAndValueArray[1]
    }

    void envVar(String name, String value) {
        env[name] = value
    }

    void envVars(Map<String, String> envToAdd) {
        env.putAll(envToAdd)
    }

}
