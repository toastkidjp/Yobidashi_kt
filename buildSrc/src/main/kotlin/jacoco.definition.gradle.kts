plugins {
  id("jacoco")
}

repositories.mavenCentral()

jacoco {
    toolVersion = "0.8.13"
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
        html.outputLocation.set(file("${layout.buildDirectory.get()}/reports/code-coverage"))
    }
    sourceDirectories.setFrom("${projectDir}/src/main/java")
    classDirectories.setFrom(
        fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
            exclude("**/view/**", "**/ui/**") 
        }
    )
    executionData.setFrom("${layout.buildDirectory.get()}/jacoco/testDebugUnitTest.exec")
}
