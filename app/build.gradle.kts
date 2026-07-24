plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.npuevalidationtest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.npuevalidationtest"
        minSdk = 34  // Android 14+ for LiteRT NPU
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // LiteRT (Google AI Edge) - use versions from Google's Maven
    implementation("com.google.ai.edge.litert:litert:1.18.0")
    implementation("com.google.ai.edge.litert:litert-support:0.4.0")
    
    // NPU delegate is part of the main litert artifact in newer versions
}