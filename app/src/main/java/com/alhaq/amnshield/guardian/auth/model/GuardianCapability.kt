package com.alhaq.amnshield.guardian.auth.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a specific capability that Guardian can grant to apps.
 * Each capability is independent and must be explicitly approved by the user.
 *
 * Islamic principle: Consent (Rida) - Explicit permission for every capability.
 * Islamic principle: Granularity - No all-or-nothing permission models.
 */
@Entity(tableName = "guardian_capabilities")
data class GuardianCapability(
    @PrimaryKey
    val id: String,                      // Unique capability ID: "manage_app_access", "manage_schedules", etc.

    val displayName: String,             // User-friendly name: "App Access Management"

    val description: String,             // Plain language explanation: "Allow Guardian to block/allow apps"

    val detailedDescription: String,     // Longer explanation including data accessed and frequency

    val scope: CapabilityScope,          // What type of capability (app control, time limits, etc.)

    val isGranted: Boolean = false,      // User's current choice for this capability

    val grantedAt: Long? = null,         // Timestamp when user granted (for audit)

    val revokedAt: Long? = null,         // Timestamp when user revoked

    val expiresAt: Long? = null,         // When this capability grant expires (optional)

    val dataAccessed: List<String> = emptyList(),  // What data will be accessed: ["blocked_apps", "time_limits"]

    val accessFrequency: String = "on-demand",    // How often data is accessed: "continuous", "on-demand", "once-daily"

    val canBeRevoked: Boolean = true,    // Whether user can revoke (most should be true)

    val notes: String = ""               // Additional notes or warnings
)

/**
 * Defines the scope of what a capability can control.
 */
enum class CapabilityScope {
    APP_BLOCKING,              // Control which apps are blocked
    CONTENT_FILTERING,         // Configure content filters
    PRODUCTIVITY_LIMITS,       // Set time limits and focus modes
    SCHEDULE_MANAGEMENT,       // Create and manage schedules
    ACCESSIBILITY_MONITORING,  // Monitor accessibility events
    NETWORK_ACCESS_CONTROL,    // Control internet access per app
    REPORTING_ANALYTICS,       // View usage reports (no sending outside device)
    CONFIGURATION_SYNC         // Sync configuration with cloud
}

/**
 * Common capabilities that Guardian can request.
 * These are pre-defined for consistency across the ecosystem.
 */
object CommonCapabilities {
    val MANAGE_APP_ACCESS = GuardianCapability(
        id = "manage_app_access",
        displayName = "App Access Management",
        description = "Allow AmnShield Guardian to block or allow internet access for specific apps",
        detailedDescription = "Guardian will be able to see which apps you have configured and modify their internet access permissions. Access happens on-demand when you make changes in Guardian.",
        scope = CapabilityScope.APP_BLOCKING,
        dataAccessed = listOf("app_ids", "blocking_status"),
        accessFrequency = "on-demand"
    )

    val MANAGE_SCHEDULES = GuardianCapability(
        id = "manage_schedules",
        displayName = "Schedule Management",
        description = "Allow AmnShield Guardian to create and modify blocking schedules",
        detailedDescription = "Guardian will be able to set time-based rules for when blocks should be enforced. For example, blocking during school hours or prayer times.",
        scope = CapabilityScope.SCHEDULE_MANAGEMENT,
        dataAccessed = listOf("schedules", "time_settings"),
        accessFrequency = "on-demand"
    )

    val MANAGE_CONTENT_FILTERS = GuardianCapability(
        id = "manage_content_filters",
        displayName = "Content Filter Configuration",
        description = "Allow AmnShield Guardian to configure content filtering keywords and rules",
        detailedDescription = "Guardian can add or remove keywords and domains to be filtered. This includes harmful websites and inappropriate keywords based on Islamic values.",
        scope = CapabilityScope.CONTENT_FILTERING,
        dataAccessed = listOf("block_list", "keywords"),
        accessFrequency = "on-demand"
    )

    val MANAGE_TIME_LIMITS = GuardianCapability(
        id = "manage_time_limits",
        displayName = "Productivity Time Limits",
        description = "Allow AmnShield Guardian to set daily or hourly usage limits",
        detailedDescription = "Guardian can configure how long apps or internet access is allowed per day/hour. Useful for protecting study time or preventing excessive screen time.",
        scope = CapabilityScope.PRODUCTIVITY_LIMITS,
        dataAccessed = listOf("time_settings", "usage_stats"),
        accessFrequency = "continuous"
    )

    val VIEW_REPORTS = GuardianCapability(
        id = "view_reports",
        displayName = "View Activity Reports",
        description = "Allow AmnShield Guardian to view usage reports and blocked content statistics",
        detailedDescription = "Guardian can see which apps were blocked, how many times, and when. No data is sent outside the device. All reports are local-only.",
        scope = CapabilityScope.REPORTING_ANALYTICS,
        dataAccessed = listOf("block_stats", "usage_history"),
        accessFrequency = "on-demand"
    )

    val SYNC_CONFIGURATION = GuardianCapability(
        id = "sync_configuration",
        displayName = "Cloud Configuration Sync",
        description = "Allow AmnShield Guardian to sync your settings across devices",
        detailedDescription = "Guardian can back up and sync your block lists, schedules, and settings to the cloud (encrypted end-to-end). Requires email account.",
        scope = CapabilityScope.CONFIGURATION_SYNC,
        dataAccessed = listOf("all_settings", "block_lists"),
        accessFrequency = "periodic"
    )

    // Get all capabilities
    fun getAll() = listOf(
        MANAGE_APP_ACCESS,
        MANAGE_SCHEDULES,
        MANAGE_CONTENT_FILTERS,
        MANAGE_TIME_LIMITS,
        VIEW_REPORTS,
        SYNC_CONFIGURATION
    )
}

