/*
 * Updated: 2025-02-12 17:26:40
 * Author: William8677
 */

package com.williamfq.xhat.ui.profile

import android.net.Uri
import com.williamfq.domain.models.Location
import com.williamfq.xhat.core.models.Gender
import com.williamfq.xhat.core.models.UserBehavior

data class ProfileState(
    // Información básica
    val id: String = "",
    val username: String = "",
    val name: String = "",
    val description: String = "",
    val birthDate: String = "",
    val age: Int = 0,
    val gender: Gender = Gender.UNSPECIFIED,

    // Ubicación
    val country: String = "",
    val state: String = "",
    val city: String = "",
    val location: Location = Location(),

    // Preferencias y características
    val languages: Set<String> = setOf(),
    val interests: Set<String> = setOf(),
    val behaviors: Set<UserBehavior> = setOf(),

    // Imágenes
    val profileImageUri: Uri? = null,
    val coverImageUri: Uri? = null,
    val profileImageUrl: String? = null,
    val coverImageUrl: String? = null,

    // Estados de validación
    val usernameError: String? = null,
    val nameError: String? = null,
    val descriptionError: String? = null,
    val birthDateError: String? = null,

    // Estados de UI
    val showDatePicker: Boolean = false,
    val showLocationPermission: Boolean = false
)