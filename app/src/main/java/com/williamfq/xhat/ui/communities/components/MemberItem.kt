/*
 * Updated: 2025-01-25 23:10:33
 * Author: William8677
 *
 * Este archivo forma parte de la app xhat.
 */

package com.williamfq.xhat.ui.communities.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.domain.model.CommunityMember
import com.williamfq.xhat.domain.model.MemberRole
import com.williamfq.domain.model.User  // Asegúrate de que este modelo esté definido correctamente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberItem(
    member: CommunityMember,
    user: User, // Se agrega el objeto User para obtener username e id
    onBan: (String) -> Unit,
    onMute: (String, Long) -> Unit,
    onPromote: (String) -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Se usan las propiedades del objeto User
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = member.role.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = { showOptions = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Más opciones"
                )
            }
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Gestionar miembro") },
            text = {
                Column {
                    if (member.role != MemberRole.ADMIN) {
                        TextButton(
                            onClick = {
                                // Se usa el id del objeto User
                                onBan(user.id)
                                showOptions = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Banear")
                        }
                        TextButton(
                            onClick = {
                                onMute(user.id, 24 * 60 * 60 * 1000) // 24 horas
                                showOptions = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Silenciar 24h")
                        }
                        if (member.role == MemberRole.MEMBER) {
                            TextButton(
                                onClick = {
                                    onPromote(user.id)
                                    showOptions = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Promover a moderador")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOptions = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
