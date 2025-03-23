/*
 * Updated: 2025-02-05 05:30:00
 * Author: William8677
 *
 * Este archivo define un menú completo para los chats,
 * incluyendo opciones de privacidad, llamadas, archivos, notificaciones,
 * configuración de fondo, mensajes temporales, reportar, bloquear, etc.
 */
package com.williamfq.xhat.ui.screens.chat.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ChatMenuOption(val title: String, val icon: ImageVector) {
    object ViewContact : ChatMenuOption("Ver contacto", Icons.Default.Person)
    object Search : ChatMenuOption("Buscar", Icons.Default.Search)
    object AddToList : ChatMenuOption("Añadir a lista", Icons.Default.AddCircle)
    object Files : ChatMenuOption("Archivos", Icons.Default.Folder)
    object Links : ChatMenuOption("Enlaces", Icons.Default.Link) // Usa Icons.Default.Link o uno similar
    object MuteNotifications : ChatMenuOption("Silenciar", Icons.Default.NotificationsOff)
    // Para silenciar por cierto tiempo, se puede usar una opción con parámetro (o submenu)
    class MuteFor(val duration: String) : ChatMenuOption("Silenciar por $duration", Icons.Default.NotificationsOff)
    object Call : ChatMenuOption("Llamada", Icons.Default.Call)
    object VideoCall : ChatMenuOption("Videollamada", Icons.Default.VideoCall)
    object ChangeWallpaper : ChatMenuOption("Cambiar fondo", Icons.Default.Wallpaper)
    object ClearChat : ChatMenuOption("Vaciar chat", Icons.Default.Delete)
    object DeleteChat : ChatMenuOption("Eliminar chat", Icons.Default.DeleteForever)
    object TemporaryMessages : ChatMenuOption("Mensajes temporales", Icons.Default.Timer)
    object Report : ChatMenuOption("Reportar", Icons.Default.Report)
    object Block : ChatMenuOption("Bloquear", Icons.Default.Block)
    object ExportChat : ChatMenuOption("Exportar chat", Icons.Default.Download)
    object CreateShortcut : ChatMenuOption("Crear acceso directo", Icons.Default.AddLink)
    object ToggleWalkieTalkie : ChatMenuOption("Walkie-Talkie", Icons.Default.Mic)
    object ToggleChatGPT : ChatMenuOption("ChatGPT", Icons.AutoMirrored.Filled.Chat)
    object ToggleVoiceRecorder : ChatMenuOption("Grabador de voz", Icons.Default.Mic)
    object ToggleCamera : ChatMenuOption("Cámara", Icons.Default.Videocam)
    object ToggleMicrophone : ChatMenuOption("Micrófono", Icons.Default.Mic)
    object ToggleSpeaker : ChatMenuOption("Speaker", Icons.AutoMirrored.Filled.VolumeUp)
    object ToggleBluetooth : ChatMenuOption("Bluetooth", Icons.Default.Bluetooth)

    companion object {
        fun getAllOptions(): List<ChatMenuOption> = listOf(
            ViewContact,
            Search,
            AddToList,
            Files,
            Links,
            MuteNotifications,
            // Ejemplo de opciones de silenciar por tiempo:
            MuteFor("1 hora"),
            MuteFor("8 horas"),
            MuteFor("1 semana"),
            Call,
            VideoCall,
            ChangeWallpaper,
            ClearChat,
            DeleteChat,
            TemporaryMessages,
            Report,
            Block,
            ExportChat,
            CreateShortcut,
            ToggleWalkieTalkie,
            ToggleVoiceRecorder,
            ToggleCamera,
            ToggleMicrophone,
            ToggleSpeaker,
            ToggleBluetooth

        )


    }
}
