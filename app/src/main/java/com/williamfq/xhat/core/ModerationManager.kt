
package com.williamfq.xhat.core

class ModerationManager {
    fun flagInappropriateContent(contentId: String, reason: String) {
        println("Contenido \$contentId marcado como inapropiado por: \$reason.")
    }

    fun banUserFromCommunity(userId: String, communityId: String) {
        println("Usuario \$userId baneado de la comunidad \$communityId.")
    }
}
