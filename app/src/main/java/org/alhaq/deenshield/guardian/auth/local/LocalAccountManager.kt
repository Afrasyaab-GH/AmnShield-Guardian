package org.alhaq.deenshield.guardian.auth.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.deenshield.blocker.auth.data.LocalAccount
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Manages local account creation, storage, and authentication.
 *
 * All operations are 100% local on-device. No network calls.
 */
class LocalAccountManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "LocalAccountManager"

        // Shared preferences file names and keys
        private const val PREFS_NAME = "deenshield_local_accounts"
        private const val KEY_USERNAME = "account_username"
        private const val KEY_PASSWORD_HASH = "account_password_hash"
        private const val KEY_SALT = "account_salt"
        private const val KEY_CREATED_AT = "account_created_at"
        private const val KEY_LAST_MODIFIED = "account_last_modified"

        // PBKDF2 parameters
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 100_000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 32

        // Minimum password requirements
        const val MIN_PASSWORD_LENGTH = 8
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 30

        @Volatile
        private var instance: LocalAccountManager? = null

        fun getInstance(context: Context): LocalAccountManager {
            return instance ?: synchronized(this) {
                instance ?: LocalAccountManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
        * Encrypted SharedPreferences instance.
        */
    private val encryptedPrefs: SharedPreferences by lazy {
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

    /**
     * Create a new local account with username and password.
     */
    fun createAccount(username: String, password: String): AccountResult {
        val usernameValidation = validateUsername(username)
        if (!usernameValidation.isValid) {
            Log.w(TAG, "Invalid username: ${usernameValidation.error}")
            return AccountResult(success = false, error = usernameValidation.error)
        }

        val passwordValidation = validatePassword(password)
        if (!passwordValidation.isValid) {
            Log.w(TAG, "Invalid password: ${passwordValidation.error}")
            return AccountResult(success = false, error = passwordValidation.error)
        }

        if (accountExists()) {
            Log.w(TAG, "Account already exists")
            return AccountResult(success = false, error = "Account already exists. Delete existing account first.")
        }

        return try {
            val salt = generateSalt()
            val saltBase64 = Base64.getEncoder().encodeToString(salt)

            val passwordHash = hashPassword(password, salt)
            val hashBase64 = Base64.getEncoder().encodeToString(passwordHash)

            val now = System.currentTimeMillis()
            encryptedPrefs.edit().apply {
                putString(KEY_USERNAME, username)
                putString(KEY_PASSWORD_HASH, hashBase64)
                putString(KEY_SALT, saltBase64)
                putLong(KEY_CREATED_AT, now)
                putLong(KEY_LAST_MODIFIED, now)
                commit()
            }

            Log.i(TAG, "Account created successfully: $username")
            AccountResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating account", e)
            AccountResult(success = false, error = "Failed to create account: ${e.message}")
        }
    }

    /**
     * Authenticate user with username and password against encrypted storage.
     */
    fun authenticateAccount(username: String, password: String): AccountResult {
        if (!accountExists()) {
            Log.w(TAG, "Account not found: $username")
            return AccountResult(success = false, error = "Account not found")
        }

        return try {
            val storedUsername = encryptedPrefs.getString(KEY_USERNAME, "")
            if (storedUsername != username) {
                Log.w(TAG, "Username mismatch")
                return AccountResult(success = false, error = "Invalid username or password")
            }

            val saltBase64 = encryptedPrefs.getString(KEY_SALT, "")
            if (saltBase64.isNullOrEmpty()) {
                Log.e(TAG, "Salt not found for account")
                return AccountResult(success = false, error = "Account corrupted")
            }

            val storedHash = encryptedPrefs.getString(KEY_PASSWORD_HASH, "")
            if (storedHash.isNullOrEmpty()) {
                Log.e(TAG, "Password hash not found")
                return AccountResult(success = false, error = "Account corrupted")
            }

            val isValid = verifyPassword(password, storedHash, saltBase64)
            if (isValid) {
                Log.i(TAG, "Authentication success for $username")
                AccountResult(success = true)
            } else {
                Log.w(TAG, "Authentication failed for $username")
                AccountResult(success = false, error = "Invalid username or password")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error authenticating", e)
            AccountResult(success = false, error = "Authentication failed: ${e.message}")
        }
    }

    /**
     * Validate password against a LocalAccount (stored hash + salt).
     */
    fun validatePassword(password: String, account: LocalAccount): Boolean {
        return verifyPassword(password, account.passwordHash, account.passwordSalt)
    }

    /**
     * Validate password using stored hash + salt in Base64.
     */
    fun verifyPassword(password: String, passwordHashBase64: String, saltBase64: String): Boolean {
        return try {
            val salt = Base64.getDecoder().decode(saltBase64)
            val computedHash = hashPassword(password, salt)
            val computedHashBase64 = Base64.getEncoder().encodeToString(computedHash)
            constantTimeEquals(computedHashBase64, passwordHashBase64)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying password", e)
            false
        }
    }

    /**
     * Get the stored password hash (Base64). Use carefully.
     */
    fun getStoredPasswordHash(): String? = encryptedPrefs.getString(KEY_PASSWORD_HASH, null)

    /**
     * Get the stored salt (Base64). Use carefully.
     */
    fun getStoredSalt(): String? = encryptedPrefs.getString(KEY_SALT, null)

    /**
     * Check if account exists in encrypted storage.
     */
    fun accountExists(): Boolean = encryptedPrefs.contains(KEY_USERNAME)

    /**
     * Get the stored username.
     */
    fun getUsername(): String? = encryptedPrefs.getString(KEY_USERNAME, null)

    /**
     * Get account creation timestamp.
     */
    fun getCreatedAt(): Long = encryptedPrefs.getLong(KEY_CREATED_AT, 0L)

    /**
     * Get last modification timestamp (password change).
     */
    fun getLastModified(): Long = encryptedPrefs.getLong(KEY_LAST_MODIFIED, 0L)

    // ============================================================================
    // Private Cryptographic Methods
    // ============================================================================

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return skf.generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: String?, b: String?): Boolean {
        if (a == null || b == null) return a == b
        if (a.length != b.length) return false

        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    // ============================================================================
    // Validation Methods
    // ============================================================================

    private fun validateUsername(username: String): ValidationResult {
        return when {
            username.length < MIN_USERNAME_LENGTH -> ValidationResult(false, "Username must be at least $MIN_USERNAME_LENGTH characters")
            username.length > MAX_USERNAME_LENGTH -> ValidationResult(false, "Username must be at most $MAX_USERNAME_LENGTH characters")
            !username.matches(Regex("^[a-zA-Z][a-zA-Z0-9_-]*$")) -> ValidationResult(false, "Username must start with letter, contain only alphanumeric, underscore, or hyphen")
            else -> ValidationResult(true)
        }
    }

    private fun validatePassword(password: String): ValidationResult {
        return when {
            password.length < MIN_PASSWORD_LENGTH -> ValidationResult(false, "Password must be at least $MIN_PASSWORD_LENGTH characters")
            else -> ValidationResult(true)
        }
    }

    data class ValidationResult(val isValid: Boolean, val error: String? = null)

    data class AccountResult(val success: Boolean, val error: String? = null)
}