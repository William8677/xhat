package com.williamfq.data.mapper

import com.williamfq.data.entities.ChatEntity
import com.williamfq.domain.model.ChatInfo
import com.williamfq.domain.model.Participant

fun ChatEntity.toDomain(): ChatInfo {
    return ChatInfo(
        id = this.id,
        title = this.title,
        photoUrl = this.photoUrl,
        isGroup = this.isGroup,
        // Aquí, si no dispones de la lista de participantes, puedes pasar una lista vacía
        participants = emptyList<Participant>(),
        createdAt = this.createdAt,
        lastMessageTime = this.lastMessageTime,
        unreadCount = this.unreadCount,
        isArchived = this.isArchived,
        isMuted = this.isMuted,
        isPinned = this.isPinned,
        otherUserId = this.otherUserId
    )
}
