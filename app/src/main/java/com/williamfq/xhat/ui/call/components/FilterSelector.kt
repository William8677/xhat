package com.williamfq.xhat.ui.call.components

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.R
import com.williamfq.xhat.filters.FilterFactory
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.domain.model.FilterType

@Composable
fun FilterSelector(
    currentFilter: Filter?,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            FilterHeader(
                isExpanded = isExpanded,
                currentFilter = currentFilter,
                onExpandClick = { isExpanded = !isExpanded }
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FilterGrid(
                    currentFilter = currentFilter,
                    onFilterSelected = { filterType ->
                        onFilterSelected(filterType)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterHeader(
    isExpanded: Boolean,
    currentFilter: Filter?,
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.filters),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onExpandClick) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Ocultar filtros" else "Mostrar filtros"
            )
        }
    }
}

@Composable
private fun FilterGrid(
    currentFilter: Filter?,
    onFilterSelected: (FilterType) -> Unit
) {
    val availableFilters = remember { FilterType.values().toList() }

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableFilters.forEach { filterType ->
            val filter = FilterFactory.createFilter(filterType)
            FilterChip(
                selected = filter == currentFilter,
                onClick = { onFilterSelected(filterType) },
                label = { Text(filterType.name.lowercase().capitalize()) }
            )
        }
    }
}