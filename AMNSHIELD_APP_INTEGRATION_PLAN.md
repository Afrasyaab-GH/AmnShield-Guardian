# AmnShield Ecosystem Integration Plan

## 🎯 Executive Summary

**Objective:** Integrate the existing **AmnShield App** (main protection app, PlayStore published) into the **AmnShield Guardian** ecosystem while maintaining complete standalone functionality.

**Architecture:** Optional Federation Model
- AmnShield App functions 100% independently (current state)
- Guardian acts as optional central hub for management
- Apps communicate via Intent + encrypted capability tokens
- All integration is user-initiated and revocable

**Timeline:** 2-3 weeks (Phases 6-8 of project roadmap)

---

## 📊 Current Ecosystem Map

```
AmnShield Ecosystem (3 Independent Apps)
════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────┐
│                   AmnShield Guardian (Central Hub)             │
│                   org.alhaq.AmnShield.guardian                 │
│                                                                  │
│  ✅ Parental control interface                                  │
│  ✅ Organizational policy management                            │
│  ✅ Optional central configuration hub                          │
│  ✅ Permission-based linking to other apps                      │
│  ⏳ Phase 1: Core Authentication (In Progress)                 │
│  ⏳ Phase 2: Email Authentication (Planned)                    │
│  ⏳ Phase 3-5: Integration Ready (Planned)                     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴──────────┐
                    │                    │
                    ▼                    ▼
        ┌──────────────────┐   ┌──────────────────┐
        │  AmnShield App  │   │ AmnShield       │
        │  (Main Content   │   │ NetBlock         │
        │   Blocker)       │   │ (Network         │
        │                  │   │  Controller)     │
        │ com.alhaq.       │   │                  │
        │ AmnShield       │   │ org.alhaq.       │
        │                  │   │ AmnShield.      │
        │ ✅ Published on  │   │ netblock         │
        │   PlayStore      │   │                  │
        │ ✅ App blocking  │   │ ✅ App blocking  │
        │ ✅ Keyword       │   │ ✅ Network       │
        │   blocking       │   │    filtering     │
        │ ✅ Content       │   │ ✅ Per-app       │
        │   filtering      │   │    blocking      │
        │ ✅ Smart blur    │   │                  │
        │ ✅ Focus mode    │   │ ⏳ Guardian      │
        │ ✅ Reports       │   │   integration    │
        │                  │   │                  │
        │ ⏳ Guardian      │   │ ⏳ Guardian      │
        │   integration    │   │   integration    │
        └──────────────────┘   └──────────────────┘
        
Each app works independently 100%.
Guardian integration is optional - users decide if they want centralized management.
```

---

## 🔗 Integration Architecture (Optional Federation)

### Current State (Before Integration)

**AmnShield App (Standalone)**
```
User Interface
      ↓
SharedPreferences (Local Settings)
      ↓
Accessibility Service (AmnShieldAccessibilityService)
      ↓
Blockers (App, Keyword, View, Focus Mode)
      ↓
Device Actions (Redirect, Toast, Block)
```

**Guardian (Standalone, Under Development)**
```
Guardian UI
      ↓
Room Database (Blocks, Capabilities, Tokens)
      ↓
VPN Service + Accessibility Service
      ↓
Deep Packet Inspection + App-Level Blocking
```

### After Integration (Optional Federation)

```
AmnShield App Interface
      ↓
┌─────────────────────────────────┐
│ New: Guardian Connection Module  │
│                                  │
│ - Check if Guardian installed   │
│ - Query available capabilities  │
│ - Request capability tokens      │
│ - Verify token validity          │
│ - Sync configuration (if granted)│
└─────────────────────────────────┘
      ↓
Local Configuration (DEFAULT - Always works)
      ↓
      AND/OR (if Guardian permission granted)
      ↓
Guardian Configuration (Optional sync)
      ↓
Accessibility Service + VPN Service
      ↓
Merged Blocking Rules
      ↓
Device Actions
```

---

## 🏗️ Integration Points & Phase Breakdown

### Phase 6: AmnShield App Guardian Integration (Week 1-2)

#### 6.1 Add Guardian Connection Module to AmnShield App

**New Package:** `com.alhaq.AmnShield.guardian_integration`

