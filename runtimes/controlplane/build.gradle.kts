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

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
//    runtimeOnly(libs.bundles.sed.connector.controlplane)

    runtimeOnly(project(":extensions:banner-extension"))

    runtimeOnly(libs.edc.bom.controlplane.base)
    runtimeOnly(libs.edc.ext.controlplane.api) // already included in controlplane.base from v0.12
    runtimeOnly(libs.edc.policy.monitor.core) // already included in controlplane.base from v0.12


    // Persistence
    runtimeOnly(libs.edc.bom.controlplane.sql)
    runtimeOnly(libs.edc.ext.policy.monitor.store.sql) // already included in controlplane.sql from v0.12
    println("This runtime compiles with PostgreSQL persistence. You will need a properly configured Postgres instance")


    // Decentralized auth dependencies: Crypto (JWT,LDP) and DIDs
    implementation(libs.edc.iam.mock)
//    runtimeOnly(libs.edc.ext.did)
//    runtimeOnly(libs.edc.ext.jwtvc)
//    runtimeOnly(libs.edc.ext.ldpvc)


    // HTTP transfer dependencies
    runtimeOnly(libs.edc.ext.transfer.pull.http.dynamicreceiver)
    println("This runtime uses \"HTTP Dynamic EDR receiver\". Remember this was deprecated after v0.11.1. See [https://github.com/eclipse-edc/Connector/tree/main/docs/developer/decision-records/2025-02-07-http-proxy-data-plane-deprecation] and [https://github.com/eclipse-edc/Samples/blob/main/transfer/README.md] for more info.")
    runtimeOnly(libs.edc.ext.validator.dataaddress.http.data)

}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

edcBuild {
    publish.set(false)
}

/*
// configure the "dockerize" task
tasks.register("dockerize", DockerBuildImage::class) {
    val dockerContextDir = project.projectDir
    dockerFile.set(file("$dockerContextDir/src/main/docker/Dockerfile"))
    images.add("${project.name}:${project.version}")
    images.add("${project.name}:latest")
    // specify platform with the -Dplatform flag:
    if (System.getProperty("platform") != null)
        platform.set(System.getProperty("platform"))
    buildArgs.put("JAR", "build/libs/${project.name}.jar")
    inputDir.set(file(dockerContextDir))
    dependsOn(tasks.named(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME))
}
*/
