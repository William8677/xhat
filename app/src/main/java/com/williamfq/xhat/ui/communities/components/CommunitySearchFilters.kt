/*
 * Updated: 2025-01-22 00:53:55
 * Author: William8677
 *
 * Este archivo forma parte de la app xhat.
 */

package com.williamfq.xhat.ui.communities.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.ui.communities.filters.CommunityCategory
import com.williamfq.xhat.ui.communities.filters.CommunitySearchFilters
import com.williamfq.xhat.ui.communities.filters.CommunitySortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunitySearchFilterSheet(
    filters: CommunitySearchFilters,
    onFiltersChanged: (CommunitySearchFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var currentFilters by remember { mutableStateOf(filters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filtros de búsqueda",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Ordenar por
            Text(
                text = "Ordenar por",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CommunitySortOption.values().forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = currentFilters.sortBy == option,
                        onClick = {
                            currentFilters = currentFilters.copy(sortBy = option)
                        }
                    )
                    Text(
                        text = option.title,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Categorías
            Text(
                text = "Categorías",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CommunityCategory.values().forEach { category ->
                    FilterChip(
                        selected = category in currentFilters.categoryFilter,
                        onClick = {
                            currentFilters = if (category in currentFilters.categoryFilter) {
                                currentFilters.copy(
                                    categoryFilter = currentFilters.categoryFilter - category
                                )
                            } else {
                                currentFilters.copy(
                                    categoryFilter = currentFilters.categoryFilter + category
                                )
                            }
                        },
                        label = { Text(category.title) }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Opciones adicionales
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SwitchSettingItem(
                    title = "Solo comunidades unidas",
                    checked = currentFilters.onlyJoined,
                    onCheckedChange = {
                        currentFilters = currentFilters.copy(onlyJoined = it)
                    }
                )

                SwitchSettingItem(
                    title = "Solo comunidades moderadas",
                    checked = currentFilters.onlyModerated,
                    onCheckedChange = {
                        currentFilters = currentFilters.copy(onlyModerated = it)
                    }
                )

                SwitchSettingItem(
                    title = "Mostrar NSFW",
                    checked = currentFilters.showNSFW,
                    onCheckedChange = {
                        currentFilters = currentFilters.copy(showNSFW = it)
                    }
                )

                SwitchSettingItem(
                    title = "Mostrar privadas",
                    checked = currentFilters.showPrivate,
                    onCheckedChange = {
                        currentFilters = currentFilters.copy(showPrivate = it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        currentFilters = CommunitySearchFilters()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Restablecer")
                }

                Button(
                    onClick = {
                        onFiltersChanged(currentFilters)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aplicar")
                }
            }
        }
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
