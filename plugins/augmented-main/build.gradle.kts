plugins {
    id("org.jetbrains.intellij.platform") version "2.0.1"
    kotlin("jvm") version "1.9.22"
}

group = "com.example"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        create("IC", "2023.1")
        bundledPlugin("com.intellij.java")
    }
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        kotlin.srcDir("src/main/kotlin")
        resources.srcDir("src/main/resources")
    }
    test {
        kotlin.srcDir("src/test/kotlin")
    }
}
