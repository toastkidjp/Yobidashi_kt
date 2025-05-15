import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.regex.Matcher
import java.util.regex.Pattern

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
        maven ( url = "https://maven.google.com" )
        maven ( url = "https://plugins.gradle.org/m2/" ) // For Play publisher plugin
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.2")
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
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_1_8)
            }
        }
}

tasks.register("clean", Delete::class) {
    delete = setOf(rootProject.layout.buildDirectory.get())
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.register("jacocoMergedTestReport", JacocoReport::class) {
    group = "verification"
    gradle.afterProject { 
        if (rootProject != project && plugins.hasPlugin("jacoco.definition")) {
            executionData.from += "${project.layout.buildDirectory.get()}/jacoco/testDebugUnitTest.exec"
            sourceDirectories.from += "${project.projectDir}/src/main/java"
            classDirectories.from.addAll(
                project.fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
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

fun readCoverages(): MutableMap<String, String> {
    var started = false
    val map = mutableMapOf<String, String>()
    val buffer = StringBuffer()

    val file = File("build/reports/jacoco/jacocoMergedTestReport/html/index.html")

    if (file.exists().not()) {
        return map
    }

    val lines = file.readText().split(">").map(String::trim)
    for (i in (0 until lines.size)) {
        val line = lines[i]
        if (line.contains("Total</td")) {
            started = true
        }
        if (!started) {
            continue
        }

        if (line.contains("<td class=\"bar\"")) {
            buffer.append(lines[i + 1].split("<")[0])
            continue
        }
        if (line.contains("<td class=\"ctr2\"")) {
            buffer.append()
            val key = if (map.size == 0) "Instruction" else "Branch"
            map.put(key, "${lines[i + 1].split("<")[0]} (${buffer.toString().replace(" of ", "/").split("<")[0]})")
            buffer.setLength(0)
            if (map.size == 2) {
                break
            }
            continue
        }
    }
    return map
}

tasks.register("printCoverageSummary") {
    val map = readCoverages()

    doLast {
        println("| Category | Coverage(%)\n|:---|:---")
        map.map { "| ${it.key} | ${it.value}" }.forEach(::println)
    }
}

/*TODO
task("mergeDetektReport", io.gitlab.arturbosch.detekt.report.ReportMergeTask::class) {
    output = project.buildDir.file("reports/detekt/merge.xml")
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
