import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ZodKmp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.piashcse.zodkmp.lib"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Configure publishing with vanniktech plugin - automatically configured by plugin for Kotlin Multiplatform
mavenPublishing {
    pom {
        name = findProperty("POM_NAME")?.toString() ?: "ZodKmp"
        description = findProperty("POM_DESCRIPTION")?.toString() ?: "A Kotlin Multiplatform library for Zod-like validation"
        inceptionYear = findProperty("POM_INCEPTION_YEAR")?.toString() ?: "2024"
        url = findProperty("POM_URL")?.toString() ?: "https://github.com/piashcse/zodkmp"

        licenses {
            license {
                name = findProperty("POM_LICENSE_NAME")?.toString() ?: "MIT License"
                url = findProperty("POM_LICENSE_URL")?.toString() ?: "https://opensource.org/licenses/MIT"
                distribution = findProperty("POM_LICENSE_DIST")?.toString() ?: "repo"
            }
        }

        developers {
            developer {
                id = findProperty("POM_DEVELOPER_ID")?.toString() ?: "piashcse"
                name = findProperty("POM_DEVELOPER_NAME")?.toString() ?: "Mehedi Hassan Piash"
                url = findProperty("POM_URL")?.toString() ?: "https://github.com/piashcse"
            }
        }

        scm {
            url = findProperty("POM_SCM_URL")?.toString() ?: "https://github.com/piashcse/zodkmp"
            connection = findProperty("POM_SCM_CONNECTION")?.toString() ?: "scm:git:https://github.com/piashcse/zodkmp.git"
            developerConnection = findProperty("POM_SCM_DEV_CONNECTION")?.toString() ?: "scm:git:ssh://github.com/piashcse/zodkmp.git"
        }
    }
}