```
guardian_integration/
├── GuardianConnectionManager.kt      # Check Guardian installed, query capabilities
├── CapabilityTokenValidator.kt       # Verify tokens from Guardian
├── ConfigurationSyncManager.kt       # Sync blocks/settings from Guardian
├── GuardianIPC.kt                    # Intent-based communication
└── models/
    ├── GuardianCapability.kt         # Mirror of Guardian model
    └── SyncConfiguration.kt          # Config to sync
```

#### 6.2 Modify Existing Blockers to Support Guardian Configuration

**Files to Update:**
```
blockers/AppBlocker.kt                # Add Guardian app list merge
blockers/KeywordBlocker.kt            # Add Guardian keywords merge
blockers/ViewBlocker.kt               # Add Guardian view blocker config
blockers/FocusModeBlocker.kt          # Add Guardian focus mode merge
```

**Pattern (Example for AppBlocker):**
```kotlin
// Get local config (always works)
val localBlockedApps = getBlockedAppsLocal()

// Get Guardian config (if available and permitted)
val guardianBlockedApps = getBlockedAppsFromGuardian()

// Merge (Guardian is additional, not replacement)
val mergedBlockedApps = localBlockedApps + guardianBlockedApps
```

#### 6.3 Update AmnShieldAccessibilityService

```kotlin
// In onServiceConnected()
guardianConnectionManager = GuardianConnectionManager(this)
configurationSyncManager = ConfigurationSyncManager(this, guardianConnectionManager)

// Listen for Guardian configuration updates
registerReceiver(guardianConfigUpdateReceiver, IntentFilter("guardian.config.updated"))

// In onAccessibilityEvent()
// Check both local and Guardian-synced blocks
```

---

### Phase 7: Guardian Integration Capability Server (Week 2-3)

#### 7.1 Enhance Guardian to Expose AmnShield App Capabilities

**Update Guardian's:** `di/AppModule.kt` and `service/BlockingVpnService.kt`

**New Capabilities to Expose:**
```kotlin
// In GuardianCapability.CommonCapabilities

MANAGE_AmnShield_APP_BLOCKING = GuardianCapability(
    id = "manage_AmnShield_app_blocking",
    displayName = "Manage AmnShield App Blocking",
    scope = CapabilityScope.APP_MANAGEMENT,
    description = "Centrally manage which apps are blocked in AmnShield",
    grantedCapabilities = setOf("add_app", "remove_app", "list_apps")
)

MANAGE_AmnShield_KEYWORDS = GuardianCapability(
    id = "manage_AmnShield_keywords",
    displayName = "Manage AmnShield Keywords",
    scope = CapabilityScope.CONTENT_FILTERING,
    description = "Centrally manage blocked keywords in AmnShield",
    grantedCapabilities = setOf("add_keyword", "remove_keyword", "list_keywords")
)

MANAGE_AmnShield_FOCUS_MODE = GuardianCapability(
    id = "manage_AmnShield_focus_mode",
    displayName = "Manage AmnShield Focus Mode",
    scope = CapabilityScope.PRODUCTIVITY,
    description = "Set focus mode schedules from Guardian",
    grantedCapabilities = setOf("create_session", "modify_schedule")
)
```

#### 7.2 Add Guardian Configuration Exposure via Intent

**Create:** `service/GuardianConfigurationService.kt`

```kotlin
class GuardianConfigurationService : Service() {
    
    override fun onBind(intent: Intent): IBinder? {
        return GuardianConfigBinder()
    }
    
    inner class GuardianConfigBinder : Binder() {
        fun getBlockedApps(capabilityToken: CapabilityToken): List<String> {
            // Verify token is valid and has permission
            if (!tokenValidator.isValid(capabilityToken, "manage_AmnShield_app_blocking")) {
                throw SecurityException("Token invalid or expired")
            }
            return blockRepository.getAllBlockedApps()
        }
        
        fun getBlockedKeywords(capabilityToken: CapabilityToken): List<String> {
            if (!tokenValidator.isValid(capabilityToken, "manage_AmnShield_keywords")) {
                throw SecurityException("Token invalid or expired")
            }
            return blockRepository.getAllBlockedKeywords()
        }
        
        fun subscribeToConfigChanges(listener: ConfigChangeListener) {
            // Notify AmnShield App when Guardian config changes
        }
    }
}
```

---

### Phase 8: End-to-End Integration & Testing (Week 3)

#### 8.1 User Flow: AmnShield App + Guardian Integration

