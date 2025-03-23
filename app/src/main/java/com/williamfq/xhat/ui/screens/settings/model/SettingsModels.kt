package com.williamfq.xhat.ui.screens.settings.model

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingsGroup(
    val title: String,
    val items: List<SettingItem>
)

data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val subItems: List<SubSettingItem> = emptyList()
)

data class SubSettingItem(
    val title: String,
    val route: String
)