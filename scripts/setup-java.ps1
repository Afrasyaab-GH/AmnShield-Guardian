param(
  [switch]$UseAndroidStudioJbr,
  [switch]$InstallOpenJDK = $true,
  [string]$JdkHome,
  [switch]$TryDownload,
  [string]$DownloadDir
param(
  [switch]$UseAndroidStudioJbr,
  [switch]$InstallOpenJDK = $true,
  [string]$JdkHome,
  [switch]$TryDownload,
  [string]$DownloadDir
)

$ErrorActionPreference = 'Stop'
Write-Host "=== Java setup for this session ==="

function Set-JavaEnv([string]$jdkHome) {
  if (-not (Test-Path (Join-Path $jdkHome 'bin\java.exe'))) {
    throw "java.exe not found under: $jdkHome"
  }
  $env:JAVA_HOME = $jdkHome
  $filtered = @()
  foreach ($p in ($env:Path -split ';')) {
    if ($p -and ($p -notmatch '\\jdk-[0-9]+' ) -and ($p -notmatch '\\jbr(\\|$)')) { $filtered += $p }
  }
  $env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + ($filtered -join ';')
  Write-Host "JAVA_HOME set to: $env:JAVA_HOME"
  & (Join-Path $env:JAVA_HOME 'bin\java.exe') -version
}

function Download-And-ExtractJdk17([string]$TargetDir) {
  if (-not $TargetDir) {
    $TargetDir = Join-Path (Resolve-Path (Join-Path $PSScriptRoot '..')).Path 'jdk-17'
  }
  New-Item -ItemType Directory -Path $TargetDir -Force | Out-Null
  $zipPath = Join-Path $TargetDir 'jdk17.zip'
  Write-Host "Downloading Temurin JDK 17 to $zipPath ..."
  $url = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk'
  [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
  Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing
  if (-not (Test-Path $zipPath)) { throw "Download failed: $zipPath not found" }
  Write-Host "Extracting..."
  Expand-Archive -Path $zipPath -DestinationPath $TargetDir -Force
  $candidates = Get-ChildItem -Directory -Path $TargetDir | Where-Object { $_.Name -match '^jdk-17' -and (Test-Path (Join-Path $_.FullName 'bin\java.exe')) }
  if ($candidates) { return ($candidates | Select-Object -First 1).FullName }
  if (Test-Path (Join-Path $TargetDir 'bin\java.exe')) { return $TargetDir }
  throw "Extracted JDK directory not found under $TargetDir"
}

# 0) Explicit JDK path
if ($JdkHome) { Set-JavaEnv $JdkHome; return }

# 1) Use Android Studio JBR if requested
if ($UseAndroidStudioJbr) {
  $jbrs = @(
    'C:\\Program Files\\Android\\Android Studio\\jbr',
    'C:\\Program Files\\Android\\jbr'
  )
  foreach ($j in $jbrs) { if (Test-Path $j) { Set-JavaEnv $j; return } }
  Write-Warning 'Android Studio JBR not found in common locations.'
}

# 2) Discover existing JDK 17 (prefer project-local)
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$localJdks = Get-ChildItem -Path $projectRoot -Directory -Filter 'jdk-17*' -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
$roots = @($localJdks,
  'C:\\Program Files\\Android\\jbr',
  'C:\\Program Files\\Android\\Android Studio\\jbr',
  'C:\\Program Files\\Microsoft',
  'C:\\Program Files\\Eclipse Adoptium',
  'C:\\Program Files\\Java',
  "$env:USERPROFILE\\.jdks",
  "$env:USERPROFILE\\scoop\\apps\\openjdk"
) | Where-Object { $_ -and (Test-Path $_) }

foreach ($root in $roots) {
  $java = Get-ChildItem -Path $root -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match '\\bin\\java.exe$' -and ($_.FullName -match 'jdk-17' -or $_.Directory.Parent.Name -match '^jdk-17') } |
    Select-Object -First 1
  if ($java) { Set-JavaEnv ($java.Directory.Parent.FullName); return }
}

# 3) Try winget install
if ($InstallOpenJDK) {
  Write-Host 'Trying winget to install OpenJDK 17...'
  $ok = $false
  try { winget install --id Microsoft.OpenJDK.17 -e --accept-source-agreements --accept-package-agreements -h 0; $ok = $true } catch { Write-Warning $_.Exception.Message }
  if (-not $ok) { try { winget install --id EclipseAdoptium.Temurin.17.JDK -e --accept-source-agreements --accept-package-agreements -h 0; $ok = $true } catch { Write-Warning $_.Exception.Message } }
  if ($ok) {
    $probe = @('C:\\Program Files\\Microsoft','C:\\Program Files\\Eclipse Adoptium','C:\\Program Files\\Java') | Where-Object { Test-Path $_ }
    foreach ($r in $probe) {
      $java = Get-ChildItem -Path $r -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match '\\bin\\java.exe$' -and ($_.FullName -match 'jdk-17' -or $_.Directory.Parent.Name -match '^jdk-17') } |
        Select-Object -First 1
      if ($java) { Set-JavaEnv ($java.Directory.Parent.FullName); return }
    }
  }
}

