# 🌍 AmnShield Ecosystem: Complete Vision & Integration Roadmap

> ⚠️ **ARCHITECTURAL SHIFT NOTICE (July 2026):** The native Android **AmnShield Guardian** app component outlined in this roadmap has been **suspended/frozen** in favor of a centralized **Web Administration Console** (React/Next.js + Supabase). The local client-side IPC structure remains available in `AmnShield-Android` via `IAmnShieldApi`, but child-to-parent control synchronization will now occur securely over HTTPS REST APIs against the central database rather than native Android-to-Android IPC bindings.


## 📊 Ecosystem Overview

**AmnShield** is a comprehensive **Islamic-values-aligned** digital wellbeing and protection ecosystem for the Muslim community and wider world.

```
AmnShield Ecosystem (3 Integrated Apps + Central Management)
══════════════════════════════════════════════════════════════════════

                    ┌─────────────────────────────────┐
                    │  Guardian (Central Management)  │
                    │  com.alhaq.amnshield.guardian  │
                    │                                 │
                    │  ✅ Parental Control Hub        │
                    │  ✅ Organizational Policies     │
                    │  ✅ Family Management           │
                    │  ✅ Multi-Device Sync           │
                    │  ✅ Permission Management       │
                    │  ✅ Centralized Configuration   │
                    │  ✅ Authentication System       │
                    │                                 │
                    └────────────┬────────────────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
                ▼                ▼                ▼
    ┌────────────────────┐ ┌──────────────┐ ┌──────────────┐
    │   AmnShield App   │ │  AmnShield  │ │  AmnShield  │
    │  (Main Protection) │ │   NetBlock   │ │   Guardian   │
    │ com.alhaq.         │ │ com.alhaq.   │ │   (Optional  │
    │ AmnShield         │ │ AmnShield.  │ │    Central   │
    │                    │ │ netblock     │ │   Hub)       │
    │ ✅ Published       │ │              │ │              │
    │ ✅ PlayStore Ready │ │ ✅ Mature    │ │ ⏳ In Dev   │
    │                    │ │ ✅ Features  │ │              │
    │ Features:          │ │              │ │              │
    │ • App Blocking     │ │ Features:    │ │ Features:    │
    │ • Keyword Blocking │ │ • Per-app    │ │ • Central    │
    │ • Reel Blocking    │ │   blocking   │ │   management │
    │ • Smart Blur       │ │ • WiFi ctrl  │ │ • Reporting  │
    │ • Focus Mode       │ │ • DNS filter │ │ • Policies   │
    │ • Reports          │ │ • No-root    │ │ • Sync       │
    │ • Premium          │ │              │ │ • Auth       │
    │ • Smart Features   │ │              │ │              │
    │                    │ │              │ │              │
    │ ⏳ Guardian        │ │ ⏳ Guardian   │ │              │
    │   Integration      │ │   Integration│ │              │
    │   (In Progress)    │ │   (Planned)  │ │              │
    │                    │ │              │ │              │
    └────────────────────┘ └──────────────┘ └──────────────┘
```

---

## 🎯 Three-Tier Integration Architecture

### Tier 1: Standalone Mode (Current)
```
AmnShield App
├─ Works 100% independently
├─ All features enabled offline
├─ Local configuration only
├─ No Guardian dependency
└─ User data stays on device
```

### Tier 2: Guardian Integration (In Development)
```
AmnShield App + Guardian (Optional Pairing)
├─ AmnShield works standalone + syncs with Guardian
├─ User controls sync via permissions
├─ Guardian configuration merged with local
├─ Temporary tokens (expiring, revocable)
├─ User can unlink anytime
└─ All data stays local
```

### Tier 3: Multi-Device Management (Planned)
```
AmnShield Ecosystem (All 3 Apps + Guardian)
├─ Centralized management across devices
├─ Family/organizational policies
├─ Optional cloud backup (encrypted)
├─ User maintains ultimate control
├─ Can opt-out at any stage
└─ Complete offline capability
```

---

## 📋 Current Status Matrix

