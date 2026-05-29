import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // Uncomment after placing google-services.json in app/ :
    // alias(libs.plugins.google.services)
    // alias(libs.plugins.firebase.crashlytics.gradle)
}

android {
    namespace = "com.weightflow"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.weightflow"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Try environment variables first (CI), then local.properties
            val storePathEnv = System.getenv("KEYSTORE_PATH")?.takeIf { it.isNotEmpty() }
            val storePassEnv = System.getenv("KEYSTORE_PASSWORD")?.takeIf { it.isNotEmpty() }
            val keyAliasEnv = System.getenv("KEY_ALIAS")?.takeIf { it.isNotEmpty() }
            val keyPassEnv = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotEmpty() }

            val props = Properties()
            val localPropsFile = rootProject.file("local.properties")
            if (localPropsFile.exists()) props.load(localPropsFile.inputStream())

            val storeFilePath = storePathEnv ?: props.getProperty("KEYSTORE_PATH")
            storeFile = storeFilePath?.let { file(it) }
            storePassword = storePassEnv ?: props.getProperty("KEYSTORE_PASSWORD") ?: ""
            keyAlias = keyAliasEnv ?: props.getProperty("KEY_ALIAS") ?: ""
            keyPassword = keyPassEnv ?: props.getProperty("KEY_PASSWORD") ?: ""
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
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

kotlin {
    jvmToolchain(17)
}

afterEvaluate {
    // Dedicated validation task
    tasks.register("validateReleaseSigning") {
        doFirst {
            val signingConfig = android.signingConfigs.getByName("release")
            val storeFile = signingConfig.storeFile
            val storePassword = signingConfig.storePassword
            val keyAlias = signingConfig.keyAlias
            val keyPassword = signingConfig.keyPassword

            val missing = mutableListOf<String>()
            if (storeFile == null || !storeFile.exists()) missing.add("storeFile (KEYSTORE_PATH)")
            if (storePassword.isNullOrBlank()) missing.add("storePassword (KEYSTORE_PASSWORD)")
            if (keyAlias.isNullOrBlank()) missing.add("keyAlias (KEY_ALIAS)")
            if (keyPassword.isNullOrBlank()) missing.add("keyPassword (KEY_PASSWORD)")

            if (missing.isNotEmpty()) {
                error("""
                    Release signing validation failed. Missing credentials:
                    ${missing.joinToString("\n    ") { "  - $it" }}

                    Provide via environment variables (CI) or local.properties (local dev):
                      • KEYSTORE_PATH / KEYSTORE_PATH env
                      • KEYSTORE_PASSWORD / KEYSTORE_PASSWORD env
                      • KEY_ALIAS / KEY_ALIAS env
                      • KEY_PASSWORD / KEY_PASSWORD env

                    Example local.properties:
                      KEYSTORE_PATH=/Users/you/.android/release.jks
                      KEYSTORE_PASSWORD=your-keystore-password
                      KEY_ALIAS=your-key-alias
                      KEY_PASSWORD=your-key-password
                """.trimIndent())
            }
        }
    }

    // Make assembleRelease depend on validateReleaseSigning
    tasks.named("assembleRelease").configure {
        dependsOn("validateReleaseSigning")
    }
    tasks.named("bundleRelease").configure {
        dependsOn("validateReleaseSigning")
    }
}

dependencies {
    // Core
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Charts
    implementation(libs.vico.compose.m3)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // SQLCipher — encrypted Room database (Android Keystore-backed key)
    implementation(libs.sqlcipher.android)

    // kotlin-csv — RFC-4180 compliant CSV parsing
    implementation(libs.kotlin.csv)

    // zip4j — AES-256 encrypted ZIP export (dependency audit pending before merge)
    implementation(libs.zip4j)

    // security-crypto — EncryptedSharedPreferences for stable database passphrase storage
    implementation(libs.androidx.security.crypto)

    // Firebase — uncomment after placing google-services.json in app/ and enabling plugins
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.crashlytics)
    // implementation(libs.firebase.analytics)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    // Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
