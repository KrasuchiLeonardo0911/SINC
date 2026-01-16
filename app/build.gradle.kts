plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")


    alias(libs.plugins.kotlin.ksp)
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sinc.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sinc.mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            // Se utiliza el keystore de depuración por defecto para el build de 'release'.
            // Esto no es para producción, solo para crear un APK de 'release' para pruebas.
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Habilitar la ofuscación y reducción de código
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // Firmar el build de 'release'
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material3:material3:1.2.0-beta02")
    implementation(libs.androidx.navigation.compose)
    implementation(project(":domain"))
    implementation(project(":data"))

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-messaging")

    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    testImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Accompanist
    implementation("com.google.accompanist:accompanist-placeholder-material3:0.32.0")

    // Material (for pull-to-refresh)
    implementation("androidx.compose.material:material")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")

    // Lottie for animations
    implementation(libs.lottie.compose)

    // osmdroid for OpenStreetMap
    implementation(libs.osmdroid)
    implementation(libs.androidx.preference)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Force IDE sync