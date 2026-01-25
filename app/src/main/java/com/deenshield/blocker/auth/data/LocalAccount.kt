package com.deenshield.blocker.auth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing local account data
 * 
 * Represents a user account stored locally on device with encrypted password.
 * Supports multiple identity modes: EMAIL, LOCAL, DEVICE_ID, NO_IDENTITY.
 * 
 * **Islamic Principle: Amanah (Trust)**
 * - All sensitive data encrypted at rest
 * - Password stored as PBKDF2 hash with unique salt
 * - Account lifecycle managed with user consent
 * 
 * @property id Unique account identifier (auto-generated)
 * @property username Display name chosen by user
 * @property email Optional email address (null for LOCAL/DEVICE_ID modes)
 * @property passwordHash PBKDF2-hashed password (100,000 iterations)
 * @property passwordSalt Unique random salt for password hashing
 * @property identityMode Type of identity (EMAIL, LOCAL, DEVICE_ID, NO_IDENTITY)
 * @property deviceId Auto-generated UUID for DEVICE_ID mode
 * @property createdAt Account creation timestamp (milliseconds since epoch)
 * @property lastLoginAt Last successful login timestamp
 * @property isActive Whether account is currently active
 * 
 * @see com.deenshield.blocker.auth.IdentityMode
 * @see com.deenshield.blocker.auth.local.LocalAccountManager
 */
@Entity(tableName = "local_accounts")
data class LocalAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val username: String,
    
    val email: String? = null,
    
    val passwordHash: String,
    
    val passwordSalt: String,
    
    val identityMode: String, // "EMAIL", "LOCAL", "DEVICE_ID", "NO_IDENTITY"
    
    val deviceId: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val lastLoginAt: Long? = null,
    
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Validates account data before storage
         * 
         * @return true if account data is valid
         */
        fun isValid(account: LocalAccount): Boolean {
            return when {
                account.username.isBlank() -> false
                account.passwordHash.isBlank() -> false
                account.passwordSalt.isBlank() -> false
                account.identityMode.isBlank() -> false
                account.identityMode == "EMAIL" && account.email.isNullOrBlank() -> false
                account.identityMode == "DEVICE_ID" && account.deviceId.isNullOrBlank() -> false
                else -> true
            }
        }
    }
}
