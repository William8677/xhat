package com.williamfq.xhat.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.williamfq.xhat.ui.screens.settings.components.SettingsGroupSection
import com.williamfq.xhat.ui.screens.settings.components.SettingsTopBar
import com.williamfq.xhat.ui.screens.settings.model.SettingItem
import com.williamfq.xhat.ui.screens.settings.model.SettingsGroup
import com.williamfq.xhat.ui.screens.settings.model.SubSettingItem

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsGroups = remember { createCompleteSettingsGroups() }
    var expandedGroup by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SettingsTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtrar grupos y items basados en la búsqueda
            val filteredGroups = if (searchQuery.isNotEmpty()) {
                settingsGroups.map { group ->
                    group.copy(
                        items = group.items.filter { item ->
                            item.title.contains(searchQuery, ignoreCase = true) ||
                                    item.subItems.any { it.title.contains(searchQuery, ignoreCase = true) }
                        }
                    )
                }.filter { it.items.isNotEmpty() }
            } else {
                settingsGroups
            }

            items(filteredGroups) { group ->
                SettingsGroupSection(
                    group = group,
                    isExpanded = expandedGroup == group.title,
                    onExpandClick = {
                        expandedGroup = if (expandedGroup == group.title) null else group.title
                    },
                    onItemClick = { route ->
                        navController.navigate(route)
                    }
                )
            }
        }
    }
}

