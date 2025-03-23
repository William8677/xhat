package com.williamfq.xhat.ui.chat

import com.williamfq.xhat.domain.model.chat.ChatRoomCategory

// Definición pública y única de la propiedad de extensión "title" para ChatRoomCategory
val ChatRoomCategory.title: String
    get() = when (this) {
        ChatRoomCategory.GENERAL -> "General"
        ChatRoomCategory.FRIENDSHIP -> "Amistad"
        ChatRoomCategory.DATING -> "Citas"
        ChatRoomCategory.GAMING -> "Juegos"
        ChatRoomCategory.MUSIC -> "Música"
        ChatRoomCategory.SPORTS -> "Deportes"
        ChatRoomCategory.TECHNOLOGY -> "Tecnología"
        ChatRoomCategory.EDUCATION -> "Educación"
        ChatRoomCategory.ARTS -> "Artes"
        ChatRoomCategory.TRAVEL -> "Viajes"
        ChatRoomCategory.FOOD -> "Comida"
        ChatRoomCategory.LIFESTYLE -> "Estilo de vida"
    }
