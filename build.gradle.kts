
plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.multiplatform.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    id("nl.littlerobots.version-catalog-update") version "1.1.0"
}

tasks.named<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
