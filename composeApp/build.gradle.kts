import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    id("io.insert-koin.compiler.plugin")
    id ("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    applyDefaultHierarchyTemplate()

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
            baseName = "ComposeApp"
//            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("io.insert-koin:koin-android:3.4.1")
            implementation("io.ktor:ktor-client-android:3.4.1")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(compose.materialIconsExtended)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
            implementation("io.github.hoc081098:kmp-viewmodel:0.8.0")
            implementation("io.github.hoc081098:kmp-viewmodel-compose:0.8.0")
            implementation("io.github.hoc081098:kmp-viewmodel-savedstate:0.8.0")
            implementation("io.insert-koin:koin-core:3.4.1")
            implementation("io.github.hoc081098:kmp-viewmodel-koin-compose:0.8.0")
            implementation("io.ktor:ktor-client-logging:3.4.1")
            implementation("io.ktor:ktor-client-core:3.4.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.4.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "edu.gvsu.cis.kmp_wordy"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "edu.gvsu.cis.kmp_wordy"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}


dependencies {
    debugImplementation(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}