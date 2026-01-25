# DeenShield Guardian - AI Coding Agent Instructions

## Project Overview
**DeenShield Guardian** is the **central protection hub and management platform** for the DeenShield Ecosystem. It offers comprehensive online protection for parents, families, organizations, and individuals, helping them stay safe while upholding ethical Islamic values.

### Current Development Status (January 24, 2026)
- **Phase 1 (Auth):** 60% complete - LocalAccountManager & TokenStorage done
- **Phase 2-5 (Enhancement):** Planned for Week 2-3
- **Phase 6-8 (DeenShield Integration):** Design complete, ready for implementation
- **Integration Docs:** ECOSYSTEM_VISION_ROADMAP.md, DEENSHIELD_APP_INTEGRATION_PLAN.md

### Mission & Purpose
DeenShield Guardian provides:
- **Comprehensive Online Protection** - Multi-layered defense against harmful content
- **Islamic Values Alignment** - Protection framework respecting Islamic ethics and principles
- **Family Safety** - Parent-managed controls for children and family devices
- **Organizational Protection** - Enterprise/school-level content management
- **Individual Accountability** - Self-managed blocking and productivity tools

### Ecosystem Consistency Goals
The DeenShield Ecosystem must be:
- **Fully Consistent** - Unified UX, branding, and interaction patterns across all apps
- **Fully Functional** - Every feature works reliably across Guardian, NetBlock, and DeenShield App
- **Accessible to All** - Premium protection available regardless of financial situation

### Compassionate Access Program ("I Can't Afford to Pay")
**Islamic Principle:** Rahmah (Mercy) - Protection should not be denied due to financial hardship.

#### User Flow
1. **Premium Screen Link:** Below premium purchase options, display:
   > *"I can't afford to pay"* (clickable link/note)

2. **Honesty & Trust Screen:** When clicked, user sees:
   - Clear message: *"This program is for those who are truly in financial need."*
   - Reminder to be truthful (amanah) - Allah knows our circumstances
   - Explanation that this helps us serve those who genuinely need it

3. **Simple Registration:**
   - **Name:** Required (for personalization)
   - **Email:** Optional (for account recovery/support)
   - **App ID:** Auto-generated (for verification)

4. **Proceed Button:** After pressing "Proceed":
   - Grant **1-year free access** to ALL DeenShield platforms (Guardian, App, NetBlock premium features)
   - Show confirmation with access details
   - No payment required, no verification, trust-based

5. **Additional Support Information:**
   Display after granting access:
   > *"If you need further assistance or have questions:"*
   > - Visit: **alhaq-initiative.org/deenshield/premium** for more information
   > - Visit: **alhaq-initiative.org/deenshield/pay** if your situation changes and you wish to contribute
   > - Email: **support@alhaq-initiative.org** with your App ID, email, and name for direct support

#### Implementation Requirements
- **All Apps:** Must implement this flow in their premium/purchase screens
- **Shared Access:** Grant applies to entire ecosystem, not just one app
- **Trust-Based:** No income verification - rely on user honesty (Islamic trust model)
- **Renewal:** After 1 year, user can re-apply if still in need
- **Tracking:** Store compassionate access grants locally + optional anonymized count for reporting

#### UI Guidelines
```
┌─────────────────────────────────────────┐
│         Premium Features                │
│  ┌─────────────────────────────────┐   │
│  │  Lifetime: £13.50               │   │
│  │  Monthly:  £3.50/month          │   │
│  │  Yearly:   £2.50/month          │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ─────────────────────────────────────  │
│  "I can't afford to pay" (link)         │
│                                         │
└─────────────────────────────────────────┘
```

#### Compassionate Access Screen
```
┌─────────────────────────────────────────┐
│         Compassionate Access            │
│                                         │
│  This program is for those who are      │
│  genuinely in financial need.           │
│                                         │
│  Please be truthful - this trust-based  │
│  system helps us serve those who truly  │
│  need protection but cannot pay.        │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ Name: [________________]        │   │
│  │ Email (optional): [_________]   │   │
│  └─────────────────────────────────┘   │
│                                         │
│  [ Proceed - Get 1 Year Free Access ]   │
│                                         │
│  ─────────────────────────────────────  │
│  Need help? Visit:                      │
│  alhaq-initiative.org/deenshield/premium│
│  Or email: support@alhaq-initiative.org │
└─────────────────────────────────────────┘
```

