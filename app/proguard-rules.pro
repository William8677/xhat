# XML Parser specific rules
-keep class org.xmlpull.v1.** { *; }
-dontwarn org.xmlpull.v1.**
-keep class android.content.res.XmlResourceParser { *; }
-dontwarn android.content.res.XmlResourceParser
-keep class org.kxml2.io.** { *; }
-dontwarn org.kxml2.io.**

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.firebase.**

# Firebase In-App Messaging
-keep class com.google.firebase.inappmessaging.** { *; }
-keep class com.google.firebase.inappmessaging.display.** { *; }

# Firebase Analytics
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.android.gms.measurement.**

# Retrofit
-keep class com.squareup.retrofit2.** { *; }
-dontwarn com.squareup.retrofit2.**
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp
-dontnote okhttp3.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepclassmembers class okhttp3.** { *; }

# Dagger/Hilt
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# Room Database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# Navigation Component
-keep class androidx.navigation.** { *; }
-keepnames class * extends androidx.navigation.Navigator

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# WebRTC
-keep class org.webrtc.** { *; }
-keep class io.getstream.webrtc.** { *; }
-dontwarn org.webrtc.**

# Signal Protocol
-keep class org.signal.** { *; }
-keep class org.whispersystems.** { *; }
-keepclassmembers class * {
    @org.signal.** *;
}

# SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Protobuf
-keep class com.google.protobuf.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.protobuf.**


# Kotlin Specific
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Compose
-keep class androidx.compose.** { *; }
-keepclasseswithmembers class * {
    @androidx.compose.ui.tooling.preview.Preview *;
}

# AdServices
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Multidex
-keep class androidx.multidex.MultiDexApplication { *; }
-keep class * extends androidx.multidex.MultiDexApplication { *; }

# Timber
-keep class timber.log.Timber { *; }
-dontwarn org.jetbrains.annotations.**

# BlockHound Integration
-dontwarn reactor.blockhound.**

# General Rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisible*Annotation*
-keepattributes InnerClasses
-keepattributes Exceptions
-keepattributes Deprecated
-keepattributes EnclosingMethod

# Keep custom application class
-keep class com.williamfq.xhat.XhatApplication { *; }

# Keep all activities, fragments, services, etc.
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep serializable and parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Reglas anteriores...

# Android Studio Preview
-keep class com.android.tools.preview.** { *; }
-keep class com.android.tools.render.** { *; }
-dontwarn com.android.tools.preview.**
-dontwarn com.android.tools.render.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firebase Auth UI
-keep class com.firebase.ui.auth.** { *; }
-dontwarn com.firebase.ui.auth.**

# Google Vision
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.android.gms.vision.**

# JNA
-keep class com.sun.jna.** { *; }
-dontwarn com.sun.jna.**

# FindBugs
-dontwarn edu.umd.cs.findbugs.annotations.**

# Java Model
-dontwarn javax.lang.model.**
-keep class javax.lang.model.** { *; }

# API Guardian
-dontwarn org.apiguardian.**

# Jetty ALPN/NPN
-dontwarn org.eclipse.jetty.**
-keep class org.eclipse.jetty.** { *; }

# JSpecify
-dontwarn org.jspecify.**

# SLF4J
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# Credentials API
-keep class com.google.android.gms.auth.api.credentials.** { *; }
-dontwarn com.google.android.gms.auth.api.credentials.**

# AutoService
-dontwarn com.google.auto.service.**


# ByteBuddy
-dontwarn net.bytebuddy.**
-keep class net.bytebuddy.** { *; }

# KotlinPoet
-dontwarn com.squareup.kotlinpoet.**
-keep class com.squareup.kotlinpoet.** { *; }

# JUnit Platform
-dontwarn org.junit.platform.**
-keep class org.junit.platform.** { *; }

# gRPC
-dontwarn io.grpc.netty.shaded.**
-keep class io.grpc.netty.shaded.** { *; }



# Keep Kotlin Metadata
-keepattributes *Annotation*, InnerClasses
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Keep R8 safe
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeRetentionPolicy

# Mantener clases serializables
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Reglas para clases de screenshot testing
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-keep class androidx.test.screenshot.** { *; }
-dontwarn androidx.test.screenshot.**

-keep class com.williamfq.xhat.XhatApplication { *; }
-keep class com.williamfq.xhat.MainActivity { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

-keepattributes *Annotation*

-keepnames class * extends android.app.Activity
-keepnames class * extends android.app.Service
-keepnames class * extends android.content.BroadcastReceiver
-keepnames class * extends android.content.ContentProvider

-keep class com.google.firebase.** { *; }
-keep class com.google.protobuf.** { *; }
-keep class com.google.firestore.** { *; }
-keep class com.google.protobuf.** { *; }

