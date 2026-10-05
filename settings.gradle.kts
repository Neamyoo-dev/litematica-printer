pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net") { name = "Fabric" }
    }
}

rootProject.name = "litematica-printer"
include(":26.2")
project(":26.2").apply {
    projectDir = file("versions/26.2")
    buildFileName = "../../build.unobfuscated.gradle.kts"
}