private fun createCompleteSettingsGroups() = listOf(
    SettingsGroup(
        title = "Cuenta y Perfil",
        items = listOf(
            SettingItem(
                title = "Perfil",
                icon = Icons.Default.Person,
                route = "settings/profile",
                subItems = listOf(
                    SubSettingItem("Foto de perfil", "settings/profile/photo"),
                    SubSettingItem("Información personal", "settings/profile/info"),
                    SubSettingItem("Estado y humor", "settings/profile/status"),
                    SubSettingItem("Enlaces y redes sociales", "settings/profile/links"),
                    SubSettingItem("Insignias y logros", "settings/profile/badges"),
                    SubSettingItem("Configuración profesional", "settings/profile/professional")
                )
            ),
            SettingItem(
                title = "Cuenta",
                icon = Icons.Default.ManageAccounts,
                route = "settings/account",
                subItems = listOf(
                    SubSettingItem("Información de la cuenta", "settings/account/info"),
                    SubSettingItem("Verificación de cuenta", "settings/account/verification"),
                    SubSettingItem("Cambiar número", "settings/account/phone"),
                    SubSettingItem("Cambiar correo", "settings/account/email"),
                    SubSettingItem("Contraseña y seguridad", "settings/account/security"),
                    SubSettingItem("Autenticación de dos factores", "settings/account/2fa"),
                    SubSettingItem("Recuperación de cuenta", "settings/account/recovery"),
                    SubSettingItem("Cuentas vinculadas", "settings/account/linked"),
                    SubSettingItem("Desactivar cuenta", "settings/account/deactivate"),
                    SubSettingItem("Eliminar cuenta", "settings/account/delete")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Privacidad y Seguridad",
        items = listOf(
            SettingItem(
                title = "Privacidad",
                icon = Icons.Default.Lock,
                route = "settings/privacy",
                subItems = listOf(
                    SubSettingItem("Visibilidad del perfil", "settings/privacy/profile"),
                    SubSettingItem("Estados y historias", "settings/privacy/stories"),
                    SubSettingItem("Última conexión", "settings/privacy/last-seen"),
                    SubSettingItem("Foto de perfil", "settings/privacy/photo"),
                    SubSettingItem("Información personal", "settings/privacy/info"),
                    SubSettingItem("Grupos y canales", "settings/privacy/groups"),
                    SubSettingItem("Llamadas", "settings/privacy/calls"),
                    SubSettingItem("Ubicación en tiempo real", "settings/privacy/location"),
                    SubSettingItem("Bloqueo de contactos", "settings/privacy/blocks"),
                    SubSettingItem("Mensajes temporales", "settings/privacy/disappearing"),
                    SubSettingItem("Cifrado de extremo a extremo", "settings/privacy/encryption"),
                    SubSettingItem("Verificación en dos pasos", "settings/privacy/2step"),
                    SubSettingItem("Contactos bloqueados", "settings/privacy/blocked"),
                    SubSettingItem("Privacidad de contenido", "settings/privacy/content")
                )
            ),
            SettingItem(
                title = "Seguridad",
                icon = Icons.Default.Security,
                route = "settings/security",
                subItems = listOf(
                    SubSettingItem("Dispositivos conectados", "settings/security/devices"),
                    SubSettingItem("Verificación de sesiones", "settings/security/sessions"),
                    SubSettingItem("Historial de acceso", "settings/security/access-history"),
                    SubSettingItem("Contraseñas guardadas", "settings/security/passwords"),
                    SubSettingItem("Bloqueo con huella/facial", "settings/security/biometric"),
                    SubSettingItem("PIN de seguridad", "settings/security/pin"),
                    SubSettingItem("Alertas de seguridad", "settings/security/alerts"),
                    SubSettingItem("Códigos de recuperación", "settings/security/recovery-codes")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Anuncios y Monetización",
        items = listOf(
            SettingItem(
                title = "Centro de Anuncios",
                icon = Icons.Default.Campaign,
                route = "settings/ads",
                subItems = listOf(
                    SubSettingItem("Crear nueva campaña", "settings/ads/create"),
                    SubSettingItem("Gestor de campañas", "settings/ads/manager"),
                    SubSettingItem("Segmentación avanzada", "settings/ads/targeting"),
                    SubSettingItem("Presupuesto y programación", "settings/ads/budget"),
                    SubSettingItem("Formatos de anuncios", "settings/ads/formats"),
                    SubSettingItem("Análisis y métricas", "settings/ads/analytics"),
                    SubSettingItem("Optimización de campañas", "settings/ads/optimization"),
                    SubSettingItem("A/B Testing", "settings/ads/testing"),
                    SubSettingItem("Audiencias personalizadas", "settings/ads/audiences"),
                    SubSettingItem("Píxel de seguimiento", "settings/ads/pixel"),
                    SubSettingItem("Facturación y pagos", "settings/ads/billing"),
                    SubSettingItem("Políticas publicitarias", "settings/ads/policies"),
                    SubSettingItem("Centro de ayuda", "settings/ads/help")
                )
            ),
            SettingItem(
                title = "Monetización",
                icon = Icons.Default.MonetizationOn,
                route = "settings/monetization",
                subItems = listOf(
                    SubSettingItem("Programa de creadores", "settings/monetization/creator"),
                    SubSettingItem("Suscripciones", "settings/monetization/subscriptions"),
                    SubSettingItem("Tienda", "settings/monetization/shop"),
                    SubSettingItem("Donaciones", "settings/monetization/donations"),
                    SubSettingItem("Estadísticas de ingresos", "settings/monetization/stats"),
                    SubSettingItem("Métodos de pago", "settings/monetization/payments"),
                    SubSettingItem("Impuestos", "settings/monetization/taxes")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Contenido y Personalización",
        items = listOf(
            SettingItem(
                title = "Historias y Estados",
                icon = Icons.Default.AutoStories,
                route = "settings/stories",
                subItems = listOf(
                    SubSettingItem("Configuración de historias", "settings/stories/settings"),
                    SubSettingItem("Archivos", "settings/stories/archive"),
                    SubSettingItem("Destacados", "settings/stories/highlights"),
                    SubSettingItem("Respuestas automáticas", "settings/stories/auto-replies"),
                    SubSettingItem("Filtros y efectos", "settings/stories/filters"),
                    SubSettingItem("Música y sonidos", "settings/stories/music"),
                    SubSettingItem("Calidad de medios", "settings/stories/quality")
                )
            ),
            SettingItem(
                title = "Chats",
                icon = Icons.AutoMirrored.Filled.Chat,
                route = "settings/chats",
                subItems = listOf(
                    SubSettingItem("Apariencia", "settings/chats/appearance"),
                    SubSettingItem("Fondos de chat", "settings/chats/wallpapers"),
                    SubSettingItem("Temas", "settings/chats/themes"),
                    SubSettingItem("Emojis y stickers", "settings/chats/emojis"),
                    SubSettingItem("Archivos compartidos", "settings/chats/shared"),
                    SubSettingItem("Backup de chats", "settings/chats/backup")
                )
            )
        )
    ),
    /*
 * Updated: 2025-02-09 16:30:15
 * Author: William8677
 */

    SettingsGroup(
        title = "Notificaciones y Comunicaciones",
        items = listOf(
            SettingItem(
                title = "Notificaciones",
                icon = Icons.Default.Notifications,
                route = "settings/notifications",
                subItems = listOf(
                    SubSettingItem("Mensajes", "settings/notifications/messages"),
                    SubSettingItem("Grupos", "settings/notifications/groups"),
                    SubSettingItem("Llamadas", "settings/notifications/calls"),
                    SubSettingItem("Historias", "settings/notifications/stories"),
                    SubSettingItem("Menciones", "settings/notifications/mentions"),
                    SubSettingItem("Comentarios", "settings/notifications/comments"),
                    SubSettingItem("Nuevos seguidores", "settings/notifications/followers"),
                    SubSettingItem("Anuncios", "settings/notifications/ads"),
                    SubSettingItem("Eventos", "settings/notifications/events"),
                    SubSettingItem("Actualizaciones", "settings/notifications/updates"),
                    SubSettingItem("Marketing", "settings/notifications/marketing"),
                    SubSettingItem("Sonidos", "settings/notifications/sounds"),
                    SubSettingItem("Vibración", "settings/notifications/vibration"),
                    SubSettingItem("LED", "settings/notifications/led"),
                    SubSettingItem("Vista previa", "settings/notifications/preview"),
                    SubSettingItem("No molestar", "settings/notifications/dnd"),
                    SubSettingItem("Prioridades", "settings/notifications/priority")
                )
            ),
            SettingItem(
                title = "Llamadas y Videollamadas",
                icon = Icons.Default.Call,
                route = "settings/calls",
                subItems = listOf(
                    SubSettingItem("Preferencias de llamadas", "settings/calls/preferences"),
                    SubSettingItem("Calidad de video", "settings/calls/video-quality"),
                    SubSettingItem("Fondo virtual", "settings/calls/background"),
                    SubSettingItem("Efectos de video", "settings/calls/effects"),
                    SubSettingItem("Reducción de ruido", "settings/calls/noise-reduction"),
                    SubSettingItem("Dispositivos de audio", "settings/calls/audio-devices"),
                    SubSettingItem("Contestador automático", "settings/calls/voicemail"),
                    SubSettingItem("Llamadas en espera", "settings/calls/waiting"),
                    SubSettingItem("Reenvío de llamadas", "settings/calls/forwarding"),
                    SubSettingItem("Bloqueo de llamadas", "settings/calls/blocking")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Datos y Almacenamiento",
        items = listOf(
            SettingItem(
                title = "Uso de datos",
                icon = Icons.Default.DataUsage,
                route = "settings/data",
                subItems = listOf(
                    SubSettingItem("Uso de datos móviles", "settings/data/mobile"),
                    SubSettingItem("Uso de WiFi", "settings/data/wifi"),
                    SubSettingItem("Descarga automática", "settings/data/auto-download"),
                    SubSettingItem("Calidad de medios", "settings/data/media-quality"),
                    SubSettingItem("Modo ahorro de datos", "settings/data/saving-mode"),
                    SubSettingItem("Límites de datos", "settings/data/limits"),
                    SubSettingItem("Estadísticas", "settings/data/statistics")
                )
            ),
            SettingItem(
                title = "Almacenamiento",
                icon = Icons.Default.Storage,
                route = "settings/storage",
                subItems = listOf(
                    SubSettingItem("Gestionar almacenamiento", "settings/storage/manage"),
                    SubSettingItem("Caché", "settings/storage/cache"),
                    SubSettingItem("Archivos temporales", "settings/storage/temp"),
                    SubSettingItem("Archivos compartidos", "settings/storage/shared"),
                    SubSettingItem("Medios", "settings/storage/media"),
                    SubSettingItem("Documentos", "settings/storage/documents"),
                    SubSettingItem("Limpieza automática", "settings/storage/auto-clean"),
                    SubSettingItem("Almacenamiento en la nube", "settings/storage/cloud"),
                    SubSettingItem("Copias de seguridad", "settings/storage/backup")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Dispositivos y Sincronización",
        items = listOf(
            SettingItem(
                title = "Dispositivos vinculados",
                icon = Icons.Default.Devices,
                route = "settings/devices",
                subItems = listOf(
                    SubSettingItem("Gestionar dispositivos", "settings/devices/manage"),
                    SubSettingItem("Versión web", "settings/devices/web"),
                    SubSettingItem("Multidispositivo", "settings/devices/multi"),
                    SubSettingItem("Sincronización", "settings/devices/sync"),
                    SubSettingItem("Seguridad de dispositivos", "settings/devices/security"),
                    SubSettingItem("Cerrar sesión remota", "settings/devices/logout"),
                    SubSettingItem("Historial de conexiones", "settings/devices/history")
                )
            ),
            SettingItem(
                title = "Respaldo y Restauración",
                icon = Icons.Default.Backup,
                route = "settings/backup",
                subItems = listOf(
                    SubSettingItem("Respaldo automático", "settings/backup/auto"),
                    SubSettingItem("Respaldo local", "settings/backup/local"),
                    SubSettingItem("Respaldo en la nube", "settings/backup/cloud"),
                    SubSettingItem("Historial de respaldos", "settings/backup/history"),
                    SubSettingItem("Restaurar respaldo", "settings/backup/restore"),
                    SubSettingItem("Exportar datos", "settings/backup/export"),
                    SubSettingItem("Importar datos", "settings/backup/import")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Idioma y Región",
        items = listOf(
            SettingItem(
                title = "Idioma",
                icon = Icons.Default.Language,
                route = "settings/language",
                subItems = listOf(
                    SubSettingItem("Idioma de la app", "settings/language/app"),
                    SubSettingItem("Teclado", "settings/language/keyboard"),
                    SubSettingItem("Traducción automática", "settings/language/translation"),
                    SubSettingItem("Diccionario personal", "settings/language/dictionary"),
                    SubSettingItem("Corrector ortográfico", "settings/language/spell-check")
                )
            ),
            SettingItem(
                title = "Región",
                icon = Icons.Default.Public,
                route = "settings/region",
                subItems = listOf(
                    SubSettingItem("País/Región", "settings/region/country"),
                    SubSettingItem("Formato de fecha", "settings/region/date"),
                    SubSettingItem("Formato de hora", "settings/region/time"),
                    SubSettingItem("Moneda", "settings/region/currency"),
                    SubSettingItem("Zona horaria", "settings/region/timezone")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Accesibilidad y Asistencia",
        items = listOf(
            SettingItem(
                title = "Accesibilidad",
                icon = Icons.Default.Accessibility,
                route = "settings/accessibility",
                subItems = listOf(
                    SubSettingItem("Tamaño de texto", "settings/accessibility/text-size"),
                    SubSettingItem("Contraste", "settings/accessibility/contrast"),
                    SubSettingItem("Lector de pantalla", "settings/accessibility/screen-reader"),
                    SubSettingItem("Subtítulos", "settings/accessibility/captions"),
                    SubSettingItem("Navegación por teclado", "settings/accessibility/keyboard"),
                    SubSettingItem("Gestos", "settings/accessibility/gestures"),
                    SubSettingItem("Audio monoaural", "settings/accessibility/mono-audio"),
                    SubSettingItem("Reducción de movimiento", "settings/accessibility/reduce-motion")
                )
            ),
            SettingItem(
                title = "Asistente Virtual",
                icon = Icons.Default.Assistant,
                route = "settings/assistant",
                subItems = listOf(
                    SubSettingItem("Configuración del asistente", "settings/assistant/settings"),
                    SubSettingItem("Comandos de voz", "settings/assistant/voice"),
                    SubSettingItem("Acciones rápidas", "settings/assistant/quick-actions"),
                    SubSettingItem("Personalización", "settings/assistant/personalization")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Rendimiento y Optimización",
        items = listOf(
            SettingItem(
                title = "Rendimiento",
                icon = Icons.Default.Speed,
                route = "settings/performance",
                subItems = listOf(
                    SubSettingItem("Modo de ahorro de batería", "settings/performance/battery"),
                    SubSettingItem("Optimización de memoria", "settings/performance/memory"),
                    SubSettingItem("Caché", "settings/performance/cache"),
                    SubSettingItem("Animaciones", "settings/performance/animations"),
                    SubSettingItem("Modo lite", "settings/performance/lite-mode")
                )
            ),
            SettingItem(
                title = "Diagnósticos",
                icon = Icons.Default.BugReport,
                route = "settings/diagnostics",
                subItems = listOf(
                    SubSettingItem("Informes de error", "settings/diagnostics/reports"),
                    SubSettingItem("Logs del sistema", "settings/diagnostics/logs"),
                    SubSettingItem("Estado del sistema", "settings/diagnostics/status"),
                    SubSettingItem("Pruebas de red", "settings/diagnostics/network")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Ayuda y Soporte Legal",
        items = listOf(
            SettingItem(
                title = "Centro de Ayuda",
                icon = Icons.AutoMirrored.Filled.Help,
                route = "settings/help",
                subItems = listOf(
                    SubSettingItem("Guías y tutoriales", "settings/help/guides"),
                    SubSettingItem("Preguntas frecuentes", "settings/help/faq"),
                    SubSettingItem("Contactar soporte", "settings/help/contact"),
                    SubSettingItem("Reportar problema", "settings/help/report"),
                    SubSettingItem("Sugerencias", "settings/help/suggestions")
                )
            ),
            SettingItem(
                title = "Legal",
                icon = Icons.Default.Policy,
                route = "settings/legal",
                subItems = listOf(
                    SubSettingItem("Términos de servicio", "settings/legal/terms"),
                    SubSettingItem("Política de privacidad", "settings/legal/privacy"),
                    SubSettingItem("Licencias", "settings/legal/licenses"),
                    SubSettingItem("Información de copyright", "settings/legal/copyright"),
                    SubSettingItem("GDPR", "settings/legal/gdpr")
                )
            )
        )
    ),
    SettingsGroup(
        title = "Desarrolladores",
        items = listOf(
            SettingItem(
                title = "Herramientas de desarrollo",
                icon = Icons.Default.Code,
                route = "settings/developer",
                subItems = listOf(
                    SubSettingItem("API", "settings/developer/api"),
                    SubSettingItem("Webhooks", "settings/developer/webhooks"),
                    SubSettingItem("SDK", "settings/developer/sdk"),
                    SubSettingItem("Documentación", "settings/developer/docs"),
                    SubSettingItem("Consola", "settings/developer/console")
                )
            )
        )
    )
)