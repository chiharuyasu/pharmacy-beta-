plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pharmacyl3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pharmacyl3"
        minSdk = 26
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
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.retrofit)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    // implementation("org.apache.poi:poi-ooxml-lite:5.2.3") // replaced with full poi-ooxml for Excel support
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("com.fasterxml:aalto-xml:1.0.0")
    implementation("javax.xml.stream:stax-api:1.0")
}