### DeenShield Ecosystem Position
DeenShield Guardian acts as the **management center** for:
- **Parental Control:** Parent-managed protection for children's devices
- **Individual Protection:** Self-managed content blocking and accountability
- **Organizational Management:** Enterprise/school-level protection policies

### Guardian's Dual Role
1. **Central Management Hub:** Configuration interface for ecosystem-wide protection settings
2. **Enhanced Network Controller:** Modern Kotlin/Compose rebuild with VPN + Accessibility services for deep packet inspection and app-level blocking (no root required)

### Related DeenShield Ecosystem Components
- **DeenShield NetBlock** (Legacy Java) - Individual app-level internet/WiFi controller and access blocker. **Standalone app** that can optionally pair with Guardian for centralized management
- **DeenShield App** (Main On-Device Protection) - Primary online protection, content filtering, blurring, and productivity features. Handles real-time content-level filtering and immoral content protection. **Standalone app** that can optionally pair with Guardian

## System Architecture & Design Philosophy

### Optional Federation Architecture (Core Principle)
**Every app must function fully independently. No app may require another to operate.**

This is the foundational principle that ensures user freedom, prevents vendor lock-in, and aligns with Islamic values of trust (amanah), consent, and non-oppression.

#### Independence Rules (Critical - Never Violate)
Each app must:
- ✅ Install independently without prerequisites
- ✅ Operate independently with full features
- ✅ Be removable without breaking other apps
- ✅ Maintain its own local settings and rules
- ✅ Never hard-depend on Guardian or any hub
- ✅ Function offline where technically possible

**What This Means:**
- NetBlock works without Guardian installed
- DeenShield App works without Guardian installed
- Guardian works without NetBlock or DeenShield App installed
- Users can install any combination

#### Guardian's Role (Assistant, Not Owner)
Guardian acts as an **optional assistant**, never as an owner or controller:
- Provides centralized configuration interface
- Offers parental control capabilities
- Manages family/organizational policies
- **But never blocks functionality** of other apps if connection is refused

**Invalid Guardian Behavior:**
- ❌ Requiring authentication to use other apps
- ❌ Forcing connection before allowing NetBlock to block
- ❌ Disabling DeenShield App if Guardian isn't installed
- ❌ Creating hard dependencies on Guardian services

### Connection Model — Permission-Based (Not Pairing)

**Core Rule:** Use permission-based linking, never ownership pairing.

#### Connection Rules (Critical)
1. **User must explicitly approve every connection** - No automatic pairing
2. **Guardian may request capabilities, never assume them** - Each request is explicit
3. **Each app independently decides to accept/reject** - Target app has agency
4. **Capabilities must be revocable at any time** - Users maintain control
5. **No master control keys** - No global authority
6. **No hierarchy** - All apps are peers

#### Capability-Based Linking Examples
Guardian can request permission for:
- "Manage app-level internet blocking schedules"
- "Set time-based focus modes"
- "Configure content filtering keywords"
- "Monitor productivity time limits"
- "Override screen time on specific apps"

**Each request includes:**
- What capability is being requested
- Why Guardian needs it (clear description)
- What data will be accessed
- How often it will be accessed
- User can: **Allow**, **Skip**, or **Revoke anytime**

#### Communication Pattern (Binder/Intent/Service)
- Use Android local IPC only (Binder, Intent, Service)
- Use temporary encrypted capability tokens
- Tokens must expire automatically (e.g., 90 days)
- Tokens must be revocable immediately
- No cloud relay required
- All communication stays device-local

### Security & Privacy Requirements

#### No Permanent Surveillance
- ❌ No persistent tracking of user activity
- ❌ No background monitoring without explicit permission
- ❌ No location tracking beyond requested capabilities
- ❌ No continuous data collection

#### No Hidden Monitoring
- ❌ All connections visible in app settings
- ❌ All permissions must be explicitly listed
- ❌ Users can see what data is being accessed
- ❌ Users can see when Guardian connects

#### No Forced Accounts
- ❌ No requirement for email/password to use apps
- ❌ "Continue without account" must always be available
- ❌ Apps must work offline where possible
- ❌ No login wall before main functionality

#### Local-First Design
- ✅ All processing on device by default
- ✅ No cloud backend required for core features
- ✅ Optional cloud sync only for accounts that choose it
- ✅ All communication token-based and encrypted
- ✅ Revocable at any time

### Islamic Ethical Principles (Core Values)

**Amanah (Trust & Responsibility)**
- Build systems that deserve trust, not demand it
- Be transparent about every request
- Never exploit user data

**Consent (Rida)**
- Explicit permission for every capability
- Clear language about what is requested
- Users decide their protection level
- No dark patterns or manipulation

