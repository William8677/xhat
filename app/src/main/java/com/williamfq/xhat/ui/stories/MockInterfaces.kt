package com.williamfq.xhat.ui.stories

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.google.android.gms.ads.nativead.NativeAd
import com.williamfq.domain.model.Message
import com.williamfq.domain.model.MessageType
import com.williamfq.xhat.utils.share.SharedContent
import kotlinx.coroutines.flow.StateFlow

interface MessageRepositoryInterface {
    suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        chatId: String,
        content: String,
        senderName: String,
        type: MessageType
    ): Message

    suspend fun getMessagesBetweenUsers(
        userId1: String,
        userId2: String,
        limit: Int,
        offset: Int
    ): List<Message>

    suspend fun markMessageAsRead(messageId: String)
    suspend fun deleteMessage(messageId: String)
}

interface ShareManagerInterface {
    suspend fun importContent(url: String): SharedContent
}

interface MediaDownloadManagerInterface {
    suspend fun preloadImage(url: String): Uri
    suspend fun preloadVideo(url: String, maxSizeMB: Int): Uri
    suspend fun preloadAudio(url: String): Uri
    suspend fun preloadDocument(url: String): Uri
    suspend fun preloadAR(url: String): Uri
    suspend fun preloadLocation(url: String): Uri
    suspend fun preloadContact(url: String): Uri
    suspend fun preloadPoll(url: String): Uri
    suspend fun preloadQuiz(url: String): Uri
    suspend fun setCacheSize(size: Long)
    suspend fun setCompressionQuality(quality: Int)
    fun clearCache()
}

interface MediaCompressorInterface {
    suspend fun compressImage(
        uri: Uri,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int,
        format: Bitmap.CompressFormat,
        applyBlur: Boolean,
        blurRadius: Float,
        addWatermark: Boolean,
        watermarkText: String?
    ): Uri

    suspend fun compressVideo(
        uri: Uri,
        targetBitrate: Int,
        targetFrameRate: Int,
        targetResolution: Pair<Int, Int>?,
        includeAudio: Boolean,
        enableHEVC: Boolean,
        addWatermark: Boolean
    ): Uri

    suspend fun compressAudio(uri: Uri): Uri
    suspend fun compressDocument(uri: Uri): Uri
    fun clearCache()
}

interface E2EEncryptionInterface {
    suspend fun initialize(userId: String)
    suspend fun encrypt(text: String): String
    suspend fun decrypt(encryptedText: String): String
}

interface NativeStoryAdManagerInterface {
    val currentNativeAd: StateFlow<NativeAd?>
    val isAdLoading: StateFlow<Boolean>
    val adError: StateFlow<String?>
    fun shouldShowAd(storyIndex: Int): Boolean
    fun markAdAsShown()
    fun resetSession()
    fun release()
}

interface AnalyticsManagerInterface {
    fun logEvent(eventName: String, params: Bundle)
}