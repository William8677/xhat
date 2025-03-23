/*
 * Updated: 2025-01-22 01:22:34
 * Author: William8677
 */

package com.williamfq.xhat.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {
    suspend fun uploadImage(path: String, uri: Uri): String = withContext(Dispatchers.IO) {
        val compressedUri = compressImage(uri)
        val ref = storage.reference.child("$path/${UUID.randomUUID()}.jpg")
        ref.putFile(compressedUri).await()
        ref.downloadUrl.await().toString()
    }

    private suspend fun compressImage(uri: Uri): Uri = withContext(Dispatchers.IO) {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val maxSize = 1024
        val ratio = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height
        )

        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()

        val compressedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")

        FileOutputStream(file).use { out ->
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        Uri.fromFile(file)
    }
}