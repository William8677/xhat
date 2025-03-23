// app/src/main/java/com/williamfq/xhat/utils/avatar/SignalAvatarManager.kt

package com.williamfq.xhat.utils.avatar

import com.williamfq.xhat.utils.ByteUtil
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject

class SignalAvatarManager @Inject constructor(
    private val context: Context,
    private val encryption: SignalAvatarEncryption
) {
    private val avatarDirectory = "secure_avatars"

    init {
        createAvatarDirectory()
    }

    private fun createAvatarDirectory() {
        File(context.filesDir, avatarDirectory).mkdirs()
    }

    fun saveAvatar(bitmap: Bitmap, userId: String): String {
        // Convertir bitmap a bytes
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val avatarBytes = byteArrayOutputStream.toByteArray()

        // Encriptar datos del avatar
        val encryptedAvatar = encryption.encryptAvatar(avatarBytes, userId)

        // Generar nombre de archivo único
        val fileName = "${UUID.randomUUID()}.bin"
        val avatarFile = File(File(context.filesDir, avatarDirectory), fileName)

        // Guardar datos encriptados
        avatarFile.writeBytes(ByteUtil.combine(
            encryptedAvatar.salt,
            encryptedAvatar.nonce,
            encryptedAvatar.encryptedData
        ))

        return fileName
    }

    fun loadAvatar(fileName: String, userId: String): Bitmap? {
        return try {
            val avatarFile = File(File(context.filesDir, avatarDirectory), fileName)
            val fileBytes = avatarFile.readBytes()

            // Extraer componentes
            val salt = fileBytes.slice(0..31).toByteArray()
            val nonce = fileBytes.slice(32..43).toByteArray()
            val encryptedData = fileBytes.slice(44..fileBytes.lastIndex).toByteArray()

            // Reconstruir EncryptedAvatar
            val encryptedAvatar = SignalAvatarEncryption.EncryptedAvatar(
                encryptedData = encryptedData,
                salt = salt,
                nonce = nonce
            )

            // Descifrar datos
            val decryptedBytes = encryption.decryptAvatar(encryptedAvatar, userId)

            // Convertir bytes a bitmap
            BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteAvatar(fileName: String) {
        File(File(context.filesDir, avatarDirectory), fileName).delete()
    }
}