| Component | Status | Role | Timeline |
|-----------|--------|------|----------|
| **AmnShield App** | ✅ Published (PlayStore) | Main protection | Ready now |
| **Guardian Core** | ⏳ Phase 1 Auth | Central hub | Week 1-2 |
| **Guardian IPC** | ⏳ Phase 3-5 | App communication | Week 2-3 |
| **AmnShield Integration** | ⏳ Phase 6-8 | App linking | Week 3-4 |
| **NetBlock Integration** | 📋 Planned | Network control | Week 4-5 |
| **Cloud Sync** | 📋 Phase 9+ | Optional backup | Month 2+ |

---

## 🔐 Islamic Principles Throughout Ecosystem

### ✅ Amanah (Trust & Responsibility)
**Definition:** Bearer of trust deserves it through transparency and strong protection

**Implementation:**
- AmnShield: Transparent encryption methods documented in code
- Guardian: Clear explanation of what data is accessed
- All three apps: No hidden data collection, full KDoc documentation
- Ecosystem: Users see exactly what's happening

**Evidence:**
```
LocalAccountManager.kt    - Full KDoc on PBKDF2 security
TokenStorage.kt          - Detailed encryption documentation
Guardian IPC             - Intent-based, auditable communication
AmnShield Integration   - Explicit permission dialogs
```

### ✅ Rida (Consent)
**Definition:** Everything requires explicit, informed user consent

**Implementation:**
- AmnShield: Explicit "Link Guardian" button (not automatic)
- Guardian: Permission dialogs for each capability
- Tokens: Explicit grant, explicit revocation
- Configuration: User chooses what syncs

**Evidence:**
```
Tier 1: Works completely standalone (no forcing)
Tier 2: User explicitly "Links Guardian"
        User explicitly selects capabilities
        User can "Unlink" anytime
No automatic pairing, no forced integration
```

### ✅ Karamah (Dignity & Autonomy)
**Definition:** Respect human autonomy and decision-making

**Implementation:**
- Each app independent (user chooses to use or not)
- No master control (Guardian is supplementary)
- Local control (user data stays on device)
- Revocation anytime (user can undo any decision)

**Evidence:**
```
AmnShield: Works without Guardian
Guardian: Optional (not required)
NetBlock: Independent (not forced)
Ecosystem: User decides integration level
```

### ✅ Adl (Justice & Non-Oppression)
**Definition:** No one entity should dominate or control others

**Implementation:**
- Apps are peers (no hierarchy)
- Temporary tokens (prevent permanent control)
- Distributed authority (no master keys)
- User retains ultimate authority

**Evidence:**
```
Tokens expire (90 days)
Guardian cannot force AmnShield to block
AmnShield cannot access Guardian data without permission
User can disable any component
```

### ✅ Rahmah (Mercy & Compassion)
**Definition:** Guidance with gentleness, not harsh control

**Implementation:**
- Clear error messages (not cryptic)
- Multiple identity modes (email, local, device ID, none)
- Flexible validation (not arbitrary restrictions)
- Recovery paths (password recovery, re-linking)
- Patient design (no forced upgrades)

**Evidence:**
```
LocalAccountManager: Multiple password reset paths
Guardian: Optional, user can skip integration
AmnShield: Works offline, no cloud dependency
All: No nagware or forced payments
```

---

## 🏗️ Integration Phases (Comprehensive Roadmap)

### Phase 1: Guardian Core Authentication (Week 1-2) ✅ In Progress
```
Status: 🟢 Currently Working
Timeline: 2 weeks
Deliverables:
  ✅ IdentityMode.kt                 [40 lines] - 4 identity options
  ✅ GuardianCapability.kt           [120 lines] - Permission model
  ✅ CapabilityToken.kt              [110 lines] - Token entity
  ✅ AuthState.kt                    [90 lines] - State machine
  ✅ LocalAccountManager.kt          [450 lines] - Password security
  ✅ TokenStorage.kt                 [500 lines] - Encryption
  ⏳ Room Database                   - Entities & DAOs
  ⏳ AuthViewModel                   - State management
  ⏳ Hilt Setup                      - Dependency injection
  ⏳ Compose UI                      - Identity selection

Total Code: ~1,900 lines
Security: Military-grade (PBKDF2, AES-256, HMAC)
Principles: All 5 Islamic ethics embedded
```

