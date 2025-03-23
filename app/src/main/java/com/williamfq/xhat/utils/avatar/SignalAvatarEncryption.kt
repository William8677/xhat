// app/src/main/java/com/williamfq/xhat/utils/avatar/SignalAvatarEncryption.kt

package com.williamfq.xhat.utils.avatar

import org.signal.libsignal.protocol.kdf.HKDF
import org.signal.libsignal.protocol.util.ByteUtil
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class SignalAvatarEncryption {
    companion object {
        private const val GCM_NONCE_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }

    private val secureRandom = SecureRandom()

    fun encryptAvatar(avatarData: ByteArray, userId: String): EncryptedAvatar {
        // Derivar clave usando HKDF de Signal
        val salt = ByteArray(32).apply { secureRandom.nextBytes(this) }
        val key = HKDF.deriveSecrets(
            userId.toByteArray(),
            salt,
            "SignalAvatarEncryption".toByteArray(),
            32
        )

        // Generar nonce
        val nonce = ByteArray(GCM_NONCE_LENGTH).apply { secureRandom.nextBytes(this) }

        // Configurar cifrado AES-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        // Encriptar datos
        val encryptedData = cipher.doFinal(avatarData)

        return EncryptedAvatar(
            encryptedData = encryptedData,
            salt = salt,
            nonce = nonce
        )
    }

    fun decryptAvatar(encryptedAvatar: EncryptedAvatar, userId: String): ByteArray {
        // Derivar clave usando HKDF
        val key = HKDF.deriveSecrets(
            userId.toByteArray(),
            encryptedAvatar.salt,
            "SignalAvatarEncryption".toByteArray(),
            32
        )

        // Configurar descifrado AES-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptedAvatar.nonce)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        // Descifrar datos
        return cipher.doFinal(encryptedAvatar.encryptedData)
    }

    data class EncryptedAvatar(
        val encryptedData: ByteArray,
        val salt: ByteArray,
        val nonce: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EncryptedAvatar

            if (!encryptedData.contentEquals(other.encryptedData)) return false
            if (!salt.contentEquals(other.salt)) return false
            if (!nonce.contentEquals(other.nonce)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = encryptedData.contentHashCode()
            result = 31 * result + salt.contentHashCode()
            result = 31 * result + nonce.contentHashCode()
            return result
        }
    }
}