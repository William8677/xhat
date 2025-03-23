
package com.williamfq.xhat.core

import org.webrtc.PeerConnectionFactory

class VideoCallManager {
    private val peerConnectionFactory: PeerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()

    fun enableScreenSharing() {
        // Lógica para compartir pantalla durante videollamadas
        println("Compartir pantalla habilitado")
    }

    fun applyARFilter(filter: String) {
        // Aplicar filtros AR durante videollamadas
        println("Filtro AR aplicado: \$filter")
    }
}
