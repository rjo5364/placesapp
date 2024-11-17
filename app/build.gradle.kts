import java.util.Properties
val localProperties = Properties()
localProperties.load(project.rootProject.file("local.properties").inputStream())

project.extensions.extraProperties["MAPS_API_KEY"] = localProperties["MAPS_API_KEY"]

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "edu.psu.sweng888.placesapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.psu.sweng888.placesapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val mapsApiKey: String? = project.findProperty("MAPS_API_KEY") as String?
        buildConfigField("String", "MAPS_API_KEY", "\"${mapsApiKey ?: ""}\"")



    }
    buildFeatures {
        buildConfig = true
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

    implementation (libs.play.services.maps.v1810)
    implementation (libs.volley)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.play.services.places)
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
