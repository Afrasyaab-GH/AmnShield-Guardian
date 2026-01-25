package org.alhaq.deenshield.guardian.model

import java.time.Instant

/**
 * Per-app WiFi and mobile internet blocking rule.
 * Mirrors DeenShield AppControl/DeenShield Access Rule model for per-app control.
 */
data class AppRule(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val isSystem: Boolean = false,
    val hasInternet: Boolean = true,
    val isEnabled: Boolean = true,
    
    // WiFi blocking
    val wifiBlocked: Boolean = false,
    
    // Mobile data blocking
    val mobileBlocked: Boolean = false,
    
    // Allow when screen is on (bypass blocking)
    val allowWifiWhenScreenOn: Boolean = false,
    val allowMobileWhenScreenOn: Boolean = false,
    
    // Roaming control
    val blockWhenRoaming: Boolean = false,
    
    // Lockdown mode (only allow if explicitly listed)
    val lockdown: Boolean = false,
    
    // Metadata
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun isBlocked(): Boolean = wifiBlocked || mobileBlocked
    
    fun requiresAttention(): Boolean = isBlocked() || lockdown
}
