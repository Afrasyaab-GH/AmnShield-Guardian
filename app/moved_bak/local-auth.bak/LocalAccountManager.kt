package org.alhaq.deenshield.guardian.auth.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBKDFKeySpec
import java.security.SecureRandom
import java.util.Base64

/**
 * Manages local account creation, storage, and authentication.
 *
 * This manager handles:
 * - Account creation with username and password
 * - PBKDF2WithHmacSHA256 password hashing (100,000 iterations)
 * - Encrypted storage in EncryptedSharedPreferences (AES-256-GCM)
 * - Credential validation for login
 * - Password changes
 * - Account deletion
 *
 * **CRITICAL SECURITY NOTES:**
 * - All operations are 100% local on-device
 * - No network calls, no backend, no cloud sync
 * - Passwords are hashed with PBKDF2, never stored plaintext
 * - Encrypted storage uses Android Keystore (keys never leave device)
 * - Salt is generated with SecureRandom (256-bit)
 * - Each password hashing operation is CPU-local only
 *
 * **Islamic Principles Embedded:**
 * - Amanah (Trust): Users' passwords are protected with industry-standard encryption
 * - Rida (Consent): User explicitly chooses LOCAL_ACCOUNT mode before creation
 * - Karamah (Dignity): No surveillance of password strength, just requirements
 *
 * **Thread Safety:**
 * - EncryptedSharedPreferences is thread-safe
 * - All read/write operations are synchronized by SharedPreferences implementation
 *
 * @param context Android application context (used for EncryptedSharedPreferences)
 */
class LocalAccountManager(private val context: Context) {

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
        private const val ITERATIONS = 100_000  // OWASP recommended minimum
        private const val KEY_LENGTH = 256      // 256-bit derived key
        private const val SALT_LENGTH = 32      // 256-bit salt (most secure)

