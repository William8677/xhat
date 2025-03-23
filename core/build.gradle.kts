plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hiltAndroid)
}

android {
    namespace = "com.williamfq.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidxCoreKtx){
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    }
    implementation(libs.androidxRoomRuntime){
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    }
    implementation(libs.androidxRoomKtx){
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    }
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.testng)
    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.junit.junit3)
    ksp(libs.androidxRoomCompiler)
    implementation(libs.mlkitFaceDetection)
    implementation(libs.playServicesMaps)
    implementation(libs.firebaseFirestoreKtx)
    implementation(libs.playServicesLocation)
    implementation(libs.dagger)
    implementation(libs.hiltAndroid)
    ksp(libs.daggerCompiler)
    ksp(libs.hiltCompiler)
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseAnalyticsKtx)
    implementation("com.google.android.material:material:1.12.0")

}

kotlin {
    jvmToolchain(17)
}
