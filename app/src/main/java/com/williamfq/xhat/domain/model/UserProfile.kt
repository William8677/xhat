/*
 * Updated: 2025-02-09 19:24:21
 * Author: William8677
 */
package com.williamfq.xhat.domain.model

import android.net.Uri
import com.williamfq.domain.models.Location
import com.williamfq.xhat.core.models.Gender
import com.williamfq.xhat.core.models.UserBehavior
import java.time.LocalDateTime

data class UserProfile(
    val id: String,
    val userId: String,
    val username: String,
    val email: String? = null,
    val phoneNumber: String,
    val name: String,
    val displayName: String? = null,
    val bio: String = "",
    val description: String = "",

    var lastUpdated: Long = System.currentTimeMillis(),

    // Imágenes de perfil
    val profileImageUrl: String? = null,
    val coverImageUrl: String? = null,
    val photoUrl: String? = null,

    // Información personal
    val birthDate: String,
    val age: Int = 0,
    val gender: Gender = Gender.UNSPECIFIED,

    // Ubicación
    val location: Location = Location(),
    val country: String,
    val state: String,
    val city: String,

    // Preferencias y comportamiento
    val languages: Set<String> = setOf(),
    val behaviors: Set<UserBehavior> = setOf(),

    // Estados y configuración
    val status: UserStatus = UserStatus.ACTIVE,
    val settings: UserSettings = UserSettings(),

    // Marcas de tiempo
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val lastActive: LocalDateTime = LocalDateTime.now()
)









