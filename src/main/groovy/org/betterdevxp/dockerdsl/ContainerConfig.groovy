package org.betterdevxp.dockerdsl

class ContainerConfig {

    String name
    String displayName
    String imageName
    Integer stopWaitTime
    List<String> args
    List<String> portBindings = []
    private List<String> options = []

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

    void env(String env) {
        option("--env=${env}")
    }

    void option(String option) {
        options.add(option)
    }

}