### Phase 2: Guardian Email Authentication (Week 2)
```
Status: 📋 Planned
Timeline: 1 week
Deliverables:
  - EmailVerificationService
  - OTP generation + verification
  - Email verification flow
  - Account recovery
  - Session persistence

Features:
  - No Firebase (local verification)
  - Token-based sessions
  - Automatic session cleanup
```

### Phase 3-5: Guardian Permission UI & Token Exchange (Week 2-3)
```
Status: 📋 Planned
Timeline: 2 weeks
Deliverables:
  - Capability permission screens
  - Token generation & exchange
  - Guardian IPC service
  - App-to-Guardian communication
  - Configuration exposure API

Features:
  - Clear permission dialogs
  - Granular capability control
  - Revocation UI
  - Status dashboard
```

### Phase 6-8: AmnShield App Integration (Week 3-4) 🔜 Next
```
Status: 📋 Ready to Start
Timeline: 2 weeks
Deliverables:
  ✅ GuardianConnectionManager.kt [Created]
  - CapabilityTokenValidator
  - ConfigurationSyncManager
  - GuardianIPC
  - Modified blockers (App, Keyword, View, Focus)
  - Modified accessibility service
  - Settings UI updates

Integration Points:
  AmnShield App ←→ Guardian
  - Request capabilities
  - Receive tokens
  - Sync configuration
  - Monitor for changes
```

### Phase 9-10: NetBlock Integration (Week 4-5)
```
Status: 📋 Planned
Timeline: 2 weeks
Deliverables:
  - NetBlock Guardian connection
  - Network control capability exposure
  - VPN service integration
  - Accessibility service enhancement

Features:
  - Centralized network blocking
  - Per-app network rules
  - Guardian-managed DNS
```

### Phase 11+: Cloud & Multi-Device (Month 2+)
```
Status: 📋 Planned
Timeline: Ongoing
Deliverables:
  - Optional cloud backup (encrypted)
  - Multi-device sync (with consent)
  - Family management UI
  - Organizational policies
  - Advanced reporting

Features:
  - User control (can disable anytime)
  - End-to-end encryption
  - No forced cloud dependency
```

---

## 💾 Data Architecture

### Data Layers (Defense-in-Depth)

```
Layer 1: EncryptedSharedPreferences (AES-256-GCM)
  ├─ User accounts
  ├─ Capability tokens
  ├─ Block configurations
  └─ Sync history

Layer 2: Android Keystore (Hardware-Backed)
  ├─ Encryption keys
  ├─ HMAC keys
  └─ Certificate chains

Layer 3: Optional Cloud (If User Opts In)
  ├─ Encrypted backups (end-to-end)
  ├─ Device sync (with consent)
  └─ Audit logs (local copies)
```

### Data Flow Examples

**Scenario 1: Local-Only (Standalone)**
```
User Configuration
    ↓
SharedPreferences (App)
    ↓
EncryptedSharedPreferences (AES-256)
    ↓
Saved locally, never transmitted
```

**Scenario 2: Guardian Sync (Optional)**
```
User Configures Block in Guardian
    ↓
Guardian Room Database
    ↓
Guardian broadcasts update via encrypted Intent
    ↓
AmnShield App receives (verifies token)
    ↓
AmnShield stores in EncryptedSharedPreferences
    ↓
Merged with local config
    ↓
No data sent back to Guardian
```

**Scenario 3: Cloud Backup (If User Chooses)**
```
User Exports Configuration
    ↓
Locally encrypted (E2E)
    ↓
User uploads to cloud (optional)
    ↓
Cloud stores encrypted blob (cannot read)
    ↓
User can restore anytime
    ↓
User can revoke cloud access anytime
```

---

## 🎯 Use Cases: How Users Interact

### Use Case 1: Individual User (No Guardian)
```
Goal: Protect my device from harmful content

Steps:
1. Install AmnShield App
2. Configure local blocks (apps, keywords, etc.)
3. Blocking active immediately
4. Can use offline
5. Guardian not needed

Features Used:
  ✅ App blocking
  ✅ Keyword blocking
  ✅ Content filtering
  ✅ Focus mode
  ✅ Reports
  ✅ All features work!
```

