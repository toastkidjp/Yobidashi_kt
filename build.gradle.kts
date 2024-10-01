/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven ( url = "https://jcenter.bintray.com/" )
        maven ( url = "https://maven.google.com" )
        maven ( url = "https://plugins.gradle.org/m2/" ) // For Play publisher plugin
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20")
        classpath("com.github.triplet.gradle:play-publisher:3.10.1")
    }

}

plugins {
    //id("io.gitlab.arturbosch.detekt").version("1.19.0")
    id("com.google.devtools.ksp").version("2.0.20-1.0.25").apply(false)
    id("com.cookpad.android.plugin.license-tools").version("1.2.8")
    id("jacoco")
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
        .configureEach {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_1_8.toString()
            }
        }
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.create("jacocoMergeReport", JacocoReport::class) {
    group = "verification"
    gradle.afterProject { 
        if (rootProject != project && plugins.hasPlugin("jacoco.definition")) {
            executionData.from += "${project.buildDir}/jacoco/testDebugUnitTest.exec"
            sourceDirectories.from += "${project.projectDir}/src/main/java"
            classDirectories.from.addAll(
                project.fileTree("${project.buildDir}/tmp/kotlin-classes/debug") { 
                    exclude("**/view/**", "**/ui/**", "**/material3/**", "**/*UiKt*", "**/*serializer**") 
                }
            )
        }
    }
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
}

/*TODO
task("mergeDetektReport", io.gitlab.arturbosch.detekt.report.ReportMergeTask::class) {
    output = project.layout.buildDirectory.file("reports/detekt/merge.xml")
}

subprojects {
    plugins.withType(io.gitlab.arturbosch.detekt.DetektPlugin) {
        tasks.withType(io.gitlab.arturbosch.detekt.Detekt) { detektTask ->
            finalizedBy(mergeDetektReport)

            mergeDetektReport.configure { mergeTask ->
                mergeTask.input.from(detektTask.xmlReportFile)
            }
        }
    }
}
*/
