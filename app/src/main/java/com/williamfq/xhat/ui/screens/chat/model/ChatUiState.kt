package com.williamfq.xhat.ui.screens.chat.model

import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.model.ChatInfo
import com.williamfq.xhat.domain.model.chat.ChatRoom
import com.williamfq.xhat.service.WalkieTalkieState

data class ChatUiState(
    // Estado básico del chat
    val isLoading: Boolean = false,
    val chatTitle: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val searchResults: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val error: String? = null,
    val showError: Boolean = false,
    val showInfo: Boolean = false,
    val infoMessage: String = "",

    // Estado de la sala y usuario
    val room: ChatRoom? = null,
    val currentUserId: String = "",
    val chatInfo: ChatInfo? = null,
    val chats: List<ChatPreview> = emptyList(),
    val typingUsers: List<String> = emptyList(),

    // Estados de comunicación
    val isRecording: Boolean = false,
    val isInCall: Boolean = false,
    val isInVideoCall: Boolean = false,
    val isChatGPTActive: Boolean = false,
    val isWalkieTalkieActive: Boolean = false,
    val walkieTalkieState: WalkieTalkieState = WalkieTalkieState.Idle,

    // Permisos
    val hasAudioPermission: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val isMicrophoneEnabled: Boolean = false,
    val isCameraEnabled: Boolean = false,
    val isSpeakerEnabled: Boolean = false,
    val isBluetoothEnabled: Boolean = false,

    // Estados de UI
    val showMenu: Boolean = false,
    val showMessageOptions: Boolean = false,
    val showAttachmentOptions: Boolean = false,
    val isSearchActive: Boolean = false,
    val showContactInfo: Boolean = false,
    val showAddToListDialog: Boolean = false,
    val showFilesScreen: Boolean = false,
    val showLinksScreen: Boolean = false,
    val showWallpaperPicker: Boolean = false,
    val showTemporaryMessagesSettings: Boolean = false,
    val showReportDialog: Boolean = false,

    // Selección y mensajes
    val selectedMessage: ChatMessage? = null, // Actualizado a ChatMessage
    val selectedMessages: List<String> = emptyList(),

    // Estados del chat
    val isMuted: Boolean = false,
    val isBlocked: Boolean = false,
    val muteDuration: String? = null,
    val shouldNavigateBack: Boolean = false,

    // Exportación y accesos directos
    val exportUrl: String? = null,
    val shortcutCreated: Boolean = false,

    val isEditing: Boolean = false,
    val canEditMessage: Boolean = false,
)