### Use Case 2: Parent Protecting Child (With Guardian)
```
Goal: Manage child device from my phone

Steps:
1. Install Guardian on parent phone
2. Create account + configure blocks
3. Install AmnShield on child phone
4. Link AmnShield to Guardian
5. Guardian-configured blocks sync to AmnShield
6. Parent manages from Guardian
7. Child cannot bypass (local + Guardian blocks)

Features:
  ✅ Centralized blocking rules
  ✅ Multiple children support
  ✅ Time-based restrictions
  ✅ Reports visible to parent
  ✅ Child still has local control
```

### Use Case 3: Organization (School/Mosque)
```
Goal: Protect devices in our organization

Steps:
1. Admin sets up Guardian with org policies
2. Employees/members install AmnShield + NetBlock
3. Link apps to Guardian
4. Organizational policies automatically apply
5. Users can add personal blocks
6. Can unlink if they leave org

Features:
  ✅ Policy enforcement
  ✅ User + org blocks merged
  ✅ Compliance tracking
  ✅ Flexible (not oppressive)
  ✅ Users maintain autonomy
```

---

## 📱 User Experience Journey

### Journey 1: Discovery
```
User discovers AmnShield on PlayStore
         ↓
Installs AmnShield App
         ↓
App works immediately (no setup needed)
         ↓
Discovers "Link Guardian" in settings (optional)
         ↓
User decides: Continue offline OR install Guardian
```

### Journey 2: Local Protection
```
Open AmnShield
         ↓
Home screen shows features
         ↓
Tap "App Blocker" → select apps to block
         ↓
Tap "Keyword Blocker" → enter keywords
         ↓
Done! Blocking active immediately
         ↓
View reports, adjust settings
```

### Journey 3: Guardian Integration (Optional)
```
User clicks "Link Guardian" in settings
         ↓
AmnShield checks if Guardian installed
         ↓
If not installed:
    "Download Guardian from PlayStore?" 
    User decides: Install or Continue Offline
         ↓
If installed:
    Guardian shows permission dialog
         ↓
User selects capabilities to grant:
    ☐ Sync Blocked Apps
    ☐ Sync Blocked Keywords
    ☐ Sync Focus Mode
         ↓
AmnShield stores token
         ↓
Integration active! 
Status shows "Linked to Guardian ✓"
```

### Journey 4: Ongoing Management
```
User in Guardian → adds/removes blocks
         ↓
Guardian notifies AmnShield
         ↓
AmnShield syncs configuration
         ↓
Merged blocks active
         ↓
User can see merged list in AmnShield UI
         ↓
Can unlink anytime → back to local-only
```

---

## 🔍 Technical Governance

### Code Organization (Modular Design)
```
Guardian/
├── auth/                    - Authentication (standalone module)
├── ui/                      - Guardian UI
├── service/                 - VPN + IPC services
├── data/                    - Room database
└── di/                      - Dependency injection

AmnShield App/
├── blockers/               - Blocking logic (unchanged)
├── guardian_integration/   - NEW: Guardian linking
├── services/               - Accessibility service (enhanced)
├── ui/                     - UI (enhanced with Guardian settings)
└── data/                   - Config loading (enhanced)

NetBlock/
├── guardian_integration/   - NEW: Guardian linking (future)
└── ...
```

### API Contracts (Intent-Based Communication)

**AmnShield App → Guardian (Request)**
```
Intent(ACTION_QUERY_CAPABILITIES)
  ├─ Source: AmnShield App
  ├─ Target: Guardian
  └─ Response: List of available capabilities
```

**Guardian → AmnShield App (Response)**
```
Intent(ACTION_CAPABILITY_RESPONSE)
  ├─ Capabilities: [List of GuardianCapability]
  ├─ Token: CapabilityToken (if already linked)
  └─ Encrypted: true
```

