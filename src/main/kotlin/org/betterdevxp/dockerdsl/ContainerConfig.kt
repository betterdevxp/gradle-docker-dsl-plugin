package org.betterdevxp.dockerdsl

import org.gradle.api.GradleException

class ContainerConfig() {
    var name: String = ""
    var displayName: String = ""
        get() = field.ifBlank { toCamelCase(name) }
    var imageName: String = ""
    var stopWaitTime: Int = 10
    var args: List<String> = listOf()
    val portBindings: MutableList<String> = mutableListOf()
    val env: MutableMap<String, String> = mutableMapOf()

    companion object {
        private fun toCamelCase(text: String): String {
            return text.replace("([_\\-])([A-Za-z0-9])".toRegex()) {
                it.groupValues[2].uppercase()
            }
        }
    }

    fun name(name: String) {
        this.name = name
    }

    fun displayName(displayName: String) {
        this.displayName = displayName
    }

    fun imageName(imageName: String) {
        this.imageName = imageName
    }

    fun stopWaitTime(stopWaitTime: Int) {
        this.stopWaitTime = stopWaitTime
    }

    fun args(vararg args: String) {
        this.args = args.asList()
    }

    fun portBinding(portBinding: String) {
        portBindings.add(portBinding)
    }

    fun portBinding(hostPort: Int, containerPort: Int) {
        portBinding("$hostPort:$containerPort")
    }

    fun portBindings(vararg args: String) {
        portBindings.addAll(args)
    }

    fun envVar(keyAndValue: String ) {
        val keyAndValueArray = keyAndValue.split("\\s*=\\s*".toRegex())
        if (keyAndValueArray.size != 2) {
            throw GradleException("Expecting input of form 'key=value', was '$keyAndValue'")
        }
        env[keyAndValueArray[0]] = keyAndValueArray[1]
    }

    fun envVar(name: String, value: String) {
        env[name] = value
    }

    fun envVars(envToAdd: Map<String, String>) {
        env.putAll(envToAdd)
    }

}
