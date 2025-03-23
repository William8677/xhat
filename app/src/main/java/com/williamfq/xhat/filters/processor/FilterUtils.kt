// com/williamfq/xhat/filters/processor/FilterUtils.kt

package com.williamfq.xhat.filters.processor

import android.graphics.Bitmap
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.service.filter.FilterProcessor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidades para aplicar múltiples filtros a una imagen.
 */
@Singleton
class FilterUtils @Inject constructor(
    private val filterProcessor: FilterProcessor
) {

    /**
     * Aplica una lista de filtros a una imagen de forma secuencial.
     *
     * @param filters Lista de filtros a aplicar.
     * @param image Imagen original.
     * @return Imagen filtrada.
     */
    suspend fun applyMultipleFilters(filters: List<Filter>, image: Bitmap): Bitmap {
        var filteredImage = image
        for (filter in filters) {
            filteredImage = filterProcessor.applyFilterToImage(filter, filteredImage)
        }
        return filteredImage
    }
}
