package com.williamfq.xhat.service.filter

import android.graphics.Bitmap
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.domain.model.FilterEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación concreta de [FilterProcessor].
 * Usa un [MutableStateFlow] para almacenar [Filter?].
 */
@Singleton
class FilterProcessorImpl @Inject constructor() : FilterProcessor {

    // El estado interno de filtro actual.
    private val _currentFilter = MutableStateFlow<Filter?>(null)

    /**
     * Devuelve el StateFlow con el filtro actual (o null si no hay).
     */
    override fun getCurrentFilter(): StateFlow<Filter?> = _currentFilter

    /**
     * Aplica un [Filter] y actualiza el estado.
     */
    override suspend fun applyFilter(filter: Filter) {
        _currentFilter.value = filter
        // Aquí podrías iniciar tu lógica interna (cargar shaders, etc.).
    }

    /**
     * Procesa la [bitmap] con el filtro actual.
     * Por ahora, devolvemos la imagen sin cambios.
     */
    override suspend fun processImage(bitmap: Bitmap): Bitmap {
        _currentFilter.value?.let { filter ->
            // Aplicar lógica de filtro basada en el tipo de filtro
            return filter.applyFilter(bitmap)
        }
        return bitmap
    }

    /**
     * Ajusta los parámetros (intensidad, brillo, etc.) en el filtro actual.
     */
    override suspend fun configureFilter(effect: FilterEffect) {
        // Implementa la configuración según tu [FilterEffect].
        // Podrías actualizar el _currentFilter.value si lo deseas,
        // o almacenar parámetros en variables internas.
    }

    /**
     * Elimina el filtro actual (vuelve a estado sin filtro).
     */
    override suspend fun removeFilter() {
        _currentFilter.value = null
    }

    /**
     * Libera recursos (buffers, etc.).
     */
    override fun release() {
        // Limpiar recursos internos si es necesario.
    }

    /**
     * Aplica el filtro a la imagen proporcionada.
     */
    override suspend fun applyFilterToImage(filter: Filter, image: Bitmap): Bitmap {
        return filter.applyFilter(image)
    }
}