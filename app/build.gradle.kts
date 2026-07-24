plugins {
    id("com.android.application")
    // AGP 9.0+ has built-in Kotlin support - do NOT apply org.jetbrains.kotlin.android
}

android {
    namespace = "com.example.npuevalidationtest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.npuevalidationtest"
        minSdk = 34
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
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.compose.material3:material3:1.2.1")
    
    // LiteRT - use same version as cipher-android (available on Google Maven)
    implementation("com.google.ai.edge.litert:litert:2.1.5")
    // litert-support may not be available; NPU delegate is in main litert artifact
}