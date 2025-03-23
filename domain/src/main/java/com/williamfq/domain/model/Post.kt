// Archivo: com/williamfq/xhat/domain/model/Post.kt
package com.williamfq.xhat.domain.model

import com.williamfq.domain.model.Comment
import com.williamfq.domain.model.User

data class Post(
    val id: String,
    val communityId: String,
    val author: User,
    val title: String,
    val content: String,
    val upvotes: Int,
    val downvotes: Int,
    val comments: List<Comment>,
    val timestamp: Long
)
