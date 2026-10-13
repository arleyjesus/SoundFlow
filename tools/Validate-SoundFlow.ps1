[CmdletBinding()]
param([switch]$Offline)

$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$taskReport = Join-Path $taskRoot ('build/validation/' + [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss') + '-UTC')
New-Item -ItemType Directory -Path $taskReport | Out-Null

function Invoke-GradleReport([string[]]$TaskArguments, [string]$LogName) {
    $taskOptions = @('--console=plain')
    if ($Offline) { $taskOptions += '--offline' }
    $taskPreviousErrorPolicy = $ErrorActionPreference
    try {
        # Windows PowerShell can surface native stderr as errors even for Gradle warnings.
        $ErrorActionPreference = 'Continue'
        & (Join-Path $taskRoot 'gradlew.bat') @TaskArguments @taskOptions 2>&1 |
            Tee-Object -FilePath (Join-Path $taskReport $LogName) | Out-Host
        $taskExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = $taskPreviousErrorPolicy }
    if ($taskExit -ne 0) { throw "Gradle failed ($taskExit). See $taskReport/$LogName" }
}

Push-Location $taskRoot
try {
    Invoke-GradleReport @(':app:assembleDebug', ':app:testDebugUnitTest', ':app:assembleDebugAndroidTest', ':app:lintDebug') 'build-and-lint.txt'
    Invoke-GradleReport @(':app:dependencies', '--configuration', 'debugRuntimeClasspath') 'runtime-dependencies.txt'
    Invoke-GradleReport @(':app:dependencies', '--configuration', 'debugCompileClasspath') 'compile-dependencies.txt'
    $taskResults = @(Get-ChildItem 'app/build/test-results/testDebugUnitTest' -Filter '*.xml' | ForEach-Object {
        $taskXml = [xml](Get-Content -LiteralPath $_.FullName -Raw)
        [PSCustomObject]@{ Suite = $taskXml.testsuite.name; Tests = [int]$taskXml.testsuite.tests
            Failures = [int]$taskXml.testsuite.failures; Errors = [int]$taskXml.testsuite.errors
            Skipped = [int]$taskXml.testsuite.skipped; Timestamp = $taskXml.testsuite.timestamp }
    })
    if (!$taskResults.Count) { throw 'No JUnit XML results found.' }
    if (($taskResults | Measure-Object Failures -Sum).Sum -or ($taskResults | Measure-Object Errors -Sum).Sum) {
        throw 'JUnit reports failures/errors.'
    }
    $taskResults | ConvertTo-Json | Set-Content (Join-Path $taskReport 'junit-summary.json')
    Copy-Item 'app/build/test-results/testDebugUnitTest' (Join-Path $taskReport 'junit-xml') -Recurse
    Get-FileHash 'app/build/outputs/apk/debug/app-debug.apk',
        'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk' |
        Select-Object Path, Algorithm, Hash | ConvertTo-Json | Set-Content (Join-Path $taskReport 'apk-sha256.json')
    & git -c core.safecrlf=false diff --check 2>&1 | Tee-Object (Join-Path $taskReport 'git-diff-check.txt') | Out-Host
    if ($LASTEXITCODE -ne 0) { throw 'git diff --check failed.' }
    & git diff --stat | Set-Content (Join-Path $taskReport 'git-diff-stat.txt')
    & git status --short | Set-Content (Join-Path $taskReport 'git-status.txt')
    Write-Host "Reports: $taskReport"
    Write-Host 'Instrumented tests were compiled, not executed. No commit or publication performed.'
} finally { Pop-Location }
