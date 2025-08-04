/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin

plugins {
    `java-library`
    alias(libs.plugins.docker)
    alias(libs.plugins.shadow)
}

val annotationProcessorVersion: String by project
//val metaModelVersion: String by project

buildscript {
    dependencies {
        val edcGradlePluginsVersion: String by project
        classpath("org.eclipse.edc.edc-build:org.eclipse.edc.edc-build.gradle.plugin:${edcGradlePluginsVersion}")
    }
}

allprojects {
    apply(plugin = "org.eclipse.edc.edc-build")

    // configure which version of the annotation processor to use. defaults to the same version as the plugin
    configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
        processorVersion.set(annotationProcessorVersion)
        outputDirectory.set(project.layout.buildDirectory.asFile)
    }

//    configure<org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension> {
//        swagger {
//            title.set("Identity HUB REST API")
//            description = "Identity HUB REST APIs - merged by OpenApiMerger"
//            outputFilename.set(project.name)
//            outputDirectory.set(file("${rootProject.projectDir.path}/resources/openapi/yaml"))
//        }
//    }

    /*
    configure<org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension> {
        versions {
            // override default dependency versions here
            metaModel.set(metaModelVersion)
        }
        pom {
            scmUrl.set("https://github.com/OWNER/REPO.git")
            scmConnection.set("scm:git:git@github.com:OWNER/REPO.git")
            developerName.set("yourcompany")
            developerEmail.set("admin@yourcompany.com")
            projectName.set("your cool project based on EDC")
            projectUrl.set("www.coolproject.com")
            description.set("your description")
            licenseUrl.set("https://opensource.org/licenses/MIT")
        }
    }
    */
}

subprojects {
    afterEvaluate {
        if (project.plugins.hasPlugin("com.gradleup.shadow") &&
            file("${project.projectDir}/src/main/docker/Dockerfile").exists()
        ) {

            //actually apply the plugin to the (sub-)project
            apply(plugin = "com.bmuschko.docker-remote-api")
            // configure the "dockerize" task
            val dockerTask: DockerBuildImage = tasks.create("dockerize", DockerBuildImage::class) {
                val dockerContextDir = project.projectDir
                dockerFile.set(file("$dockerContextDir/src/main/docker/Dockerfile"))
                images.add("${project.name}:${project.version}")
                images.add("${project.name}:latest")
                // specify platform with the -Dplatform flag:
                if (System.getProperty("platform") != null)
                    platform.set(System.getProperty("platform"))
                buildArgs.put("JAR", "build/libs/${project.name}.jar")
                inputDir.set(file(dockerContextDir))
            }
            // make sure always runs after "dockerize" and after "copyOtel"
            dockerTask.dependsOn(tasks.named(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME))
        }
    }
}