# 4) Portable download fallback
if ($TryDownload) {
  $target = if ($DownloadDir) { $DownloadDir } else { $null }
  $jdk = Download-And-ExtractJdk17 $target
  Set-JavaEnv $jdk
  return
}

throw 'No JDK 17 found or installed. Options: -JdkHome <path>, -UseAndroidStudioJbr, -InstallOpenJDK (winget), or -TryDownload for a portable ZIP.'
  $candidates = Get-ChildItem -Path $root -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match '\\bin\\java.exe$' -and ($_.FullName -match 'jdk-17' -or $_.Directory.Parent.Name -match '^jdk-17') }
  if ($candidates) { $javaExe = $candidates | Select-Object -First 1; break }
}

if ($javaExe) {
  $jdkHome = $javaExe.Directory.Parent.FullName
  Set-JavaEnv $jdkHome
  return
}

# 3) Install OpenJDK via winget if permitted
if ($InstallOpenJDK) {
  Write-Host "Installing OpenJDK 17 via winget (requires admin or elevation prompt)..."
  $installed = $false
  try {
    winget install --id Microsoft.OpenJDK.17 -e --accept-source-agreements --accept-package-agreements -h 0
    $installed = $true
  } catch {
    Write-Warning "Microsoft OpenJDK install failed: $($_.Exception.Message)"
  }
  if (-not $installed) {
    try {
      winget install --id EclipseAdoptium.Temurin.17.JDK -e --accept-source-agreements --accept-package-agreements -h 0
      $installed = $true
    } catch {
      Write-Warning "Temurin OpenJDK install failed: $($_.Exception.Message)"
    }
  }

  if ($installed) {
    # Re-scan common locations for jdk-17
    $roots = @(
      'C:\\Program Files\\Microsoft',
      'C:\\Program Files\\Eclipse Adoptium',
      'C:\\Program Files\\Java'
    ) | Where-Object { Test-Path $_ }

    $javaExe = $null
    foreach ($root in $roots) {
      $candidates = Get-ChildItem -Path $root -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match '\\bin\\java.exe$' -and ($_.FullName -match 'jdk-17' -or $_.Directory.Parent.Name -match '^jdk-17') }
      if ($candidates) { $javaExe = $candidates | Select-Object -First 1; break }
    }

    if ($javaExe) {
      $jdkHome = $javaExe.Directory.Parent.FullName
      Set-JavaEnv $jdkHome
      return
    }
  }
}

# 4) Fallback: portable download/extract
if ($TryDownload) {
  $target = if ($DownloadDir) { $DownloadDir } else { $null }
  $jdkPath = Download-And-ExtractJdk17 -TargetDir $target
  Set-JavaEnv $jdkPath
  return
}

