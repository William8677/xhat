package com.williamfq.xhat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SerializationModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true // Ignora campos desconocidos
        isLenient = true // Permite JSON más flexible
        encodeDefaults = true // Incluye valores por defecto en la serialización
        prettyPrint = false // Desactivado para producción
        coerceInputValues = true // Convierte valores nulos a defaults cuando sea posible
    }
}