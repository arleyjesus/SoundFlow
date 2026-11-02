# Internal helper. Dot-sourcing this file only defines a function; it performs no login.
# Callers must capture the result and never print credential fields.
function Get-SoundFlowGitCredential {
    [CmdletBinding()]
    param(
        [string]$Request = "protocol=https`nhost=github.com`npath=arleyjesus/SoundFlow.git`nusername=arleyjesus`n`n"
    )
    $taskProcess = New-Object System.Diagnostics.Process
    $taskOutput = $null
    try {
        $taskProcess.StartInfo.FileName = (Get-Command git -CommandType Application | Select-Object -First 1).Source
        $taskProcess.StartInfo.Arguments = '-c credential.interactive=false credential fill'
        $taskProcess.StartInfo.WorkingDirectory = (Get-Location).ProviderPath
        $taskProcess.StartInfo.UseShellExecute = $false
        $taskProcess.StartInfo.CreateNoWindow = $true
        $taskProcess.StartInfo.RedirectStandardInput = $true
        $taskProcess.StartInfo.RedirectStandardOutput = $true
        $taskProcess.StartInfo.RedirectStandardError = $true
        # .NET Framework takes Console.InputEncoding when constructing the stdin writer.
        # UTF-8 with a BOM can write its preamble before any protocol bytes, even via BaseStream.
        # Modern .NET exposes StandardInputEncoding; PS 5.1 needs a temporary, restored console setting.
        if ($taskProcess.StartInfo.PSObject.Properties.Name -contains 'StandardInputEncoding') {
            $taskProcess.StartInfo.StandardInputEncoding = [Text.Encoding]::ASCII
            if (!$taskProcess.Start()) { throw 'No se pudo iniciar Git.' }
        } else {
            $taskSavedInputEncoding = [Console]::InputEncoding
            try {
                [Console]::InputEncoding = [Text.Encoding]::ASCII
                if (!$taskProcess.Start()) { throw 'No se pudo iniciar Git.' }
            } finally { [Console]::InputEncoding = $taskSavedInputEncoding }
        }
        $taskOutputRead = $taskProcess.StandardOutput.ReadToEndAsync()
        $taskErrorRead = $taskProcess.StandardError.ReadToEndAsync()
        # Bypass PowerShell's native pipeline encoding (PS 5.1 profiles can add a BOM/NULs).
        # The protocol fields used here are ASCII and end with the required blank line.
        $taskBytes = [Text.Encoding]::ASCII.GetBytes($Request.Replace("`r`n", "`n"))
        $taskProcess.StandardInput.BaseStream.Write($taskBytes, 0, $taskBytes.Length)
        $taskProcess.StandardInput.BaseStream.Flush()
        $taskProcess.StandardInput.Close()
        if (!$taskProcess.WaitForExit(30000)) {
            $taskProcess.Kill()
            throw 'Git tardo demasiado al consultar las credenciales. No se modificaron cuentas.'
        }
        $taskOutput = $taskOutputRead.GetAwaiter().GetResult()
        # Consume stderr privately: helpers/tracing may include sensitive information.
        $null = $taskErrorRead.GetAwaiter().GetResult()
        if ($taskProcess.ExitCode -ne 0) {
            throw "No se pudo leer la credencial del proyecto (Git codigo $($taskProcess.ExitCode)). Revisa la autenticacion."
        }
        return @($taskOutput -split '\r?\n' | Where-Object { $_.Length -gt 0 })
    } finally {
        $taskOutput = $null; $taskBytes = $null; $Request = $null
        $taskProcess.Dispose()
    }
}