throw "No JDK 17 found or installed. Options: -JdkHome <path>, -UseAndroidStudioJbr if Android Studio is installed, -InstallOpenJDK (winget), or -TryDownload for a portable ZIP."
    param(
      [string]$TargetDir
    )
    if (-not $TargetDir) {
      $TargetDir = Join-Path (Resolve-Path (Join-Path $PSScriptRoot '..')).Path 'jdk-17'
    }
    New-Item -ItemType Directory -Path $TargetDir -Force | Out-Null
    $zipPath = Join-Path $TargetDir 'jdk17.zip'
    Write-Host "Downloading Temurin JDK 17 (zip) to $zipPath ..."
    try {
      # Adoptium latest GA JDK 17 for Windows x64 (archive/zip)
      $url = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk'
      [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
      Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing
    } catch {
      throw "Failed to download JDK 17: $($_.Exception.Message)"
    }
    if (-not (Test-Path $zipPath)) { throw "Download failed: $zipPath not found" }
    Write-Host "Extracting JDK zip ..."
    try {
      Expand-Archive -Path $zipPath -DestinationPath $TargetDir -Force
    } catch {
      throw "Failed to extract JDK zip: $($_.Exception.Message)"
    }
    # Find extracted JDK home
    $jdkDirs = Get-ChildItem -Directory -Path $TargetDir | Where-Object { $_.Name -match '^jdk-17' -and (Test-Path (Join-Path $_.FullName 'bin\java.exe')) }
    if (-not $jdkDirs) {
      # Some zips may extract directly without nested dir; try TargetDir itself
      if (Test-Path (Join-Path $TargetDir 'bin\java.exe')) { return $TargetDir }
      throw "Extracted JDK directory not found under $TargetDir"
    }
    return ($jdkDirs | Select-Object -First 1).FullName
  }

  # 0) Explicit JDK path
  if ($JdkHome) {
    Set-JavaEnv $JdkHome
    return
  }

  # 1) Use Android Studio JBR if requested
  if ($UseAndroidStudioJbr) {
    $jbrCandidates = @(
      'C:\\Program Files\\Android\\Android Studio\\jbr',
      'C:\\Program Files\\Android\\jbr'
    )
    foreach ($jbr in $jbrCandidates) {
      if (Test-Path $jbr) { Set-JavaEnv $jbr; return }
    }
    Write-Warning "Android Studio JBR not found in common locations."
  }

  # 2) Try to discover an existing JDK 17+ (prefer project-local)
  $projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
  $localJdkDirs = Get-ChildItem -Path $projectRoot -Directory -Filter 'jdk-17*' -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
  $searchRoots = @(
    $localJdkDirs,
    'C:\\Program Files\\Android\\jbr',
    'C:\\Program Files\\Android\\Android Studio\\jbr',
    'C:\\Program Files\\Microsoft',
    'C:\\Program Files\\Eclipse Adoptium',
    'C:\\Program Files\\Java',
    "$env:USERPROFILE\\.jdks",
    "$env:USERPROFILE\\scoop\\apps\\openjdk"
  ) | Where-Object { $_ -and (Test-Path $_) }

  $javaExe = $null
  foreach ($root in $searchRoots) {
    $candidates = Get-ChildItem -Path $root -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
      Where-Object { $_.FullName -match '\\bin\\java.exe$' -and $_.FullName -match 'jdk-17' }
    if ($candidates) { $javaExe = $candidates | Select-Object -First 1; break }
  }

  if ($javaExe) {
    $jdkHome = $javaExe.Directory.Parent.FullName
    Set-JavaEnv $jdkHome
    return
  }

  # 3) Install OpenJDK via winget if permitted
  if ($InstallOpenJDK) {
    Write-Host "Installing OpenJDK 17 via winget (requires admin or elevation prompt)..."
    $installed = $false
    try {
      winget install --id Microsoft.OpenJDK.17 -e --accept-source-agreements --accept-package-agreements -h 0
      $installed = $true
    } catch {
      Write-Warning "Microsoft OpenJDK install failed: $($_.Exception.Message)"
    }
    if (-not $installed) {
      try {
        winget install --id EclipseAdoptium.Temurin.17.JDK -e --accept-source-agreements --accept-package-agreements -h 0
        $installed = $true
      } catch {
        Write-Warning "Temurin OpenJDK install failed: $($_.Exception.Message)"
      }
    }

    if ($installed) {
      # Re-scan common locations for jdk-17
      $roots = @(
        'C:\\Program Files\\Microsoft',
        'C:\\Program Files\\Eclipse Adoptium',
        'C:\\Program Files\\Java'
      ) | Where-Object { Test-Path $_ }

      $javaExe = $null
      foreach ($root in $roots) {
        $candidates = Get-ChildItem -Path $root -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
          Where-Object { $_.FullName -match '\\bin\\java.exe$' -and $_.FullName -match 'jdk-17' }
        if ($candidates) { $javaExe = $candidates | Select-Object -First 1; break }
      }

      if ($javaExe) {
        $jdkHome = $javaExe.Directory.Parent.FullName
        Set-JavaEnv $jdkHome
        return
      }
    }
  }

  # 4) Fallback: portable download/extract
  if ($TryDownload) {
    $target = if ($DownloadDir) { $DownloadDir } else { $null }
    $jdkPath = Download-And-ExtractJdk17 -TargetDir $target
    Set-JavaEnv $jdkPath
    return
  }

  throw "No JDK 17 found or installed. Options: -JdkHome <path>, -UseAndroidStudioJbr if Android Studio is installed, -InstallOpenJDK (winget), or -TryDownload for a portable ZIP."