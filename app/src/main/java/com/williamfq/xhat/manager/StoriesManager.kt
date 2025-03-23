
package com.williamfq.xhat.manager

class StoriesManager {
    fun setStoryDuration(userId: String, duration: Int) {
        if (duration in listOf(30, 60, 120)) {
            println("Duración de la historia establecida en \$duration segundos para el usuario \$userId")
        } else {
            println("Duración no permitida. Use 30, 60 o 120 segundos.")
        }
    }
}
