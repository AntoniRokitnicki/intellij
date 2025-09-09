import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.intellij.platform") version "2.0.1"
    kotlin("jvm") version "1.9.21"
}

group = "com.yourorg"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "GitHub Read-Only (gh)"
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "241.*"
        }
    }
}

dependencies {
    intellijPlatform {
        create("IC")
        bundledPlugin("com.intellij.java")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

sourceSets {
    main {
        kotlin.srcDirs("src")
        resources.srcDirs("resources")
    }
    test {
        kotlin.srcDirs("test")
        resources.srcDirs("testResources")
    }
}

tasks {
    buildSearchableOptions { enabled = false }
}
