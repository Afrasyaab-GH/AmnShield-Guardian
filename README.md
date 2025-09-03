# DeenShield Blocker (Kotlin)

This is a fresh Android (Kotlin + Jetpack Compose) scaffold to migrate the Flutter blocker features.

## Structure

android-kotlin/
- settings.gradle.kts
- build.gradle.kts
- gradle.properties
- app/
  - build.gradle.kts
  - proguard-rules.pro
  - src/main/
    - AndroidManifest.xml
    - java/com/deenshield/blocker/
      - MainActivity.kt
      - model/Block.kt
      - viewmodel/BlockViewModel.kt
      - ui/Screens.kt
    - res/values/
      - colors.xml
      - themes.xml

## Next steps
- Implement Native services: AccessibilityService, UsageStats, Overlay.
- Mirror provider logic into repositories & DataStore.
- Wire permission flows like in Flutter `NativeBridge`.
