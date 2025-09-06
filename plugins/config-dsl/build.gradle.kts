plugins {
    id("org.jetbrains.intellij.platform") version "2.0.1"
    kotlin("jvm") version "1.9.22"
}

group = "com.example"
version = "0.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2024.1")
        bundledPlugin("org.jetbrains.kotlin")
        instrumentationTools()
    }
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    pluginConfiguration {
        name = "Config DSL"
        ideaVersion {
            sinceBuild = "232"
        }
    }
}
