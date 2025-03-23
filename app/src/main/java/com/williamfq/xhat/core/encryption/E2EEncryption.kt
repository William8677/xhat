package com.williamfq.xhat.core.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.williamfq.xhat.ui.stories.E2EEncryptionInterface
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import kotlinx.coroutines.*
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class E2EEncryption @Inject constructor(
    private val logger: LoggerInterface
) : E2EEncryptionInterface {
    private var keyPair: KeyPair? = null
    private val keyStore = KeyStore.getInstance("AndroidKeyStore")
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val encryptionQueue = mutableListOf<suspend () -> Unit>()

    companion object {
        private const val TAG = "E2EEncryption"
        private const val KEY_SIZE = 2048
        private const val AES_KEY_SIZE = 256
        private const val GCM_NONCE_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val KEY_ALIAS_PREFIX = "xhat_key_"
        private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    }

    override suspend fun initialize(userId: String) {
        withContext(scope.coroutineContext) {
            try {
                keyStore.load(null)
                val keyAlias = "${KEY_ALIAS_PREFIX}${userId}"

                if (!keyStore.containsAlias(keyAlias)) {
                    generateKeyPair(keyAlias)
                }

                val privateKey = keyStore.getKey(keyAlias, null) as PrivateKey
                val publicKey = keyStore.getCertificate(keyAlias).publicKey
                keyPair = KeyPair(publicKey, privateKey)

                logger.logEvent(TAG, "E2E encryption initialized for user: $userId", LogLevel.INFO)
            } catch (e: Exception) {
                logger.logEvent(TAG, "Failed to initialize E2E encryption", LogLevel.ERROR, e)
                throw e
            }
        }
    }

    private suspend fun generateKeyPair(alias: String) = withContext(Dispatchers.Default) {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(KEY_SIZE)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
            .setCertificateNotBefore(Date())
            .setCertificateNotAfter(Date(System.currentTimeMillis() + 31536000000)) // 1 año
            .build()

        keyPairGenerator.initialize(spec)
        keyPair = keyPairGenerator.generateKeyPair()
    }

    override suspend fun encrypt(text: String): String = withContext(Dispatchers.Default) {
        try {
            // Generar clave AES aleatoria
            val aesKey = generateAESKey()
            val nonce = generateNonce()

            // Cifrar datos con AES-GCM
            val encryptedData = encryptWithAES(text.toByteArray(), aesKey, nonce)

            // Cifrar la clave AES con RSA
            val encryptedKey = encryptWithRSA(aesKey.encoded)

            // Combinar todo en un formato seguro
            val combined = CipherData(
                encryptedKey = encryptedKey,
                encryptedData = encryptedData,
                nonce = nonce
            )

            combined.serialize()
        } catch (e: Exception) {
            logger.logEvent(TAG, "Encryption failed", LogLevel.ERROR, e)
            throw e
        }
    }

    override suspend fun decrypt(encryptedText: String): String = withContext(Dispatchers.Default) {
        try {
            // Deserializar datos
            val cipherData = CipherData.deserialize(encryptedText)

            // Descifrar la clave AES
            val aesKey = decryptWithRSA(cipherData.encryptedKey)
            val secretKey = SecretKeySpec(aesKey, "AES")

            // Descifrar datos
            val decryptedBytes = decryptWithAES(
                cipherData.encryptedData,
                secretKey,
                cipherData.nonce
            )

            String(decryptedBytes)
        } catch (e: Exception) {
            logger.logEvent(TAG, "Decryption failed", LogLevel.ERROR, e)
            throw e
        }
    }

    suspend fun signMessage(message: String): String = withContext(Dispatchers.Default) {
        try {
            val signature = Signature.getInstance("SHA512withRSA")
            signature.initSign(keyPair?.private)
            signature.update(message.toByteArray())
            Base64.getEncoder().encodeToString(signature.sign())
        } catch (e: Exception) {
            logger.logEvent(TAG, "Message signing failed", LogLevel.ERROR, e)
            throw e
        }
    }

    suspend fun verifySignature(message: String, signature: String, publicKey: PublicKey): Boolean =
        withContext(Dispatchers.Default) {
            try {
                val sig = Signature.getInstance("SHA512withRSA")
                sig.initVerify(publicKey)
                sig.update(message.toByteArray())
                sig.verify(Base64.getDecoder().decode(signature))
            } catch (e: Exception) {
                logger.logEvent(TAG, "Signature verification failed", LogLevel.ERROR, e)
                false
            }
        }

    private fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
        return keyGen.generateKey()
    }

    private fun generateNonce(): ByteArray {
        val nonce = ByteArray(GCM_NONCE_LENGTH)
        SecureRandom().nextBytes(nonce)
        return nonce
    }

    private fun encryptWithAES(data: ByteArray, key: SecretKey, nonce: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        return cipher.doFinal(data)
    }

    private fun decryptWithAES(data: ByteArray, key: SecretKey, nonce: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, nonce)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(data)
    }

    private fun encryptWithRSA(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keyPair?.public)
        return cipher.doFinal(data)
    }

    private fun decryptWithRSA(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keyPair?.private)
        return cipher.doFinal(data)
    }

    fun getPublicKey(): PublicKey? = keyPair?.public

    private data class CipherData(
        val encryptedKey: ByteArray,
        val encryptedData: ByteArray,
        val nonce: ByteArray
    ) {
        fun serialize(): String {
            val combined = encryptedKey + nonce + encryptedData
            return Base64.getEncoder().encodeToString(combined)
        }

        companion object {
            fun deserialize(data: String): CipherData {
                val combined = Base64.getDecoder().decode(data)
                return CipherData(
                    encryptedKey = combined.slice(0 until KEY_SIZE / 8).toByteArray(),
                    nonce = combined.slice(KEY_SIZE / 8 until KEY_SIZE / 8 + GCM_NONCE_LENGTH).toByteArray(),
                    encryptedData = combined.slice(KEY_SIZE / 8 + GCM_NONCE_LENGTH until combined.size).toByteArray()
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}