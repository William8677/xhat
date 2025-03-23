package com.williamfq.xhat.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.R
import com.williamfq.xhat.ui.screens.settings.model.SettingItem
import com.williamfq.xhat.ui.screens.settings.model.SettingsGroup
import com.williamfq.xhat.ui.screens.settings.model.SubSettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Buscar configuración") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Atrás")
            }
        }
    )
}

@Composable
fun SettingsGroupSection(
    group: SettingsGroup,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpandClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Contraer" else "Expandir"
            )
        }

        if (isExpanded) {
            group.items.forEach { item ->
                SettingItemRow(
                    item = item,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun SettingItemRow(
    item: SettingItem,
    onItemClick: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (item.subItems.isEmpty()) {
                        onItemClick(item.route)
                    } else {
                        isExpanded = !isExpanded
                    }
                }
                .padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = item.title)
            }

            if (item.subItems.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir"
                )
            }
        }

        if (isExpanded) {
            item.subItems.forEach { subItem ->
                SubSettingItemRow(
                    subItem = subItem,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun SubSettingItemRow(
    subItem: SubSettingItem,
    onItemClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(subItem.route) }
            .padding(start = 72.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = subItem.title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}