package com.williamfq.xhat.ui.communities.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.williamfq.domain.model.User
import com.williamfq.xhat.domain.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.williamfq.domain.model.Community
import com.williamfq.domain.model.CommunityRule

// Función de ayuda para formatear la fecha
private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(date)
}

@Composable
fun CommunityCard(
    community: Community,
    onCommunityClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onCommunityClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Banner
            AsyncImage(
                model = community.bannerUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            // Info básica
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${community.memberCount} miembros",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = onJoinClick
                ) {
                    Text(if (community.isJoined) "Unido" else "Unirse")
                }
            }

            // Tags
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                community.tags.forEach { tag ->
                    AssistChip(
                        onClick = { },
                        label = { Text(tag) }
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityRuleItem(
    rule: CommunityRule,
    onEditClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = rule.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (onEditClick != null) {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar regla")
                    }
                }
            }
            Text(
                text = rule.description,
                style = MaterialTheme.typography.bodyMedium
            )
            if (rule.isRequired) {
                AssistChip(
                    onClick = { },
                    label = { Text("Requerido") }
                )
            }
        }
    }
}

@Composable
fun CommunityFlairSelector(
    flairs: List<CommunityFlair>,
    selectedFlair: CommunityFlair?,
    onFlairSelected: (CommunityFlair) -> Unit
) {
    Column {
        Text(
            text = "Selecciona un flair",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            flairs.forEach { flair ->
                AssistChip(
                    onClick = { onFlairSelected(flair) },
                    label = { Text(flair.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(android.graphics.Color.parseColor(flair.backgroundColor)),
                        labelColor = Color(android.graphics.Color.parseColor(flair.textColor))
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (flair == selectedFlair)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

@Composable
fun CommunityMemberItem(
    member: CommunityMember,
    user: User,
    onActionClick: ((MemberAction) -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(user.username) },
        supportingContent = { Text("Miembro desde ${formatDate(Date(member.joinedAt))}") },
        leadingContent = {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            if (onActionClick != null) {
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    MemberAction.entries.forEach { action ->
                        DropdownMenuItem(
                            text = { Text(action.title) },
                            onClick = {
                                onActionClick(action)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    )
}

enum class MemberAction(val title: String) {
    BAN("Banear"),
    MUTE("Silenciar"),
    APPROVE("Aprobar"),
    REMOVE("Eliminar")
}
