/**
 * ViewModel for real-time location sharing
 * @author William8677
 * @since 2025-02-10 19:43:48
 */
package com.williamfq.xhat.panic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.location.LocationTracker
import com.williamfq.domain.models.Location
import com.williamfq.domain.models.LocationUpdate
import com.williamfq.domain.repository.LocationRepository
import com.williamfq.domain.repository.UserRepository
import com.williamfq.domain.usecases.UpdateLocationUseCase
import com.williamfq.xhat.ui.Navigation.ChatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class RealTimeLocationViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository,
    private val updateLocationUseCase: UpdateLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RealTimeLocationUiState())
    val uiState = _uiState.asStateFlow()

    private var locationUpdateJob: Job? = null
    private val updateInterval = TimeUnit.SECONDS.toMillis(3)

    fun updateLocation(location: Location, chatId: String, chatType: ChatType) {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val userId = userRepository.getCurrentUserId()

                _uiState.update { currentState ->
                    currentState.copy(
                        currentLocation = location,
                        lastUpdateTime = currentTime,
                        error = null
                    )
                }

                val locationUpdate = LocationUpdate(
                    userId = userId,
                    chatId = chatId,
                    chatType = chatType.name,
                    location = location,
                    timestamp = currentTime,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    altitude = location.altitude,
                    bearing = location.bearing
                )

                locationRepository.updateLocation(locationUpdate)
                notifySelectedContacts(locationUpdate)
                locationRepository.saveLocationHistory(locationUpdate)

                updateLocationUseCase(
                    location = location,
                    chatId = chatId,
                    chatType = chatType
                )

            } catch (e: Exception) {
                Timber.e(e, "Error actualizando ubicación")
                handleError(e)
            }
        }
    }

    fun startLocationSharing(chatId: String, chatType: ChatType, selectedContacts: List<String>) {
        locationUpdateJob?.cancel()
        locationUpdateJob = viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                _uiState.update { currentState ->
                    currentState.copy(
                        isSharing = true,
                        sharingStartTime = startTime,
                        selectedContacts = selectedContacts
                    )
                }

                locationTracker.getLocationUpdates()
                    .catch { e -> handleError(e) }
                    .collectLatest { location ->
                        updateLocation(location, chatId, chatType)

                        val currentDuration = System.currentTimeMillis() - startTime
                        _uiState.update { currentState ->
                            currentState.copy(sharingDuration = currentDuration)
                        }

                        delay(updateInterval)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error iniciando compartición de ubicación")
                handleError(e)
            }
        }
    }

    fun stopLocationSharing() {
        locationUpdateJob?.cancel()
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId()
                _uiState.update { currentState ->
                    currentState.copy(
                        isSharing = false,
                        sharingDuration = 0L,
                        sharingStartTime = 0L,
                        selectedContacts = emptyList()
                    )
                }

                locationRepository.stopLocationSharing(userId)
            } catch (e: Exception) {
                Timber.e(e, "Error deteniendo compartición de ubicación")
                handleError(e)
            }
        }
    }

    private fun notifySelectedContacts(locationUpdate: LocationUpdate) {
        viewModelScope.launch {
            uiState.value.selectedContacts.forEach { contactId ->
                try {
                    locationRepository.notifyLocationUpdate(
                        contactId = contactId,
                        locationUpdate = locationUpdate
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error notificando a contacto: $contactId")
                }
            }
        }
    }

    fun handleError(exception: Throwable) {
        _uiState.update { currentState ->
            currentState.copy(
                error = exception.message ?: "Error desconocido",
                isSharing = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationSharing()
    }
}