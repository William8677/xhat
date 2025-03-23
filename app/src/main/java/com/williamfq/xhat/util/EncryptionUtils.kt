// EncryptionUtils.kt: Utilidad para gestionar el cifrado de extremo a extremo
package com.williamfq.xhat.util

import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.Base64

object EncryptionUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val TAG_LENGTH_BIT = 128
    private const val IV_SIZE = 12 // GCM suele utilizar un IV de 12 bytes

    // Genera una clave secreta AES de 256 bits
    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(KEY_SIZE)
        return keyGen.generateKey()
    }

    // Genera un IV aleatorio de tamaño adecuado para GCM
    fun generateIv(): ByteArray {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        return iv
    }

    // Función para encriptar datos utilizando AES/GCM/NoPadding
    fun encrypt(data: String, secretKey: SecretKey, iv: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedData)
    }

    // Función para desencriptar datos utilizando AES/GCM/NoPadding
    fun decrypt(encryptedData: String, secretKey: SecretKey, iv: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val decodedData = Base64.getDecoder().decode(encryptedData)
        val decryptedBytes = cipher.doFinal(decodedData)
        return String(decryptedBytes)
    }
}
