package com.williamfq.xhat.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.williamfq.xhat.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getChannels(filter: ChannelFilter = ChannelFilter()): Flow<List<Channel>> = callbackFlow {
        var query = db.collection("channels")
            .orderBy("stats.subscribersCount", Query.Direction.DESCENDING)

        filter.category?.let {
            query = query.whereEqualTo("category", it)
        }
        filter.language?.let {
            query = query.whereEqualTo("settings.language", it)
        }
        if (filter.verified) {
            query = query.whereEqualTo("isVerified", true)
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error obteniendo canales")
                close(error)
                return@addSnapshotListener
            }

            val channels = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Channel::class.java)
            } ?: emptyList()

            trySend(channels)
        }

        awaitClose { subscription.remove() }
    }

    fun getChannelPosts(
        channelId: String,
        limit: Long = 50
    ): Flow<List<ChannelPost>> = callbackFlow {
        val subscription = db.collection("channels")
            .document(channelId)
            .collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error obteniendo publicaciones del canal $channelId")
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChannelPost::class.java)
                } ?: emptyList()

                trySend(posts)
            }

        awaitClose { subscription.remove() }
    }

    fun getPosts(channelId: String, limit: Long = 50): Flow<List<ChannelPost>> {
        return getChannelPosts(channelId, limit)
    }

    suspend fun createChannel(channel: Channel) {
        try {
            db.collection("channels")
                .document(channel.id)
                .set(channel)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error creando canal ${channel.id}")
            throw e
        }
    }

    suspend fun createPost(channelId: String, post: ChannelPost) {
        try {
            db.collection("channels")
                .document(channelId)
                .collection("posts")
                .add(post)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error creando publicación en el canal $channelId")
            throw e
        }
    }

    suspend fun updateChannelStats(channelId: String, stats: ChannelStats) {
        try {
            db.collection("channels")
                .document(channelId)
                .update("stats", stats)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error actualizando estadísticas del canal $channelId")
            throw e
        }
    }

    suspend fun subscribeToChannel(subscription: ChannelSubscription) {
        try {
            db.collection("channelSubscriptions")
                .add(subscription)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error suscribiendo al canal")
            throw e
        }
    }

    suspend fun unsubscribeFromChannel(channelId: String, userId: String) {
        try {
            db.collection("channelSubscriptions")
                .whereEqualTo("channelId", channelId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .forEach { it.reference.delete().await() }
        } catch (e: Exception) {
            Timber.e(e, "Error desuscribiendo del canal $channelId")
            throw e
        }
    }

    suspend fun addReactionToPost(channelId: String, postId: String, emoji: String) {
        try {
            db.collection("channels")
                .document(channelId)
                .collection("posts")
                .document(postId)
                .update("reactions.$emoji", FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error añadiendo reacción a la publicación $postId")
            throw e
        }
    }

    suspend fun deletePost(channelId: String, postId: String) {
        try {
            db.collection("channels")
                .document(channelId)
                .collection("posts")
                .document(postId)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error eliminando publicación $postId")
            throw e
        }
    }

    suspend fun pinPost(channelId: String, postId: String) {
        try {
            db.collection("channels")
                .document(channelId)
                .collection("posts")
                .document(postId)
                .update("isPinned", true)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error fijando publicación $postId")
            throw e
        }
    }
}

data class ChannelFilter(
    val category: ChannelCategory? = null,
    val language: String? = null,
    val verified: Boolean = false,
    val searchQuery: String = ""
)