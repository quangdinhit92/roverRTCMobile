plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.plugin)
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization") version "2.1.10"
}

android {
    namespace = "com.dinh.myfirstkmm.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.dinh.myfirstkmm.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }

    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)

  //  implementation(libs.hilt.viewmodel.navigation)

    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")


    implementation("io.github.webrtc-sdk:android:125.6422.06.1")
    implementation("com.squareup.moshi:moshi:1.15.0")  // Replace with the latest version
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")  // For Kotlin support
    implementation(libs.org.json)
    implementation("androidx.compose.material:material:1.5.1")

}
//kapt {
//    correctErrorTypes = true
//}