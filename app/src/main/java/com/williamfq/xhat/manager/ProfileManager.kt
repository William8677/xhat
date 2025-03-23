
package com.williamfq.xhat.manager

class ProfileManager {
    fun updateProfile(userId: String, profileData: Map<String, Any>) {
        println("Perfil del usuario \$userId actualizado con: \$profileData")
    }

    fun enableBirthdayNotifications(userId: String, isEnabled: Boolean) {
        if (isEnabled) {
            println("Notificaciones de cumpleaños activadas para el usuario \$userId")
        } else {
            println("Notificaciones de cumpleaños desactivadas para el usuario \$userId")
        }
    }
}
