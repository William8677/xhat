package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.williamfq.data.converters.Converters
import java.time.LocalDateTime

/**
 * Entidad que representa una comunidad en la base de datos.
 * @property id Identificador único de la comunidad
 * @property name Nombre de la comunidad
 * @property description Descripción de la comunidad
 * @property createdBy ID del usuario que creó la comunidad
 * @property createdAt Fecha y hora de creación
 * @property isPrivate Indica si la comunidad es privada
 * @property isActive Indica si la comunidad está activa
 */
@Entity(tableName = "communities")
@TypeConverters(Converters::class)
data class CommunityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val description: String?,

    val createdBy: String,

    val createdAt: Long = System.currentTimeMillis(),

    val isPrivate: Boolean = false,

    val isActive: Boolean = true,

    val memberCount: Int = 0,

    val categories: List<String> = emptyList(),

    val avatarUrl: String? = null,

    val rules: List<String> = emptyList(),

    val isSubscribed: Boolean = false,

    val lastActivityAt: Long = System.currentTimeMillis(),

    val pinnedMessages: List<Long> = emptyList(),

    val moderators: List<String> = emptyList(),

    val visibility: String = "PUBLIC"
) {
    /**
     * Valida que la entidad tenga datos válidos
     * @throws IllegalArgumentException si los datos no son válidos
     */
    fun validate() {
        require(name.isNotBlank()) { "El nombre de la comunidad no puede estar vacío" }
        require(name.length <= MAX_NAME_LENGTH) { "El nombre no puede exceder $MAX_NAME_LENGTH caracteres" }
        require(description == null || description.length <= MAX_DESCRIPTION_LENGTH) {
            "La descripción no puede exceder $MAX_DESCRIPTION_LENGTH caracteres"
        }
        require(categories.size <= MAX_CATEGORIES) {
            "No puede tener más de $MAX_CATEGORIES categorías"
        }
        require(rules.size <= MAX_RULES) {
            "No puede tener más de $MAX_RULES reglas"
        }
        require(moderators.size <= MAX_MODERATORS) {
            "No puede tener más de $MAX_MODERATORS moderadores"
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MAX_DESCRIPTION_LENGTH = 1000
        const val MAX_CATEGORIES = 5
        const val MAX_RULES = 20
        const val MAX_MODERATORS = 10
    }
}