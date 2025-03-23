package com.williamfq.xhat.ui.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.williamfq.domain.models.Location
import com.williamfq.xhat.core.models.Gender
import com.williamfq.xhat.data.repository.UserRepository
import com.williamfq.xhat.domain.model.UserProfile
import com.williamfq.xhat.utils.ImageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val locationManager: LocationManager,
    private val imageManager: ImageManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileSetupState>(ProfileSetupState.Initial)
    val uiState: StateFlow<ProfileSetupState> = _uiState

    private val _profileState = MutableStateFlow(ProfileState(
        id = "",
        username = "",
        name = "",
        description = "",
        birthDate = "",
        country = "",
        state = "",
        city = "",
        age = 0,
        gender = Gender.UNSPECIFIED,
        location = Location(),
        languages = setOf(),
        interests = setOf(),
        behaviors = setOf()
    ))
    val profileState: StateFlow<ProfileState> = _profileState

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    init {
        loadInitialData()
        getCurrentLocation()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val savedProfile = withContext(Dispatchers.IO) {
                    userRepository.getCurrentProfile()
                }
                savedProfile?.let { profile ->
                    _profileState.value = _profileState.value.copy(
                        id = profile.id,
                        username = profile.username,
                        name = profile.name,
                        description = profile.description,
                        birthDate = profile.birthDate,
                        country = profile.country,
                        state = profile.state,
                        city = profile.city,
                        age = profile.age,
                        gender = profile.gender,
                        location = profile.location,
                        languages = profile.languages,
                        behaviors = profile.behaviors,
                        profileImageUri = profile.profileImageUrl?.let { Uri.parse(it) },
                        coverImageUri = profile.coverImageUrl?.let { Uri.parse(it) }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ProfileSetupState.Error(e.message ?: "Error al cargar perfil")
            }
        }
    }

    fun updateUsername(username: String) {
        viewModelScope.launch {
            val error = validateUsername(username)
            _profileState.value = _profileState.value.copy(
                username = username,
                usernameError = error
            )
        }
    }

    fun updateName(name: String) {
        _profileState.value = _profileState.value.copy(name = name)
    }

    fun updateDescription(description: String) {
        _profileState.value = _profileState.value.copy(description = description)
    }

    fun updateBirthDate(birthDate: Long) {
        val localDate = Instant.ofEpochMilli(birthDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        _profileState.value = _profileState.value.copy(birthDate = localDate.format(formatter))
    }

    fun requestLocation() {
        viewModelScope.launch {
            try {
                getCurrentLocation()
            } catch (e: Exception) {
                showError("Error al solicitar ubicación")
            }
        }
    }

    fun isProfileValid(): Boolean {
        val state = _profileState.value
        return state.name.isNotBlank() &&
                state.username.isNotBlank() &&
                state.description.isNotBlank() &&
                state.usernameError == null
    }

    fun selectProfileImage() {
        viewModelScope.launch {
            val dummyUri = Uri.parse("content://dummy_profile_image")
            _profileState.value = _profileState.value.copy(profileImageUri = dummyUri)
        }
    }

    fun selectCoverImage() {
        viewModelScope.launch {
            val dummyUri = Uri.parse("content://dummy_cover_image")
            _profileState.value = _profileState.value.copy(coverImageUri = dummyUri)
        }
    }

    fun toggleDatePicker(show: Boolean) {
        _profileState.value = _profileState.value.copy(showDatePicker = show)
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            try {
                getCurrentLocation()
            } catch (e: Exception) {
                showError("Error al solicitar permisos de ubicación")
            }
        }
    }

    fun toggleLocationPermission(show: Boolean) {
        _profileState.value = _profileState.value.copy(showLocationPermission = show)
    }

    fun resetUiState() {
        _uiState.value = ProfileSetupState.Initial
    }

    private suspend fun validateUsername(username: String): String? {
        return when {
            username.isEmpty() -> "El nombre de usuario es requerido"
            username.length < 3 -> "Mínimo 3 caracteres"
            username.length > 30 -> "Máximo 30 caracteres"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Solo letras, números y guion bajo"
            withContext(Dispatchers.IO) {
                userRepository.isUsernameTaken(username)
            } -> "Nombre de usuario no disponible"
            else -> null
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileSetupState.Loading

                if (!isProfileValid()) {
                    _uiState.value = ProfileSetupState.Error("Datos del perfil inválidos")
                    return@launch
                }

                val currentProfile = withContext(Dispatchers.IO) {
                    userRepository.getCurrentProfile()
                } ?: throw IllegalStateException("Usuario no encontrado")

                val profileImageUrl: String? = _profileState.value.profileImageUri?.let {
                    withContext(Dispatchers.IO) {
                        imageManager.uploadImage("profile_images", it)
                    }
                }
                val coverImageUrl: String? = _profileState.value.coverImageUri?.let {
                    withContext(Dispatchers.IO) {
                        imageManager.uploadImage("cover_images", it)
                    }
                }

                val profile = UserProfile(
                    id = currentProfile.id,
                    userId = currentProfile.userId,
                    username = _profileState.value.username,
                    phoneNumber = currentProfile.phoneNumber,
                    name = _profileState.value.name,
                    displayName = _profileState.value.name,
                    description = _profileState.value.description,
                    birthDate = _profileState.value.birthDate,
                    country = _profileState.value.country,
                    state = _profileState.value.state,
                    city = _profileState.value.city,
                    location = _profileState.value.location,
                    age = _profileState.value.age,
                    gender = _profileState.value.gender,
                    languages = _profileState.value.languages,
                    behaviors = _profileState.value.behaviors,
                    profileImageUrl = profileImageUrl,
                    coverImageUrl = coverImageUrl,
                    status = currentProfile.status,
                    settings = currentProfile.settings,
                    createdAt = currentProfile.createdAt,
                    lastUpdated = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    userRepository.saveProfile(profile)
                }
                _uiState.value = ProfileSetupState.Success
            } catch (e: Exception) {
                _uiState.value = ProfileSetupState.Error(e.message ?: "Error al guardar perfil")
            }
        }
    }

    fun updateProfile(
        name: String,
        description: String,
        country: String,
        state: String,
        city: String,
        birthDate: String
    ) {
        _profileState.value = _profileState.value.copy(
            name = name,
            description = description,
            country = country,
            state = state,
            city = city,
            birthDate = birthDate
        )
    }

    private fun getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                _profileState.value = _profileState.value.copy(showLocationPermission = true)
                return
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModelScope.launch {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = withContext(Dispatchers.IO) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            }
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                updateProfileState { state ->
                                    state.copy(
                                        country = address.countryName ?: "",
                                        state = address.adminArea ?: "",
                                        city = address.locality ?: ""
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            showError("Error al obtener la ubicación: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            showError("Error al obtener la ubicación")
        }
    }

    private fun compressImage(uri: Uri): Uri {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val maxWidth = 1024
        val maxHeight = 1024
        val ratio = min(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()

        val compressedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        return Uri.fromFile(file)
    }

    private fun showError(message: String) {
        _uiState.value = ProfileSetupState.Error(message)
    }

    private fun updateProfileState(transform: (ProfileState) -> ProfileState) {
        _profileState.value = transform(_profileState.value)
    }
}