**User Journey:**
```
1. User opens AmnShield App
   ↓
2. Settings → "Link with Guardian" (NEW)
   ↓
3. AmnShield requests Guardian permission
   ↓
4. Guardian not installed?
   → User can install from PlayStore OR continue offline
   ↓
5. Guardian installed?
   → Show "Request Permissions" dialog
   ↓
6. User selects permissions:
   ☐ Sync blocked apps
   ☐ Sync blocked keywords
   ☐ Sync focus mode settings
   ☐ Sync content filters
   ↓
7. Guardian creates capability token
   ↓
8. AmnShield App stores token
   ↓
9. AmnShield App periodically:
   - Verifies token validity
   - Syncs Guardian configuration
   - Merges with local settings
   - Applies merged blocks
   ↓
10. User can unlink anytime → revoke token → back to offline mode
```

#### 8.2 Testing Scenarios

**Scenario 1: Standalone Mode**
```
✓ Install AmnShield App
✓ Configure blocks locally
✓ Blocking works without Guardian
✓ All features accessible
✓ No crashes if Guardian not installed
```

**Scenario 2: Guardian Integration (Optional)**
```
✓ Install AmnShield App
✓ Install Guardian
✓ Link AmnShield App to Guardian
✓ Grant permissions
✓ Guardian blocks appear in AmnShield
✓ Local blocks + Guardian blocks merged
✓ Blocking works with combined rules
✓ Guardian config changes sync to AmnShield
```

**Scenario 3: Permission Revocation**
```
✓ User revokes Guardian permission
✓ AmnShield App immediately uses local config only
✓ Guardian blocks disappear from AmnShield
✓ No crashes, graceful fallback
✓ Local settings preserved
```

**Scenario 4: Guardian Uninstall**
```
✓ Uninstall Guardian
✓ AmnShield App detects Guardian missing
✓ Falls back to local config
✓ No crashes
✓ Blocks continue from local settings
```

---

## 💾 Data Flow: Configuration Sync

### Scenario: Guardian Blocks App, AmnShield Syncs It

```
Step 1: User blocks app in Guardian
        │
        ├→ Guardian stores in Room Database
        └→ Broadcasts: "guardian.config.updated"
        │
Step 2: AmnShield App receives broadcast
        │
        ├→ Verifies capability token
        ├→ Queries Guardian service for blocked apps
        └→ Gets encrypted list
        │
Step 3: AmnShield App merges configurations
        │
        ├→ Local blocks: [App1, App2]
        ├→ Guardian blocks: [App3, App4]
        └→ Merged: [App1, App2, App3, App4]
        │
Step 4: AmnShield Accessibility Service uses merged list
        │
        └→ All apps blocked (local + Guardian)
```

---

## 🔐 Security & Privacy Considerations

### 1. Token Validation
- AmnShield App validates Guardian token before each sync
- Token expiration enforced (90 days default)
- Invalid token → graceful fallback to local config
- Token revocation checked on every operation

### 2. Configuration Encryption
- Guardian blocks encrypted in transit (via encrypted Intent extras)
- AmnShield App stores Guardian config encrypted (same EncryptedSharedPreferences pattern)
- Double encryption: Guardian's Keystore + AmnShield's EncryptedSharedPreferences

### 3. No Master Keys
- Guardian cannot force AmnShield to block anything
- AmnShield always maintains local control
- User can disable Guardian sync anytime
- Local blocks always active (Guardian is supplementary)

### 4. No Data Exfiltration
- Guardian only shares configuration, not traffic data
- AmnShield only requests what user permitted
- No analytics or telemetry between apps
- Blocked domains/keywords stay local

### 5. Permission Scope Enforcement
- AmnShield only accesses capabilities it requested
- Guardian token specifies exact permissions
- Attempting access without permission → error
- Audit logs (local only) track sync operations

---

## 📋 Implementation Checklist

### Phase 6: AmnShield App Integration (Week 1-2)

**Week 1 - Guardian Connection:**
- [ ] Create `GuardianConnectionManager.kt`
  - [ ] Detect if Guardian installed
  - [ ] Query Guardian for available capabilities
  - [ ] Request capability tokens
  - [ ] Handle Guardian not installed case
  
- [ ] Create `CapabilityTokenValidator.kt`
  - [ ] Verify token signature
  - [ ] Check token expiration
  - [ ] Verify token permissions
  - [ ] Graceful handling of invalid tokens

