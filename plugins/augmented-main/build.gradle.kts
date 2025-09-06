plugins {
    id("org.jetbrains.intellij") version "1.16.0"
    kotlin("jvm") version "1.9.22"
}

group = "com.example"
version = "0.1.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.1")
    type.set("IC")
    plugins.set(listOf("java"))
}

kotlin {
    jvmToolchain(17)
}
