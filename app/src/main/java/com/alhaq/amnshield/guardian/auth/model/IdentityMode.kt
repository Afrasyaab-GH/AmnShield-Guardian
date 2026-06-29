package com.alhaq.amnshield.guardian.auth.model

/**
 * Represents the three identity modes supported by DeenShield Guardian.
 * Users choose during first launch. All modes work independently.
 *
 * Islamic principle: Consent (Rida) - Users choose their preferred privacy/convenience level.
 */
enum class IdentityMode {
    /**
     * Email-based account with optional cloud sync.
     * Enables multi-device management and remote access.
     * Best for: Parents managing multiple devices, organizations.
     */
    EMAIL_ACCOUNT,

    /**
     * Local-only account (username + password/PIN).
     * Maximum privacy - all data stays on device.
     * Best for: Individual users prioritizing privacy.
     */
    LOCAL_ACCOUNT,

    /**
     * Device ID-based identification (auto-generated UUID).
     * Zero friction - no account creation required.
     * Pair devices via QR code, NFC, or local network.
     * Best for: Family networks, quick setup.
     */
    DEVICE_ID,

    /**
     * No identity - continue without any account.
     * App works fully standalone; can connect to Guardian via device ID later.
     * Best for: Users avoiding account creation entirely.
     */
    NO_IDENTITY
}

