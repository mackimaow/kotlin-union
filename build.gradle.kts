plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("convention.publication")
    alias(libs.plugins.kover)
}

repositories {
    mavenCentral()
}

group = "io.github.mackimaow"
version = "3.0.0"

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
                implementation(libs.coroutines.test)
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
