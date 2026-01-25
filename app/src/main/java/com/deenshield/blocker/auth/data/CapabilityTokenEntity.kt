package com.deenshield.blocker.auth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing capability tokens
 * 
 * Stores encrypted tokens exchanged between Guardian and target apps.
 * Tokens grant temporary, revocable access to Guardian capabilities.
 * 
 * **Islamic Principle: Amanah & Adl (Trust & Justice)**
 * - Tokens are temporary (90-day expiration)
 * - Revocable at any time by user
 * - Encrypted payload prevents tampering
 * - HMAC signature ensures authenticity
 * 
 * **Security:**
 * - encryptedPayload: AES-256-GCM encrypted permissions
 * - hmacSignature: HMAC-SHA256 for integrity verification
 * - Never store plaintext tokens or permissions
 * 
 * @property id Unique token ID
 * @property sessionId Unique session identifier (UUID)
 * @property targetPackage Target app package receiving token
 * @property encryptedPayload AES-256 encrypted token data
 * @property hmacSignature HMAC-SHA256 signature for verification
 * @property issuedAt Token issuance timestamp
 * @property expiresAt Token expiration timestamp (default: +90 days)
 * @property revokedAt Revocation timestamp (null if still valid)
 * @property isRevoked Whether token has been revoked
 * 
 * @see com.deenshield.blocker.auth.CapabilityToken
 * @see com.deenshield.blocker.auth.local.TokenStorage
 */
@Entity(tableName = "capability_tokens")
data class CapabilityTokenEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val sessionId: String, // Unique UUID for this token
    
    val targetPackage: String, // "com.alhaq.deenshield", etc.
    
    val encryptedPayload: String, // AES-256 encrypted token data
    
    val hmacSignature: String, // HMAC-SHA256 signature
    
    val issuedAt: Long = System.currentTimeMillis(),
    
    val expiresAt: Long, // Default: issuedAt + 90 days
    
    val revokedAt: Long? = null,
    
    val isRevoked: Boolean = false
) {
    companion object {
        /**
         * Default token validity period (90 days)
         */
        const val DEFAULT_VALIDITY_DAYS = 90L
        const val DEFAULT_VALIDITY_MILLIS = DEFAULT_VALIDITY_DAYS * 24 * 60 * 60 * 1000
        
        /**
         * Check if token has expired
         */
        fun isExpired(token: CapabilityTokenEntity): Boolean {
            return System.currentTimeMillis() > token.expiresAt
        }
        
        /**
         * Check if token is valid (not expired and not revoked)
         */
        fun isValid(token: CapabilityTokenEntity): Boolean {
            return !token.isRevoked && !isExpired(token)
        }
        
        /**
         * Calculate expiration timestamp from issuance
         */
        fun calculateExpiration(issuedAt: Long = System.currentTimeMillis()): Long {
            return issuedAt + DEFAULT_VALIDITY_MILLIS
        }
    }
}
