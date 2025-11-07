plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ao.co.isptec.aplm.projetoanuncioloc"
    compileSdk = 36

    defaultConfig {
        applicationId = "ao.co.isptec.aplm.projetoanuncioloc"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // SQLITE LOCAL (JAVA PURO)
    implementation("androidx.sqlite:sqlite:2.3.1")
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // UI
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}