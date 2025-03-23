package com.williamfq.xhat.filters

import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.filters.effects.BlurEffect
import com.williamfq.xhat.filters.effects.GlowEffect
import com.williamfq.xhat.filters.base.Filter

object FilterFactory {
    private val filterMap = mapOf(
        FilterType.BLUR to BlurEffect(),
        FilterType.GLOW to GlowEffect()
        // Agrega más filtros aquí
    )

    fun createFilter(filterType: FilterType): Filter {
        return filterMap[filterType] ?: throw IllegalArgumentException("Unknown FilterType: $filterType")
    }

    fun getAvailableFilters(): List<Filter> {
        return filterMap.values.toList()
    }
}