package com.alhaq.amnshield.guardian.auth.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.alhaq.amnshield.guardian.auth.model.CapabilityToken
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages secure storage and verification of capability tokens.
 * All operations are local and encrypted.
 */
class TokenStorage private constructor(private val context: Context) {

    companion object {
        private const val TAG = "TokenStorage"

        private const val PREFS_NAME = "deenshield_tokens"
        private const val KEY_TOKENS_MAP = "tokens_map"
        private const val KEY_REVOKED_TOKENS = "revoked_tokens"
        private const val KEY_CLEANUP_TIMESTAMP = "cleanup_timestamp"

        private const val KEY_ALIAS = "deenshield_token_key"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"

        private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val GCM_IV_LENGTH_BYTES = 12

        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val CLEANUP_INTERVAL_MS = 24L * 60 * 60 * 1000

        const val DEFAULT_TOKEN_EXPIRATION_MS = 90L * 24 * 60 * 60 * 1000

        @Volatile
        private var instance: TokenStorage? = null

        fun getInstance(context: Context): TokenStorage {
            if (System.getProperty("java.vendor")?.contains("Android") != true) {
                return TokenStorage(context.applicationContext)
            }
            return instance ?: synchronized(this) {
                instance ?: TokenStorage(context.applicationContext).also { instance = it }
            }
        }
    }

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        if (System.getProperty("java.vendor")?.contains("Android") != true) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } else {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    private val keyStore: KeyStore by lazy {
        if (System.getProperty("java.vendor")?.contains("Android") != true) {
            KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null) }
        } else {
            KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        }
    }

    private var testSecretKey: javax.crypto.SecretKey? = null

    private fun getEncryptionKey(): javax.crypto.SecretKey {
        if (System.getProperty("java.vendor")?.contains("Android") != true) {
            return testSecretKey ?: synchronized(this) {
                testSecretKey ?: javax.crypto.KeyGenerator.getInstance("AES").apply {
                    init(256)
                }.generateKey().also { testSecretKey = it }
            }
        }

        ensureEncryptionKeyExists()
        return keyStore.getKey(KEY_ALIAS, null) as? javax.crypto.SecretKey
            ?: throw TokenStorageException("Encryption key not found in Android Keystore")
    }

    private fun ensureEncryptionKeyExists() {
        if (System.getProperty("java.vendor")?.contains("Android") != true) {
            return
        }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            try {
                val keyGen = KeyGenerator.getInstance("AES", KEYSTORE_PROVIDER)
                val keyGenSpec = android.security.keystore.KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(256)
                    .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setIsStrongBoxBacked(true)
                    .build()

                keyGen.init(keyGenSpec)
                keyGen.generateKey()

                Log.i(TAG, "Encryption key created and stored in Android Keystore")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create encryption key", e)
                throw TokenStorageException("Encryption key creation failed: ${e.message}", e)
            }
        }
    }

    fun storeToken(token: CapabilityToken): TokenStorageResult {
        return try {
            val tokenJson = json.encodeToString(token)
            val tokenBytes = tokenJson.toByteArray(Charsets.UTF_8)

            val nonce = ByteArray(GCM_IV_LENGTH_BYTES)
            SecureRandom().nextBytes(nonce)

            val key = getEncryptionKey()

            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            val encryptedData = cipher.doFinal(tokenBytes)

            val hmacKey = javax.crypto.spec.SecretKeySpec(key.encoded, HMAC_ALGORITHM)
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(hmacKey)
            val signature = mac.doFinal(encryptedData)

            val tokenBundle = TokenBundle(
                nonceBase64 = Base64.getEncoder().encodeToString(nonce),
                encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedData),
                signatureBase64 = Base64.getEncoder().encodeToString(signature),
                timestamp = System.currentTimeMillis()
            )

            val tokensJson = encryptedPrefs.getString(KEY_TOKENS_MAP, "{}") ?: "{}"
            val tokensMap = parseTokensMap(tokensJson)
            tokensMap[token.sessionId] = json.encodeToString(tokenBundle)

            encryptedPrefs.edit().apply {
                putString(KEY_TOKENS_MAP, json.encodeToString(tokensMap))
                commit()
            }

            Log.i(TAG, "Token stored: ${token.sessionId}")
            TokenStorageResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error storing token", e)
            TokenStorageResult(success = false, error = "Token storage failed: ${e.message}")
        }
    }

    fun retrieveToken(sessionId: String): TokenRetrievalResult {
        return try {
            val revokedTokens = getRevokedTokenIds()
            if (revokedTokens.contains(sessionId)) {
                Log.w(TAG, "Token is revoked: $sessionId")
                return TokenRetrievalResult(success = false, error = "Token has been revoked")
            }

            val tokensJson = encryptedPrefs.getString(KEY_TOKENS_MAP, "{}") ?: "{}"
            val tokensMap = parseTokensMap(tokensJson)
            val tokenBundleJson = tokensMap[sessionId]
                ?: return TokenRetrievalResult(success = false, error = "Token not found")

            val tokenBundle = json.decodeFromString<TokenBundle>(tokenBundleJson)

            val key = getEncryptionKey()

            val encryptedData = Base64.getDecoder().decode(tokenBundle.encryptedDataBase64)
            val storedSignature = Base64.getDecoder().decode(tokenBundle.signatureBase64)

            val hmacKey = javax.crypto.spec.SecretKeySpec(key.encoded, HMAC_ALGORITHM)
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(hmacKey)
            val computedSignature = mac.doFinal(encryptedData)

            if (!constantTimeEquals(storedSignature, computedSignature)) {
                Log.e(TAG, "Token signature verification failed (tampering detected): $sessionId")
                return TokenRetrievalResult(success = false, error = "Token signature invalid")
            }

            val nonce = Base64.getDecoder().decode(tokenBundle.nonceBase64)
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
            val decryptedBytes = cipher.doFinal(encryptedData)
            val tokenJson = String(decryptedBytes, Charsets.UTF_8)

            val token = json.decodeFromString<CapabilityToken>(tokenJson)
            if (token.isExpired(System.currentTimeMillis())) {
                Log.w(TAG, "Token is expired: $sessionId")
                return TokenRetrievalResult(success = false, error = "Token has expired")
            }

            Log.i(TAG, "Token retrieved successfully: $sessionId")
            TokenRetrievalResult(success = true, token = token)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving token", e)
            TokenRetrievalResult(success = false, error = "Token retrieval failed: ${e.message}")
        }
    }

    fun revokeToken(sessionId: String): TokenStorageResult {
        return try {
            val revokedTokens = getRevokedTokenIds().toMutableSet()
            revokedTokens.add(sessionId)

            encryptedPrefs.edit().apply {
                putStringSet(KEY_REVOKED_TOKENS, revokedTokens)
                commit()
            }

            Log.i(TAG, "Token revoked: $sessionId")
            TokenStorageResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error revoking token", e)
            TokenStorageResult(success = false, error = "Token revocation failed: ${e.message}")
        }
    }

    fun deleteToken(sessionId: String): TokenStorageResult {
        return try {
            val tokensJson = encryptedPrefs.getString(KEY_TOKENS_MAP, "{}") ?: "{}"
            val tokensMap = parseTokensMap(tokensJson).toMutableMap()
            tokensMap.remove(sessionId)

            encryptedPrefs.edit().apply {
                putString(KEY_TOKENS_MAP, json.encodeToString(tokensMap))
                commit()
            }

            Log.i(TAG, "Token deleted: $sessionId")
            TokenStorageResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting token", e)
            TokenStorageResult(success = false, error = "Token deletion failed: ${e.message}")
        }
    }

    fun clearAllTokens() {
        try {
            encryptedPrefs.edit().clear().commit()
            Log.i(TAG, "All tokens cleared from storage")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing tokens", e)
        }
    }

    fun getAllTokenIds(): List<String> {
        return try {
            val tokensJson = encryptedPrefs.getString(KEY_TOKENS_MAP, "{}") ?: "{}"
            parseTokensMap(tokensJson).keys.toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting token IDs", e)
            emptyList()
        }
    }

    fun getRevokedTokenIds(): Set<String> {
        return try {
            encryptedPrefs.getStringSet(KEY_REVOKED_TOKENS, emptySet()) ?: emptySet()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting revoked token IDs", e)
            emptySet()
        }
    }

    fun cleanupExpiredTokens(): TokenStorageResult {
        return try {
            val lastCleanup = encryptedPrefs.getLong(KEY_CLEANUP_TIMESTAMP, 0L)
            val now = System.currentTimeMillis()

            if (now - lastCleanup < CLEANUP_INTERVAL_MS) {
                return TokenStorageResult(success = true)
            }

            val tokensJson = encryptedPrefs.getString(KEY_TOKENS_MAP, "{}") ?: "{}"
            val tokensMap = parseTokensMap(tokensJson).toMutableMap()
            var deletedCount = 0

            for ((sessionId, _) in tokensMap.toList()) {
                val result = retrieveToken(sessionId)
                if (!result.success && result.error?.contains("expired") == true) {
                    tokensMap.remove(sessionId)
                    deletedCount++
                }
            }

            encryptedPrefs.edit().apply {
                putString(KEY_TOKENS_MAP, json.encodeToString(tokensMap))
                putLong(KEY_CLEANUP_TIMESTAMP, now)
                commit()
            }

            Log.i(TAG, "Cleanup completed: deleted $deletedCount expired tokens")
            TokenStorageResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
            TokenStorageResult(success = false, error = "Cleanup failed: ${e.message}")
        }
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    private fun parseTokensMap(jsonString: String): MutableMap<String, String> {
        return try {
            if (jsonString.isBlank() || jsonString == "{}") return mutableMapOf()
            json.decodeFromString<Map<String, String>>(jsonString).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    data class TokenStorageResult(val success: Boolean, val error: String? = null)

    data class TokenRetrievalResult(
        val success: Boolean,
        val token: CapabilityToken? = null,
        val error: String? = null
    )

    @Serializable
    private data class TokenBundle(
        val nonceBase64: String,
        val encryptedDataBase64: String,
        val signatureBase64: String,
        val timestamp: Long
    )

    class TokenStorageException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
