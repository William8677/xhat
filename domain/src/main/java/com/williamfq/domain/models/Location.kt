/*
 * Updated: 2025-02-10 04:32:34
 * Author: William8677
 */

package com.williamfq.domain.models

/**
 * Modelo completo de ubicación geográfica.
 * Incluye información detallada de ubicación, región y características del lugar.
 */
data class Location(
    // Coordenadas básicas
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // Datos de precisión y movimiento
    val accuracy: Float = 0f,
    val altitude: Double = 0.0,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val time: Long = System.currentTimeMillis(),

    // Información geográfica
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val region: String? = null,
    val continent: String? = null,
    val placeName: String = "",
    val address: String = "",
    val zipCode: String? = null,

    // Códigos y identificadores
    val countryCode: String? = null,
    val continentCode: String? = null,
    val iso3: String? = null,

    // Características adicionales
    val radius: Double = 0.0,
    val timezone: String? = null,
    val flag: String? = null,

    // Indicadores booleanos
    val isEU: Boolean = false,
    val isOceania: Boolean = false,
    val isLandlocked: Boolean = false,
    val isPrimetime: Boolean = false,
    val isRTL: Boolean = false,
    val isEuropeanUnion: Boolean = false
)