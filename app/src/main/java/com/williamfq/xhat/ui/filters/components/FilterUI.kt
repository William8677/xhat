/*
 * Updated: 2025-01-21 20:01:49
 * Author: William8677
 */

package com.williamfq.xhat.ui.filters.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.williamfq.xhat.R
import com.williamfq.xhat.domain.model.FilterCategory
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.ui.filters.components.controls.ArtisticControls
import com.williamfq.xhat.ui.filters.components.controls.BeautyControls
import com.williamfq.xhat.ui.filters.components.controls.EffectsControls
import com.williamfq.xhat.ui.filters.components.controls.FunControls
import com.williamfq.xhat.utils.ar.ARSystem



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: FilterType?,
    onFilterSelected: (FilterType?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    arSystem: ARSystem = ARSystem(LocalContext.current)
) {
    var selectedCategory by remember { mutableStateOf(FilterCategory.BEAUTY) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Título
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Categorías
            FilterCategories(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // Lista de filtros
            FilterGrid(
                category = selectedCategory,
                currentFilter = currentFilter,
                onFilterSelected = onFilterSelected
            )
        }
    }
}

@Composable
private fun FilterCategories(
    selectedCategory: FilterCategory,
    onCategorySelected: (FilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = selectedCategory.ordinal,
        modifier = modifier,
        edgePadding = 16.dp,
        divider = {}
    ) {
        FilterCategory.values().forEach { category ->
            Tab(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = when (category) {
                            FilterCategory.BEAUTY -> "Belleza"
                            FilterCategory.FUN -> "Diversión"
                            FilterCategory.ARTISTIC -> "Artístico"
                            FilterCategory.EFFECTS -> "Efectos"
                        }
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = when (category) {
                                FilterCategory.BEAUTY -> R.drawable.ic_filter_beauty
                                FilterCategory.FUN -> R.drawable.ic_filter_fun
                                FilterCategory.ARTISTIC -> R.drawable.ic_filter_artistic
                                FilterCategory.EFFECTS -> R.drawable.ic_filter_effects
                            }
                        ),
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
private fun FilterGrid(
    category: FilterCategory,
    currentFilter: FilterType?,
    onFilterSelected: (FilterType?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // Opción "Sin filtro"
        item {
            NoFilterOption(
                isSelected = currentFilter == null,
                onSelected = { onFilterSelected(null) }
            )
        }
        // Filtros disponibles: se especifica el tipo para evitar confusiones.
        items(
            items = getFiltersForCategory(category),
            key = { it.id }
        ) { filter ->
            FilterOption(
                filter = filter,
                isSelected = filter == currentFilter,
                onSelected = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun NoFilterOption(
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterOptionCard(
        isSelected = isSelected,
        onClick = onSelected,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_filter_none),
                contentDescription = "Sin filtro",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Original",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FilterOption(
    filter: FilterType,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterOptionCard(
        isSelected = isSelected,
        onClick = onSelected,
        modifier = modifier
    ) {
        Column {
            // Previsualización del filtro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = filter.previewUrl,
                    contentDescription = filter.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Nombre del filtro
            Text(
                text = filter.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun FilterOptionCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        modifier = modifier.aspectRatio(1f)
    ) {
        content()
    }
}

@Composable
fun FilterControls(
    filter: FilterType?,
    onIntensityChange: (Float) -> Unit,
    onParameterChange: (String, Float) -> Unit = { _, _ -> },
    arSystem: ARSystem = ARSystem(LocalContext.current),
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = filter != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            if (filter != null) {
                // Nombre del filtro
                Text(
                    text = filter.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Control de intensidad
                FilterIntensitySlider(
                    intensity = filter.intensity,
                    onIntensityChange = onIntensityChange
                )
                // Controles adicionales específicos del filtro
                when (filter.category) {
                    FilterCategory.BEAUTY -> BeautyControls(filter, onParameterChange)
                    FilterCategory.FUN -> FunControls(filter, arSystem, onAREffectSelected = { /* default */ }, onParameterChange = onParameterChange)
                    FilterCategory.ARTISTIC -> ArtisticControls(filter, onParameterChange)
                    FilterCategory.EFFECTS -> EffectsControls(filter, onParameterChange)
                }
            }
        }
    }
}

@Composable
private fun FilterIntensitySlider(
    intensity: Float,
    onIntensityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Intensidad",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = intensity,
            onValueChange = onIntensityChange,
            valueRange = 0f..1f,
            steps = 100
        )
    }
}

private fun getFiltersForCategory(category: FilterCategory): List<FilterType> {
    // Aquí deberías obtener los filtros desde tu ViewModel o repositorio
    return emptyList<FilterType>()
}
