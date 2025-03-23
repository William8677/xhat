package com.williamfq.xhat.manager

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class CommunityManager : CoroutineScope {

    private val db = FirebaseFirestore.getInstance()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    /**
     * Crear una nueva comunidad.
     */
    fun createCommunity(name: String, description: String, createdBy: String, callback: (Boolean, String?) -> Unit) {
        if (name.isBlank() || createdBy.isBlank()) {
            callback(false, "El nombre o el creador no pueden estar vacíos.")
            return
        }

        val communityData = hashMapOf(
            "name" to name,
            "description" to description,
            "createdBy" to createdBy,
            "createdAt" to System.currentTimeMillis(),
            "isActive" to true
        )

        db.collection("communities").add(communityData)
            .addOnSuccessListener { callback(true, it.id) }
            .addOnFailureListener { callback(false, it.message) }
    }

    /**
     * Publicar en una comunidad.
     */
    fun postToCommunity(communityId: String, authorId: String, content: String, callback: (Boolean, String?) -> Unit) {
        if (content.isBlank()) {
            callback(false, "El contenido no puede estar vacío.")
            return
        }

        val postData = hashMapOf(
            "authorId" to authorId,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("communities").document(communityId).collection("posts").add(postData)
            .addOnSuccessListener { callback(true, it.id) }
            .addOnFailureListener { callback(false, it.message) }
    }

    /**
     * Comentar en una publicación de una comunidad.
     */
    fun commentOnPost(communityId: String, postId: String, authorId: String, comment: String, callback: (Boolean, String?) -> Unit) {
        if (comment.isBlank()) {
            callback(false, "El comentario no puede estar vacío.")
            return
        }

        val commentData = hashMapOf(
            "authorId" to authorId,
            "comment" to comment,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("communities").document(communityId).collection("posts").document(postId).collection("comments").add(commentData)
            .addOnSuccessListener { callback(true, it.id) }
            .addOnFailureListener { callback(false, it.message) }
    }

    /**
     * Eliminar una comunidad solo si el usuario es administrador.
     */
    fun deleteCommunity(communityId: String, requesterId: String, callback: (Boolean, String?) -> Unit) {
        db.collection("communities").document(communityId).get()
            .addOnSuccessListener { document ->
                val createdBy = document.getString("createdBy")
                if (createdBy == requesterId) {
                    db.collection("communities").document(communityId).delete()
                        .addOnSuccessListener { callback(true, null) }
                        .addOnFailureListener { callback(false, it.message) }
                } else {
                    callback(false, "No tienes permisos para eliminar esta comunidad.")
                }
            }
            .addOnFailureListener { callback(false, it.message) }
    }

    /**
     * Obtener miembros de una comunidad.
     */
    fun getMembers(communityId: String, callback: (List<Map<String, Any>>?, String?) -> Unit) {
        db.collection("communities").document(communityId).collection("members").get()
            .addOnSuccessListener { snapshot ->
                val members = snapshot.documents.mapNotNull { it.data }
                callback(members, null)
            }
            .addOnFailureListener { callback(null, it.message) }
    }

    /**
     * Asignar rol a un miembro.
     */
    fun assignRole(communityId: String, memberId: String, role: String, callback: (Boolean, String?) -> Unit) {
        val validRoles = listOf("Administrador", "Moderador", "Miembro")
        if (role !in validRoles) {
            callback(false, "Rol inválido. Los roles permitidos son: $validRoles")
            return
        }

        db.collection("communities").document(communityId).collection("members").document(memberId)
            .update("role", role)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it.message) }
    }

    /**
     * Añadir miembro a una comunidad.
     */
    fun addMember(communityId: String, memberId: String, callback: (Boolean, String?) -> Unit) {
        val memberData = hashMapOf(
            "memberId" to memberId,
            "role" to "Miembro",
            "joinedAt" to System.currentTimeMillis()
        )

        db.collection("communities").document(communityId).collection("members").document(memberId).set(memberData)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it.message) }
    }

    /**
     * Limpiar las corrutinas al destruir el objeto.
     */
    fun clear() {
        job.cancel()
    }
}
