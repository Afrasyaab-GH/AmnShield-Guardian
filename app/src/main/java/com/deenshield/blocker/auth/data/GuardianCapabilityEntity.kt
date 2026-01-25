package com.deenshield.blocker.auth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing Guardian capability grants
 * 
 * Tracks which capabilities user has granted to Guardian integration.
 * Each capability represents specific permission (e.g., "MANAGE_APP_BLOCKING").
 * 
 * **Islamic Principle: Rida (Consent)**
 * - Every capability requires explicit user approval
 * - Capabilities can be revoked at any time
 * - Granular permissions (not all-or-nothing)
 * - Audit trail via grantedAt/revokedAt timestamps
 * 
 * @property id Unique capability grant ID
 * @property capabilityId Capability identifier (e.g., "MANAGE_APP_BLOCKING")
 * @property targetPackage Target app package name (e.g., "com.alhaq.deenshield")
 * @property isGranted Whether capability is currently granted
 * @property grantedAt Timestamp when capability was granted
 * @property revokedAt Timestamp when capability was revoked (null if still granted)
 * @property expiresAt Optional expiration timestamp (null for no expiration)
 * 
 * @see com.deenshield.blocker.auth.GuardianCapability
 */
@Entity(tableName = "guardian_capabilities")
data class GuardianCapabilityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val capabilityId: String, // "MANAGE_APP_BLOCKING", etc.
    
    val targetPackage: String, // "com.alhaq.deenshield", etc.
    
    val isGranted: Boolean = true,
    
    val grantedAt: Long = System.currentTimeMillis(),
    
    val revokedAt: Long? = null,
    
    val expiresAt: Long? = null // Optional expiration
) {
    companion object {
        /**
         * Standard capability IDs for DeenShield App
         */
        const val MANAGE_APP_BLOCKING = "MANAGE_DEENSHIELD_APP_BLOCKING"
        const val MANAGE_KEYWORDS = "MANAGE_DEENSHIELD_KEYWORDS"
        const val MANAGE_FOCUS_MODE = "MANAGE_DEENSHIELD_FOCUS_MODE"
        const val MANAGE_VIEW_BLOCKER = "MANAGE_DEENSHIELD_VIEW_BLOCKER"
        const val MANAGE_SMART_FEATURES = "MANAGE_DEENSHIELD_SMART_FEATURES"
        const val VIEW_REPORTS = "VIEW_DEENSHIELD_REPORTS"
        
        /**
         * Standard capability IDs for NetBlock
         */
        const val MANAGE_NETWORK_BLOCKING = "MANAGE_NETBLOCK_BLOCKING"
        const val MANAGE_VPN_CONFIG = "MANAGE_NETBLOCK_VPN_CONFIG"
        const val MANAGE_DNS_FILTERING = "MANAGE_NETBLOCK_DNS_FILTERING"
        
        /**
         * Check if capability has expired
         */
        fun isExpired(entity: GuardianCapabilityEntity): Boolean {
            val expiresAt = entity.expiresAt ?: return false
            return System.currentTimeMillis() > expiresAt
        }
    }
}
