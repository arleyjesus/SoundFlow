[CmdletBinding()]
param([switch]$RequireWrite)

# Read-only identity/access check. Never prints tokens or changes saved accounts.
$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Push-Location $taskRoot
$taskHeaders = $null
try {
    . (Join-Path $PSScriptRoot 'Get-SoundFlowGitCredential.ps1')
    $taskCredential = @(Get-SoundFlowGitCredential)
    $taskTokenLine = $taskCredential | Where-Object { $_ -like 'password=*' } | Select-Object -First 1
    if (!$taskTokenLine) { throw 'La credencial no incluye un token utilizable.' }
    $taskHeaders = @{ 'User-Agent' = 'SoundFlow-access-check'; Accept = 'application/vnd.github+json';
        Authorization = 'Bearer ' + $taskTokenLine.Substring(9) }
    $taskCredential = $null; $taskTokenLine = $null; $taskInput = $null
    try {
        $taskUser = Invoke-RestMethod -Uri 'https://api.github.com/user' -Headers $taskHeaders
        $taskRepo = Invoke-RestMethod -Uri 'https://api.github.com/repos/arleyjesus/SoundFlow' -Headers $taskHeaders
    } catch { throw 'No se pudo verificar la cuenta con GitHub. Revisa la conexion y la autenticacion.' }
    [PSCustomObject]@{ Account = $taskUser.login; Repository = $taskRepo.full_name; CanPush = [bool]$taskRepo.permissions.push }
    if ($RequireWrite -and !$taskRepo.permissions.push) {
        throw "La cuenta autenticada '$($taskUser.login)' no tiene permiso para subir a arleyjesus/SoundFlow."
    }
} finally {
    if ($taskHeaders) { $taskHeaders.Remove('Authorization') }
    $taskHeaders = $null; $taskCredential = $null; $taskTokenLine = $null; $taskInput = $null
    Pop-Location
}
