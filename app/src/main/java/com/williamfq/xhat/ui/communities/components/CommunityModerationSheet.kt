/*
 * Updated: 2025-01-25 01:56:11
 * Author: William8677
 *
 * Este archivo forma parte de la app xhat.
 */

package com.williamfq.xhat.ui.communities.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.domain.model.*  // Asegúrate de que los modelos  CommunityRule, CommunitySettings, CommunityMember, ModerationAction, UserManagementAction, ReportAction, etc. estén definidos correctamente
import androidx.compose.ui.text.style.TextAlign
import com.williamfq.domain.model.Community
import com.williamfq.domain.model.CommunityRule
import com.williamfq.domain.model.CommunitySettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityModerationSheet(
    community: Community,
    onAction: (ModerationAction) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Reglas", "Configuración", "Miembros", "Reportes")

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Moderación: ${community.name}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index: Int, title: String ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> RulesTab(
                    rules = community.rules,
                    onUpdateRules = { updatedRules: List<CommunityRule> ->
                        onAction(ModerationAction.UpdateRules(updatedRules))
                    }
                )
                1 -> SettingsTab(
                    settings = community.settings,
                    onUpdateSettings = { newSettings: CommunitySettings ->
                        onAction(ModerationAction.UpdateSettings(newSettings))
                    }
                )
                2 -> MembersTab(
                    communityId = community.id,
                    onManageUser = { userId: String, action: UserManagementAction, duration: Long? ->
                        onAction(ModerationAction.ManageUser(userId, action, duration))
                    }
                )
                3 -> ReportsTab(
                    communityId = community.id,
                    onHandleReport = { reportId: String, action: ReportAction ->
                        onAction(ModerationAction.HandleReport(reportId, action))
                    }
                )
            }
        }
    }
}

@Composable
private fun RulesTab(
    rules: List<CommunityRule>,
    onUpdateRules: (List<CommunityRule>) -> Unit
) {
    var editingRules by remember { mutableStateOf(rules) }
    var showAddRule by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Button(
            onClick = { showAddRule = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar regla")
            Spacer(Modifier.width(8.dp))
            Text("Agregar regla")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(editingRules) { rule ->
                CommunityRuleItem(
                    rule = rule,
                    onEdit = { editedRule: CommunityRule ->
                        val updatedRules: List<CommunityRule> = editingRules.map { currentRule ->
                            if (currentRule.id == editedRule.id) editedRule else currentRule
                        }
                        editingRules = updatedRules
                        onUpdateRules(updatedRules)
                    },
                    onDelete = { ruleId: String ->
                        val updatedRules: List<CommunityRule> = editingRules.filter { it.id != ruleId }
                        editingRules = updatedRules
                        onUpdateRules(updatedRules)
                    }
                )
            }
        }
    }

    if (showAddRule) {
        AddRuleDialog(
            onDismiss = { showAddRule = false },
            onAddRule = { newRule: CommunityRule ->
                editingRules = editingRules + newRule
                onUpdateRules(editingRules)
                showAddRule = false
            }
        )
    }
}

@Composable
private fun SettingsTab(
    settings: CommunitySettings,
    onUpdateSettings: (CommunitySettings) -> Unit
) {
    var editingSettings by remember { mutableStateOf(settings) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        SettingsSection(title = "Contenido") {
            SwitchSettingItem(
                title = "Permitir imágenes",
                checked = editingSettings.allowImages,
                onCheckedChange = { newValue: Boolean ->
                    editingSettings = editingSettings.copy(allowImages = newValue)
                    onUpdateSettings(editingSettings)
                }
            )
            SwitchSettingItem(
                title = "Permitir videos",
                checked = editingSettings.allowVideos,
                onCheckedChange = { newValue: Boolean ->
                    editingSettings = editingSettings.copy(allowVideos = newValue)
                    onUpdateSettings(editingSettings)
                }
            )
            SwitchSettingItem(
                title = "Permitir enlaces",
                checked = editingSettings.allowLinks,
                onCheckedChange = { newValue: Boolean ->
                    editingSettings = editingSettings.copy(allowLinks = newValue)
                    onUpdateSettings(editingSettings)
                }
            )
            SwitchSettingItem(
                title = "Permitir encuestas",
                checked = editingSettings.allowPolls,
                onCheckedChange = { newValue: Boolean ->
                    editingSettings = editingSettings.copy(allowPolls = newValue)
                    onUpdateSettings(editingSettings)
                }
            )
        }

        SettingsSection(title = "Restricciones") {
            SwitchSettingItem(
                title = "Requerir flair en publicaciones",
                checked = editingSettings.requirePostFlair,
                onCheckedChange = { newValue: Boolean ->
                    editingSettings = editingSettings.copy(requirePostFlair = newValue)
                    onUpdateSettings(editingSettings)
                }
            )
            SwitchSettingItem(
                title = "Aprobar publicaciones manualmente",
                checked = editingSettings.requirePostApproval,
                onCheckedChange = { newValue: Boolean ->
                    editingSettings = editingSettings.copy(requirePostApproval = newValue)
                    onUpdateSettings(editingSettings)
                }
            )
            SwitchSettingItem(
                title = "Restringir publicación a moderadores",
                checked = editingSettings.restrictPosting,
                onCheckedChange = { newValue: Boolean ->
                    editingSettings = editingSettings.copy(restrictPosting = newValue)
                    onUpdateSettings(editingSettings)
                }
            )
        }
    }
}

@Composable
private fun MembersTab(
    communityId: String,
    onManageUser: (String, UserManagementAction, Long?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var members by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery: String -> searchQuery = newQuery },
            label = { Text("Buscar miembros") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(members) { member ->
                // Para efectos de ejemplo, creamos un objeto User "dummy"
                // Se pasa también un valor para avatarUrl (por ejemplo, cadena vacía)
                val user = com.williamfq.domain.model.User(
                    id = member.userId,
                    username = "Usuario ${member.userId}",
                    avatarUrl = ""  // Se agrega avatarUrl, ya que es requerido
                )
                MemberItem(
                    member = member,
                    user = user,  // Se pasa el objeto user requerido
                    onBan = { userId: String ->
                        onManageUser(userId, UserManagementAction.Ban, null)
                    },
                    onMute = { userId: String, duration: Long ->
                        onManageUser(userId, UserManagementAction.Mute, duration)
                    },
                    onPromote = { userId: String ->
                        onManageUser(userId, UserManagementAction.Promote, null)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReportsTab(
    communityId: String,
    onHandleReport: (String, ReportAction) -> Unit
) {
    var reports by remember { mutableStateOf<List<CommunityReport>>(emptyList()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        items(reports) { report ->
            ReportItem(
                report = report,
                onApprove = {
                    onHandleReport(report.id, ReportAction.Approve)
                },
                onReject = {
                    onHandleReport(report.id, ReportAction.Reject)
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Start
        )
        content()
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
