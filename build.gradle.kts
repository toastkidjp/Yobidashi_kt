
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

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
        classpath("com.android.tools.build:gradle:9.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
        classpath("com.github.triplet.gradle:play-publisher:4.0.0")
    }

}

plugins {
    id("com.google.devtools.ksp").version("2.3.6").apply(false)
    id("org.jetbrains.kotlinx.kover") version libraries.versions.kover
    kotlin("android") apply false
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            jvmToolchain(21)
        }
    }

    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper> {
        configure<KotlinProjectExtension> {
            jvmToolchain(21)
        }
    }

    plugins.withId("com.android.library") {
        configure<LibraryExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete = setOf(rootProject.layout.buildDirectory.get())
}

kover {
    reports {
        total {
            // common filters for all reports of all variants
            filters {
                // exclusions for reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("jp.toastkid.yobidashi4.infrastructure.di.*")
                    classes("*ComposableSingletons*")
                    classes("*\$inject\$*")
                    packages("org.koin.ksp.generated")
                    packages("jp.toastkid.yobidashi4.library.resources")
                }
            }
        }
    }
}

dependencies {
    // Kover
    kover(project(path = ":data"))
    kover(project(path = ":calendar"))
    kover(project(path = ":lib"))
    kover(project(path = ":todo"))
    kover(project(path = ":barcode:ui"))
    kover(project(path = ":number"))
    kover(project(path = ":barcode:library"))
    kover(project(path = ":article"))
    kover(project(path = ":about"))
    kover(project(path = ":api"))
    kover(project(path = ":app"))
    kover(project(path = ":search"))
    kover(project(path = ":rss"))
    kover(project(path = ":image"))
    kover(project(path = ":licenses"))
    kover(project(path = ":loan"))
    kover(project(path = ":pdf"))
    kover(project(path = ":music"))
    kover(project(path = ":editor"))
}

fun readCoverages(): MutableMap<String, String> {
    var started = false
    val keys = arrayOf(
        "Class",
        "Method",
        "Branch",
        "Line",
        "Instruction"
    );
    val map = mutableMapOf<String, String>()
    val buffer = StringBuffer()

    val file = File("build/reports/kover/html/index.html")
    if (file.exists().not()) {
        return map
    }

    val lines = file.readText().split("\n")
    for (i in (0 until lines.size)) {
        val line = lines[i]
        if (line.contains("<td class=\"name\">all classes</td>")) {
            started = true
        }
        if (!started) {
            continue
        }
        if (line.contains("<span class=\"percent\">")) {
            buffer.append(lines[i + 1].trim())
            continue
        }
        if (line.contains("<span class=\"absValue\">")) {
            buffer.append(" ").append(lines[i + 1].trim())
            map.put("${keys.get(map.size)}", buffer.toString())
            buffer.setLength(0);
            continue
        }
        if (line.contains("</table>")) {
            break
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
