[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$ApkPath,
    [Parameter(Mandatory)][string]$NotesFile,
    [string]$ExpectedCertificate = '31cbd14903f1fb7fe086efe8650d2465fbf7cfb6b9ffeaeef7adf01e1d91419b',
    [switch]$Publish
)

# Default is a dry review. -Publish explicitly creates the public release with one APK.
$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Push-Location $taskRoot
try {
    $taskTrackedChanges = @(& git status --porcelain --untracked-files=no)
    if ($taskTrackedChanges.Count) { throw 'Commit/review tracked changes before publishing; no automatic commit is performed.' }
    $taskCommit = (& git rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Cannot identify source commit.' }
    $taskBranch = (& git branch --show-current).Trim()
    if ($taskBranch -ne 'refactor/soundflow-4') { throw 'Use the approved development branch.' }
    $taskConfig = Get-Content 'app/build.gradle.kts' -Raw
    $taskVersion = [regex]::Match($taskConfig, 'versionName\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"').Groups[1].Value
    $taskCode = [regex]::Match($taskConfig, 'versionCode\s*=\s*(\d+)').Groups[1].Value
    if (!$taskVersion -or !$taskCode) { throw 'Expected a stable version/code in the source.' }
    $taskTag = "v$taskVersion"
    $taskApk = Get-Item -LiteralPath $ApkPath
    $taskNotes = Get-Content -LiteralPath $NotesFile -Raw
    if ($taskApk.Extension -ne '.apk' -or $taskApk.Length -gt 150MB) { throw 'Invalid APK artifact.' }
    $taskSdkLine = Get-Content 'local.properties' | Where-Object { $_ -match '^sdk.dir=' } | Select-Object -First 1
    $taskSdk = ($taskSdkLine -replace '^sdk.dir=', '').Replace('\:', ':').Replace('\\', '\').Replace('\/', '/')
    $taskBuildTools = Get-ChildItem (Join-Path $taskSdk 'build-tools') -Directory |
        Where-Object { $_.Name -match '^\d+\.\d+\.\d+$' } | Sort-Object { [version]$_.Name } -Descending | Select-Object -First 1
    if (!$taskBuildTools) { throw 'Android build tools not found.' }
    $taskCertOutput = @(& (Join-Path $taskBuildTools.FullName 'apksigner.bat') verify --verbose --print-certs $taskApk.FullName 2>&1)
    if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed.' }
    $taskCerts = @($taskCertOutput | Select-String '^Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]+)$' |
        ForEach-Object { $_.Matches[0].Groups[1].Value.ToLowerInvariant() })
    if ($taskCerts.Count -ne 1 -or $taskCerts[0] -ne $ExpectedCertificate.ToLowerInvariant()) {
        throw 'APK signer differs from the previous public release; do not publish a debug or replacement signature.'
    }
    $taskBadging = @(& (Join-Path $taskBuildTools.FullName 'aapt2.exe') dump badging $taskApk.FullName 2>&1)
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect APK identity.' }
    $taskPackage = [regex]::Match(($taskBadging -join "`n"), "package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'")
    if (!$taskPackage.Success -or $taskPackage.Groups[1].Value -ne 'com.example.comarleyaetheraudio' -or
        $taskPackage.Groups[2].Value -ne $taskCode -or $taskPackage.Groups[3].Value -ne $taskVersion) { throw 'APK package/version does not match source.' }
    $taskHash = (Get-FileHash -LiteralPath $taskApk.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    Write-Host "Reviewed $taskTag / code $taskCode / commit $taskCommit"
    Write-Host "Certificate: $($taskCerts[0])"
    Write-Host "SHA256: $taskHash"
    if (!$Publish) { Write-Host 'Review only. Add -Publish after build/APK verification and authorization.'; return }
    $taskHeaders = @{ 'User-Agent' = 'SoundFlow-release'; Accept = 'application/vnd.github+json'; 'X-GitHub-Api-Version' = '2022-11-28' }
    # Use the existing Git credential helper in memory; never print/store credentials.
    $taskCredential = "protocol=https`nhost=github.com`npath=arleyjesus/SoundFlow.git`n`n" | & git credential fill
    if ($LASTEXITCODE -ne 0) { throw 'GitHub credentials unavailable.' }
    $taskTokenLine = $taskCredential | Where-Object { $_ -like 'password=*' } | Select-Object -First 1
    if (!$taskTokenLine) { throw 'GitHub credentials unavailable.' }
    $taskHeaders.Authorization = 'Bearer ' + $taskTokenLine.Substring(9)
    $taskCredential = $null
    $taskTokenLine = $null
    try {
        $taskRemote = Invoke-RestMethod -Uri 'https://api.github.com/repos/arleyjesus/SoundFlow/git/ref/heads/refactor/soundflow-4' -Headers $taskHeaders
        if ($taskRemote.object.sha -ne $taskCommit) { throw 'Push the reviewed commit to the development branch first.' }
        $taskExisting = @(Invoke-RestMethod -Uri 'https://api.github.com/repos/arleyjesus/SoundFlow/releases?per_page=100' -Headers $taskHeaders)
        if ($taskExisting | Where-Object { $_.tag_name -eq $taskTag }) { throw 'Release already exists; review it instead of overwriting.' }
        # Draft first: asset upload failure must never expose a public release without its APK.
        $taskBody = @{ tag_name = $taskTag; target_commitish = $taskCommit; name = "SoundFlow $taskVersion";
            body = $taskNotes + "`n`nAPK SHA-256: $taskHash"; draft = $true; prerelease = $false } | ConvertTo-Json
        $taskRelease = Invoke-RestMethod -Method Post -Uri 'https://api.github.com/repos/arleyjesus/SoundFlow/releases' -Headers $taskHeaders -ContentType 'application/json' -Body $taskBody
        $taskAssetName = "SoundFlow-$taskVersion.apk"
        $taskUpload = ($taskRelease.upload_url -replace '\{.*$', '') + '?name=' + $taskAssetName
        $taskAsset = Invoke-RestMethod -Method Post -Uri $taskUpload -Headers $taskHeaders -ContentType 'application/vnd.android.package-archive' -InFile $taskApk.FullName
        if ($taskAsset.state -ne 'uploaded' -or $taskAsset.size -ne $taskApk.Length) { throw 'Asset upload incomplete; release remains draft.' }
        if ($taskAsset.digest -and $taskAsset.digest -ne "sha256:$taskHash") { throw 'Remote digest mismatch; release remains draft.' }
        $taskPublished = Invoke-RestMethod -Method Patch -Uri $taskRelease.url -Headers $taskHeaders -ContentType 'application/json' -Body '{"draft":false,"make_latest":"true"}'
        Write-Host "Published: $($taskPublished.html_url)"
    } catch { throw 'Release publication failed. Review GitHub drafts and credentials; no secrets are printed.' }
    finally { $taskHeaders.Remove('Authorization'); $taskHeaders = $null }
} finally { Pop-Location }
