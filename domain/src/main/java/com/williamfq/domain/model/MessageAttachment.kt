package com.williamfq.domain.model

data class MessageAttachment(
    val url: String,
    val fileName: String = "",
    val fileSize: Long = 0L,
    val mimeType: String = ""
)
