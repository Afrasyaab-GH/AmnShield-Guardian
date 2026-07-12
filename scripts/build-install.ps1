param(
  [switch]$InstallOnDevice
)

$ErrorActionPreference = 'Stop'

# Ensure local.properties points to SDK
$localProps = Join-Path $PSScriptRoot '..\local.properties'
if (-not (Test-Path $localProps)) { throw "local.properties not found" }
$props = Get-Content $localProps | Where-Object { $_ -match '=' } | ForEach-Object {
  $k,$v = $_ -split '=',2; @{ Key=$k.Trim(); Value=$v.Trim() }
}
$propsDict = @{}
$props | ForEach-Object { $propsDict[$_.Key] = $_.Value }
if (-not $propsDict['sdk.dir'] -or -not (Test-Path $propsDict['sdk.dir'])) { throw "Android SDK not found at sdk.dir in local.properties" }

# Prefer gradlew for consistent wrapper
$gradlew = Join-Path $PSScriptRoot '..\gradlew.bat'
if (-not (Test-Path $gradlew)) { throw "gradlew.bat not found" }

Write-Host "=== Assemble Debug APK ==="
& $gradlew assembleDebug --no-daemon --stacktrace

if ($LASTEXITCODE -ne 0) { throw "assembleDebug failed" }

Write-Host "=== Locate APK ==="
$apk = Get-ChildItem -Recurse -Path (Join-Path $PSScriptRoot '..\app\build\outputs\apk\debug') -Filter *.apk -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $apk) { throw "Debug APK not found" }
Write-Host "APK: $($apk.FullName)"

if ($InstallOnDevice) {
  Write-Host "=== Installing on connected device ==="
  $adb = Join-Path $propsDict['sdk.dir'] 'platform-tools\adb.exe'
  if (-not (Test-Path $adb)) { throw "adb.exe not found under SDK platform-tools" }
  # Restart adb to ensure fresh state
  & $adb kill-server | Out-Null
  & $adb start-server | Out-Null

  # Check device authorization status and wait if needed
  $attempts = 0
  while ($true) {
    $list = & $adb devices
    $deviceLines = $list | Select-Object -Skip 1 | Where-Object { $_ -and $_.Trim() -ne '' }
    if (-not $deviceLines -or $deviceLines.Count -eq 0) {
      if ($attempts -eq 0) {
        Write-Warning "No devices detected. Connect a device or start an emulator, then enable USB debugging."
      }
    } else {
      # Parse state from first device line
      $cols = ($deviceLines[0] -split "\s+")
      if ($cols.Length -ge 2) {
        $state = $cols[1]
        if ($state -eq 'device') { break }
        if ($state -eq 'unauthorized') {
          if ($attempts -eq 0) {
            Write-Warning "Device is unauthorized. On your phone, accept the USB debugging RSA prompt. Waiting up to 90s..."
          }
        } elseif ($state -eq 'offline') {
          if ($attempts -eq 0) {
            Write-Warning "Device is offline. Replug the USB cable and ensure USB mode is File Transfer (MTP)."
          }
        }
      }
    }
    $attempts++
    if ($attempts -ge 30) { throw "ADB device not authorized/ready after waiting. Please accept the RSA prompt on the device and retry." }
    Start-Sleep -Seconds 3
  }

  # Proceed with install once device is authorized
  $pkg = 'com.alhaq.amnshield.guardian.debug'
  $installOutput = & $adb install -r -d $apk.FullName 2>&1
  if ($LASTEXITCODE -ne 0) {
    if ($installOutput -match 'INSTALL_FAILED_UPDATE_INCOMPATIBLE') {
      Write-Warning "Existing app signature mismatch. Uninstalling old build and retrying..."
      & $adb uninstall $pkg | Out-Null
      $installOutput = & $adb install -r -d $apk.FullName 2>&1
      if ($LASTEXITCODE -ne 0) { throw "adb install failed: $installOutput" }
    } else {
      throw "adb install failed: $installOutput"
    }
  }

  # Try to launch main activity using package name
  & $adb shell monkey -p $pkg -c android.intent.category.LAUNCHER 1
  Write-Host "Launched $pkg"
}
