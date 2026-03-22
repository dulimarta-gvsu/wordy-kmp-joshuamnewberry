import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.3.0"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export("io.github.hoc081098:kmp-viewmodel:0.8.0")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api("io.github.hoc081098:kmp-viewmodel:0.8.0")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
        }
    }
}

android {
    namespace = "edu.gvsu.cis.kmp_wordy"

    // THIS is the line the error is complaining about
    compileSdk = 35

    defaultConfig {
        // Required for an Android Application
        applicationId = "edu.gvsu.cis.kmp_wordy"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}