- [ ] Create `GuardianIPC.kt`
  - [ ] Define Intent actions
  - [ ] Create request/response models
  - [ ] Implement encrypted communication
  - [ ] Error handling

**Week 1 - Blocker Updates:**
- [ ] Update `AppBlocker.kt`
  - [ ] Merge local + Guardian blocks
  - [ ] Prioritize user safety (more blocks = more safe)
  
- [ ] Update `KeywordBlocker.kt`
  - [ ] Merge local + Guardian keywords
  - [ ] Handle overlaps
  
- [ ] Update `ViewBlocker.kt`
  - [ ] Sync Guardian view blocker config
  
- [ ] Update `FocusModeBlocker.kt`
  - [ ] Merge focus mode settings

**Week 2 - Service Integration:**
- [ ] Update `AmnShieldAccessibilityService.kt`
  - [ ] Initialize Guardian connection
  - [ ] Listen to Guardian config updates
  - [ ] Merge configurations on update
  - [ ] Handle Guardian availability changes
  
- [ ] Update `MainActivity.kt`
  - [ ] Add "Guardian Integration" settings
  - [ ] Show Guardian link status
  - [ ] Implement "Link Guardian" flow
  - [ ] Implement "Unlink Guardian" flow

- [ ] Create `receivers/GuardianConfigUpdateReceiver.kt`
  - [ ] Listen for Guardian config changes
  - [ ] Trigger configuration sync
  - [ ] Notify blockers of updates

### Phase 7: Guardian Capability Server (Week 2-3)

**Week 2 - Guardian Enhancement:**
- [ ] Add AmnShield app capabilities to `GuardianCapability.kt`
  - [ ] MANAGE_AmnShield_APP_BLOCKING
  - [ ] MANAGE_AmnShield_KEYWORDS
  - [ ] MANAGE_AmnShield_FOCUS_MODE
  - [ ] MANAGE_AmnShield_VIEW_BLOCKER

- [ ] Create `GuardianConfigurationService.kt`
  - [ ] Implement configuration exposure via Binder
  - [ ] Verify tokens on every access
  - [ ] Provide blocked apps list
  - [ ] Provide blocked keywords list
  - [ ] Provide focus mode config

**Week 3 - Configuration Broadcasting:**
- [ ] Update `BlockViewModel.kt` (Guardian)
  - [ ] Broadcast config updates to AmnShield
  - [ ] Action: "guardian.config.updated"
  - [ ] Include update type (apps, keywords, etc.)

- [ ] Add configuration change listeners
  - [ ] Detect when blocks change
  - [ ] Notify listening apps

### Phase 8: Testing & Finalization (Week 3)

- [ ] Unit tests for Guardian connection
- [ ] Integration tests for configuration sync
- [ ] End-to-end testing of all scenarios
- [ ] Manual testing on multiple devices
- [ ] Documentation updates
- [ ] Code review

---

## 📱 UI Changes Required

### AmnShield App Settings Screen (New Section)

**Add to Settings/Fragment:**
```
┌─────────────────────────────────────┐
│        Guardian Integration         │
├─────────────────────────────────────┤
│                                     │
│ 🔗 Link with Guardian               │
│    Status: Not Linked               │
│    [Link Guardian]                  │
│                                     │
│    OR                               │
│                                     │
│    Status: Linked ✓                 │
│    Connected to Guardian            │
│    Token Expires: Jan 25, 2027      │
│                                     │
│    ☐ Sync Blocked Apps              │
│    ☐ Sync Blocked Keywords          │
│    ☐ Sync Focus Mode                │
│                                     │
│    [Unlink Guardian]                │
│                                     │
└─────────────────────────────────────┘
```

### Guardian Capability Request Dialog

**What Guardian Shows:**
```
┌────────────────────────────────────────────┐
│  AmnShield App Requests Access            │
├────────────────────────────────────────────┤
│                                            │
│  AmnShield wants to:                      │
│                                            │
│  ☐ Sync Blocked Apps                      │
│     "Allow AmnShield to use your          │
│      app blocking rules"                   │
│                                            │
│  ☐ Sync Blocked Keywords                  │
│     "Allow AmnShield to use your          │
│      keyword blocking rules"               │
│                                            │
│  ☐ Sync Focus Mode                        │
│     "Allow AmnShield to use your          │
│      focus mode schedules"                 │
│                                            │
│  □ I understand this allows AmnShield    │
│    to sync your rules. You can unlink     │
│    at any time.                           │
│                                            │
│  [   Cancel   ]     [   Allow   ]          │
│                                            │
└────────────────────────────────────────────┘
```

