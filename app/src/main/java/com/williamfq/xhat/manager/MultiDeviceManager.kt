// MultiDeviceManager.kt: Clase para gestionar la integración multidispositivo
package com.williamfq.xhat.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MultiDeviceManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun notifyMultiDeviceLogin() {
        val currentUser = auth.currentUser
        currentUser?.let {
            db.collection("users").document(it.uid).collection("sessions").add(hashMapOf(
                "timestamp" to System.currentTimeMillis()
            )).addOnSuccessListener {
                println("Notificación enviada: Sesión iniciada en otro dispositivo")
            }
        }
    }
}
