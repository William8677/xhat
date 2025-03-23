package com.williamfq.xhat.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FirebaseInit {
    companion object {
        fun initialize(application: android.app.Application) {
            // Initialize Firebase
            FirebaseApp.initializeApp(application)

            // Configure Firestore settings
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            // Apply settings to Firestore instance
            FirebaseFirestore.getInstance().apply {
                firestoreSettings = settings
            }
        }
    }
}