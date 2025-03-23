# Reglas para SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Reglas para Room
-keep class androidx.room.** { *; }
-keep @androidx.room.* class * { *; }
-dontwarn androidx.room.**

# Reglas para Hilt (Dagger)
-dontwarn dagger.**
-keep class dagger.hilt.** { *; }
-keep @dagger.** class * { *; }
-keep @javax.inject.* class * { *; }

# Reglas para Firebase Firestore
-dontwarn com.google.firebase.firestore.**
-keep class com.google.firebase.firestore.** { *; }

# Reglas para Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature

# Reglas para Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature

# Reglas para ML Kit
-dontwarn com.google.mlkit.**
-keep class com.google.mlkit.** { *; }

# Reglas generales
-keepattributes SourceFile,LineNumberTable
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# Mantener clases LiveData y ViewModel
-keep class androidx.lifecycle.LiveData { *; }
-keep class androidx.lifecycle.ViewModel { *; }

# Reglas para evitar problemas con OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keepattributes Signature


# Opcional: Si usas logs en producción y quieres eliminarlos
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Reglas para evitar problemas con androidx.sqlite
-keep class androidx.sqlite.** { *; }
-dontwarn androidx.sqlite.**
