# Quick Build and Install Script for DeenShield

Write-Host "DeenShield Build & Install Script" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Set error action
$ErrorActionPreference = "Stop"

# Define paths
$ADB = "C:\Users\habib\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$APK = "app\build\outputs\apk\debug\app-debug.apk"

# Step 1: Clean
Write-Host "[1/4] Cleaning build..." -ForegroundColor Yellow
.\gradlew.bat clean --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "Clean failed!" -ForegroundColor Red
    exit 1
}

# Step 2: Build
Write-Host "[2/4] Building APK..." -ForegroundColor Yellow
.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

# Step 3: Check device
Write-Host "[3/4] Checking for connected devices..." -ForegroundColor Yellow
$devices = & $ADB devices | Select-String "device$" | Measure-Object
if ($devices.Count -eq 0) {
    Write-Host "No devices connected! Please connect a device or start an emulator." -ForegroundColor Red
    exit 1
}
Write-Host "Device found!" -ForegroundColor Green

# Step 4: Install
Write-Host "[4/4] Installing APK..." -ForegroundColor Yellow
& $ADB install -r $APK
if ($LASTEXITCODE -ne 0) {
    Write-Host "Install failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Success! App installed." -ForegroundColor Green
Write-Host ""
Write-Host "Launching app..." -ForegroundColor Yellow
& $ADB shell monkey -p com.deenshield.blocker.debug 1

Write-Host ""
Write-Host "Done! Check your device." -ForegroundColor Green
Write-Host ""
Write-Host "To monitor logs, run:" -ForegroundColor Cyan
Write-Host "  & '$ADB' logcat | Select-String 'BlockingVpn|Accessibility'" -ForegroundColor White