**Dignity (Karamah)**
- Respect user autonomy and choice
- Don't control when guidance suffices
- Don't force when offering is appropriate
- Treat users as decision-makers, not subjects

**Non-Oppression (Adl)**
- No single app dominates others
- All apps are equals in federation
- Power distributed, never centralized
- Users free to choose their path

**Mercy (Rahmah)**
- Design for guidance, not punishment
- Provide second chances and flexibility
- Allow recovery from mistakes
- Support without judgment

### Modular Independence Architecture

#### Package Namespace Structure
```
DeenShield Ecosystem
├── com.deenshield.guardian       (Central hub - Guardian)
├── com.deenshield.blocker        (Content filtering - DeenShield App)
└── org.alhaq.deenshield.netblock (Network control - NetBlock)
```

Each package is independently installable and maintains its own:
- Database (Room or SQLite)
- Preferences (SharedPreferences or DataStore)
- Services (Background processing)
- Permissions (Android manifests)

#### Isolation Boundaries
Apps communicate via:
- **Intents** - Fire-and-forget requests
- **Broadcast Receivers** - One-to-many notifications
- **Binder Services** - Synchronous capability queries
- **Content Providers** - Read-only data access (if needed)

**NOT via:**
- Direct method calls
- Shared memory/static variables
- Database sharing
- Package dependencies

### Permission Token Design

#### Token Structure
```kotlin
data class CapabilityToken(
    val grantedBy: String,           // Package that granted (e.g., "com.deenshield.guardian")
    val grantedTo: String,           // Package that received (e.g., "org.alhaq.deenshield.netblock")
    val capabilities: Set<String>,   // ["schedule_management", "time_limits"]
    val sessionId: String,           // Unique UUID for this grant
    val createdAt: Long,            // Timestamp when granted
    val expiresAt: Long,            // Expiration (e.g., +90 days)
    val encryptedData: ByteArray,   // AES-256 encrypted permissions
    val signature: String           // HMAC-SHA256 for integrity verification
)
```

#### Token Lifecycle
1. **Generation** - Guardian creates token when user approves
2. **Distribution** - Token sent securely to target app
3. **Storage** - Target app stores in encrypted local storage
4. **Validation** - Target app verifies signature and expiration before each use
5. **Expiration** - Token automatically invalid after expiration timestamp
6. **Revocation** - User revokes in Guardian or target app immediately invalidates
7. **Non-Renewal** - Expired tokens require fresh user approval

#### No Master Keys
- ❌ Guardian never has master control key
- ❌ Each token is independent
- ❌ Revoking one token doesn't affect others
- ❌ No backdoor authentication mechanism

### UX Principles (User Experience Design)

#### Clear & Honest Communication
- ✅ Explain what is being requested in plain language
- ✅ Describe why Guardian needs this capability
- ✅ Show what data will be accessed
- ✅ Explain consequences of Allow vs. Skip
- ✅ Avoid technical jargon; use user-friendly terms

#### Avoid Dark Patterns
- ❌ No pre-selected checkboxes
- ❌ No "Allow" button that's larger/brighter than "Skip"
- ❌ No urgent/scary language ("Your child is at risk!")
- ❌ No nagging notifications after user skips
- ❌ No hidden settings that force features

#### Guardian Feels Like Guidance, Not Control
- UI uses "suggestions" and "recommendations" language
- Users see suggestions but can override
- Failed overrides are shown as educational moments, not punishments
- Features feel like helpful coaching, not surveillance

#### Revocation Must Be Easy
- One-tap revocation from permission settings
- Immediate effect (no delays)
- Clear feedback that connection is stopped
- Option to re-enable later without complications

### Testing Requirements Before Merge

1. **Complete Standalone Operation**
   - Each app installs and works without others
   - All features function offline
   - No dependency errors if other apps aren't installed

2. **Permission Independence**
   - Skipping one permission doesn't revoke others
   - Users can allow multiple distinct permissions
   - Each permission works independently

3. **Token Expiration & Revocation**
   - Expired token immediately stops Guardian access
   - User revocation in Guardian stops all access
   - User revocation in app stops Guardian sync
   - Tokens cannot be extended indefinitely

4. **No Lock-In**
   - Users can uninstall Guardian without losing app data
   - Uninstalling target app doesn't affect Guardian
   - Repairing apps works after uninstall/reinstall
   - No cached credentials survive app removal

