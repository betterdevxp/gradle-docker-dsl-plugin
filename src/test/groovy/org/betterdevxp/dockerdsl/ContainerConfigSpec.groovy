package org.betterdevxp.dockerdsl

import spock.lang.Specification


class ContainerConfigSpec extends Specification {

    ContainerConfig config = new ContainerConfig()

    def "getDisplayName should return displayName if set"() {
        given:
        config.name = "some-name"
        config.displayName = "otherName"

        expect:
        assert config.displayName == "otherName"
    }

    def "getDisplayName should convert name to camel case if displayName not set"() {
        given:
        config.name = name

        expect:
        assert config.displayName == expectedDisplayName

        where:
        name                 | expectedDisplayName
        "container"          | "container"
        "the-container-name" | "theContainerName"
        "the_container_name" | "theContainerName"
        "the-container_name" | "theContainerName"
        "theContainerName"   | "theContainerName"
    }

    def "getImageDisplayName should strip special chars and convert image name to camel"() {
        given:
        config.imageName = imageName

        expect:
        assert config.imageDisplayName == expectedDisplayName

        where:
        imageName                  | expectedDisplayName
        "image"                    | "image"
        "the-image-name"           | "theImageName"
        "the_image_name"           | "theImageName"
        "the-image:name"           | "theImageName"
        "the-image:10.1-alpine3.5" | "theImage101Alpine35"
        "theImageName"             | "theImageName"
    }

}
