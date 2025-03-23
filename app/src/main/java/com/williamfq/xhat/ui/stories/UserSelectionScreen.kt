package com.williamfq.xhat.ui.stories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.williamfq.domain.model.User
import com.williamfq.domain.repository.UserRepository
import com.williamfq.xhat.ui.stories.viewmodel.UserSelectionViewModel
import java.time.Instant

@Composable
fun UserSelectionScreen(
    navController: NavHostController,
    viewModel: UserSelectionViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState(initial = emptyList())
    val selectedUsers by viewModel.selectedUsers.collectAsState(initial = emptySet())
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Selecciona usuarios",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(users) { user ->
                UserItem(
                    user = user,
                    isSelected = selectedUsers.contains(user.id),
                    onSelect = { isSelected ->
                        if (isSelected) viewModel.selectUser(user.id)
                        else viewModel.deselectUser(user.id)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.confirmSelection()
                navController.popBackStack()
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Confirmar", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun UserItem(
    user: User,
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelect,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            user.lastMessage?.let {
                Text(
                    text = "Último mensaje: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserSelectionScreenPreview() {
    val mockRepository = object : UserRepository {
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
        override suspend fun getUsers(): List<User> = listOf(
            User(
                id = "1",
                username = "user1",
                avatarUrl = "",
                name = "Usuario 1",
                lastMessage = "Hola, ¿qué tal?",
                lastActiveTime = Instant.now()
            ),
            User(
                id = "2",
                username = "user2",
                avatarUrl = "",
                name = "Usuario 2",
                lastMessage = "Genial, gracias!",
                lastActiveTime = Instant.now()
            )
        )
        override suspend fun saveSelectedUsers(selectedUserIds: Set<String>) {}
    }
    UserSelectionScreen(
        navController = rememberNavController(),
        viewModel = UserSelectionViewModel(mockRepository)
    )
}