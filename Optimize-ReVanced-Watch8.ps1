# Non-destructive, profile-guided compilation for YouTube ReVanced on Wear OS.
param(
  [Parameter(Mandatory=$true)][string]$Serial,
  [string]$Adb = "adb",
  [int]$MinimumBatteryPercent = 25,
  [int]$MaximumBatteryTemperatureC = 38
)
$ErrorActionPreference = "Stop"
function Invoke-Adb([string[]]$ArgsList) {
  $result = @(& $Adb -s $Serial @ArgsList 2>&1)
  if ($LASTEXITCODE -ne 0) { throw "ADB command failed: $($ArgsList -join ' ')`n$($result -join [Environment]::NewLine)" }
  return $result
}
$state = Invoke-Adb -ArgsList @("get-state")
if (($state -join " ").Trim() -ne "device") { throw "ADB device not authorized or offline." }
$battery = Invoke-Adb -ArgsList @("shell","dumpsys","battery")
function Read-Property([string]$name) {
  $line = @($battery | Where-Object { $_ -match ("^\s*" + $name + ":\s*\d+") } | Select-Object -First 1)
  if (!$line) { throw "Unable to read battery $name." }
  return [int](([regex]::Match($line[0], "\d+")).Value)
}
$level = Read-Property "level"
$tempTenths = Read-Property "temperature"
$tempC = $tempTenths / 10
Write-Host "Battery: $level%; temperature: $tempC C"
if ($level -lt $MinimumBatteryPercent -or $tempC -gt $MaximumBatteryTemperatureC) {
  throw "Optimization skipped: charge watch above $MinimumBatteryPercent% and cool below $MaximumBatteryTemperatureC C. Override thresholds only if appropriate."
}
Write-Host "Compiling only ReVanced primary DEX with speed-profile..."
$compiled = Invoke-Adb -ArgsList @("shell","cmd","package","compile","-m","speed-profile","--primary-dex","app.revanced.android.youtube")
$compiled | ForEach-Object { Write-Host $_ }
if (-not ($compiled -match "Success")) { throw "Compiler did not report Success." }
$report = Invoke-Adb -ArgsList @("shell","dumpsys","package","dexopt")
$lines = $report -join "`n"
$ix = $lines.IndexOf("[app.revanced.android.youtube]")
if ($ix -lt 0) { throw "Missing package dexopt entry." }
$section = $lines.Substring($ix, [Math]::Min(900, $lines.Length-$ix))
Write-Host "ReVanced dexopt details:"
Write-Host $section
if ($section -notmatch "status=speed-profile") { Write-Warning "Expected profile status was not found." }
Write-Host "Done. No app data, DPI, accounts or system packages were changed."
