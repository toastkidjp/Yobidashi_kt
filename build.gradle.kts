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
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
        classpath("com.github.triplet.gradle:play-publisher:3.7.0")
    }

}

plugins {
    //id("io.gitlab.arturbosch.detekt").version("1.19.0")
    id("com.cookpad.android.plugin.license-tools").version("1.2.8")
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.8"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // For PhotoView
    }
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}

/*TODO
task("jacocoMergedTestReport", JacocoReport::class)(
    //group: "verification"
) {
    gradle.afterProject { project, _ ->
        if (project.rootProject != project && project.plugins.hasPlugin('jacoco')) {
            getExecutionData().from += "${project.buildDir}/jacoco/testDebugUnitTest.exec"
            getSourceDirectories().from += "${project.projectDir}/src/main/java"
            getClassDirectories().from += project.fileTree(dir = "${project.buildDir}/tmp/kotlin-classes/debug")
        }
    }
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
*/

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