5. **Privacy Audit**
   - Zero network calls from Core or NetBlock without permission
   - Guardian never silently syncs data
   - All external communication is user-visible
   - DNS and traffic stays local in NetBlock

6. **Accessibility Safe**
   - Accessibility permissions never used for covert monitoring
   - Accessibility features work with permission model
   - No forced redirection based on Guardian rules
   - User can disable any blocking feature

### Scalability & Future Expansion

This architecture supports:
- **Individual Mode** - Single user with offline protection
- **Family Mode** - Parents managing children's devices
- **Organizational Mode** - Schools, mosques, enterprises
- **AI-Based Features** - Visual content detection (future)
- **Overlay Protection** - Content shielding (future)
- **Multi-Device Management** - Cross-device sync (optional)

**Without architectural redesign** because:
- Apps remain independent units
- New capabilities are just new permission tokens
- Communication stays local-first with optional cloud
- No single point of failure or control

### Integration Architecture
- **Standalone Apps:** DeenShield and NetBlock function independently with complete features
- **Guardian as Optional Hub:** Apps can pair with Guardian when users choose full ecosystem protection
- **Centralized Management:** When paired, Guardian provides unified configuration, parental controls, and organizational policies
- **User Choice:** Integration is optional and user-driven. Guardian is **recommended but not forced** - users decide if they want centralized management

## Authentication & Connection Architecture

### Three-Step Connection Model (All Optional)
DeenShield Guardian uses a permission-based, temporary trust model instead of permanent pairing. Users maintain autonomy at every step and can skip Guardian integration entirely.

#### **Step 1: Shared Identity (Optional)**
**Purpose:** Establish optional user identity
- **Email-Based Identity:** User creates account with email (cloud sync capable)
- **Local-Only Mode:** Username + password/PIN stored locally (no cloud)
- **Device ID Mode:** Auto-generated UUID, no account needed
- **"Continue Without Account":** ALWAYS available; users can skip identity entirely
- **User Controls:**
  - Choose identity method during first launch
  - Skip account creation and proceed as unidentified user
  - Switch identity modes later (with data export/import)
  - All identity modes work independently

**Key Principle:** Identity is optional. Apps work fully without any account.

#### **Step 2: Capability-Based Linking (Optional)**
**Purpose:** Grant specific permissions for Guardian integration
- **Permission-Based, Not Pairing:** Guardian requests specific capabilities via permission dialogs
- **Examples:**
  - "Allow DeenShield Guardian to manage App Access?" (Block list sync, access control)
  - "Allow DeenShield Guardian to manage Content Filtering?" (Filter configuration)
  - "Allow DeenShield Guardian to manage Productivity Settings?" (Time limits, schedules)
- **User Choices:**
  - **Allow:** Grant permission; app syncs with Guardian
  - **Skip:** Deny permission; app works locally only (user reminded of missing full protection benefits)
- **Granular Control:**
  - Users can allow some permissions, skip others
  - Permissions can be revoked anytime in settings
  - Each permission is independent; skipping one doesn't block others
- **No Lock-In:** Saying "Skip" doesn't prevent asking again later

**Key Principle:** Users decide which Guardian capabilities to use, not whether to use Guardian entirely.

#### **Step 3: Token-Based Connection (Behind the Scenes)**
**Purpose:** Establish temporary, revocable trust for verification
- **Encrypted Local Token:** Guardian generates encrypted token (AES-256)
- **Token Structure:**
  - Unique session ID (UUID)
  - Encrypted permission set
  - Expiration timestamp (e.g., 90 days)
  - HMAC signature for integrity
- **Token Exchange:**
  - Guardian sends token to protection apps via secure channel (local QR, NFC, encrypted Intent)
  - Apps store token locally in encrypted storage
  - Token verified before accepting Guardian commands
- **Temporary Trust:**
  - Token expires automatically; permanent dependency prevented
  - User must renew trust periodically (recommit to Guardian integration)
  - Token revocation available anytime in Guardian settings
  - Revoked token immediately invalidates connection
- **Revocation:**
  - User revokes in Guardian → token invalid
  - User revokes in protection app → app stops accepting Guardian commands
  - Apps continue working locally after revocation

**Key Principle:** "Temporary trust" model prevents ownership/control; users maintain ultimate autonomy.

### Connection Flow (User's Perspective)
```
Step 1: Choose Identity (or skip)
  ↓
Step 2: Grant Permissions (choose which ones)
  ↓
Step 3: Guardian generates token
  ↓
Apps accept token, sync with Guardian
  ↓
Full ecosystem protection active (user-chosen capabilities only)
  ↓
Anytime: User can revoke permissions, token expires, or switch modes
```

