package com.williamfq.data.mapper

import com.williamfq.data.entities.MessageAttachmentEntity
import com.williamfq.domain.model.MessageAttachment

fun MessageAttachment.toEntity(): MessageAttachmentEntity =
    MessageAttachmentEntity(
        url = this.url,
        fileName = this.fileName,
        fileSize = this.fileSize,
        mimeType = this.mimeType
    )

fun MessageAttachmentEntity.toDomain(): MessageAttachment =
    MessageAttachment(
        url = this.url,
        fileName = this.fileName,
        fileSize = this.fileSize,
        mimeType = this.mimeType
    )
