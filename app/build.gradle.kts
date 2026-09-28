plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zybergo.browser"
    compileSdk = 34

    // Keep Java and Kotlin bytecode targets consistent in CI.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        applicationId = "com.zybergo.browser"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // Vector drawables instead of PNGs at multiple densities -> smaller APK, less bitmap RAM
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Deliberately minimal dependency set. Every extra library is RAM/APK-size cost.
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // EncryptedSharedPreferences
    implementation("androidx.recyclerview:recyclerview:1.3.2")        // tab strip, lightweight
    // NOTE: intentionally no Compose, no Glide/Coil, no DI framework (Hilt/Koin).
    // Manual DI via a small ServiceLocator keeps process memory baseline lower.
}
