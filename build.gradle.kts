import java.io.File

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// Set the version for the library
allprojects {
    version = providers.gradleProperty("library.version").getOrElse("1.0.0")
    group = "io.github.piashcse"
}

// Configure repositories for publishing
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Task to bump the library version
tasks.register("bumpVersion") {
    doLast {
        val newVersion = project.findProperty("to") ?: error("Please specify version with -Pto=VERSION")
        val gradlePropertiesFile = File(rootDir, "gradle.properties")
        val lines = gradlePropertiesFile.readLines()
        val updatedLines = lines.map { line ->
            if (line.startsWith("library.version=")) {
                "library.version=$newVersion"
            } else {
                line
            }
        }
        
        gradlePropertiesFile.writeText(updatedLines.joinToString(System.lineSeparator()))
        println("Updated library.version to $newVersion in gradle.properties")
    }
}