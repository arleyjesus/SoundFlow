[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Za-z0-9_.:-]+$')][string]$Serial,
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Za-z0-9_-]+$')][string]$Label,
    [string]$AdbPath,
    [switch]$ResetFrameStats
)

$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (!$AdbPath) {
    $taskSdkLine = Get-Content (Join-Path $taskRoot 'local.properties') |
        Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
    if (!$taskSdkLine) { throw 'Pass -AdbPath or configure sdk.dir in local.properties.' }
    $taskSdk = $taskSdkLine.Substring(8).Replace('\\', '\').Replace('\:', ':')
    $AdbPath = Join-Path $taskSdk 'platform-tools/adb.exe'
}
if (!(Test-Path -LiteralPath $AdbPath -PathType Leaf)) { throw 'adb.exe not found.' }
$taskPackage = 'com.example.comarleyaetheraudio'
$taskState = & $AdbPath -s $Serial get-state 2>&1
if ($LASTEXITCODE -ne 0 -or (($taskState -join '').Trim() -ne 'device')) { throw 'Selected Android device is unavailable or unauthorized.' }
$taskReport = Join-Path $taskRoot ('build/metrics/' + [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss') + '-UTC-' + $Label)
New-Item -ItemType Directory -Path $taskReport | Out-Null
foreach ($taskKind in @('meminfo', 'gfxinfo')) {
    $taskArguments = @('-s', $Serial, 'shell', 'dumpsys', $taskKind, $taskPackage)
    if ($taskKind -eq 'gfxinfo') { $taskArguments += 'framestats' }
    & $AdbPath @taskArguments 2>&1 | Set-Content (Join-Path $taskReport ($taskKind + '.txt'))
    if ($LASTEXITCODE -ne 0) { throw "Could not capture $taskKind." }
}
if ($ResetFrameStats) {
    & $AdbPath -s $Serial shell dumpsys gfxinfo $taskPackage reset 2>&1 |
        Set-Content (Join-Path $taskReport 'frame-reset.txt')
    if ($LASTEXITCODE -ne 0) { throw 'Could not reset frame counters.' }
}
Write-Host "Raw measurements: $taskReport"
Write-Host 'No audio files, Room data or preferences were changed. Interpret measurements on the same device/build.'
