
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.multiplatform.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}

val gitHash: String by lazy {
    providers.exec {
        commandLine("git", "rev-parse", "HEAD")
    }.standardOutput.asText.get().trim()
}

val gitLastCommitMessage: String by lazy {
    providers.exec {
        commandLine("git", "log", "--oneline", "-1", "--format=%s")
    }.standardOutput.asText.get().trim()
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

tasks.register<Copy>("copyRepoFiles") {
    from(listOf("README.md", "CHANGELOG.md", "LICENSE.txt"))
    into("app/src/main/res/raw")
    rename { fileName -> fileName.lowercase() }
}

tasks.register("assemble") {
    dependsOn("copyRepoFiles")
}
