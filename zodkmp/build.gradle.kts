import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    `maven-publish`
    signing
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

// Configure publications for Maven Central
publishing {
    publications {
        withType<MavenPublication> {
            // Configure artifact IDs for different variants
            // Only set artifactId for main publications to avoid conflicts
            if (name == "kotlinMultiplatform") {
                artifactId = "zodkmp"
            } else if (name == "androidRelease") {
                artifactId = "zodkmp"
            } else if (name == "androidDebug") {
                artifactId = "zodkmp"
            }
            
            pom {
                name.set("ZodKmp")
                description.set("A Kotlin Multiplatform library for Zod-like validation")
                url.set("https://github.com/piashcse/zodkmp")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("piashcse")
                        name.set("Mehedi Hassan Piash")
                        email.set("piash599@gmail.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:https://github.com/piashcse/zodkmp.git")
                    developerConnection.set("scm:git:ssh://github.com/piashcse/zodkmp.git")
                    url.set("https://github.com/piashcse/zodkmp")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "Central"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
        
        // For testing purposes - local repository
        maven {
            name = "Local"
            url = uri("$buildDir/repository")
        }
    }
}

// Signing configuration
signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD")
    )
    sign(publishing.publications)
    isRequired = !project.hasProperty("skipSigning")
}