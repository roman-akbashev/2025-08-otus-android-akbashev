import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.linguacards.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mock)
    testImplementation(libs.kotlin.coroutines.test)
}