### Security & Privacy Principles
- **Local-First:** All processing happens on device by default
- **Explicit Consent:** Every capability requires user permission
- **Temporary Trust:** No permanent dependency or lock-in
- **Revocable:** User can disconnect anytime
- **Islamic/Ethical:** Respects user autonomy and choice; no hidden tracking or forced integration
- **End-to-End:** Token encryption ensures Guardian cannot intercept protection data
- **Minimal Trust:** Guardian only receives data for granted capabilities

### Implementation Guidance

**Step 1 Implementation:**
```kotlin
// User selects identity method at launch
enum class IdentityMode {
    EMAIL_ACCOUNT,      // Cloud sync capable
    LOCAL_ACCOUNT,      // Device-only, no cloud
    DEVICE_ID,          // No account, UUID-based
    NO_IDENTITY         // Skip entirely, remain unidentified
}
```

**Step 2 Implementation:**
```kotlin
// Granular permissions for Guardian integration
data class GuardianCapability(
    val id: String,                    // "manage_app_access", etc.
    val description: String,           // "Allow Guardian to manage App Access"
    val scope: PermissionScope,        // What data/control is granted
    val isGranted: Boolean,            // User's choice
    val grantedAt: Long?               // When permission was granted
)
```

**Step 3 Implementation:**
```kotlin
// Encrypted token for temporary trust
data class GuardianToken(
    val sessionId: String,             // Unique session UUID
    val encryptedPermissions: ByteArray, // AES-256 encrypted capability list
    val expiresAt: Long,              // Expiration timestamp (e.g., +90 days)
    val hmacSignature: String,         // HMAC-SHA256 for integrity
    val createdAt: Long               // Token creation time
)
```

### Testing Requirements
1. **Standalone Verification:** Each app works completely without Guardian
2. **Permission Granularity:** Users can grant subset of permissions
3. **Token Revocation:** Revoked token immediately stops Guardian sync
4. **Reauth Flow:** Expired token prompts user to renew (not automatic)
5. **Mode Switching:** Users can switch identity modes with data preservation
6. **Privacy Audit:** Verify no data sent to Guardian without explicit permission

## Architecture & Key Components

### Core Blocking Mechanisms
**VPN Service (BlockingVpnService):**
- Intercepts ALL network traffic via TUN interface (`addRoute("0.0.0.0", 0)`)
- `ContentFilter` performs deep packet inspection (DNS, HTTP headers, IP addresses)
- `PacketParser` decodes IP/TCP/UDP packets from TUN interface
- `DnsProxy` handles DNS query interception for domain filtering
- Stats tracked: packets processed, packets blocked, bytes processed

**App-Level Blocking (AccessibilityBlocker):**
- Monitors accessibility events to detect app launches
- Redirects forbidden apps to Home screen with Toast notification
- 2-second cooldown prevents notification spam
- Reads blocked app list from `UserPrefs` via Room database

**State Management (BlockViewModel):**
- Central Compose state holder; uses mutableStateOf for UI reactivity
- Maintains list of `Block` objects (each has: name, apps, websites, keywords, schedule)
- Predefined toggles: `blockHarmfulKeywords`, `blockHarmfulWebsites`, `blockSocialMedia`
- Sends Intent to VPN service when configuration changes (immediate effect)

### Persistence & Data Flow
```
UI (Compose Screens) 
  ↓ [BlockViewModel]
  ↓
Room Database (BlockRepository) ← Block entities
  ↓
Intent broadcast to services
  ↓
BlockingVpnService + AccessibilityBlocker
  ↓
Real-time enforcement
```

**Room Database:**
- `Block` entity: id, name, appIds, websites, keywords, weeklySchedule, limits
- `BlockRepository`: singleton managing CRUD operations

## Build & Deployment

