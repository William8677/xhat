/*
 * Updated: 2025-01-22 00:20:31
 * Author: William8677
 */

package com.williamfq.xhat.ui.stories.model

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.util.UUID

// Modelos de datos para las historias
sealed class StoryContent {
    data class Image(
        val url: String,
        val aspectRatio: Float = 1f
    ) : StoryContent()

    data class Video(
        val url: String,
        val duration: Long,
        val thumbnail: String
    ) : StoryContent()

    data class Poll(
        val question: String,
        val options: List<PollOption>,
        val endTime: Long,
        val isMultipleChoice: Boolean = false
    ) : StoryContent()

    data class Music(
        val songUrl: String,
        val artistName: String,
        val songName: String,
        val albumCover: String
    ) : StoryContent()

    data class Quiz(
        val question: String,
        val options: List<QuizOption>,
        val correctAnswer: Int,
        val explanation: String
    ) : StoryContent()

    data class Countdown(
        val title: String,
        val endTime: Long,
        val background: String
    ) : StoryContent()

    data class Location(
        val latitude: Double,
        val longitude: Double,
        val placeName: String,
        val address: String
    ) : StoryContent()
}

data class PollOption(
    val id: String,
    val text: String,
    var votes: Int = 0,
    val voters: MutableList<String> = mutableListOf()
)

data class QuizOption(
    val id: String,
    val text: String,
    var participants: Int = 0
)

data class StoryInteraction(
    val type: InteractionType,
    val userId: String,
    val timestamp: Long,
    val content: String? = null
)

enum class InteractionType {
    REACTION, COMMENT, POLL_VOTE, QUIZ_ANSWER, SHARE
}

// Componentes de UI para las nuevas funcionalidades

@Composable
fun StoryPollCreator(
    onPollCreated: (StoryContent.Poll) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(List(2) { "" }) }
    var isMultipleChoice by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf(24f) } // horas

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = question,
            onValueChange = { newQuestion -> question = newQuestion },
            label = { Text("Pregunta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opciones de respuesta
        options.forEachIndexed { index, optionValue ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = optionValue,
                    onValueChange = { newOption ->
                        options = options.toMutableList().also { list -> list[index] = newOption }
                    },
                    label = { Text("Opción ${index + 1}") },
                    modifier = Modifier.weight(1f)
                )
                if (options.size > 2) {
                    IconButton(onClick = {
                        options = options.filterIndexed { i, _ -> i != index }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar opción")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Agregar opción
        TextButton(
            onClick = { options = options + "" },
            enabled = options.size < 4
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar opción")
        }

        // Configuración adicional
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Selección múltiple")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isMultipleChoice,
                onCheckedChange = { checked -> isMultipleChoice = checked }
            )
        }

        // Duración
        Text("Duración: ${duration.toInt()} horas")
        Slider(
            value = duration,
            onValueChange = { newDuration -> duration = newDuration },
            valueRange = 1f..48f,
            steps = 47
        )

        Button(
            onClick = {
                val poll = StoryContent.Poll(
                    question = question,
                    options = options.map { optionText ->
                        PollOption(
                            id = UUID.randomUUID().toString(),
                            text = optionText
                        )
                    },
                    endTime = System.currentTimeMillis() + (duration * 3600000).toLong(),
                    isMultipleChoice = isMultipleChoice
                )
                onPollCreated(poll)
            },
            enabled = question.isNotBlank() && options.all { it.isNotBlank() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear encuesta")
        }
    }
}

@Composable
fun StoryQuizCreator(
    onQuizCreated: (StoryContent.Quiz) -> Unit
) {
    // Implementación similar a StoryPollCreator pero para Quiz.
    // Completa según tus necesidades.
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Creador de Quiz (pendiente de implementación)")
    }
}

@Composable
fun StoryCountdownCreator(
    onCountdownCreated: (StoryContent.Countdown) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDateTime.now()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = { newTitle -> title = newTitle },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        // Aquí agregar un selector de fecha/hora, según tu implementación.
        Button(
            onClick = {
                // Ejemplo dummy: calcula endTime a partir de la fecha actual.
                val countdown = StoryContent.Countdown(
                    title = title,
                    endTime = System.currentTimeMillis() + 3600000, // 1 hora en el futuro.
                    background = ""
                )
                onCountdownCreated(countdown)
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear cuenta regresiva")
        }
    }
}

@Composable
fun StoryStickerSelector(
    onStickerSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var stickers by remember { mutableStateOf(emptyList<String>()) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { newQuery -> searchQuery = newQuery },
            label = { Text("Buscar stickers") },
            modifier = Modifier.fillMaxWidth()
        )
        // Implementa la lista de stickers según tu lógica.
        // Por ejemplo, un LazyRow o Column con imágenes.
        Button(
            onClick = { onStickerSelected("sticker_id_dummy") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar sticker dummy")
        }
    }
}

@Composable
fun StoryMusicSelector(
    onMusicSelected: (StoryContent.Music) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var songs by remember { mutableStateOf(emptyList<StoryContent.Music>()) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { newQuery -> searchQuery = newQuery },
            label = { Text("Buscar música") },
            modifier = Modifier.fillMaxWidth()
        )
        // Implementa la lista de canciones según tu lógica.
        Button(
            onClick = {
                val dummyMusic = StoryContent.Music(
                    songUrl = "https://dummy.url/song.mp3",
                    artistName = "Artista Dummy",
                    songName = "Canción Dummy",
                    albumCover = "https://dummy.url/cover.jpg"
                )
                onMusicSelected(dummyMusic)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar música dummy")
        }
    }
}
