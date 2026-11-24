import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
}

val localProperties = Properties().apply {
    val fichier = rootProject.file("local.properties")
    if (fichier.exists()) fichier.inputStream().use { load(it) }
}

android {
    namespace = "com.example.apex"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.apex"
        // java.time (Instant, ZoneId, DateTimeFormatter) est disponible nativement dès l'API 26.
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        val apiKey = localProperties.getProperty("MOVEBASE_API_KEY")
            ?: System.getenv("MOVEBASE_API_KEY") ?: ""
        val escapedKey = apiKey.replace("\\", "\\\\").replace("\"", "\\\"")
        buildConfigField("String", "MOVEBASE_API_KEY", "\"$escapedKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        // Fait apparaître Apex dans « Langue de l'application » (Android 13+).
        generateLocaleConfig = true
    }

    sourceSets {
        // Les schémas exportés servent au test de migration (MigrationTestHelper).
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    buildTypes {
        debug {
            isPseudoLocalesEnabled = true
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.room.testing)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.location)
    implementation(libs.androidx.biometric)
    // biometric tire fragment 1.2.x, incompatible avec rememberLauncherForActivityResult dans une
    // FragmentActivity (« Can only use lower 16 bits for requestCode ») : corrigé dès 1.3.0.
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}