### Build Commands
```powershell
# Full debug APK build
.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace

# Output location
# app\build\outputs\apk\debug\app-debug.apk

# Install on device/emulator
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

**Build Configuration:**
- `build.gradle.kts`: Kotlin DSL (modern Gradle)
- Plugins: AGP 8.13.2, Kotlin 2.0.21, Hilt DI, Compose, Room, Serialization
- Target: Java 17 / Kotlin jvmTarget 17
- Min API: 28, Target API: 35

### Dependencies (Notable)
- **Hilt:** Dependency injection (@HiltAndroidApp, @AndroidEntryPoint)
- **Compose:** Jetpack Compose BOM 2024.12.01; Material3 theme
- **Room:** Offline storage for Block configurations
- **Coroutines:** For async VPN packet processing (withContext Dispatchers.IO)
- **Serialization:** kotlinx-serialization for Block serialization
- **ML Kit:** Text recognition, language detection (optional future features)
- **Google Play Services:** Location, Maps, Biometrics

## Critical Architectural Patterns

### Service Communication
**Intent-Based Configuration Updates:**
- ViewModel calls `updateVpnServiceConfig()` → sends Intent with extras
- VPN service listens in `onStartCommand()` for action `ACTION_UPDATE_CONFIG`
- Extras: `EXTRA_BLOCKED_DOMAINS`, `EXTRA_BLOCK_SOCIAL_MEDIA`, `EXTRA_BLOCK_ADULT_CONTENT`
- Configuration immediately applied; no polling needed

### Thread Safety (VPN Packet Processing)
- `ContentFilter` fields marked `@Volatile` (atomic visibility)
- `updateConfiguration()` method marked `@Synchronized` (thread-safe mutations)
- VPN packet reading runs on `Dispatchers.IO` in serviceScope
- ByteBuffer (65KB) reused for each packet to avoid allocation overhead

### Resource Lifecycle
**VPN Service:**
```kotlin
onCreate() → createTunInterface() + startProcessingJob()
onDestroy() → closeVpnInterface() + cancelCoroutines() + stopForeground()
```
**Accessibility Service:**
- Binds to preferences Flow; collects updates when Block list changes
- Stops monitoring when `onDestroy()` called

### User Preferences (UserPrefs.kt)
- Stores user settings as Datastore/SharedPreferences
- ViewModel reads prefs for default toggles
- Accessible to both VPN and Accessibility services

## UI Navigation & Screens

### Main Navigation (Compose NavHost)
- **blocks** - BlocksScreen: Display all blocks in list; FloatingActionButton to add new
- **usage** - UsageScreen: Placeholder; shows network statistics
- **reports** - ReportsScreen: Placeholder; blocked content logs
- **add** - AddBlockScreen: Create/edit block with app picker, website list, keywords
- **weeklySchedule** - WeeklyScheduleScreen: Set time-based blocking rules
- **settings** - SettingsScreen: Toggle protection services, predefined block lists

### Key UI Components (Screens.kt)
- **BlocksScreen:** Lists all Block objects; shows counts (apps/websites/keywords)
- **ServiceControls:** Real-time status cards for VPN + Accessibility with color indicators
- **AddBlockScreen:** Form-based block creation with validation
- **InstalledAppsPickerDialog:** Material3 Card grid showing installed apps; multi-select with checkboxes
- **ChipsFlow:** Display selected items with delete chips

## Important Known Issues & Applied Fixes

### Critical Fixes (See CRITICAL_FIX_FOREGROUND_SERVICE.md & FIXES_APPLIED.md)
1. **VPN Loop Prevention:** Double negation bug fixed (`!isActive.not()` → `isActive`)
2. **Foreground Service Type (Android 14+):** Only `dataSync` allowed, NOT `location` (prevents SecurityException)
3. **Configuration Sync:** Custom blocks ignored until ViewModel sends Intent to VPN service
4. **Accessibility Service:** Device must explicitly enable via Settings > Accessibility
5. **App Picker UX:** Redesigned with Material3 Cards for better selection feedback

### Test Results
- ✅ VPN routes all traffic (tested with packet capture)
- ✅ Websites blocked at packet level (DNS interception works)
- ✅ Apps blocked via accessibility service (redirect + Toast)
- ✅ Blocks saved to Room database persist across app restarts
- ✅ Time-based schedules enforced (weekly schedule checked on app access)

## Testing Checklist

1. **Build & Install**
   ```powershell
   .\gradlew.bat :app:assembleDebug --no-daemon --stacktrace
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

2. **Enable Services**
   - Open app → Settings → Enable VPN (green status card appears)
   - Device Settings → Accessibility → Enable AccessibilityBlocker

3. **Create Block**
   - Tap + button → AddBlockScreen
   - Add app (e.g., Twitter) OR website (e.g., example.com)
   - Save

4. **Verify Blocking**
   - Try opening blocked app → Redirect to Home + Toast
   - Try visiting blocked website → VPN intercepts (timeout/no response)

5. **Check Persistence**
   - Restart app → Block still exists (Room database)

