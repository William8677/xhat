package com.williamfq.xhat.manager

import android.content.Context
import android.graphics.Bitmap
import com.williamfq.xhat.domain.model.FilterDefinition
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.filters.processor.FilterUtils
import com.williamfq.xhat.domain.repository.FilterRepository
import com.williamfq.xhat.service.filter.FilterProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageFilterManager @Inject constructor(
    private val filterProcessor: FilterProcessor,
    private val filterUtils: FilterUtils,
    private val filterRepository: FilterRepository
) {

    /**
     * Aplica un único filtro a la imagen proporcionada.
     *
     * @param filter El filtro a aplicar.
     * @param image La imagen original.
     * @return La imagen con el filtro aplicado.
     */
    suspend fun applyFilter(filter: Filter, image: Bitmap): Bitmap {
        return filterProcessor.applyFilterToImage(filter, image)
    }

    /**
     * Aplica múltiples filtros en secuencia a la imagen proporcionada.
     *
     * @param filters La lista de filtros a aplicar.
     * @param image La imagen original.
     * @return La imagen con todos los filtros aplicados.
     */
    suspend fun applyMultipleFilters(filters: List<Filter>, image: Bitmap): Bitmap {
        return filterUtils.applyMultipleFilters(filters, image)
    }

    /**
     * Obtiene una lista de filtros disponibles.
     *
     * @return Lista de FilterDefinition disponibles.
     */
    suspend fun getAvailableFilters(): List<FilterDefinition> {
        return filterRepository.getAvailableFilters()
    }
}
