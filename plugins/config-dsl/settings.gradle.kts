pluginManagement {
    repositories {
        maven {
            url = uri("https://cache-redirector.jetbrains.com/maven.pkg.jetbrains.space/public/p/ij/intellij-platform")
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "config-dsl"
