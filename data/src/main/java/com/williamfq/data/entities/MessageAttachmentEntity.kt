package com.williamfq.data.entities

data class MessageAttachmentEntity(
    val url: String,
    val fileName: String = "",
    val fileSize: Long = 0L,
    val mimeType: String = ""
)