**Guardian → AmnShield App (Config Update)**
```
Broadcast(ACTION_CONFIG_UPDATED)
  ├─ UpdateType: "app_blocks" | "keywords" | etc
  ├─ ConfigData: Encrypted blob
  └─ Signature: HMAC-SHA256
```

---

## ✅ Ecosystem Readiness Checklist

### Phase 1: Core (Today)
- [x] Architecture designed
- [x] Islamic principles embedded
- [x] Security parameters set
- [x] Code created
- [ ] Tests written
- [ ] Guardian auth complete
- [ ] Ready for Phase 2

### Phase 6-8: Integration (Next Sprint)
- [ ] AmnShield integration module
- [ ] Blocker updates
- [ ] Service enhancements
- [ ] Settings UI
- [ ] Token handling
- [ ] Configuration sync
- [ ] Comprehensive testing

### Phase 9+: Complete Ecosystem
- [ ] All 3 apps integrated
- [ ] Multi-device support
- [ ] Optional cloud
- [ ] Family management
- [ ] Organizational policies
- [ ] Complete documentation

---

## 📊 Impact & Benefits

### For Individual Users
```
✅ Choice: Use apps individually or together
✅ Control: Decide what Guardian can access
✅ Privacy: All data stays on device
✅ Flexibility: Can unlink anytime
✅ Offline: Works without internet
```

### For Parents
```
✅ Central management across child devices
✅ Can't bypass (merged local + Guardian blocks)
✅ Time-based restrictions
✅ Reports and insights
✅ Still respects child autonomy
```

### For Organizations
```
✅ Policy enforcement
✅ Compliance tracking
✅ User + org blocks merged
✅ Flexible implementation
✅ Audit trails
```

### For Muslim Community
```
✅ Aligned with Islamic values (5 principles embedded)
✅ Serves Muslim needs specifically
✅ Open to everyone globally
✅ Respects user autonomy
✅ Encourages healthy digital habits
```

---

## 🚀 Launch Timeline

```
Week 1-2    Phase 1: Guardian Auth (In Progress)
            └─ Core authentication system

Week 2-3    Phase 2: Email Auth + Phase 3-5: Permissions
            └─ Guardian enhancement

Week 3-4    Phase 6-8: AmnShield App Integration 🔜 NEXT
            └─ First app linked to Guardian

Week 4-5    Phase 9-10: NetBlock Integration
            └─ Second app linked

Month 2     Phase 11+: Cloud & Advanced
            └─ Optional cloud sync, family management

Month 3+    Full Ecosystem Release
            └─ All 3 apps + Guardian + optional cloud
```

---

## 📞 Key Documents

| Document | Purpose | Location |
|----------|---------|----------|
| **AmnShield_APP_INTEGRATION_PLAN.md** | Complete phase-by-phase plan | Guardian root |
| **GUARDIAN_INTEGRATION_ARCHITECTURE.md** | AmnShield App architecture | AmnShield root |
| **.github/copilot-instructions.md** | Full architecture spec | Both apps |
| **IMPLEMENTATION_ROADMAP.md** | 5-week technical plan | Guardian |
| **PHASE_1_STATUS.md** | Current progress | Guardian |

---

## 🎓 What Makes This Ecosystem Special

### ✅ **Aligned with Islamic Values**
Every architectural decision reflects Amanah, Rida, Karamah, Adl, Rahmah

### ✅ **Privacy-First by Design**
All data stays local. No tracking, no analytics by default. User controls everything.

### ✅ **Fully Independent**
Each app works 100% standalone. Guardian integration is optional, not required.

### ✅ **User-Controlled**
Users decide: standalone vs integrated, which capabilities to grant, when to unlink.

### ✅ **No Lock-In**
Temporary tokens expire. User can revoke anytime. Can use apps without Guardian forever.

### ✅ **Community-Focused**
Designed for Muslim community but open to everyone. Respects family structures while maintaining individual dignity.

---

**Ecosystem Status:** 🟢 **READY FOR IMPLEMENTATION**  
**Next Phase:** Begin Phase 6 (AmnShield App Integration)  
**Timeline:** 5 weeks to complete ecosystem  
**Vision:** Islamic-values-aligned digital wellbeing for families and communities
