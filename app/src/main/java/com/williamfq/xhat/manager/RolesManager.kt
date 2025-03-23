
package com.williamfq.xhat.manager

import com.google.firebase.firestore.FirebaseFirestore

class RolesManager {
    private val db = FirebaseFirestore.getInstance()

    fun addRoleToUser(userId: String, role: String) {
        // Lógica para agregar un rol a un usuario en una comunidad
        db.collection("roles").document(userId).set(mapOf("role" to role))
    }

    fun removeRoleFromUser(userId: String) {
        // Lógica para eliminar un rol
        db.collection("roles").document(userId).delete()
    }
}
