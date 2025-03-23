package com.williamfq.xhat.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.williamfq.xhat.R
import com.williamfq.xhat.domain.model.FilterCategory
import com.williamfq.xhat.domain.model.FilterDefinition
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.filters.FilterFactory
import com.williamfq.xhat.filters.base.Filter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val filters = mutableListOf<FilterDefinition>()

    init {
        initializeFilters()
    }

    private fun initializeFilters() {
        // Filtros de belleza
        filters.addAll(listOf(
            FilterDefinition(
                id = "beauty_natural",
                name = "Natural",
                description = "Mejora sutil y natural",
                category = FilterCategory.BEAUTY,
                previewUrl = R.drawable.filter_preview_natural,
                parameters = mapOf(
                    "smoothing" to 0.3f,
                    "brightness" to 0.1f,
                    "contrast" to 1.1f
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "beauty_glow",
                name = "Resplandor",
                description = "Efecto de piel radiante",
                category = FilterCategory.BEAUTY,
                previewUrl = R.drawable.filter_preview_glow,
                parameters = mapOf(
                    "smoothing" to 0.5f,
                    "brightness" to 0.2f,
                    "contrast" to 1.2f,
                    "glow" to 0.3f
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "beauty_soft",
                name = "Suave",
                description = "Suavizado delicado",
                category = FilterCategory.BEAUTY,
                previewUrl = R.drawable.filter_preview_soft,
                parameters = mapOf(
                    "smoothing" to 0.7f,
                    "brightness" to 0.15f,
                    "contrast" to 1.05f
                ),
                intensity = 1f
            )
        ))

        // Filtros divertidos AR
        filters.addAll(listOf(
            FilterDefinition(
                id = "fun_dog",
                name = "Perrito",
                description = "Orejas y nariz de perro",
                category = FilterCategory.FUN,
                previewUrl = R.drawable.filter_preview_dog,
                parameters = mapOf(
                    "arEffect" to "dog_ears",
                    "animated" to true,
                    "animationSpeed" to 1.0f
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "fun_flower_crown",
                name = "Corona de Flores",
                description = "Corona de flores brillantes",
                category = FilterCategory.FUN,
                previewUrl = R.drawable.filter_preview_flower_crown,
                parameters = mapOf(
                    "arEffect" to "flower_crown",
                    "animated" to true,
                    "animationSpeed" to 0.8f
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "fun_butterfly",
                name = "Mariposas",
                description = "Mariposas animadas",
                category = FilterCategory.FUN,
                previewUrl = R.drawable.filter_preview_butterfly,
                parameters = mapOf(
                    "arEffect" to "butterfly_crown",
                    "animated" to true,
                    "animationSpeed" to 1.2f
                ),
                intensity = 1f
            )
        ))

        // Filtros artísticos
        filters.addAll(listOf(
            FilterDefinition(
                id = "artistic_oil",
                name = "Óleo",
                description = "Efecto de pintura al óleo",
                category = FilterCategory.ARTISTIC,
                previewUrl = R.drawable.filter_preview_oil,
                parameters = mapOf(
                    "artisticIntensity" to 0.6f,
                    "brushSize" to 3
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "artistic_comic",
                name = "Cómic",
                description = "Estilo de historieta",
                category = FilterCategory.ARTISTIC,
                previewUrl = R.drawable.filter_preview_comic,
                parameters = mapOf(
                    "artisticIntensity" to 0.8f,
                    "edgeThreshold" to 0.4f
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "artistic_watercolor",
                name = "Acuarela",
                description = "Efecto de acuarela suave",
                category = FilterCategory.ARTISTIC,
                previewUrl = R.drawable.filter_preview_watercolor,
                parameters = mapOf(
                    "artisticIntensity" to 0.5f,
                    "blendMode" to "soft_light"
                ),
                intensity = 1f
            )
        ))

        // Efectos especiales
        filters.addAll(listOf(
            FilterDefinition(
                id = "effect_glitter",
                name = "Glitter",
                description = "Destellos brillantes",
                category = FilterCategory.EFFECTS,
                previewUrl = R.drawable.filter_preview_glitter,
                parameters = mapOf(
                    "glitterDensity" to 0.5f,
                    "glitterSize" to 0.5f,
                    "glitterSpeed" to 1.0f
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "effect_neon",
                name = "Neón",
                description = "Brillo de neón",
                category = FilterCategory.EFFECTS,
                previewUrl = R.drawable.filter_preview_neon,
                parameters = mapOf(
                    "neonGlow" to 0.6f,
                    "neonColor" to "#00FFFF"
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "effect_galaxy",
                name = "Galaxia",
                description = "Efecto espacial con estrellas",
                category = FilterCategory.EFFECTS,
                previewUrl = R.drawable.filter_preview_galaxy,
                parameters = mapOf(
                    "starDensity" to 0.7f,
                    "galaxyRotation" to 1.0f
                ),
                intensity = 1f
            ),
            FilterDefinition(
                id = "effect_hearts",
                name = "Corazones",
                description = "Corazones flotantes animados",
                category = FilterCategory.EFFECTS,
                previewUrl = R.drawable.filter_preview_hearts,
                parameters = mapOf(
                    "heartSize" to 0.5f,
                    "heartSpeed" to 1.0f,
                    "heartColor" to "#FF0000"
                ),
                intensity = 1f
            )
        ))
    }

    /**
     * Retorna la lista de filtros disponibles.
     */
    suspend fun getAvailableFilters(): List<FilterDefinition> = withContext(Dispatchers.IO) {
        filters
    }

    /**
     * Retorna un filtro específico por su ID.
     */
    suspend fun getFilterById(filterId: String): FilterDefinition? = withContext(Dispatchers.IO) {
        filters.find { it.id == filterId }
    }

    /**
     * Retorna los filtros que pertenecen a una categoría específica.
     */
    suspend fun getFiltersByCategory(category: FilterCategory): List<FilterDefinition> = withContext(Dispatchers.IO) {
        filters.filter { it.category == category }
    }

    /**
     * Retorna el Bitmap de previsualización de un filtro por su ID.
     */
    suspend fun getFilterPreview(filterId: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val filter = filters.find { it.id == filterId } ?: return@withContext null
            // Se usa decodeResource para cargar recursos drawable
            BitmapFactory.decodeResource(context.resources, filter.previewUrl)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualiza los parámetros de un filtro existente.
     */
    suspend fun updateFilterParameters(
        filterId: String,
        parameters: Map<String, Any>
    ): FilterDefinition? = withContext(Dispatchers.IO) {
        val index = filters.indexOfFirst { it.id == filterId }
        if (index == -1) return@withContext null

        val updatedFilter = filters[index].copy(
            parameters = filters[index].parameters + parameters
        )
        filters[index] = updatedFilter
        updatedFilter
    }

    /**
     * Crea una instancia de Filter basado en el FilterType del FilterDefinition.
     */
    fun getFilterInstance(filterDefinition: FilterDefinition): Filter {
        val filterType = when(filterDefinition.id) {
            "blur_effect" -> FilterType.BLUR
            "glow_effect" -> FilterType.GLOW
            else -> throw IllegalArgumentException("Unknown Filter ID: ${filterDefinition.id}")
        }
        return FilterFactory.createFilter(filterType)
    }

    companion object {
        private const val TAG = "FilterRepository"
    }
}
