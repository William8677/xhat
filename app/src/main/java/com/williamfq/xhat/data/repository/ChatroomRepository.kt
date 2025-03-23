package com.williamfq.xhat.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.williamfq.domain.model.ChatMessage
import com.williamfq.xhat.domain.model.chat.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatroomRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getChatRooms(filter: ChatRoomFilter? = null): Flow<List<ChatRoom>> = callbackFlow {
        var query = db.collection("chatRooms")
            .orderBy("memberCount", Query.Direction.DESCENDING)

        filter?.let {
            if (it.type != null) {
                query = query.whereEqualTo("type", it.type)
            }
            if (it.category != null) {
                query = query.whereEqualTo("category", it.category)
            }
            if (it.country != null) {
                query = query.whereEqualTo("location.country", it.country)
            }
            if (it.language != null) {
                query = query.whereEqualTo("language", it.language)
            }
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error obteniendo salas de chat")
                close(error)
                return@addSnapshotListener
            }

            val rooms = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ChatRoom::class.java)
            } ?: emptyList()

            trySend(rooms)
        }

        awaitClose { subscription.remove() }
    }

    fun getChatMessages(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error obteniendo mensajes de la sala $roomId")
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createSystemRooms() {
        val systemRooms = listOf(
            ChatRoom(
                name = "Sala Global",
                description = "Sala de chat global para todos los usuarios",
                type = ChatRoomType.SYSTEM_LOCATION,
                category = ChatRoomCategory.GENERAL
            ),
            ChatRoom(
                name = "Sala de Amistad",
                description = "Conoce nuevos amigos de todo el mundo",
                type = ChatRoomType.SYSTEM_FRIENDSHIP,
                category = ChatRoomCategory.FRIENDSHIP
            )
        )

        systemRooms.forEach { room ->
            try {
                val existingRoom = db.collection("chatRooms")
                    .whereEqualTo("name", room.name)
                    .whereEqualTo("type", room.type)
                    .get()
                    .await()

                if (existingRoom.isEmpty) {
                    db.collection("chatRooms").add(room).await()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error creando sala de sistema ${room.name}")
            }
        }
    }

    suspend fun createLocationBasedRoom(location: ChatRoomLocation) {
        val locationRooms = listOf(
            ChatRoom(
                name = "${location.country} Chat",
                description = "Sala de chat para ${location.country}",
                type = ChatRoomType.SYSTEM_LOCATION,
                location = location,
                category = ChatRoomCategory.GENERAL
            ),
            ChatRoom(
                name = "${location.region} Chat",
                description = "Sala de chat para ${location.region}",
                type = ChatRoomType.SYSTEM_LOCATION,
                location = location,
                category = ChatRoomCategory.GENERAL
            ),
            ChatRoom(
                name = "${location.city} Chat",
                description = "Sala de chat para ${location.city}",
                type = ChatRoomType.SYSTEM_LOCATION,
                location = location,
                category = ChatRoomCategory.GENERAL
            )
        )

        locationRooms.forEach { room ->
            try {
                val existingRoom = db.collection("chatRooms")
                    .whereEqualTo("name", room.name)
                    .whereEqualTo("type", room.type)
                    .get()
                    .await()

                if (existingRoom.isEmpty) {
                    db.collection("chatRooms").add(room).await()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error creando sala basada en ubicación ${room.name}")
            }
        }
    }

    suspend fun createUserRoom(room: ChatRoom): String {
        try {
            val docRef = db.collection("chatRooms").add(room).await()
            return docRef.id
        } catch (e: Exception) {
            Timber.e(e, "Error creando sala de usuario")
            throw e
        }
    }

    suspend fun sendMessage(message: ChatMessage) {
        try {
            db.collection("chatRooms")
                .document(message.roomId)
                .collection("messages")
                .add(message)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error enviando mensaje a la sala ${message.roomId}")
            throw e
        }
    }

    suspend fun joinRoom(roomId: String, member: ChatRoomMember) {
        try {
            db.collection("chatRooms")
                .document(roomId)
                .collection("members")
                .document(member.userId)
                .set(member)
                .await()

            db.collection("chatRooms")
                .document(roomId)
                .update("memberCount", FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error uniéndose a la sala $roomId")
            throw e
        }
    }

    suspend fun leaveRoom(roomId: String, userId: String) {
        try {
            db.collection("chatRooms")
                .document(roomId)
                .collection("members")
                .document(userId)
                .delete()
                .await()

            db.collection("chatRooms")
                .document(roomId)
                .update("memberCount", FieldValue.increment(-1))
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error abandonando la sala $roomId")
            throw e
        }
    }
}

data class ChatRoomFilter(
    val type: ChatRoomType? = null,
    val category: ChatRoomCategory? = null,
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    val language: String? = null
)