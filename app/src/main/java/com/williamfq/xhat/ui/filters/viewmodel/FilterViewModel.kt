package com.williamfq.xhat.ui.filters.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.xhat.domain.model.FilterCategory
import com.williamfq.xhat.domain.model.FilterDefinition
import com.williamfq.xhat.domain.model.FilterEffect
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.domain.model.toFilterType
import com.williamfq.xhat.domain.repository.FilterRepository
import com.williamfq.xhat.service.filter.FilterProcessor
import com.williamfq.xhat.utils.ar.ARSystem
import com.williamfq.xhat.utils.filters.shaders.ShaderProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.VideoFrame
import javax.inject.Inject

// ========================================================
// EXTENSIONES Y CLASES AUXILIARES (CONFIGURACIÓN)
// ========================================================

/**
 * Clase que encapsula la configuración de parámetros a aplicar.
 */
class FilterConfiguration {
    var smoothness: Float = 0.5f
    var brightness: Float = 0f
    var contrast: Float = 1f
    var animated: Boolean = false
    var animationSpeed: Float = 1f
    var intensity: Float = 0.5f
    var saturation: Float = 1f
    var colorTint: Int? = null
    var glitterDensity: Float = 0.5f
    var glitterSize: Float = 0.5f
    var glitterSpeed: Float = 1f
    var neonGlow: Float = 0.5f
    var neonColor: Int? = null
    var starDensity: Float = 0.5f
    var galaxyRotation: Float = 1f
    var heartSize: Float = 0.5f
    var heartSpeed: Float = 1f
    var heartColor: Int? = null
}

/**
 * Función de extensión para convertir una configuración en un mapa de parámetros.
 */
fun FilterConfiguration.toMap(): Map<String, Any> {
    return mapOf(
        "smoothness" to smoothness,
        "brightness" to brightness,
        "contrast" to contrast,
        "animated" to animated,
        "animationSpeed" to animationSpeed,
        "intensity" to intensity,
        "saturation" to saturation,
        "colorTint" to (colorTint ?: "none"),
        "glitterDensity" to glitterDensity,
        "glitterSize" to glitterSize,
        "glitterSpeed" to glitterSpeed,
        "neonGlow" to neonGlow,
        "neonColor" to (neonColor ?: "none"),
        "starDensity" to starDensity,
        "galaxyRotation" to galaxyRotation,
        "heartSize" to heartSize,
        "heartSpeed" to heartSpeed,
        "heartColor" to (heartColor ?: "none")
    )
}

/**
 * Extensión para aplicar una configuración al FilterProcessor.
 *
 * Se convierte un objeto FilterConfiguration en un FilterEffect utilizando
 * el subtipo dummy FilterEffect.Special (definido en el dominio) y se invoca
 * configureFilter(effect).
 */
suspend fun FilterProcessor.configure(action: FilterConfiguration.() -> Unit) {
    val config = FilterConfiguration().apply(action)
    // Utilizamos FilterEffect.Special para simular la configuración.
    val effect = FilterEffect.Special(
        effectType = "dummy", // Valor dummy; en una implementación real usar el tipo correcto.
        parameters = config.toMap()
    )
    configureFilter(effect)
}

/**
 * Extensión para resetear el FilterProcessor utilizando removeFilter().
 */
suspend fun FilterProcessor.reset() {
    removeFilter()
}

/**
 * Extensión dummy para procesar un VideoFrame en FilterProcessor.
 *
 * En una implementación real, se podría convertir el VideoFrame a Bitmap,
 * aplicar processImage() y luego reconvertir a VideoFrame.
 */
suspend fun FilterProcessor.processVideoFrame(frame: VideoFrame): VideoFrame {
    println("FilterProcessor: Processing VideoFrame with current filter")
    return frame
}

/**
 * Extensión dummy para procesar un VideoFrame en ShaderProcessor.
 *
 * En una implementación real, se podría aplicar algún shader sobre el frame.
 */
suspend fun ShaderProcessor.processFrame(frame: VideoFrame): VideoFrame {
    println("ShaderProcessor: Processing frame (dummy)")
    return frame
}