## File Structure
```
app/src/main/java/com/deenshield/blocker/
├── BlockerApplication.kt           # @HiltAndroidApp; notification channels
├── MainActivity.kt                 # Compose entry point; navigation scaffold
├── service/
│   ├── BlockingVpnService.kt      # VPN service; packet interception
│   ├── AccessibilityBlocker.kt    # App-level blocking
│   ├── BlockUtils.kt              # Validation helpers
│   └── network/
│       ├── ContentFilter.kt       # Domain/keyword matching
│       ├── PacketParser.kt        # IP/TCP/UDP parsing
│       └── DnsProxy.kt            # DNS query handling
├── viewmodel/
│   └── BlockViewModel.kt          # State management; service communication
├── data/
│   ├── BlockRepository.kt         # Room database access
│   └── UserPrefs.kt               # User preferences storage
├── model/
│   ├── Block.kt                   # Block entity
│   └── Scheduler.kt               # Schedule evaluation
├── ui/
│   ├── Screens.kt                 # All Compose screens
│   ├── theme/
│   │   └── BlockerTheme.kt        # Material3 theme
│   └── schedule/
│       └── WeeklyScheduleScreen.kt
└── di/
    └── AppModule.kt               # Hilt module; ContentFilter, PacketParser singletons
```

## Critical Safety Guidelines

### Data Protection & Local-Only Processing
- ⚠️ **Network Traffic Visibility:** VPN service intercepts ALL user network traffic. NEVER persist, log, or transmit traffic metadata without explicit user consent
- ⚠️ **Local-Only Traffic Processing:** ALL traffic routing, packet inspection, and DNS resolution MUST occur locally on device. NEVER send traffic through external servers, proxies, or third-party services
- ⚠️ **No Third-Party Infrastructure:** NEVER forward, relay, or analyze packets through cloud services, CDNs, or external proxies. All processing must be in-process on device
- ⚠️ **DNS Leak Prevention:** Ensure DNS queries NEVER leak to external servers (Google DNS, Cloudflare, etc.). DnsProxy must intercept and resolve locally or block entirely
- ⚠️ **No Network Callbacks:** VPN service MUST NOT make outbound network calls. All packet processing is CPU-local with zero external connectivity
- ⚠️ **No Telemetry or Analytics:** NEVER collect, aggregate, or send telemetry about traffic patterns, blocked domains, user behavior, or device state
- ⚠️ **User Blocking History:** Block list may reveal sensitive user information (sites, keywords they want to avoid). Protect with encryption at rest, stored locally only
- ⚠️ **Accessible Service Data:** Accessibility events contain app launch data; ensure UserPrefs storage is secure (no world-readable permissions)
- ⚠️ **Room Database:** All Block queries must validate input. Never expose database paths or internals via logs or exceptions. Keep database local and encrypted
- ⚠️ **DNS Proxy Data:** DNS queries are highly sensitive. Never cache, persist, or transmit DNS lookup history. Local resolution only

### Operational Safety
- ⚠️ **VPN Orphaning Risk:** Incomplete cleanup in `onDestroy()` can leave VPN active after uninstall. Must call `stopVpnService()` and `serviceScope.cancel()`
- ⚠️ **Foreground Service Type:** Android 14+ enforces strict `dataSync` type. NO `location`, `camera`, or other sensor types allowed
- ⚠️ **Intent-Based IPC:** Configuration Intents are not encrypted. NEVER put sensitive data in Intent extras; use secure storage instead
- ⚠️ **Room Migrations:** Block entity changes require proper `@Migration` definitions. Missing migrations cause app crashes on update
- ⚠️ **Coroutine Cleanup:** VPN service scope must be cancelled in `onDestroy()` to prevent resource leaks and background packet processing
- ⚠️ **File Descriptor Leaks:** `vpnInterface.close()`, `inputStream.close()`, `outputStream.close()` MUST be called. Leaked FDs consume device resources
- ⚠️ **Network Access Audit:** Verify ZERO network calls in `BlockingVpnService`, `ContentFilter`, `PacketParser`. All processing is CPU-local only
- ⚠️ **TUN Interface Security:** Ensure TUN interface processes packets locally. NEVER forward packets to external gateways, cloud services, or remote servers
- ⚠️ **Certificate Pinning:** If HTTPS inspection needed, use local certificate store only. NEVER download certificates from external Certificate Authorities
- ⚠️ **Packet Buffer Isolation:** ByteBuffer (65KB) must not be sent to external services. Process, discard, or block locally only

