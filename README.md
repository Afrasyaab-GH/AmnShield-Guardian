# AmnGuard (AmnShield Guardian)

> 🛡️ **Centralized management, family protection, and policy synchronization for the Amn Protection Ecosystem.**

**AmnGuard** (application ID `com.alhaq.amnshield.guardian`) is the administrative hub of the Amn Digital Wellbeing Suite. Developed by **Al-Haq Studio**, it allows parents, organization administrators, or individuals to define, sync, and secure content filters, app limits, and category blocklists across devices running **AmnShield** and other paired ecosystem applications.

---

## 🌟 Key Features

*   **Centralized Configuration**: Build once, deploy everywhere. Define a master policy and apply it to multiple client devices.
*   **Parental Controls & Family Profiles**: Pair child devices running AmnShield with Guardian using secure, revocable token handshakes.
*   **Multi-Device Synchronization**: Sync custom domain blocklists, app blockers, keyword blocks, and settings.
*   **Local Federation Architecture**: Operates offline-first. Uses secure Inter-Process Communication (IPC) and on-device token exchange rather than locking user data to cloud servers.
*   **Security & Anti-Uninstall Controls**: Lock child profiles and block tampering with administrative settings.

---

## 🛠️ Tech Stack & Architecture

*   **Platform**: Native Android (Kotlin)
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Dependency Injection**: Dagger Hilt
*   **Local Storage**: Room Database & EncryptedSharedPreferences
*   **IPC Protocol**: Secure AIDL / Messenger service binders

---

## 📁 Project Structure

*   `app/`: Main Android Application module
    *   `src/main/java/com/alhaq/amnshield/guardian/`: Main Kotlin source files
    *   `src/main/aidl/`: AIDL interfaces for secure client app synchronization
*   `ECOSYSTEM_VISION_ROADMAP.md`: Vision of standalone vs. federated mode integration
*   `IMPLEMENTATION_ROADMAP.md`: Phase-by-phase status of the Guardian app
*   `AUTH_IMPLEMENTATION_SUMMARY.md`: Details of the token generation and handshake security

---

## 🚀 Getting Started

1. Open this repository in Android Studio.
2. Compile and install the app using:
   ```bash
   ./gradlew assembleDebug
   ```
3. Load the corresponding **AmnShield** application on the target devices.
4. Follow the setup wizard in AmnGuard to pair the devices via a local token handshake.

---

© 2026 Al-Haq Studio & Afrasyaab Meranai. All rights reserved.
