import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

group = project.property("group")!!
version = project.property("version")!!

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        // TARGET: IntelliJ IDEA base stabile moderna
        intellijIdeaCommunity("2024.3")

        testFramework(TestFrameworkType.Platform)
    }
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // minimum supported version (IMPORTANT)
            sinceBuild = "243"

            // allow future minor/patch versions
            untilBuild = ""
        }
    }
}