        // Minimum password requirements
        const val MIN_PASSWORD_LENGTH = 8
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 30
    }

    /**
     * Encrypted SharedPreferences instance.
     * Uses Android Keystore for master key storage (keys never leave device).
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

    // ============================================================================
    // Account Lifecycle Methods
    // ============================================================================

    /**
     * Create a new local account with username and password.
     *
     * **Process:**
     * 1. Validate username and password
     * 2. Check account doesn't already exist
     * 3. Generate random salt (256-bit)
     * 4. Hash password using PBKDF2 (100,000 iterations)
     * 5. Store encrypted in EncryptedSharedPreferences
     * 6. Record creation timestamp
     *
     * **Security Guarantees:**
     * - Password never stored plaintext
     * - Salt randomized per account
     * - Hash computation is CPU-local (no network)
     * - Encrypted storage on device only
     *
     * @param username Username (3-30 alphanumeric characters)
     * @param password Password (min 8 chars, should include uppercase, digits, symbols)
     * @return Result with success flag and optional error message
     */
    fun createAccount(username: String, password: String): AccountResult {
        // Validate inputs
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

        // Check account doesn't already exist
        if (accountExists()) {
            Log.w(TAG, "Account already exists")
            return AccountResult(
                success = false,
                error = "Account already exists. Delete existing account first."
            )
        }

        return try {
            // Generate random salt
            val salt = generateSalt()
            val saltBase64 = Base64.getEncoder().encodeToString(salt)

            // Hash password
            val passwordHash = hashPassword(password, salt)
            val hashBase64 = Base64.getEncoder().encodeToString(passwordHash)

            // Store in encrypted preferences
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
            AccountResult(
                success = false,
                error = "Failed to create account: ${e.message}"
            )
        }
    }

    /**
     * Authenticate user with username and password.
     *
     * **Process:**
     * 1. Check account exists
     * 2. Retrieve stored salt
     * 3. Hash provided password with same salt
     * 4. Compare computed hash with stored hash
     *
     * **Security Guarantees:**
     * - Passwords compared as hashes, never plaintext
     * - Timing-safe comparison prevents timing attacks
     * - Failed attempts logged (but not stored for brute-force tracking)
     * - Salt from encrypted storage remains encrypted
     *
     * @param username Username to authenticate
     * @param password Password to verify
     * @return Result with success flag and optional error message
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

            val salt = Base64.getDecoder().decode(saltBase64)
            val computedHash = hashPassword(password, salt)
            val computedHashBase64 = Base64.getEncoder().encodeToString(computedHash)

            val storedHash = encryptedPrefs.getString(KEY_PASSWORD_HASH, "")

            // Timing-safe comparison (prevents timing attacks)
            if (constantTimeEquals(computedHashBase64, storedHash)) {
                Log.i(TAG, "Authentication successful for: $username")
                AccountResult(success = true)
            } else {
                Log.w(TAG, "Authentication failed: invalid password")
                AccountResult(success = false, error = "Invalid username or password")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error authenticating account", e)
            AccountResult(success = false, error = "Authentication failed: ${e.message}")
        }
    }

    /**
     * Change password for existing account.
     *
     * **Process:**
     * 1. Verify old password (prevents unauthorized changes)
     * 2. Validate new password
     * 3. Generate new salt
     * 4. Hash new password with new salt
     * 5. Update encrypted storage
     *
     * @param oldPassword Current password (for verification)
     * @param newPassword New password
     * @return Result with success flag and optional error message
     */
    fun changePassword(oldPassword: String, newPassword: String): AccountResult {
        val username = encryptedPrefs.getString(KEY_USERNAME, "") ?: return AccountResult(
            success = false,
            error = "Account not found"
        )

        // Verify old password first
        val authResult = authenticateAccount(username, oldPassword)
        if (!authResult.success) {
            return AccountResult(success = false, error = "Current password is incorrect")
        }

        // Validate new password
        val validation = validatePassword(newPassword)
        if (!validation.isValid) {
            return AccountResult(success = false, error = validation.error)
        }

        if (oldPassword == newPassword) {
            return AccountResult(success = false, error = "New password must be different from current password")
        }

        return try {
            val salt = generateSalt()
            val saltBase64 = Base64.getEncoder().encodeToString(salt)
            val passwordHash = hashPassword(newPassword, salt)
            val hashBase64 = Base64.getEncoder().encodeToString(passwordHash)

            val now = System.currentTimeMillis()
            encryptedPrefs.edit().apply {
                putString(KEY_PASSWORD_HASH, hashBase64)
                putString(KEY_SALT, saltBase64)
                putLong(KEY_LAST_MODIFIED, now)
                commit()
            }

            Log.i(TAG, "Password changed successfully")
            AccountResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error changing password", e)
            AccountResult(success = false, error = "Failed to change password: ${e.message}")
        }
    }

    /**
     * Delete the local account and all associated data.
     *
     * **Security Note:** This operation is irreversible. User must create new account after deletion.
     *
     * @return Result with success flag and optional error message
     */
    fun deleteAccount(): AccountResult {
        return try {
            encryptedPrefs.edit().apply {
                remove(KEY_USERNAME)
                remove(KEY_PASSWORD_HASH)
                remove(KEY_SALT)
                remove(KEY_CREATED_AT)
                remove(KEY_LAST_MODIFIED)
                commit()
            }
            Log.i(TAG, "Account deleted successfully")
            AccountResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            AccountResult(success = false, error = "Failed to delete account: ${e.message}")
        }
    }

    // ============================================================================
    // Query Methods
    // ============================================================================

    /**
     * Check if a local account exists.
     *
     * @return true if account exists, false otherwise
     */
    fun accountExists(): Boolean {
        val username = encryptedPrefs.getString(KEY_USERNAME, null)
        return !username.isNullOrEmpty()
    }

    /**
     * Get the stored username.
     *
     * @return Username or null if no account
     */
    fun getUsername(): String? {
        return encryptedPrefs.getString(KEY_USERNAME, null)
    }

    /**
     * Get account creation timestamp.
     *
     * @return Milliseconds since epoch
     */
    fun getCreatedAt(): Long {
        return encryptedPrefs.getLong(KEY_CREATED_AT, 0L)
    }

    /**
     * Get last modification timestamp (password change).
     *
     * @return Milliseconds since epoch
     */
    fun getLastModified(): Long {
        return encryptedPrefs.getLong(KEY_LAST_MODIFIED, 0L)
    }

    // ============================================================================
    // Private Cryptographic Methods
    // ============================================================================

    /**
     * Generate random salt for PBKDF2.
     *
     * @return 256-bit (32-byte) random salt
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Hash password using PBKDF2WithHmacSHA256.
     *
     * **Parameters:**
     * - Algorithm: PBKDF2WithHmacSHA256
     * - Iterations: 100,000 (OWASP recommended)
     * - Key Length: 256 bits
     *
     * **Process:**
     * 1. Create PBKDFKeySpec with password, salt, iterations, key length
     * 2. Derive key using SecretKeyFactory
     * 3. Extract raw bytes and return
     *
     * **Security Properties:**
     * - CPU-intensive (prevents brute-force attacks)
     * - Salt randomization (prevents rainbow tables)
     * - Each account has unique salt
     * - All computation is local on-device
     *
     * @param password Password to hash
     * @param salt Salt bytes for this account
     * @return 256-bit (32-byte) derived key
     */
    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBKDFKeySpec(
            password.toCharArray(),
            salt,
            ITERATIONS,
            KEY_LENGTH
        )
        val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return skf.generateSecret(spec).encoded
    }

    /**
     * Timing-safe string comparison.
     *
     * Prevents timing attacks by comparing ALL characters even after finding a mismatch.
     *
     * @param a First string
     * @param b Second string
     * @return true if equal, false otherwise
     */
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

    /**
     * Validate username format.
     *
     * **Requirements:**
     * - Length: 3-30 characters
     * - Alphanumeric, underscore, hyphen allowed
     * - Cannot start with digit
     *
     * @param username Username to validate
     * @return ValidationResult with isValid flag and optional error message
     */
    private fun validateUsername(username: String): ValidationResult {
        return when {
            username.length < MIN_USERNAME_LENGTH -> ValidationResult(
                isValid = false,
                error = "Username must be at least $MIN_USERNAME_LENGTH characters"
            )
            username.length > MAX_USERNAME_LENGTH -> ValidationResult(
                isValid = false,
                error = "Username must be at most $MAX_USERNAME_LENGTH characters"
            )
            !username.matches(Regex("^[a-zA-Z][a-zA-Z0-9_-]*$")) -> ValidationResult(
                isValid = false,
                error = "Username must start with letter, contain only alphanumeric, underscore, or hyphen"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Validate password strength.
     *
     * **Requirements:**
     * - Length: minimum 8 characters
     * - Recommended: uppercase, lowercase, digit, special character
     *
     * @param password Password to validate
     * @return ValidationResult with isValid flag and optional error message
     */
    private fun validatePassword(password: String): ValidationResult {
        return when {
            password.length < MIN_PASSWORD_LENGTH -> ValidationResult(
                isValid = false,
                error = "Password must be at least $MIN_PASSWORD_LENGTH characters"
            )
            !password.matches(Regex(".*[A-Z].*")) -> ValidationResult(
                isValid = false,
                error = "Password must contain at least one uppercase letter"
            )
            !password.matches(Regex(".*[a-z].*")) -> ValidationResult(
                isValid = false,
                error = "Password must contain at least one lowercase letter"
            )
            !password.matches(Regex(".*\\d.*")) -> ValidationResult(
                isValid = false,
                error = "Password must contain at least one digit"
            )
            !password.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) -> ValidationResult(
                isValid = false,
                error = "Password must contain at least one special character"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    // ============================================================================
    // Result Data Classes
    // ============================================================================

    /**
     * Result of account operation (create, authenticate, change password, delete).
     */
    data class AccountResult(
        val success: Boolean,
        val error: String? = null
    )

    /**
     * Result of validation operation.
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null
    )
}
