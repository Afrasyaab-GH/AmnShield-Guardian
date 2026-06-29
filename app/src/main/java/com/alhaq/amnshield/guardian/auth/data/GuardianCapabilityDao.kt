package com.alhaq.amnshield.guardian.auth.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for GuardianCapabilityEntity
 * 
 * Manages capability grants for Guardian integration.
 * Supports querying by capability, target app, and grant status.
 * 
 * **Thread Safety:**
 * - All operations are coroutine-safe
 * - Flow emissions update reactively
 * 
 * **Islamic Principle: Rida (Consent)**
 * - Clear audit trail of all grants/revocations
 * - Expired capabilities automatically filtered
 * 
 * @see GuardianCapabilityEntity
 */
@Dao
interface GuardianCapabilityDao {
    
    /**
     * Insert new capability grant
     * 
     * @param capability Capability to insert
     * @return ID of inserted capability
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(capability: GuardianCapabilityEntity): Long
    
    /**
     * Update capability grant
     * 
     * @param capability Capability with updated fields
     */
    @Update
    suspend fun update(capability: GuardianCapabilityEntity)
    
    /**
     * Delete capability grant
     * 
     * @param capability Capability to delete
     */
    @Delete
    suspend fun delete(capability: GuardianCapabilityEntity)
    
    /**
     * Get all granted capabilities for target app
     * 
     * Excludes revoked and expired capabilities.
     * 
     * @param targetPackage Target app package (e.g., "com.alhaq.deenshield")
     * @return Flow emitting list of granted capabilities
     */
    @Query("""
        SELECT * FROM guardian_capabilities 
        WHERE targetPackage = :targetPackage 
        AND isGranted = 1 
        AND (expiresAt IS NULL OR expiresAt > :currentTime)
        ORDER BY grantedAt DESC
    """)
    fun getGrantedCapabilities(
        targetPackage: String, 
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<GuardianCapabilityEntity>>
    
    /**
     * Get specific capability by ID
     * 
     * @param capabilityId Capability identifier
     * @param targetPackage Target app package
     * @return Flow emitting capability or null if not found
     */
    @Query("""
        SELECT * FROM guardian_capabilities 
        WHERE capabilityId = :capabilityId 
        AND targetPackage = :targetPackage 
        LIMIT 1
    """)
    fun getCapability(capabilityId: String, targetPackage: String): Flow<GuardianCapabilityEntity?>
    
    /**
     * Check if capability is granted and not expired
     * 
     * @param capabilityId Capability identifier
     * @param targetPackage Target app package
     * @return true if capability is granted and valid
     */
    @Query("""
        SELECT COUNT(*) FROM guardian_capabilities 
        WHERE capabilityId = :capabilityId 
        AND targetPackage = :targetPackage 
        AND isGranted = 1 
        AND (expiresAt IS NULL OR expiresAt > :currentTime)
    """)
    suspend fun isCapabilityGranted(
        capabilityId: String, 
        targetPackage: String,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Revoke capability
     * 
     * Marks capability as revoked with timestamp.
     * 
     * @param capabilityId Capability identifier
     * @param targetPackage Target app package
     * @param revokedAt Revocation timestamp
     */
    @Query("""
        UPDATE guardian_capabilities 
        SET isGranted = 0, revokedAt = :revokedAt 
        WHERE capabilityId = :capabilityId 
        AND targetPackage = :targetPackage
    """)
    suspend fun revokeCapability(
        capabilityId: String, 
        targetPackage: String,
        revokedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Revoke all capabilities for target app
     * 
     * Used when user unlinks from Guardian completely.
     * 
     * @param targetPackage Target app package
     */
    @Query("""
        UPDATE guardian_capabilities 
        SET isGranted = 0, revokedAt = :revokedAt 
        WHERE targetPackage = :targetPackage
    """)
    suspend fun revokeAllCapabilities(
        targetPackage: String,
        revokedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Get all capabilities (granted and revoked) for audit
     * 
     * @param targetPackage Target app package
     * @return Flow emitting all capabilities
     */
    @Query("""
        SELECT * FROM guardian_capabilities 
        WHERE targetPackage = :targetPackage 
        ORDER BY grantedAt DESC
    """)
    fun getAllCapabilities(targetPackage: String): Flow<List<GuardianCapabilityEntity>>
    
    /**
     * Delete all capabilities (for testing)
     */
    @Query("DELETE FROM guardian_capabilities")
    suspend fun deleteAll()
}

