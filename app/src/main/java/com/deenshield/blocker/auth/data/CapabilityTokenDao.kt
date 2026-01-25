package com.deenshield.blocker.auth.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CapabilityTokenEntity
 * 
 * Manages capability tokens for Guardian-to-app integration.
 * Supports token issuance, validation, and revocation.
 * 
 * **Thread Safety:**
 * - All operations are coroutine-safe
 * - Token validation checks expiration automatically
 * 
 * **Islamic Principle: Amanah (Trust)**
 * - Tokens are temporary and revocable
 * - Clear audit trail of all tokens
 * 
 * @see CapabilityTokenEntity
 */
@Dao
interface CapabilityTokenDao {
    
    /**
     * Insert new token
     * 
     * @param token Token to insert
     * @return ID of inserted token
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: CapabilityTokenEntity): Long
    
    /**
     * Update token
     * 
     * @param token Token with updated fields
     */
    @Update
    suspend fun update(token: CapabilityTokenEntity)
    
    /**
     * Delete token
     * 
     * @param token Token to delete
     */
    @Delete
    suspend fun delete(token: CapabilityTokenEntity)
    
    /**
     * Get token by session ID
     * 
     * @param sessionId Unique session identifier
     * @return Flow emitting token or null if not found
     */
    @Query("SELECT * FROM capability_tokens WHERE sessionId = :sessionId LIMIT 1")
    fun getTokenBySessionId(sessionId: String): Flow<CapabilityTokenEntity?>
    
    /**
     * Get active token for target app
     * 
     * Returns most recent non-revoked, non-expired token.
     * 
     * @param targetPackage Target app package
     * @return Flow emitting active token or null
     */
    @Query("""
        SELECT * FROM capability_tokens 
        WHERE targetPackage = :targetPackage 
        AND isRevoked = 0 
        AND expiresAt > :currentTime 
        ORDER BY issuedAt DESC 
        LIMIT 1
    """)
    fun getActiveToken(
        targetPackage: String,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<CapabilityTokenEntity?>
    
    /**
     * Get all tokens for target app (active and inactive)
     * 
     * @param targetPackage Target app package
     * @return Flow emitting all tokens
     */
    @Query("""
        SELECT * FROM capability_tokens 
        WHERE targetPackage = :targetPackage 
        ORDER BY issuedAt DESC
    """)
    fun getAllTokens(targetPackage: String): Flow<List<CapabilityTokenEntity>>
    
    /**
     * Revoke token by session ID
     * 
     * @param sessionId Session identifier
     * @param revokedAt Revocation timestamp
     */
    @Query("""
        UPDATE capability_tokens 
        SET isRevoked = 1, revokedAt = :revokedAt 
        WHERE sessionId = :sessionId
    """)
    suspend fun revokeToken(
        sessionId: String,
        revokedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Revoke all tokens for target app
     * 
     * Used when user completely unlinks from Guardian.
     * 
     * @param targetPackage Target app package
     */
    @Query("""
        UPDATE capability_tokens 
        SET isRevoked = 1, revokedAt = :revokedAt 
        WHERE targetPackage = :targetPackage
    """)
    suspend fun revokeAllTokens(
        targetPackage: String,
        revokedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Delete expired tokens (cleanup)
     * 
     * Should be run periodically to remove old tokens.
     * 
     * @param currentTime Current timestamp
     * @return Number of deleted tokens
     */
    @Query("""
        DELETE FROM capability_tokens 
        WHERE expiresAt < :currentTime
    """)
    suspend fun deleteExpiredTokens(currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Check if valid token exists for target app
     * 
     * @param targetPackage Target app package
     * @return true if valid token exists
     */
    @Query("""
        SELECT COUNT(*) FROM capability_tokens 
        WHERE targetPackage = :targetPackage 
        AND isRevoked = 0 
        AND expiresAt > :currentTime
    """)
    suspend fun hasValidToken(
        targetPackage: String,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Delete all tokens (for testing)
     */
    @Query("DELETE FROM capability_tokens")
    suspend fun deleteAll()
}
