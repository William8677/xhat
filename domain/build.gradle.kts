plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hiltAndroid)
}

android {
    namespace = "com.williamfq.domain"
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
    implementation(project(":core"))
    implementation(libs.dagger)
    implementation(libs.hiltAndroid)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.firebase.inappmessaging.ktx)
    implementation(libs.firebaseMessagingKtx)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.ui.geometry.android)
    implementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.junit.junit2)
    ksp(libs.daggerCompiler)
    ksp(libs.hiltCompiler)
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseAnalyticsKtx)
    implementation("com.google.android.material:material:1.12.0")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

kotlin {
    sourceSets {
        getByName("main") {
            kotlin.srcDirs("build/generated/ksp/main/kotlin")
        }
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
