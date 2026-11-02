[CmdletBinding()]
param()

# Local regression only: synthetic complete credentials bypass helpers, network and real tokens.
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Get-SoundFlowGitCredential.ps1')
$taskOriginalEncoding = $OutputEncoding
$taskOriginalConsoleInput = [Console]::InputEncoding
$taskOriginalPreamble = [Convert]::ToBase64String($taskOriginalConsoleInput.GetPreamble())
$taskCases = @(
    [Text.Encoding]::ASCII,
    (New-Object Text.UTF8Encoding($true)),
    [Text.Encoding]::Unicode
)
try {
    foreach ($taskEncoding in $taskCases) {
        $OutputEncoding = $taskEncoding
        foreach ($taskEnding in @("`n", "`r`n")) {
            $taskRequest = (@('protocol=https', 'host=soundflow-test.invalid', 'username=synthetic',
                'password=SOUNDFLOW_SYNTHETIC_NOT_A_SECRET', '', '') -join $taskEnding)
            $taskResult = @(Get-SoundFlowGitCredential -Request $taskRequest)
            if ([Console]::InputEncoding.CodePage -ne $taskOriginalConsoleInput.CodePage -or
                [Convert]::ToBase64String([Console]::InputEncoding.GetPreamble()) -ne $taskOriginalPreamble) {
                throw 'The original console input encoding was not restored.'
            }
            if ($taskResult -notcontains 'protocol=https' -or $taskResult -notcontains 'host=soundflow-test.invalid' -or
                $taskResult -notcontains 'username=synthetic' -or $taskResult -notcontains 'password=SOUNDFLOW_SYNTHETIC_NOT_A_SECRET') {
                throw 'Credential protocol regression failed. No real credential was used.'
            }
        }
        Write-Output "PASS PowerShell $($PSVersionTable.PSVersion): $($taskEncoding.WebName), LF and CRLF"
    }
} finally { $OutputEncoding = $taskOriginalEncoding; $taskResult = $null; $taskRequest = $null }
