// DeepLinkManager.kt: Clase para gestionar los enlaces directos a funciones
package com.williamfq.xhat.manager

import android.content.Intent
import android.net.Uri
import android.content.Context

class DeepLinkManager(private val context: Context) {

    fun getDeepLinkToChat(chatId: String): Intent {
        val deepLinkUri = Uri.parse("xhat://chat/$chatId")
        return Intent(Intent.ACTION_VIEW, deepLinkUri)
    }

    fun getDeepLinkToStory(storyId: String): Intent {
        val deepLinkUri = Uri.parse("xhat://story/$storyId")
        return Intent(Intent.ACTION_VIEW, deepLinkUri)
    }

    fun getDeepLinkToVideoCall(videoCallId: String): Intent {
        val deepLinkUri = Uri.parse("xhat://videocall/$videoCallId")
        return Intent(Intent.ACTION_VIEW, deepLinkUri)
    }
}