---

## 🧪 Testing Matrix

| Scenario | AmnShield Standalone | With Guardian | Guardian Uninstalled | Token Revoked |
|----------|---|---|---|---|
| App Blocker | ✓ Works | ✓ Merged | ✓ Fallback | ✓ Fallback |
| Keyword Blocker | ✓ Works | ✓ Merged | ✓ Fallback | ✓ Fallback |
| View Blocker | ✓ Works | ✓ Merged | ✓ Fallback | ✓ Fallback |
| Focus Mode | ✓ Works | ✓ Merged | ✓ Fallback | ✓ Fallback |
| Local Config | ✓ Works | ✓ Works | ✓ Works | ✓ Works |
| App Launch | ✓ Works | ✓ Works | ✓ Works | ✓ Works |
| Settings Access | ✓ Works | ✓ Works | ✓ Works | ✓ Works |
| Performance | ✓ Normal | ✓ +10% | ✓ Normal | ✓ Normal |

---

## 📊 Project Impact Summary

### Before Integration
```
AmnShield App: Standalone, Full-Featured
Guardian: Standalone, Parental Control
NetBlock: Standalone, Network Control

❌ No central management
❌ No cross-app coordination
❌ Users must configure each app separately
```

### After Integration
```
AmnShield App: Full-Featured + Optional Guardian Sync
Guardian: Central Hub + AmnShield App Management
NetBlock: Optional Guardian Management

✅ Central management for all 3 apps
✅ Cross-app coordination when Guardian linked
✅ Single point of configuration (optional)
✅ Complete backward compatibility (apps work standalone)
✅ User choice: use individually or together
✅ Islamic principles maintained (user control)
```

---

## 🎯 Success Criteria (Phase 6-8)

**Functional:**
- ✅ AmnShield App works 100% standalone (no regression)
- ✅ Guardian app works 100% standalone
- ✅ Guardian integration optional (both apps ask for permission)
- ✅ Configuration sync works in both directions
- ✅ Token validation enforced
- ✅ Graceful fallback if Guardian unavailable

**Security:**
- ✅ No plaintext communication between apps
- ✅ Tokens verified on every access
- ✅ Invalid tokens cause graceful fallback
- ✅ No data leaks between apps
- ✅ User privacy maintained

**User Experience:**
- ✅ Simple "Link Guardian" flow
- ✅ Clear permission dialogs
- ✅ Status shows Guardian connection
- ✅ Easy unlink option
- ✅ No crashes or errors

**Code Quality:**
- ✅ No external dependencies added
- ✅ Unit tests (80%+ coverage)
- ✅ Integration tests
- ✅ Documentation complete
- ✅ Code reviewed and approved

---

## 📅 Timeline

```
Week 1 (Jan 27-31)      Phase 6: AmnShield App Integration
    ├─ Day 1-2: Guardian Connection Module
    ├─ Day 3-4: Blocker Updates
    └─ Day 5: Service Integration

Week 2 (Feb 3-7)        Phase 7: Guardian Enhancement
    ├─ Day 1-2: Guardian Capabilities
    ├─ Day 3: Configuration Service
    └─ Day 4: Broadcasting

Week 3 (Feb 10-14)      Phase 8: Testing & Release
    ├─ Day 1-2: Comprehensive Testing
    ├─ Day 3: Documentation
    └─ Day 4: Code Review & Release
```

---

## 🚀 Next Steps

1. **Immediate (Next 24 hours):**
   - Finalize this integration plan
   - Get team approval
   - Create feature branches

2. **Week 1:**
   - Start Phase 6 implementation
   - Create Guardian connection module
   - Update blockers

3. **Week 2:**
   - Enhance Guardian with capabilities
   - Implement configuration service

4. **Week 3:**
   - Complete testing
   - Release integration

---

**Prepared:** January 24, 2026  
**Status:** 🟢 **READY FOR IMPLEMENTATION**  
**Dependencies:** Phase 1 Guardian Auth (In Progress)  
**Next Review:** Upon Phase 1 completion  
**Contact:** Development Team
