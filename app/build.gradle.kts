import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.3.0"
}
val localProps = Properties()
val localPropertiesFile = File(rootProject.rootDir,"local.properties")
if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
    localPropertiesFile.inputStream().use {
        localProps.load(it)
    }
}
android {
    namespace = "com.example.habitverse"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.habitverse"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_KEY", localProps.getProperty("API_KEY"))// IF any
        }
        debug {
            buildConfigField("String", "API_KEY", localProps.getProperty("API_KEY"))// IF any
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11) // Sets jvmTarget to JVM_11 and configures Java tasks
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    ksp(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation (libs.material.v1120)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.dagger:hilt-android:2.59.1")
    ksp("com.google.dagger:hilt-android-compiler:2.59.1")
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))
    // other dependencies
    // Compose
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.accompanist:accompanist-themeadapter-material3:0.36.0")


    debugImplementation("androidx.compose.ui:ui-tooling")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("androidx.hilt:hilt-work:1.3.0")

    // Annotation processor (use ksp if configured, otherwise kapt)
    ksp("androidx.hilt:hilt-compiler:1.3.0")

    // Standard WorkManager dependency
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation ("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    // Source: https://mvnrepository.com/artifact/com.squareup.okhttp3/logging-interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")

}