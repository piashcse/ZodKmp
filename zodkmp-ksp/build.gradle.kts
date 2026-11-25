plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ktlint)
}

kotlin {
    explicitApi()
    jvmToolchain(21)
}

dependencies {
    implementation(libs.ksp.api)
}

mavenPublishing {
    coordinates(
        artifactId = "zodkmp-ksp",
        version = version.toString(),
    )

    pom {
        name = "ZodKmp KSP"
        description = "KSP processor that generates ZodKmp schemas for @ZodModel data classes"
        inceptionYear = "2025"
        url = "https://github.com/piashcse/zodkmp"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "piashcse"
                name = "Mehedi Hassan Piash"
                url = "https://github.com/piashcse"
                email = "piash599@gmail.com"
            }
        }
        scm {
            url = "https://github.com/piashcse/zodkmp"
            connection = "scm:git:https://github.com/piashcse/zodkmp.git"
            developerConnection = "scm:git:ssh://github.com/piashcse/zodkmp.git"
        }
    }
}