### Security Best Practices
- ⚠️ **ProGuard Rules:** Keep security-critical classes (`service.**, network.**`) from obfuscation
- ⚠️ **Accessibility Abuse:** AccessibilityBlocker can redirect ANY app. Add safeguards: never block critical apps (Phone, Settings, Emergency)
- ⚠️ **Configuration Injection:** Validate all block list entries (domains, keywords) before adding to ContentFilter to prevent ReDoS attacks
- ⚠️ **Manifest Permissions:** Ensure all required permissions are declared. Missing permissions cause silent failures at runtime

### Testing Requirements Before Merge
1. **Uninstall Test:** With VPN active, uninstall app → verify VPN stops (check Settings > VPN) within 2 seconds
2. **Device Restart:** Create blocks, enable VPN, restart → blocks + VPN must re-enable on boot
3. **Permission Removal:** Disable VPN permission in device settings → app must handle gracefully (show error, not crash)
4. **Storage Full:** Fill device storage to 100% → Room database operations must fail gracefully without data corruption
5. **Memory Pressure:** With VPN processing packets, trigger low-memory scenario → app must free buffers and continue
6. **Accessibility Denial:** Disable accessibility in device settings → AccessibilityBlocker must stop cleanly

## What NOT to Do
- ❌ Remove `addDisallowedApplication(packageName)` from VPN (causes infinite loop or traffic interception after uninstall)
- ❌ Set foreground service type to `location` on Android 14+ (causes SecurityException at runtime)
- ❌ Access VPN service state without checking Android version + permissions first
- ❌ Call service methods directly; always use Intent + extras for inter-process communication
- ❌ Update ContentFilter without @Synchronized (causes race conditions in packet processing)
- ❌ Bypass cooldown in AccessibilityBlocker (notification spam degrades UX + battery drain)
- ❌ Log user traffic patterns, DNS queries, or block history without consent
- ❌ Store encryption keys or sensitive config unencrypted in SharedPreferences
- ❌ Modify Block entity fields without creating Room migration (causes crash on app update)
- ❌ Block critical system apps (Phone, Settings, Emergency) via accessibility service
- ❌ Send block lists via unencrypted HTTP (blocks reveal sensitive user information)
- ❌ Modify Intent extras structure without backwards compatibility (breaks IPC)
- ❌ Leave VPN service running on app crash (implement watchdog to verify cleanup)
- ❌ Persist DNS/IP logs from packet processing (major privacy violation)
- ❌ Skip validate input on block entries (malicious regex can cause ReDoS)
- ❌ **Forward traffic packets to external DNS servers, proxies, VPN providers, or cloud services**
- ❌ **Send traffic metadata, DNS queries, or network data to analytics/telemetry/crash reporting services**
- ❌ **Add Firebase, Google Analytics, Mixpanel, Segment, or any third-party tracking SDKs**
- ❌ **Implement device fingerprinting, machine learning, or AI features requiring external servers**
- ❌ **Cache or persist DNS resolution history for any purpose other than local blocking**
- ❌ **Export traffic logs, packet captures, or network statistics to cloud storage (Google Drive, OneDrive, etc.)**
- ❌ **Use third-party ad networks, SDKs, ad frameworks (AdMob, Facebook Ads) in VPN or blocking services**
- ❌ **Create background sync, cloud backup, or remote configuration features for traffic/blocks**
- ❌ **Download block lists, filtering rules, or threat intelligence from external CDNs without hash verification**
- ❌ **Implement device remote management, control panels, or command-and-control features with external servers**
- ❌ **Allow root/ADB access tunneling through the VPN to external services**
- ❌ **Enable HTTP packet interception/MITM for traffic inspection (use DNS blocking instead)**

## Debugging Tips
- **VPN Not Starting:** Check foreground service permissions in AndroidManifest.xml; verify device Settings > Accessibility enabled; check `isActive` flag in logcat
- **Blocks Not Applied:** Verify Intent sent from ViewModel (`updateVpnServiceConfig()`); check ContentFilter initialization; verify `@Synchronized` method called
- **Notification Spam:** Verify AccessibilityBlocker cooldown (2s) is respected; check timestamp comparison logic
- **Room Database Issues:** Check entity/DAO annotations; ensure migrations defined if schema changes; verify database version incremented
- **VPN Loop:** Check `addDisallowedApplication()` is called for own package; verify `isActive` logic not inverted
- **Service Memory Leak:** Verify `serviceScope.cancel()` called in `onDestroy()`; check coroutine job cancellation
- **File Descriptor Leak:** Use `adb shell lsof | grep app_process` to see open FDs; verify all streams closed in cleanup
