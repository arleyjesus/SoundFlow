plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" // Plugin KSP compatible con Kotlin 2.1.0
}

android {
    namespace = "com.arleyaetheraudio"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.comarleyaetheraudio"
        minSdk = 24
        targetSdk = 35
        versionCode = 6          // Incrementado para v2.2.0
        versionName = "3.1.0"    // Versión oficial 2.2.0

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Core & Lifecycle
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.documentfile)

    // Jetpack Compose UI
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation(libs.androidx.palette.ktx)

    // Base de datos local (Room con KSP)
    val roomVersion = "2.7.0-alpha13" // Versión de Room con soporte nativo para Kotlin 2.1+
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Media3 (ExoPlayer & MediaSession)
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-session:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")

    // Coil (Carátulas)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Librería para leer y editar etiquetas ID3 de MP3/FLAC
    implementation("net.jthink:jaudiotagger:3.0.1")

    configurations.all {
        resolutionStrategy {
            force("androidx.core:core-ktx:1.12.0")
            force("androidx.annotation:annotation:1.7.0")
        }
    }

    android {
        // ... tu configuración actual ...

        // Desactiva el fallo por conflicto de AAR Metadata
        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
        }
    }

}