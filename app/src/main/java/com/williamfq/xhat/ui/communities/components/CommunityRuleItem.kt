/*
 * Updated: 2025-01-25 23:10:33
 * Author: William8677
 */

package com.williamfq.xhat.ui.communities.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.domain.model.CommunityRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityRuleItem(
    rule: CommunityRule,
    onEdit: (CommunityRule) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = rule.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rule.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onEdit(rule) }) {
                    Text("Editar")
                }
                TextButton(onClick = { onDelete(rule.id) }) {
                    Text("Eliminar")
                }
            }
        }
    }
}