import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

fun getLocalProperty(propertyName: String): String {
    val localPropertiesFile = project.file("../local.properties")
    val localProperties = Properties()
    localProperties.load(FileInputStream(localPropertiesFile))
    return localProperties.getProperty(propertyName)
}

android {
    namespace = "com.jnu_alarm.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jnu_alarm.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", getLocalProperty("BASE_URL"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "adUnitId", "ca-app-pub-4183402691727093/6476970284")
        }
        debug {
            resValue("string", "adUnitId", "ca-app-pub-3940256099942544/9214589741")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.preference:preference-ktx:1.2.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // admob
    implementation("com.google.android.gms:play-services-ads:23.0.0")
}