plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.ksp)
    alias(libs.plugins.composeCompiler)
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(rootProject.file("my-release-key.jks"))
            storePassword = "Stephan8677@"
            keyAlias = "xhat-key"
            keyPassword = "Stephan8677@"
        }
    }
    namespace = "com.williamfq.xhat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.williamfq.xhat"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "1.9"
        apiVersion = "1.9"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
        dataBinding = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += setOf(
                "libsignal_jni*.dylib",
                "signal_jni*.dll",
                "META-INF/*.kotlin_module",
                "META-INF/versions/**",
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/DEPENDENCIES.txt",
                "META-INF/services/javax.annotation.processing.Processor",
                "META-INF/INDEX.LIST",
                "META-INF/proguard/**",
                "META-INF/*.version",
                "META-INF/AL2.0",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE.md",
                "META-INF/AL2.0.txt",
                "META-INF/*.properties"
            )
            pickFirsts += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "META-INF/groovy-release-info.properties",
                "google/protobuf/empty.proto",
                "google/protobuf/field_mask.proto",
                "google/protobuf/type.proto",
                "META-INF/AL2.0",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "google/protobuf/struct.proto",
                "google/protobuf/empty.proto",
                "google/protobuf/field_mask.proto",
                "google/protobuf/type.proto",
                "google/protobuf/struct.proto",
                "META-INF/com/google/protobuf/empty.proto",
                "META-INF/com/google/protobuf/field_mask.proto",
                "META-INF/com/google/protobuf/type.proto",
                "META-INF/com/google/protobuf/struct.proto"
            )
            merges += setOf("META-INF/services/*")
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-java:3.25.5")
    }
    exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
    exclude(group = "com.google.protobuf", module = "protobuf-javalite")
    exclude(group = "com.google.protobuf", module = "protobuf-kotlin")
    exclude(group = "com.google.protobuf", module = "protobuf-java")
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(libs.espressoCore)
    implementation(libs.firebase.analytics)
    implementation(libs.mediation.test.suite)
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.service)
    androidTestImplementation(libs.androidx.benchmark.macro.junit4)
    implementation(platform(libs.firebaseBom))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidxCoreKtx)
    androidTestImplementation(libs.screenshot.validation.junit.engine)
    androidTestImplementation(libs.androidx.tools.core)
    implementation(libs.firebase.appcheck.playintegrity)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation(libs.firebaseAds)
    implementation(libs.firebaseAnalyticsKtx)
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.protobuf:protobuf-java:3.25.5")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation(libs.firebaseCrashlyticsKtx)
    implementation(libs.firebaseAuthKtx)
    implementation(libs.firebaseDatabaseKtx)
    implementation(libs.firebaseMessagingKtx)
    implementation(libs.firebaseDynamicLinksKtx)
    implementation(libs.firebaseAppdistributionKtx)
    implementation(libs.firebaseMlVision){
        exclude(group = "com.google.android.gms", module = "play-services-vision-common")
    }
    implementation(libs.firebaseMlModelInterpreter)
    implementation(libs.firebaseMlNaturalLanguage)
    implementation(libs.firebaseMlNaturalLanguageTranslateModel)

    implementation(libs.androidxAppcompat)
    implementation(libs.androidxMaterial)
    implementation(libs.androidxConstraintlayout)
    implementation(libs.androidxMultidex)
    implementation(libs.androidxNavigationFragmentKtx)
    implementation(libs.androidxNavigationUiKtx)

    implementation(libs.playServicesAds)
    implementation(libs.playServicesMeasurementApi)
    implementation(libs.playServicesMaps)

    implementation(libs.androidxRoomRuntime)
    implementation(libs.androidxRoomKtx)
    implementation(libs.foundationAndroid)
    implementation(libs.foundationDesktop)
    implementation(libs.androidx.hilt.common)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.hilt.work)
    implementation(libs.playServicesLocation)
    implementation(libs.firebase.storage.ktx)
    ksp(libs.androidxRoomCompiler)
    implementation(libs.androidxRoomMigration)

    implementation(libs.androidxLifecycleRuntimeKtx)
    implementation(libs.androidxLifecycleViewmodelKtx)
    implementation(libs.androidxWorkmanager)
    implementation(libs.androidxBiometric)

    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.lottie)
    implementation(libs.mediator)

    implementation(libs.glide)
    ksp(libs.glideCompiler)

    implementation(libs.circleImageView)
    implementation(libs.mpAndroidChart)
    implementation(libs.toasty)

    implementation(libs.dagger)
    implementation(libs.hiltAndroid)
    ksp(libs.daggerCompiler)
    ksp(libs.hiltCompiler)

    implementation(libs.media3ExoPlayer)
    implementation(libs.media3ExoPlayerDash)
    implementation(libs.media3ExoPlayerHls)
    implementation(libs.media3ExoPlayerSmoothstreaming)
    implementation(libs.media3ExoPlayerRtsp)
    implementation(libs.media3ExoPlayerMidi)
    implementation(libs.media3ExoPlayerIma)
    implementation(libs.media3DataSourceCronet)
    implementation(libs.media3DataSourceOkhttp)
    implementation(libs.media3DataSourceRtmp)
    implementation(libs.media3Ui)
    implementation(libs.media3UiLeanback)
    implementation(libs.media3Session)
    implementation(libs.media3Extractor)
    implementation(libs.media3Cast)
    implementation(libs.media3ExoPlayerWorkmanager)
    implementation(libs.media3Transformer)
    implementation(libs.media3Effect)
    implementation(libs.media3Muxer)
    implementation(libs.media3TestUtils)
    implementation(libs.media3TestUtilsRobolectric)
    implementation(libs.media3Container)
    implementation(libs.media3Database)
    implementation(libs.media3Decoder)
    implementation(libs.media3DataSource)
    implementation(libs.media3Common)
    implementation(libs.media3CommonKtx)

    implementation(libs.coroutinesCore)
    implementation(libs.googleCloudTranslate)
    implementation(libs.googleCloudSpeech)
    implementation(libs.protoGoogleCommonProtos)
    implementation(libs.mlkitFaceDetection)
    implementation(libs.timber)

    implementation(libs.androidxComposeUi)
    implementation(libs.androidxComposeMaterial)
    implementation(libs.androidxComposeUiToolingPreview)
    debugImplementation(libs.androidxComposeUiTooling)
    implementation(libs.activityCompose)
    implementation(libs.androidxMaterial3)
    implementation(libs.databinding)
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(libs.bundles.serialization)
    implementation("androidx.compose.material3:material3:1.4.0-alpha10")

    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.databinding:databinding-runtime:8.10.0-beta01")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-base:18.6.0")
    implementation("com.google.android.gms:play-services-maps:19.1.0")
    implementation("com.google.android.gms:play-services-ads:24.1.0")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")
    releaseImplementation("com.squareup.leakcanary:leakcanary-android-noop:2.13")

    implementation("androidx.compose.animation:animation:1.7.8")
    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")
    implementation("com.google.dagger:hilt-android:2.55")
    implementation("com.google.android.gms:play-services-basement:18.6.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation(platform("androidx.compose:compose-bom:2025.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.8.0-rc01")
    implementation("androidx.compose.runtime:runtime:1.7.8")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.accompanist:accompanist-pager:0.36.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.36.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.0")
    implementation("com.google.maps.android:maps-compose:6.4.1")
    implementation("io.getstream:stream-webrtc-android:1.3.7")
    implementation("io.getstream:stream-webrtc-android-ui:1.3.7")
    implementation("io.getstream:stream-webrtc-android-ktx:1.3.7")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)

    implementation("org.signal:libsignal-android:0.65.3")
    implementation("org.signal:libsignal-client:0.65.3")
}

kotlin {
    jvmToolchain(17)
}