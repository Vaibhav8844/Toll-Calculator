plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tollcalculator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tollcalculator"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.gms:play-services-location:21.0.1")  // For GPS location updates
    implementation("org.osmdroid:osmdroid-android:6.1.11")  // For map rendering
    implementation("com.opencsv:opencsv:5.7.1")  // For reading CSV files
    implementation("com.google.code.gson:gson:2.9.1")  // For JSON parsing
}

