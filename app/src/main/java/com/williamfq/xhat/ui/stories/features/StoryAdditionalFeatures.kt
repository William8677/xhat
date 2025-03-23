package com.williamfq.xhat.ui.stories.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.domain.model.User
import com.williamfq.xhat.ui.stories.BackgroundType
import com.williamfq.xhat.ui.stories.StoryBackground
import java.util.UUID

// ---------------------------
// Dummy composables
// ---------------------------
@Composable
fun UserSuggestionItem(user: User, onSelect: () -> Unit) {
    Button(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
        Text(text = user.username)
    }
}

@Composable
fun HashtagChip(tag: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = "#$tag")
    }
}

@Composable
fun ColorPicker(onColorSelected: (String) -> Unit) {
    Button(onClick = { onColorSelected("#FFFFFF") }, modifier = Modifier.fillMaxWidth()) {
        Text("Seleccionar color")
    }
}

@Composable
fun GradientPicker(onGradientSelected: (List<String>) -> Unit) {
    Button(onClick = { onGradientSelected(listOf("#FF0000", "#00FF00", "#0000FF")) }, modifier = Modifier.fillMaxWidth()) {
        Text("Seleccionar gradiente")
    }
}

@Composable
fun AnimatedBackgroundPicker(onAnimationSelected: (String) -> Unit) {
    Button(onClick = { onAnimationSelected("https://dummy.url/animation.gif") }, modifier = Modifier.fillMaxWidth()) {
        Text("Seleccionar animación")
    }
}

@Composable
fun QuickReplyChip(reply: QuickReply, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(reply.text)
    }
}

@Composable
fun SocialNetworkButton(network: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(network)
    }
}

// ---------------------------
// Data models (puedes reemplazarlos por los reales)
// ---------------------------
data class QuickReply(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val emoji: String? = null
)

data class StoryHighlight(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val coverUrl: String,
    val stories: List<String>
)

data class StoryMention(
    val userId: String,
    val username: String,
    val startIndex: Int,
    val endIndex: Int
)

data class StoryHashtag(
    val tag: String,
    val startIndex: Int,
    val endIndex: Int
)

// ---------------------------
// Composables para características adicionales
// ---------------------------

@Composable
fun MentionSelector(
    onMentionSelected: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<User>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { newQuery -> searchQuery = newQuery },
            label = { Text("Mencionar a alguien") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn {
            items(items = suggestions) { user ->
                UserSuggestionItem(
                    user = user,
                    onSelect = { onMentionSelected(user) }
                )
            }
        }
    }
}

@Composable
fun HashtagSelector(
    onHashtagSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var trendingTags by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { newQuery -> searchQuery = newQuery },
            label = { Text("Agregar hashtag") },
            leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Tendencias",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Usamos un Row para simular un FlowRow (o importa FlowRow si usas accompanist)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            trendingTags.forEach { tag ->
                HashtagChip(
                    tag = tag,
                    onClick = { onHashtagSelected(tag) }
                )
            }
        }
    }
}

@Composable
fun BackgroundSelector(
    onBackgroundSelected: (StoryBackground) -> Unit
) {
    var selectedType by remember { mutableStateOf(BackgroundType.SOLID_COLOR) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TabRow(selectedTabIndex = BackgroundType.values().indexOf(selectedType)) {
            BackgroundType.values().forEach { type ->
                Tab(
                    selected = type == selectedType,
                    onClick = { selectedType = type },
                    text = { Text(type.name) }
                )
            }
        }

        when (selectedType) {
            BackgroundType.SOLID_COLOR -> ColorPicker(
                onColorSelected = { color ->
                    onBackgroundSelected(StoryBackground(BackgroundType.SOLID_COLOR, color))
                }
            )
            BackgroundType.GRADIENT -> GradientPicker(
                onGradientSelected = { colors ->
                    onBackgroundSelected(StoryBackground(BackgroundType.GRADIENT, "", colors))
                }
            )
            BackgroundType.IMAGE -> {
                Button(
                    onClick = { onBackgroundSelected(StoryBackground(BackgroundType.IMAGE, "https://dummy.url/image.jpg")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar imagen")
                }
            }
            BackgroundType.ANIMATED -> AnimatedBackgroundPicker(
                onAnimationSelected = { url ->
                    onBackgroundSelected(StoryBackground(BackgroundType.ANIMATED, url))
                }
            )
        }
    }
}

@Composable
fun QuickReplySelector(
    onQuickReplySelected: (QuickReply) -> Unit
) {
    val defaultReplies = remember {
        listOf(
            QuickReply(text = "¡Me encanta! 😍"),
            QuickReply(text = "¡Increíble! 🔥"),
            QuickReply(text = "¡Wow! ⭐"),
            QuickReply(text = "jajaja 😂"),
            QuickReply(text = "¡Genial! 👏")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Respuestas rápidas",
            style = MaterialTheme.typography.titleMedium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = defaultReplies) { reply ->
                QuickReplyChip(
                    reply = reply,
                    onClick = { onQuickReplySelected(reply) }
                )
            }
        }
    }
}

@Composable
fun StoryHighlightCreator(
    onHighlightCreated: (StoryHighlight) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedStories by remember { mutableStateOf<List<String>>(emptyList()) }
    var coverUrl by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = { newTitle -> title = newTitle },
            label = { Text("Título del destacado") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                // Implementar selección de portada
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (coverUrl == null) "Seleccionar portada" else "Cambiar portada")
        }

        Text(
            text = "Seleccionar historias",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Aquí implementa el selector de historias

        Button(
            onClick = {
                if (title.isNotBlank() && coverUrl != null) {
                    onHighlightCreated(
                        StoryHighlight(
                            title = title,
                            coverUrl = coverUrl!!,
                            stories = selectedStories
                        )
                    )
                }
            },
            enabled = title.isNotBlank() && coverUrl != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear destacado")
        }
    }
}

@Composable
fun SharingOptions(
    onShare: (String) -> Unit
) {
    val socialNetworks = remember {
        listOf(
            "WhatsApp",
            "Facebook",
            "Instagram",
            "Twitter",
            "Telegram"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Compartir en",
            style = MaterialTheme.typography.titleMedium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = socialNetworks) { network ->
                SocialNetworkButton(
                    network = network,
                    onClick = { onShare(network) }
                )
            }
        }
    }
}

