import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" && requested.name == "kotlinx-metadata-jvm") {
            useVersion("2.1.0")
        }
    }
}


plugins {
    alias(libs.plugins.android.application)
    // alias(libs.plugins.kotlin.android) // Built-in with AGP 9.0

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
}

android {
    compileSdk = 36

    defaultConfig {
        resValue("string", "manifest_package_id", "net.gsantner.markor")
        applicationId = "net.gsantner.markor"
        versionName = "2.15.2"
        versionCode = 161
        minSdk = 26
        targetSdk = 36

        buildConfigField("boolean", "IS_TEST_BUILD", "false")
        buildConfigField("boolean", "IS_GPLAY_BUILD", "false")

        androidResources {
            localeFilters += listOf(
                "en", "de", "it", "es", "fr", "ru", "zh", "ja", "ko", "pt", "nl", "pl", "tr",
                "cs", "vi", "ar", "hi", "th", "uk", "ca", "el", "fa", "he", "hu", "in", "no",
                "ro", "sv", "bg", "hr", "da", "fi", "sk", "sl", "uk"
            )
        }

    }

    flavorDimensions += "default"
    productFlavors {
        create("flavorAtest") {
            applicationId = "net.gsantner.markor_test"
            versionCode = SimpleDateFormat("yyMMdd", Locale.US).format(Date()).toInt()
            versionName = "${android.defaultConfig.versionName}-${SimpleDateFormat("HHmm", Locale.US).format(Date())}"
            buildConfigField("boolean", "IS_TEST_BUILD", "true")
            buildConfigField("boolean", "IS_GPLAY_BUILD", "false")
        }
        create("flavorDefault") {}
        create("flavorGplay") {
            buildConfigField("boolean", "IS_GPLAY_BUILD", "true")
            buildConfigField("boolean", "IS_TEST_BUILD", "false")
        }
    }

    sourceSets {
        getByName("main") {
            assets.directories += setOf("src/main/assets", "thirdparty/assets")
            kotlin.directories += setOf("src/main/kotlin")
            res.directories += setOf("src/main/res", "thirdparty/res")
        }
        getByName("test") {
            kotlin.directories += setOf("src/test/kotlin")
        }
        getByName("androidTest") {
            kotlin.directories += setOf("src/androidTest/kotlin")
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

    packaging {
        resources {
            excludes += listOf(
                "META-INF/LICENSE-LGPL-2.1.txt",
                "META-INF/LICENSE-LGPL-3.txt",
                "META-INF/LICENSE-W3C-TEST"
            )
        }
    }

    compileOptions {
        encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false
        resValues = true
    }

    namespace = "net.gsantner.markor"

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
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}

dependencies {
    implementation(project(":shared"))
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)

    // implementation(platform(libs.androidx.compose.bom)) // JetBrains Compose manages versions needed
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.preview)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.androidx.material3.window)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    
    debugImplementation(compose.uiTooling)
    // debugImplementationAttribute(libs.androidx.ui.test.manifest) // UI Test manifest

    implementation(libs.androidx.datastore.preferences)

    // Glance for widgets
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.bundles.flexmark)

    implementation(libs.opencsv) {
        exclude(group = "commons-beanutils", module = "commons-beanutils")
    }

    implementation(libs.appintro)
    implementation(libs.colorpicker)
    implementation(libs.geneRate)
    implementation(libs.epubParser)

    implementation(libs.material.kolor)
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.koin.android)
}
