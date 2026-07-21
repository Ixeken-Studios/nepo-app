import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.ixeken.nepo"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ixeken.nepo"
        minSdk = 34
        targetSdk = 37
        versionCode = 4
        versionName = "1.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            val keyAliasVal = keystoreProperties["keyAlias"] as? String ?: System.getenv("KEY_ALIAS")
            val keyPasswordVal = keystoreProperties["keyPassword"] as? String ?: System.getenv("KEY_PASSWORD")
            val storePasswordVal = keystoreProperties["storePassword"] as? String ?: System.getenv("STORE_PASSWORD")
            val storeFileVal = keystoreProperties["storeFile"] as? String ?: System.getenv("STORE_FILE")

            if (keyAliasVal != null && keyPasswordVal != null && storePasswordVal != null && storeFileVal != null) {
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
                storeFile = rootProject.file(storeFileVal)
                storePassword = storePasswordVal
            } else {
                // Fallback gracefully to debug credentials so build doesn't crash on clean checkouts
                val debugConfig = signingConfigs.getByName("debug")
                keyAlias = debugConfig.keyAlias
                keyPassword = debugConfig.keyPassword
                storeFile = debugConfig.storeFile
                storePassword = debugConfig.storePassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // Configuración Avanzada de Optimización R8
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), 
                "proguard-rules.pro"
            )
        }
        debug {
            // Modo de compilación rápida para desarrollo local
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:mathematics"))
    implementation(project(":features:calculator"))
    implementation(project(":features:converter"))
    implementation(libs.icons.lucide)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}