package org.betterdevxp.dockerdsl

class ContainerConfig {

    String name
    String displayName
    String imageName
    private List<String> options = []

    void name(String name) {
        this.name = name
    }

    void displayName(String displayName) {
        this.displayName = displayName
    }

    void imageName(String imageName) {
        this.imageName = imageName
    }

    String getDisplayName() {
        displayName ?: toCamelCase(name)
    }

    private String toCamelCase(String text) {
        text.replaceAll("(_|-)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() })
    }

    void publish(String publishedPort) {
        option("--publish=${publishedPort}")
    }

    void env(String env) {
        option("--env=${env}")
    }

    void option(String option) {
        options.add(option)
    }

}