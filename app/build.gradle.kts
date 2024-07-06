plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.aquasys"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aquasys"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // glide libs
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Glide v4 uses this new annotation processor -- see https://bumptech.github.io/glide/doc/generatedapi.html
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    //implementation gif loadding
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.28")
    // circle img
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.ismaeldivita.chipnavigation:chip-navigation-bar:1.3.3")

    implementation ("com.google.android.material:material:1.12.0")
    // new lib for smooth navigation bar
    implementation ("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")
    // sending email
    implementation ( "com.jakewharton.timber:timber:5.0.1" ) // for logging
    implementation ("com.sun.mail:android-mail:1.6.5") // for email sending
    //implementation ("com.github.AnyChart:AnyChart-Android:1.1.5") // lib draw chard
    implementation ("com.google.code.gson:gson:2.10") // lib store to json
    //noinspection GradleDependency
    implementation("com.onesignal:OneSignal:[5.0.0, 5.99.99]")
}