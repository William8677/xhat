package com.williamfq.xhat.service.filter

import android.graphics.Bitmap
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.domain.model.FilterEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interfaz para procesar filtros sobre imágenes, permitiendo configuraciones,
 * remoción, y obtención del filtro actual.
 */
interface FilterProcessor {

    /**
     * Aplica un filtro específico.
     *
     * @param filter El filtro a aplicar.
     */
    suspend fun applyFilter(filter: Filter)

    /**
     * Procesa una imagen con el filtro actual.
     *
     * @param bitmap La imagen original.
     * @return La imagen procesada con el filtro aplicado.
     */
    suspend fun processImage(bitmap: Bitmap): Bitmap

    /**
     * Configura los parámetros del filtro actual (ej. intensidad, brillo, etc.).
     *
     * @param effect Los efectos a configurar.
     */
    suspend fun configureFilter(effect: FilterEffect)

    /**
     * Retorna un [StateFlow] con el filtro actual, o `null` si no hay filtro.
     */
    fun getCurrentFilter(): StateFlow<Filter?>

    /**
     * Remueve el filtro actual (vuelve a estado sin filtro).
     */
    suspend fun removeFilter()

    /**
     * Libera recursos (si el procesador usa buffers, memoria, etc.).
     */
    fun release()

    /**
     * Aplica el filtro a la imagen proporcionada.
     */
    suspend fun applyFilterToImage(filter: Filter, image: Bitmap): Bitmap

    class DummyFilterProcessor : FilterProcessor {

        private var currentFilter: Filter? = null
        private val _currentFilterFlow = MutableStateFlow<Filter?>(null)

        override suspend fun applyFilter(filter: Filter) {
            currentFilter = filter
            _currentFilterFlow.value = filter
            println("DummyFilterProcessor: Filtro aplicado -> ${filter.javaClass.simpleName}")
        }

        override suspend fun processImage(bitmap: Bitmap): Bitmap {
            println("DummyFilterProcessor: Procesando imagen (dummy)")
            // En esta dummy simplemente devolvemos la misma imagen.
            return bitmap
        }

        override suspend fun configureFilter(effect: FilterEffect) {
            // Aquí se aplicaría la configuración del filtro.
            println("DummyFilterProcessor: Configurando filtro con efecto -> $effect")
        }

        override fun getCurrentFilter(): StateFlow<Filter?> {
            return _currentFilterFlow
        }

        override suspend fun removeFilter() {
            println("DummyFilterProcessor: Removiendo filtro")
            currentFilter = null
            _currentFilterFlow.value = null
        }

        override fun release() {
            println("DummyFilterProcessor: Liberando recursos")
            // Aquí se liberarían recursos, buffers, etc.
        }

        override suspend fun applyFilterToImage(filter: Filter, image: Bitmap): Bitmap {
            applyFilter(filter)
            return processImage(image)
        }
    }
}