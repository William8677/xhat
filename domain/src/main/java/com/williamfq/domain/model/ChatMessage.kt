package com.williamfq.domain.model

data class ChatMessage(
    val id: Int = 0,
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val username: String = "",
    val isRead: Boolean = false,
    val isSent: Boolean = false,
    val isDeleted: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT,
    val roomId: String = "",
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val replyTo: String? = null,
    val mentions: List<String> = emptyList(),
    val attachments: List<MessageAttachment> = emptyList(),
    val extraData: Map<String, String> = emptyMap(),
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val deletionType: DeletionType = DeletionType.NONE,
    val autoDestructAt: Long? = null,
    val isMediaMessage: Boolean = false,
    val canBeEdited: Boolean = true
) {
    fun getAttachmentUrls(): List<String> = attachments.map { it.url }
}
