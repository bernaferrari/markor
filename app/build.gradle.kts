import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

android {
    compileSdk = 36

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "com.bernaferrari.remarkor"
        versionName = "0.1"
        versionCode = 1
        minSdk = 26
        targetSdk = 36
    }

    flavorDimensions += "default"
    productFlavors {
        create("flavorAtest") {
            applicationId = "com.bernaferrari.remarkor_test"
            versionCode = SimpleDateFormat("yyMMdd", Locale.US).format(Date()).toInt()
            versionName = "${android.defaultConfig.versionName}-${
                SimpleDateFormat("HHmm", Locale.US).format(Date())
            }"
        }
        create("flavorDefault") {}
        create("flavorGplay") {
        }
    }

    sourceSets {
        getByName("main") {
            assets.directories += setOf("src/main/assets", "thirdparty/assets")
            res.directories += setOf("src/main/res", "thirdparty/res")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    namespace = "com.bernaferrari.remarkor"

    lint {
        abortOnError = false
        disable += listOf(
            "MissingTranslation", "InvalidPackage", "ObsoleteLintCustomCheck",
            "DefaultLocale", "UnusedAttribute", "VectorRaster", "InflateParams",
            "IconLocation", "UnusedResources", "TypographyEllipsis"
        )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi"
        )
    }
}

dependencies {
    implementation(project(":shared"))
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.datastore.preferences)

    // Glance for widgets
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(libs.bundles.coroutines)
    implementation(libs.koin.android)
}
