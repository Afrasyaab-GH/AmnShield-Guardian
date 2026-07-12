# AmnShield Guardian - Implementation Roadmap

## Overview
Building the AmnShield Guardian ecosystem based on Optional Federation Architecture with Islamic ethical values.

## Project Architecture
```
com.alhaq.amnshield.guardian/
├── BlockerApplication.kt           # @HiltAndroidApp; notification channels
├── MainActivity.kt                 # Compose entry point; navigation scaffold
├── auth/                           # NEW: Authentication module
│   ├── model/
│   │   ├── IdentityMode.kt
│   │   ├── GuardianCapability.kt
│   │   ├── CapabilityToken.kt
│   │   └── AuthState.kt
│   ├── local/
│   │   ├── LocalAccountManager.kt
│   │   ├── DeviceIdManager.kt
│   │   └── TokenStorage.kt
│   ├── remote/
│   │   ├── EmailAuthService.kt
│   │   └── CloudAuthApi.kt
│   ├── ui/
│   │   ├── AuthScreen.kt
│   │   ├── IdentityModeSelector.kt
│   │   ├── CapabilityPermissionDialog.kt
│   │   └── PermissionSettings.kt
│   └── viewmodel/
│       └── AuthViewModel.kt
├── ipc/                            # NEW: Inter-Process Communication
│   ├── CapabilityBinder.kt
│   ├── CapabilityBroadcaster.kt
│   └── TokenVerifier.kt
├── service/
│   ├── BlockingVpnService.kt      # Existing
│   ├── AccessibilityBlocker.kt    # Existing
│   ├── GuardianConnectionService.kt # NEW: Manages app connections
│   └── CapabilityExecutor.kt       # NEW: Executes granted capabilities
├── data/
│   ├── BlockRepository.kt         # Existing
│   ├── UserPrefs.kt               # Existing
│   ├── PermissionRepository.kt     # NEW: Manages permissions
│   ├── TokenRepository.kt          # NEW: Manages tokens
│   └── IdentityRepository.kt       # NEW: Manages identity modes
├── viewmodel/
│   ├── BlockViewModel.kt          # Existing
│   └── AuthViewModel.kt            # NEW: Auth state management
├── ui/
│   ├── Screens.kt                 # Existing
│   └── AuthScreens.kt             # NEW: Auth UI screens
└── di/
    └── AppModule.kt               # Existing; add new providers
```

## Implementation Phases

### Phase 1: Core Authentication Model & Local Account (Weeks 1-2)
**Goal:** Foundational authentication system with local-only account support

**Deliverables:**
- [ ] Create `auth/model/` package with:
  - `IdentityMode.kt` - Enum for EMAIL_ACCOUNT, LOCAL_ACCOUNT, DEVICE_ID, NO_IDENTITY
  - `GuardianCapability.kt` - Data class for capability permissions
  - `CapabilityToken.kt` - Encrypted token structure
  - `AuthState.kt` - State representation

- [ ] Create `auth/local/LocalAccountManager.kt`:
  - Store username + hashed password (PBKDF2WithHmacSHA256)
  - Use Android Keystore for encryption keys
  - Validate credentials locally
  - No network calls

- [ ] Create `auth/local/TokenStorage.kt`:
  - Encrypted storage for capability tokens
  - AES-256 encryption using Keystore-derived keys
  - Token validation (signature + expiration)

- [ ] Create `PermissionRepository.kt`:
  - Room database for granted permissions
  - Track which capabilities are granted
  - Track grant timestamps and expiration

- [ ] Create `AuthViewModel.kt`:
  - State management for login/identity selection
  - Local account creation/validation
  - Account mode switching

- [ ] Update `di/AppModule.kt`:
  - Add Hilt providers for LocalAccountManager, TokenStorage, PermissionRepository

**Tests:**
- [ ] LocalAccountManager: password hashing, verification, edge cases
- [ ] TokenStorage: encryption/decryption, corruption handling
- [ ] AuthViewModel: state transitions, error handling

---

### Phase 2: Device ID & Permission UI (Weeks 3-4)
**Goal:** Device-based identification and capability permission dialogs

