package com.williamfq.xhat.ui.stories

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.williamfq.domain.model.MediaType
import com.williamfq.domain.model.PrivacyLevel
import com.williamfq.domain.model.Reaction
import com.williamfq.domain.model.Story
import com.williamfq.domain.model.StoryHashtag
import com.williamfq.domain.model.StoryHighlight
import com.williamfq.domain.model.StoryInteraction
import com.williamfq.domain.model.StoryMention
import com.williamfq.domain.model.User
import com.williamfq.domain.repository.StoryRepository
import com.williamfq.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUserId.value = userRepository.getCurrentUserId()
        }
    }

    fun addStory(story: Story) {
        viewModelScope.launch {
            storyRepository.addStory(story)
        }
    }
}

@Composable
fun AddStoryScreen(
    navController: NavHostController,
    viewModel: AddStoryViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf(MediaType.TEXT) }
    var mediaUrl by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf(PrivacyLevel.PUBLIC) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Crear Nueva Historia",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tipo de Medio", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MediaType.values().forEach { type ->
                FilterChip(
                    selected = selectedMediaType == type,
                    onClick = { selectedMediaType = type },
                    label = { Text(type.name) }
                )
            }
        }
        if (selectedMediaType != MediaType.TEXT) {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = mediaUrl,
                onValueChange = { mediaUrl = it },
                label = { Text("URL del Medio") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Privacidad", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PrivacyLevel.values().forEach { level ->
                FilterChip(
                    selected = privacy == level,
                    onClick = { privacy = level },
                    label = { Text(level.name) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val story = Story(
                    id = 0,
                    userId = viewModel.currentUserId.value ?: return@Button,
                    title = title,
                    description = description,
                    content = description,
                    mediaType = selectedMediaType,
                    mediaUrl = if (selectedMediaType != MediaType.TEXT) mediaUrl else null,
                    timestamp = System.currentTimeMillis(),
                    isActive = true,
                    views = 0,
                    durationHours = 24,
                    durationSeconds = 30,
                    privacy = privacy,
                    reactions = emptyList(),
                    comments = emptyList(),
                    mentions = emptyList(),
                    hashtags = emptyList(),
                    interactiveElements = null
                )
                viewModel.addStory(story)
                navController.popBackStack()
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Publicar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddStoryScreenPreview() {
    AddStoryScreen(
        navController = rememberNavController(),
        viewModel = AddStoryViewModel(
            storyRepository = object : StoryRepository {
                override val stories: Flow<List<Story>> = flowOf(emptyList())
                override suspend fun getStories(): List<Story> = emptyList()
                override suspend fun getStoryById(storyId: Int): Story? = null
                override suspend fun addStory(story: Story) {}
                override suspend fun updateStory(updatedStory: Story) {}
                override suspend fun deleteStory(storyId: Int) {}
                override suspend fun markStoryAsViewed(storyId: Int) {}
                override suspend fun addReaction(storyId: Int, reaction: Reaction) {}
                override suspend fun addComment(storyId: Int, commentId: String, comment: String) {}
                override suspend fun getMoreStories(offset: Int): List<Story> = emptyList()
                override suspend fun getStoriesByUserId(userId: String): List<Story> = emptyList()
                override suspend fun searchStories(query: String): List<Story> = emptyList()
                override suspend fun addMention(storyId: Int, mention: StoryMention) {}
                override suspend fun addHashtag(storyId: Int, hashtag: StoryHashtag) {}
                override suspend fun addInteraction(storyId: Int, interaction: StoryInteraction) {}
                override suspend fun createHighlight(highlight: StoryHighlight) {}
                override suspend fun shareStory(storyId: Int, platform: String) {}
                override suspend fun updateStories(storiesToUpdate: List<Story>) {}
            },
            userRepository = object : UserRepository {
                override fun getCurrentUserId(): String = "user1"
                override suspend fun saveUserId(userId: String) {}
                override fun isUserLoggedIn(): Boolean = true
                override suspend fun clearUserData() {}
                override suspend fun getCurrentUserName(): String = "User1"
                override suspend fun getCurrentUserEmail(): String = "user1@example.com"
                override suspend fun getCurrentUserPhotoUrl(): String = ""
                override suspend fun getCurrentUserFollowersCount(): Int = 0
                override suspend fun getCurrentUserFollowingCount(): Int = 0
                override suspend fun getCurrentUserPostsCount(): Int = 0
                override suspend fun getUsers(): List<User> = emptyList()
                override suspend fun saveSelectedUsers(selectedUserIds: Set<String>) {}
            }
        )
    )
}