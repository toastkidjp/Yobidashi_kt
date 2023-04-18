import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
  id("jacoco")
}

repositories.mavenCentral()

jacoco {
    toolVersion = "0.8.8"
    //reportsDirectory.set(layout.buildDirectory.dir("coverage"))
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class.java) {
    group = "verification"
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(false)
        html.required.set(true)
        html.outputLocation.set(file("${buildDir}/reports/code-coverage"))
    }
    sourceDirectories.setFrom("${projectDir}/src/main/java")
    classDirectories.setFrom("${buildDir}/tmp/kotlin-classes/debug")
    executionData.setFrom("${buildDir}/jacoco/testDebugUnitTest.exec")
}