// ========================================================
// FIN DE EXTENSIONES Y CLASES AUXILIARES
// ========================================================

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val filterRepository: FilterRepository,
    private val filterProcessor: FilterProcessor,
    private val shaderProcessor: ShaderProcessor,
    private val arSystem: ARSystem
) : ViewModel() {

    data class FilterUiState(
        val currentFilter: FilterType? = null,
        val availableFilters: Map<FilterCategory, List<FilterType>> = emptyMap(),
        val isProcessing: Boolean = false,
        val error: String? = null,
        val previewMode: Boolean = false,
        val customParameters: Map<String, Float> = emptyMap()
    )

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState

    private val _processedFrames = MutableSharedFlow<VideoFrame>()
    val processedFrames = _processedFrames.asSharedFlow()

    init {
        loadFilters()
    }

    private fun loadFilters() {
        viewModelScope.launch {
            try {
                // Se espera que getAvailableFilters() retorne una lista de FilterDefinition.
                val definitions: List<FilterDefinition> = filterRepository.getAvailableFilters()
                // La función de extensión toFilterType() convierte cada definición a FilterType.
                val filters: List<FilterType> = definitions.map { it.toFilterType() }
                _uiState.value = _uiState.value.copy(
                    availableFilters = filters.groupBy { it.category },
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar filtros: ${e.message}"
                )
            }
        }
    }

    fun selectFilter(filter: FilterType?) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    currentFilter = filter,
                    isProcessing = true
                )
                when (filter?.category) {
                    FilterCategory.BEAUTY -> configureBeautyFilter(filter)
                    FilterCategory.FUN -> configureFunFilter(filter)
                    FilterCategory.ARTISTIC -> configureArtisticFilter(filter)
                    FilterCategory.EFFECTS -> configureEffectsFilter(filter)
                    null -> clearFilters()
                }
                _uiState.value = _uiState.value.copy(isProcessing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al aplicar filtro: ${e.message}",
                    isProcessing = false
                )
            }
        }
    }

    private fun configureBeautyFilter(filter: FilterType) {
        shaderProcessor.setShader("beauty")
        viewModelScope.launch {
            filterProcessor.configure {
                smoothness = filter.parameters["smoothing"] as? Float ?: 0.5f
                brightness = filter.parameters["brightness"] as? Float ?: 0f
                contrast = filter.parameters["contrast"] as? Float ?: 1f
            }
        }
    }

    private fun configureFunFilter(filter: FilterType) {
        filter.parameters["arEffect"]?.let { effectId ->
            arSystem.setARAsset(effectId.toString())
        }
        filter.parameters["shader"]?.let { shader ->
            shaderProcessor.setShader(shader.toString())
        }
        viewModelScope.launch {
            filterProcessor.configure {
                animated = filter.parameters["animated"] as? Boolean ?: false
                animationSpeed = filter.parameters["animationSpeed"] as? Float ?: 1f
            }
        }
    }

    private fun configureArtisticFilter(filter: FilterType) {
        shaderProcessor.setShader(filter.name.lowercase())
        viewModelScope.launch {
            filterProcessor.configure {
                intensity = filter.parameters["artisticIntensity"] as? Float ?: 0.5f
                saturation = filter.parameters["saturation"] as? Float ?: 1f
                colorTint = filter.parameters["colorTint"] as? Int
            }
        }
    }

    private fun configureEffectsFilter(filter: FilterType) {
        shaderProcessor.setShader(filter.name.lowercase())
        when (filter.name) {
            "Glitter" -> viewModelScope.launch { configureGlitterEffect(filter) }
            "Neon" -> viewModelScope.launch { configureNeonEffect(filter) }
            "Galaxy" -> viewModelScope.launch { configureGalaxyEffect(filter) }
            "Hearts" -> viewModelScope.launch { configureHeartsEffect(filter) }
        }
    }

    private suspend fun configureGlitterEffect(filter: FilterType) {
        filterProcessor.configure {
            glitterDensity = filter.parameters["glitterDensity"] as? Float ?: 0.5f
            glitterSize = filter.parameters["glitterSize"] as? Float ?: 0.5f
            glitterSpeed = filter.parameters["glitterSpeed"] as? Float ?: 1f
        }
    }

    private suspend fun configureNeonEffect(filter: FilterType) {
        filterProcessor.configure {
            neonGlow = filter.parameters["neonGlow"] as? Float ?: 0.5f
            neonColor = filter.parameters["neonColor"] as? Int
        }
    }

    private suspend fun configureGalaxyEffect(filter: FilterType) {
        filterProcessor.configure {
            starDensity = filter.parameters["starDensity"] as? Float ?: 0.5f
            galaxyRotation = filter.parameters["galaxyRotation"] as? Float ?: 1f
        }
    }

    private suspend fun configureHeartsEffect(filter: FilterType) {
        filterProcessor.configure {
            heartSize = filter.parameters["heartSize"] as? Float ?: 0.5f
            heartSpeed = filter.parameters["heartSpeed"] as? Float ?: 1f
            heartColor = filter.parameters["heartColor"] as? Int
        }
    }

    private fun clearFilters() {
        arSystem.setARAsset(null)
        shaderProcessor.setShader("default")
        viewModelScope.launch {
            filterProcessor.reset()
        }
    }

    fun updateFilterParameter(name: String, value: Float) {
        viewModelScope.launch {
            try {
                val currentParams = _uiState.value.customParameters
                val updatedParams = currentParams + (name to value)
                _uiState.value = _uiState.value.copy(customParameters = updatedParams)
                _uiState.value.currentFilter?.let { selectFilter(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar parámetro: ${e.message}"
                )
            }
        }
    }

    fun processFrame(frame: VideoFrame) {
        viewModelScope.launch {
            try {
                var processedFrame = frame
                _uiState.value.currentFilter?.let { filter ->
                    processedFrame = shaderProcessor.processFrame(processedFrame)
                    if (filter.category == FilterCategory.FUN) {
                        processedFrame = arSystem.processFrame(processedFrame)
                    }
                    // Usamos la extensión renombrada para procesar el VideoFrame.
                    processedFrame = filterProcessor.processVideoFrame(processedFrame)
                }
                _processedFrames.emit(processedFrame)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al procesar frame: ${e.message}"
                )
            }
        }
    }

    fun togglePreviewMode() {
        _uiState.value = _uiState.value.copy(previewMode = !_uiState.value.previewMode)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        clearFilters()
        shaderProcessor.release()
    }
}
