import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    // iOS Targets (arm64 only - device + M-series simulator)
    iosArm64()
    iosSimulatorArm64()
    
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
    
    androidLibrary {
        namespace = "net.gsantner.markor.shared"
        compileSdk = 36
        minSdk = 26
        androidResources.enable = true
        
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "-Xexpect-actual-classes"
            )
        }
    }
    
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "-Xexpect-actual-classes"
            )
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.okio)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.material.kolor)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(compose.materialIconsExtended)
            implementation(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        
        // iOS-specific source sets
        iosMain.dependencies {
            // iOS-specific dependencies can be added here
        }
        iosTest.dependencies {
            implementation(kotlin("test"))
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
}

room {
    schemaDirectory("$projectDir/schemas")
}
