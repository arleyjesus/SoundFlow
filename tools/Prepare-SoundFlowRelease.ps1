[CmdletBinding()]
param([switch]$Offline)

# Builds a local candidate using the existing local signing configuration. Never creates keys or publishes.
$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$taskReport = Join-Path $taskRoot ('build/release-check/' + [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss') + '-UTC')
New-Item -ItemType Directory -Path $taskReport | Out-Null
Push-Location $taskRoot
try {
    if (!(Test-Path -LiteralPath (Join-Path $taskRoot 'signing.properties'))) {
        throw 'Release signing is not configured. See signing.properties.example; never share/commit passwords.'
    }
    $taskOptions = @(':app:assembleRelease', ':app:testReleaseUnitTest', ':app:lintRelease', '--console=plain')
    if ($Offline) { $taskOptions += '--offline' }
    $taskOldPolicy = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & .\gradlew.bat @taskOptions 2>&1 | Tee-Object (Join-Path $taskReport 'build-and-lint.txt') | Out-Host
        $taskExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = $taskOldPolicy }
    if ($taskExit -ne 0) { throw "Build failed; see $taskReport/build-and-lint.txt" }
    $taskOutput = Join-Path $taskRoot 'app/build/outputs/apk/release'
    $taskMetadataFile = Join-Path $taskOutput 'output-metadata.json'
    $taskMetadata = Get-Content -LiteralPath $taskMetadataFile -Raw | ConvertFrom-Json
    $taskApks = @(foreach ($taskElement in $taskMetadata.elements) {
        $taskName = [string]$taskElement.outputFile
        if ([IO.Path]::GetFileName($taskName) -ne $taskName -or !$taskName.EndsWith('.apk')) {
            throw 'Invalid APK path in generated output metadata.'
        }
        Get-Item -LiteralPath (Join-Path $taskOutput $taskName)
    })
    if (!$taskApks.Count) { throw 'No release APK generated.' }
    $taskArtifactDirectory = Join-Path $taskReport 'artifacts'
    New-Item -ItemType Directory -Path $taskArtifactDirectory | Out-Null
    $taskManifest = @(foreach ($taskApk in $taskApks) {
        $taskCopy = Join-Path $taskArtifactDirectory $taskApk.Name
        Copy-Item -LiteralPath $taskApk.FullName -Destination $taskCopy
        $taskHash = (Get-FileHash -LiteralPath $taskCopy -Algorithm SHA256).Hash
        if ($taskHash -ne (Get-FileHash -LiteralPath $taskApk.FullName -Algorithm SHA256).Hash) {
            throw 'APK changed during snapshot; rerun without a concurrent build.'
        }
        [PSCustomObject]@{ File = $taskApk.Name; SHA256 = $taskHash; Bytes = $taskApk.Length }
    })
    $taskManifest | ConvertTo-Json | Set-Content (Join-Path $taskReport 'artifacts-sha256.json')
    Copy-Item -LiteralPath $taskMetadataFile -Destination (Join-Path $taskReport 'output-metadata.json')
    Copy-Item 'app/build/test-results/testReleaseUnitTest' (Join-Path $taskReport 'junit-xml') -Recurse
    Copy-Item 'app/build/reports/lint-results-release.html' (Join-Path $taskReport 'lint-results-release.html')
    & git -c core.safecrlf=false diff --check 2>&1 | Set-Content (Join-Path $taskReport 'git-diff-check.txt')
    if ($LASTEXITCODE -ne 0) { throw 'git diff --check failed.' }
    & git status --short | Set-Content (Join-Path $taskReport 'git-status.txt')
    & git -c core.safecrlf=false diff --stat | Set-Content (Join-Path $taskReport 'git-diff-stat.txt')
    & git branch --show-current | Set-Content (Join-Path $taskReport 'git-branch.txt')
    Write-Host "Local candidate and reports: $taskReport"
    Write-Host 'Build success does not confirm release signing. Check the certificate before distributing.'
    Write-Host 'Source version retained. Uses configured signing only; no new key, installation, commit or publication.'
} finally { Pop-Location }
