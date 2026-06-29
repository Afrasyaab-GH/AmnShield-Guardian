package com.alhaq.amnshield.guardian.premium

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PremiumManager - Manages premium status and Compassionate Access Program
 * 
 * Compassionate Access Program:
 * - Trust-based system for users in financial need
 * - 1-year free access to all DeenShield ecosystem features
 * - No payment required, no verification needed
 * - Based on Islamic principle of Rahmah (Mercy)
 */
@Singleton
class PremiumManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("premium_status", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_PREMIUM_TYPE = "premium_type"
        private const val KEY_PREMIUM_EXPIRES_AT = "premium_expires_at"
        private const val KEY_COMPASSIONATE_ACCESS_ID = "compassionate_access_id"
        private const val KEY_COMPASSIONATE_NAME = "compassionate_name"
        private const val KEY_COMPASSIONATE_EMAIL = "compassionate_email"
        private const val KEY_COMPASSIONATE_GRANTED_AT = "compassionate_granted_at"
        
        // Premium types
        const val TYPE_FREE = "free"
        const val TYPE_LIFETIME = "lifetime"
        const val TYPE_MONTHLY = "monthly"
        const val TYPE_YEARLY = "yearly"
        const val TYPE_COMPASSIONATE = "compassionate"
        
        // 1 year in milliseconds
        private const val ONE_YEAR_MILLIS = 365L * 24 * 60 * 60 * 1000
    }
    
    /**
     * Check if user has active premium access
     */
    fun isPremium(): Boolean {
        if (!prefs.getBoolean(KEY_IS_PREMIUM, false)) {
            return false
        }
        
        // Check if compassionate access has expired
        val type = prefs.getString(KEY_PREMIUM_TYPE, TYPE_FREE)
        if (type == TYPE_COMPASSIONATE) {
            val expiresAt = prefs.getLong(KEY_PREMIUM_EXPIRES_AT, 0)
            if (System.currentTimeMillis() > expiresAt) {
                // Expired - revoke access
                revokePremium()
                return false
            }
        }
        
        return true
    }
    
    /**
     * Get current premium type
     */
    fun getPremiumType(): String {
        return prefs.getString(KEY_PREMIUM_TYPE, TYPE_FREE) ?: TYPE_FREE
    }
    
    /**
     * Grant Compassionate Access (1-year free premium)
     * 
     * @param name User's name (required)
     * @param email User's email (optional)
     * @return Generated App ID for user's records
     */
    fun grantCompassionateAccess(name: String, email: String?): String {
        val appId = generateAppId()
        val expiresAt = System.currentTimeMillis() + ONE_YEAR_MILLIS
        
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, true)
            .putString(KEY_PREMIUM_TYPE, TYPE_COMPASSIONATE)
            .putLong(KEY_PREMIUM_EXPIRES_AT, expiresAt)
            .putString(KEY_COMPASSIONATE_ACCESS_ID, appId)
            .putString(KEY_COMPASSIONATE_NAME, name)
            .putString(KEY_COMPASSIONATE_EMAIL, email ?: "")
            .putLong(KEY_COMPASSIONATE_GRANTED_AT, System.currentTimeMillis())
            .apply()
        
        return appId
    }
    
    /**
     * Get Compassionate Access details
     */
    fun getCompassionateAccessDetails(): CompassionateAccessDetails? {
        if (getPremiumType() != TYPE_COMPASSIONATE) {
            return null
        }
        
        return CompassionateAccessDetails(
            appId = prefs.getString(KEY_COMPASSIONATE_ACCESS_ID, "") ?: "",
            name = prefs.getString(KEY_COMPASSIONATE_NAME, "") ?: "",
            email = prefs.getString(KEY_COMPASSIONATE_EMAIL, "") ?: "",
            grantedAt = prefs.getLong(KEY_COMPASSIONATE_GRANTED_AT, 0),
            expiresAt = prefs.getLong(KEY_PREMIUM_EXPIRES_AT, 0)
        )
    }
    
    /**
     * Grant lifetime premium (for purchases)
     */
    fun grantLifetimePremium() {
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, true)
            .putString(KEY_PREMIUM_TYPE, TYPE_LIFETIME)
            .putLong(KEY_PREMIUM_EXPIRES_AT, Long.MAX_VALUE)
            .apply()
    }
    
    /**
     * Grant monthly subscription
     */
    fun grantMonthlySubscription() {
        val expiresAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, true)
            .putString(KEY_PREMIUM_TYPE, TYPE_MONTHLY)
            .putLong(KEY_PREMIUM_EXPIRES_AT, expiresAt)
            .apply()
    }
    
    /**
     * Grant yearly subscription
     */
    fun grantYearlySubscription() {
        val expiresAt = System.currentTimeMillis() + ONE_YEAR_MILLIS
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, true)
            .putString(KEY_PREMIUM_TYPE, TYPE_YEARLY)
            .putLong(KEY_PREMIUM_EXPIRES_AT, expiresAt)
            .apply()
    }
    
    /**
     * Revoke premium access
     */
    fun revokePremium() {
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, false)
            .putString(KEY_PREMIUM_TYPE, TYPE_FREE)
            .remove(KEY_PREMIUM_EXPIRES_AT)
            .apply()
    }
    
    /**
     * Check if compassionate access is about to expire (within 30 days)
     */
    fun isCompassionateAccessExpiringSoon(): Boolean {
        if (getPremiumType() != TYPE_COMPASSIONATE) {
            return false
        }
        
        val expiresAt = prefs.getLong(KEY_PREMIUM_EXPIRES_AT, 0)
        val thirtyDaysFromNow = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        return expiresAt < thirtyDaysFromNow
    }
    
    /**
     * Get days remaining for compassionate access
     */
    fun getDaysRemaining(): Long {
        if (getPremiumType() != TYPE_COMPASSIONATE) {
            return -1
        }
        
        val expiresAt = prefs.getLong(KEY_PREMIUM_EXPIRES_AT, 0)
        val remainingMillis = expiresAt - System.currentTimeMillis()
        return remainingMillis / (24 * 60 * 60 * 1000)
    }
    
    /**
     * Generate unique App ID for compassionate access
     */
    private fun generateAppId(): String {
        val uuid = UUID.randomUUID().toString().take(8).uppercase()
        return "DS-CA-$uuid"
    }
}

/**
 * Data class for Compassionate Access details
 */
data class CompassionateAccessDetails(
    val appId: String,
    val name: String,
    val email: String,
    val grantedAt: Long,
    val expiresAt: Long
) {
    /**
     * Get days remaining until expiry
     */
    fun getDaysRemaining(): Long {
        val remainingMillis = expiresAt - System.currentTimeMillis()
        return remainingMillis / (24 * 60 * 60 * 1000)
    }
    
    /**
     * Check if expired
     */
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }
    
    /**
     * Check if expiring soon (within 30 days)
     */
    fun isExpiringSoon(): Boolean {
        val thirtyDaysFromNow = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        return expiresAt < thirtyDaysFromNow
    }
}

