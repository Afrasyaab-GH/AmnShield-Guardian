# Critical Fix Applied - October 17, 2025

## Issue Found During Testing

### Error:
```
E BlockingVpnService: java.lang.SecurityException: Starting FGS with type location callerApp=ProcessRecord
targetSDK=35 requires permissions: all of the permissions allOf=true 
[android.permission.FOREGROUND_SERVICE_LOCATION] any of the permissions allOf=false 
[android.permission.ACCESS_COARSE_LOCATION, android.permission.ACCESS_FINE_LOCATION]
```

### Root Cause:
The VPN service was declared with `foregroundServiceType="dataSync|location"` in the AndroidManifest.xml, but:
1. We don't actually use location services in the VPN
2. Android 14+ (targetSDK 35) strictly enforces that location foreground services must have active location permissions
3. The VPN service doesn't need location - it only filters network traffic

### Solution:
Changed the foreground service type from `"dataSync|location"` to just `"dataSync"` since:
- VPN blocking is data synchronization (filtering network traffic)
- No location tracking is performed
- Removes unnecessary permission requirement

### File Modified:
- **app/src/main/AndroidManifest.xml**
  - Line 70: Changed `android:foregroundServiceType="dataSync|location"` → `android:foregroundServiceType="dataSync"`

### Impact:
✅ VPN service will now start successfully without requiring location permissions
✅ App still has location permissions declared (for potential future features)
✅ Foreground service runs legally under Android 14+ restrictions

### Testing:
After rebuild and reinstall:
1. Enable VPN in Settings
2. Service should start successfully
3. Check logcat - no more SecurityException
4. VPN status card should turn GREEN

---

## Why This Happened:
During initial development, the service was configured with multiple foreground service types without realizing Android 14+ enforces strict runtime permission checks for each type. The location type was unnecessary for VPN filtering functionality.

## Prevention:
- Only declare foreground service types that are actually used
- Test on Android 14+ devices to catch permission issues early
- Match service type declarations with actual functionality
