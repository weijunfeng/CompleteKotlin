pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("de.fayard.refreshVersions") version "0.30.2"
}

rootProject.name = "CompleteKotlin"
include("plugin")