**Deliverables:**
- [ ] Create `auth/local/DeviceIdManager.kt`:
  - Generate UUID on first launch
  - Persistent storage in encrypted SharedPreferences
  - QR code generation: `{deviceId, publicKey, timestamp, signature}`

- [ ] Create `auth/model/QrPairingData.kt`:
  - Structure for QR code pairing payload
  - Signature generation (HMAC-SHA256)

- [ ] Create `auth/ui/IdentityModeSelector.kt` (Compose):
  - Material3 cards for each identity mode
  - Descriptions of privacy/convenience tradeoffs
  - "Continue without account" button always visible

- [ ] Create `auth/ui/CapabilityPermissionDialog.kt` (Compose):
  - Shows capability description (plain language)
  - Lists data being accessed
  - Allow / Skip buttons (equal sizing)
  - No dark patterns

- [ ] Create `auth/ui/PermissionSettings.kt` (Compose):
  - Shows connected apps
  - List of granted capabilities per app
  - One-tap revocation buttons
  - Immediate effect feedback

- [ ] Create `IdentityRepository.kt`:
  - Room database for identity preferences
  - Track selected identity mode

**Tests:**
- [ ] DeviceIdManager: UUID uniqueness, QR generation
- [ ] CapabilityPermissionDialog: state transitions, Allow/Skip logic
- [ ] Revocation: immediate effect, permission isolation

---

### Phase 3: Token Generation & IPC Communication (Weeks 5-6)
**Goal:** Secure token management and inter-app communication

**Deliverables:**
- [ ] Create `auth/token/TokenGenerator.kt`:
  - Generate unique session IDs (UUID)
  - Encrypt permission set using AES-256
  - Sign token with HMAC-SHA256
  - Set expiration (90 days default)

- [ ] Create `ipc/TokenVerifier.kt`:
  - Verify HMAC signature
  - Check expiration
  - Validate against stored permissions
  - Return revoked status

- [ ] Create `ipc/CapabilityBinder.kt`:
  - Hilt-injected service implementing IInterface
  - Receive capability requests from remote apps
  - Verify token before processing
  - Log all requests (for audit)

- [ ] Create `ipc/CapabilityBroadcaster.kt`:
  - Send configuration updates via Intent
  - Include encrypted token in broadcast
  - Handle broadcast failures gracefully

- [ ] Create `service/GuardianConnectionService.kt`:
  - Listens for app connection requests
  - Manages active connections
  - Revokes expired tokens
  - Broadcasts updates to connected apps

- [ ] Create `service/CapabilityExecutor.kt`:
  - Executes capabilities from remote apps
  - Enforces permission checks
  - Returns results via callback

**Tests:**
- [ ] TokenGenerator: encryption/decryption, signature validity
- [ ] TokenVerifier: expiration detection, corruption handling
- [ ] CapabilityBinder: IPC communication, token validation
- [ ] Token revocation: immediate effect across apps

---

### Phase 4: Email-Based Identity & Cloud Sync (Weeks 7-8)
**Goal:** Multi-device capability with optional cloud sync

**Deliverables:**
- [ ] Create `auth/remote/EmailAuthService.kt`:
  - Email verification flow
  - Password requirements enforcement
  - JWT token generation

- [ ] Create `auth/remote/CloudAuthApi.kt` (Retrofit):
  - Registration endpoint
  - Login endpoint
  - Token refresh endpoint
  - Configuration sync endpoints

- [ ] Create `auth/ui/EmailAuthScreen.kt` (Compose):
  - Email input with validation
  - Password input with strength indicator
  - Verification code input
  - "Continue without email" option

- [ ] Create cloud sync for Block configurations:
  - End-to-end encryption before upload
  - Decryption on download
  - Conflict resolution (device/cloud)

- [ ] Update `AuthViewModel.kt`:
  - Handle email registration/login
  - Manage JWT tokens
  - Handle network errors gracefully

**Tests:**
- [ ] Email validation, password requirements
- [ ] JWT token refresh
- [ ] Configuration encryption/decryption
- [ ] Sync conflict resolution

---

### Phase 5: Complete Auth UI Integration & Testing (Weeks 9-10)
**Goal:** Seamless first-launch experience and comprehensive testing

