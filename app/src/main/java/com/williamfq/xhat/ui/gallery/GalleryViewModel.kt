package com.williamfq.xhat.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.xhat.domain.model.GalleryImage
import com.williamfq.xhat.domain.repository.GalleryRepository
import com.williamfq.xhat.utils.image.ImageFilter
import com.williamfq.xhat.utils.image.ImageProcessor
import com.williamfq.xhat.utils.image.ProcessingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadImages()
    }

    private fun loadImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val images = galleryRepository.getImages()
                _uiState.update { state ->
                    state.copy(images = images, isLoading = false, error = null)
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(isLoading = false, error = "Error al cargar imágenes: ${e.message}")
                }
            }
        }
    }

    fun selectImage(image: GalleryImage) {
        _uiState.update { it.copy(selectedImage = image) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedImage = null) }
    }

    fun deleteSelectedImages() {
        viewModelScope.launch {
            val imagesToDelete = _uiState.value.selectedImages
            if (imagesToDelete.isEmpty()) return@launch

            _uiState.update { it.copy(isProcessing = true) }
            try {
                galleryRepository.deleteImages(imagesToDelete)
                loadImages()
                clearSelection() // Llamada añadida
                _uiState.update { state ->
                    state.copy(selectedImages = emptyList(), isProcessing = false, error = null)
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(isProcessing = false, error = "Error al eliminar imágenes: ${e.message}")
                }
            }
        }
    }

    fun processAndShareImages(images: List<GalleryImage>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                val processedImages = images.map { image ->
                    val outputFile = File(image.path)
                    val filters = listOf(ImageFilter.Resize(1024, 1024))
                    val result = imageProcessor.processImage(image.uri, filters, outputFile)
                    when (result) {
                        is ProcessingResult.Success -> image.copy(path = result.file.path)
                        is ProcessingResult.Error -> throw Exception(result.message)
                    }
                }
                galleryRepository.shareImages(processedImages)
                _uiState.update { it.copy(isProcessing = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(isProcessing = false, error = "Error al procesar o compartir imágenes: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class GalleryUiState(
    val images: List<GalleryImage> = emptyList(),
    val selectedImage: GalleryImage? = null,
    val selectedImages: List<GalleryImage> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)