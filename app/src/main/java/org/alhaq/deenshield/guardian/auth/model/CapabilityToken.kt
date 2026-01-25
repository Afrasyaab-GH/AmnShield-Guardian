package org.alhaq.deenshield.guardian.auth.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a temporary, encrypted token that grants specific capabilities.
 *
 * This embodies the "temporary trust" model:
 * - Token is generated when user grants a capability
 * - Token expires automatically (90 days default)
 * - Token can be revoked immediately
 * - No permanent lock-in; user maintains ultimate control
 *
 * Islamic principle: Temporary trust for verification, not ownership.
 * Islamic principle: Amanah (trust) - Token-based, not permanent control.
 */
@Serializable
@Entity(tableName = "capability_tokens")
data class CapabilityToken(
    @PrimaryKey
    val sessionId: String,                           // Unique UUID for this token session

    val grantedBy: String,                           // Package that generated token (e.g., "com.deenshield.guardian")

    val grantedTo: String,                           // Package that will use token (e.g., "org.alhaq.deenshield.netblock")

    val capabilities: String,                        // JSON list of granted capability IDs: ["manage_app_access", "manage_schedules"]

    val createdAt: Long,                            // Timestamp when token was created

    val expiresAt: Long,                            // Expiration timestamp (must check before use)

    val encryptedData: ByteArray,                   // AES-256 encrypted capability list + metadata

    val hmacSignature: String,                      // HMAC-SHA256 signature for integrity verification

    val isRevoked: Boolean = false,                 // User explicitly revoked this token

    val revokedAt: Long? = null,                    // Timestamp when revoked

    val revokeReason: String = ""                   // Why was it revoked: "user_request", "app_uninstall", etc.
) {
    /**
     * Check if token is still valid:
     * - Not revoked by user
     * - Not expired
     * - Signature is valid (must be verified by caller)
     */
    fun isValid(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        return !isRevoked && currentTimeMs < expiresAt
    }

    /**
     * Check if token has expired.
     */
    fun isExpired(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        return currentTimeMs >= expiresAt
    }

    /**
     * Get remaining time until expiration in milliseconds.
     * Returns 0 if already expired.
     */
    fun getTimeUntilExpiration(currentTimeMs: Long = System.currentTimeMillis()): Long {
        return maxOf(0, expiresAt - currentTimeMs)
    }

    /**
     * Get age of token in milliseconds.
     */
    fun getAge(currentTimeMs: Long = System.currentTimeMillis()): Long {
        return currentTimeMs - createdAt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CapabilityToken) return false

        if (sessionId != other.sessionId) return false
        if (grantedBy != other.grantedBy) return false
        if (grantedTo != other.grantedTo) return false
        if (capabilities != other.capabilities) return false
        if (!encryptedData.contentEquals(other.encryptedData)) return false
        if (hmacSignature != other.hmacSignature) return false
        if (isRevoked != other.isRevoked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + grantedBy.hashCode()
        result = 31 * result + grantedTo.hashCode()
        result = 31 * result + capabilities.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + hmacSignature.hashCode()
        result = 31 * result + isRevoked.hashCode()
        return result
    }
}

/**
 * Token builder for convenient creation with validation.
 */
class CapabilityTokenBuilder(
    val sessionId: String,
    val grantedBy: String,
    val grantedTo: String,
    val capabilities: List<String>
) {
    var createdAt: Long = System.currentTimeMillis()
    var expiresInDays: Long = 90                                    // Default 90 days
    var encryptedData: ByteArray = byteArrayOf()
    var hmacSignature: String = ""

    fun expiresAt(): Long = createdAt + (expiresInDays * 24 * 60 * 60 * 1000)

    fun build(): CapabilityToken {
        require(sessionId.isNotBlank()) { "Session ID cannot be blank" }
        require(grantedBy.isNotBlank()) { "GrantedBy cannot be blank" }
        require(grantedTo.isNotBlank()) { "GrantedTo cannot be blank" }
        require(capabilities.isNotEmpty()) { "At least one capability must be granted" }
        require(encryptedData.isNotEmpty()) { "Encrypted data must be set" }
        require(hmacSignature.isNotBlank()) { "HMAC signature must be set" }

        return CapabilityToken(
            sessionId = sessionId,
            grantedBy = grantedBy,
            grantedTo = grantedTo,
            capabilities = capabilities.joinToString(","),
            createdAt = createdAt,
            expiresAt = expiresAt(),
            encryptedData = encryptedData,
            hmacSignature = hmacSignature
        )
    }
}
