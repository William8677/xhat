plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)

}

android {
    namespace = "com.williamfq.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Módulos internos
    implementation(project(":core"))
    implementation(project(":domain"))

    // Room (Base de datos)
    implementation(libs.androidxRoomRuntime)
    implementation(libs.androidxRoomKtx)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.common)
    implementation(libs.playServicesLocation)
    implementation(libs.firebase.inappmessaging.ktx)
    implementation(libs.androidx.databinding.adapters)
    implementation(libs.firebase.database)
    implementation(libs.firebaseMessagingKtx)
    implementation(libs.androidx.ui.graphics.android)
    ksp(libs.androidxRoomCompiler)
    implementation(libs.androidxRoomMigration)
    implementation("com.google.firebase:firebase-auth:23.2.0")


    // Firebase (Firestore)
    implementation(libs.firebaseFirestoreKtx)

    // Hilt (Inyección de dependencias)
    implementation(libs.hiltAndroid)
    ksp(libs.hiltCompiler)

    // Networking (Retrofit y OkHttp)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttp)

    // ML Kit (Detección facial)
    implementation(libs.mlkitFaceDetection)

    // Seguridad de base de datos
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite)

    // JSON avanzado
    implementation(libs.gson)

    // Arquitectura (LiveData y ViewModel)
    implementation(libs.androidxLifecycleViewModelKtx)
    implementation(libs.androidxLifecycleLiveDataKtx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxTestExtJunit)
    androidTestImplementation(libs.espressoCore)

    // UI y Utilidades
    implementation(libs.easyPermissions)
    implementation(libs.chucker)
    implementation(libs.timber)
    implementation(libs.coil)

    implementation(libs.androidxRoomMigration)


    implementation(libs.bundles.serialization)

    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseAnalyticsKtx)
    implementation ("com.google.android.material:material:1.12.0")


}

tasks.register<Copy>("copyRoomSchemas") {
    from(fileTree(layout.buildDirectory.dir("schemas")))
    into("$projectDir/schemas")
    doLast {
        println("Room schema files have been copied to $projectDir/schemas")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

kotlin {
    sourceSets {
        getByName("main") {
            kotlin.srcDirs("build/generated/ksp/main/kotlin")
        }
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}
