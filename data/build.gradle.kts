plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.ksp)
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.sinc.mobile.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "com.sinc.mobile.HiltTestRunner" // Custom Hilt Test Runner
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    implementation(project(":domain"))

    // Retrofit & Kotlinx Serialization
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging.interceptor)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Location Services
    implementation(libs.google.play.services.location)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.google.truth)
    androidTestImplementation(libs.hilt.android.testing) // Hilt testing dependency
    kspAndroidTest(libs.hilt.android.compiler) // Hilt compiler for androidTest
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.mockwebserver) // Added for testing API calls
}
