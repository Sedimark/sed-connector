/*
 *  Copyright (c) 2025 Universidad de Cantabria (UC)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Pablo Sotres (UC)
 *       Victor Gonzalez (UC)
 */

// this is needed to have access to snapshot builds of plugins
pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {

    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "sed-connector"


include(":runtimes:controlplane")
include(":runtimes:dataplane")

include(":extensions:banner-extension")


// module project custom names
project(":runtimes:controlplane").name = "sed-conn-controlplane"
project(":runtimes:dataplane").name = "sed-conn-dataplane"


