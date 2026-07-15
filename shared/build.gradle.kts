@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.androidx.room)
}

kotlin {
    // iOS Targets
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi"
        )
    }

    // iOS Framework configuration for Xcode integration
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    android {
        namespace = "com.bernaferrari.remarkor.shared"
        compileSdk = 37
        minSdk = 26
        androidResources.enable = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    wasmJs {
        browser()
    }

    sourceSets {
        val nativeStorageMain by creating {
            dependsOn(commonMain.get())
        }

        // Workaround: CMP Resources does not wire generated sources when using the AGP 9
        // androidLibrary plugin (com.android.kotlin.multiplatform.library).
        // Track: https://youtrack.jetbrains.com/issue/CMP-7611
        // When fixed, remove this block and rely on default compose.resources { generateResClass = auto }.
        val composeGeneratedKotlin = layout.buildDirectory.dir("generated/compose/resourceGenerator/kotlin")
        fun org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.addComposeGeneratedSources(vararg dirs: String) {
            dirs.forEach { dir -> kotlin.srcDir(composeGeneratedKotlin.map { it.dir(dir) }) }
        }

        commonMain {
            addComposeGeneratedSources(
                "commonResClass",
                "commonMainResourceAccessors",
                "commonMainResourceCollectors",
            )
        }
        androidMain { addComposeGeneratedSources("androidMainResourceCollectors") }
        jvmMain { addComposeGeneratedSources("jvmMainResourceCollectors") }
        iosMain { addComposeGeneratedSources("iosMainResourceCollectors") }
        iosArm64Main { addComposeGeneratedSources("iosArm64MainResourceCollectors") }
        iosSimulatorArm64Main { addComposeGeneratedSources("iosSimulatorArm64MainResourceCollectors") }
        wasmJsMain { addComposeGeneratedSources("wasmJsMainResourceCollectors") }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.okio)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.material.kolor)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.ui.tooling.preview)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.coil.compose)
            // Room 3 common schema (entities/DAO/DB); drivers are platform-specific.
            implementation(libs.androidx.room.runtime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutines.test)
        }
        nativeStorageMain.dependencies {
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.sqlite.bundled)
        }

        androidMain {
            dependsOn(nativeStorageMain)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        jvmMain {
            dependsOn(nativeStorageMain)
        }

        // iOS-specific source sets
        iosMain {
            dependsOn(nativeStorageMain)
        }
        iosMain.dependencies {
            // iOS-specific dependencies can be added here
        }
        iosTest.dependencies {
            implementation(kotlin("test"))
        }
        wasmJsMain.dependencies {
            implementation("com.squareup.okio:okio-fakefilesystem:${libs.versions.okio.get()}")
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
            // Room 3 web (official): WebWorkerSQLiteDriver + local worker package
            // https://developer.android.com/jetpack/androidx/releases/room3#3.0.0
            implementation(libs.androidx.sqlite.web)
            implementation(npm("sqlite-web-worker", file("sqlite-web-worker")))
        }
    }

    // Disable iOS tests on non-Apple hosts
    tasks.withType<KotlinNativeTest> {
        enabled = org.apache.tools.ant.taskdefs.condition.Os.isFamily("mac")
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspWasmJs", libs.androidx.room.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    // `auto` does not emit Res.kt with androidLibrary; see CMP-7611 above.
    // Docs: https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html
    generateResClass = org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration.Always
}
