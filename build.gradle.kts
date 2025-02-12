import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    kotlin("multiplatform") version "2.0.10"
    id("convention.publication")
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
//    id("java-library")
//    jacoco
}

repositories {
    mavenCentral()
}

group = "io.github.mackimaow"
version = "2.0.1"

kotlin {
    jvm()
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Apply the default hierarchical project structure
    applyDefaultHierarchyTemplate()


    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

// Jacoco plugin
//
//jacoco {
//    toolVersion = "0.8.10" // Specify the JaCoCo version
//    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
//}
//
//tasks.getByName("jvmTest") {
//    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
//}
//
//tasks.jacocoTestReport {
//    dependsOn("jvmTest")
//
//    val coverageSourceDirs = files(
//        layout.projectDirectory.dir("src/commonMain/kotlin"),
//        layout.projectDirectory.dir("src/jvmMain/kotlin")
//    )
//
//    val classFiles = fileTree(layout.buildDirectory.dir("classes/kotlin/jvm"))
//    classDirectories.setFrom(classFiles)
//    sourceDirectories.setFrom(coverageSourceDirs)
//
//    executionData
//        .setFrom(layout.buildDirectory.file("jacoco/jvmTest.exec"))
//
//    reports {
//        xml.required = true
//        html.required = true
//    }
//}