**Deliverables:**
- [ ] Create first-launch flow:
  - Skip tutorial → Identity mode selector
  - Local account → username/password setup
  - Device ID → QR display + pairing instructions
  - Email → registration + verification
  - No account → generate UUID + proceed

- [ ] Integrate auth screens into MainActivity navigation:
  - Replace current settings with new auth flow
  - Add permission settings screen
  - Add connected apps view

- [ ] Create `util/AuthStatePreserver.kt`:
  - Save state when app backgrounded
  - Restore state on resume
  - Handle identity mode switching

- [ ] Complete testing suite:
  - Standalone operation (all apps work without Guardian)
  - Permission independence (skip one doesn't revoke others)
  - Token lifecycle (generation, expiration, revocation)
  - Multi-app communication
  - UI/UX flows

**Tests:**
- [ ] Complete standalone functionality
- [ ] Multi-app communication with tokens
- [ ] UI flow: identity → capabilities → confirmation
- [ ] Token expiration & revocation
- [ ] Mode switching with data preservation

---

## Dependencies to Add

```gradle
// Security
implementation("androidx.security:security-crypto:1.1.0-alpha06")  // Android Keystore
implementation("org.mindrot:jbcrypt:0.4")                         // PBCRYPT

// Encryption
implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")  // AES encryption

// QR Code
implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
implementation("io.github.g00fy2:qrcode-kotlin:3.0.0")  // QR generation

// JWT/Auth (if using Email mode)
implementation("com.auth0.android:jwtdecode:2.0.1")    // JWT decoding
implementation("io.jsonwebtoken:jjwt-api:0.11.5")     // JWT handling

// NSD (Network Service Discovery)
// Built-in Android API, no dependency needed
```

---

## Critical Implementation Notes

### Security & Privacy
- ✅ All encryption keys stored in Android Keystore
- ✅ No sensitive data in Intent extras (use encrypted tokens)
- ✅ Tokens must expire (not permanent)
- ✅ Token revocation must be immediate
- ✅ All IPC must use Binder/Intent (device-local only)
- ✅ NEVER send network requests from VPN service

### User Autonomy
- ✅ "Continue without account" always available
- ✅ Each permission can be skipped independently
- ✅ Revocation UI must be prominent and easy
- ✅ No dark patterns (no pre-checked boxes, equal button sizes)
- ✅ Guardian feels like guidance, not control

### Islamic Ethical Principles
- ✅ Amanah: Transparent about every request
- ✅ Consent: Explicit permission for every capability
- ✅ Dignity: Treat users as decision-makers
- ✅ Adl: No app dominates others; all are peers
- ✅ Rahmah: Design for guidance, provide flexibility

---

## Success Criteria

### Phase 1 Complete
- [ ] Local account creation/login works offline
- [ ] Tokens stored encrypted and validated
- [ ] AuthViewModel manages state correctly
- [ ] All unit tests pass

### Phase 2 Complete
- [ ] Device ID mode generates unique identifiers
- [ ] QR codes display correctly
- [ ] Permission dialogs are clear and honest
- [ ] Revocation UI works and has immediate effect

### Phase 3 Complete
- [ ] Tokens generated, encrypted, and signed correctly
- [ ] IPC communication works between Guardian and apps
- [ ] Token verification prevents unauthorized access
- [ ] Token expiration prevents permanent lock-in

### Phase 4 Complete
- [ ] Email registration/login works with JWT
- [ ] Cloud sync encrypts data end-to-end
- [ ] Configuration syncs across devices
- [ ] Network errors handled gracefully

### Phase 5 Complete
- [ ] Complete first-launch experience
- [ ] All apps work standalone
- [ ] Multi-app communication tested
- [ ] Privacy audit passes
- [ ] UI/UX feels like guidance, not control

---

## Build & Test Commands

```powershell
# Build
.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace

# Unit tests
.\gradlew.bat :app:testDebugUnitTest --no-daemon --stacktrace

# Integration tests
.\gradlew.bat :app:connectedAndroidTest --no-daemon --stacktrace

# Install
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## Next Steps

1. **NOW:** Review this roadmap with team
2. **TODAY:** Start Phase 1 - Create auth/model/ classes
3. **WEEK 1:** Complete LocalAccountManager + AuthViewModel
4. **WEEK 2:** Complete TokenStorage + PermissionRepository + tests
