plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":zodkmp"))
    ksp(project(":zodkmp-ksp"))
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
}
