#requires -Version 5.1
$ErrorActionPreference = 'Stop'

$installDir = 'C:\Users\Floy\Apps\Apache Maven\apache-maven-3.9.16'
$mvnExe     = Join-Path $installDir 'bin\mvn.cmd'

if (-not (Test-Path $mvnExe)) {
    throw "Maven binary not found: $mvnExe"
}

$target = 'User'

# 1) MAVEN_HOME
[Environment]::SetEnvironmentVariable('MAVEN_HOME', $installDir, $target)
Write-Host "[ok] MAVEN_HOME = $installDir"

# 2) PATH -- append %MAVEN_HOME%\bin if missing
$currentPath = [Environment]::GetEnvironmentVariable('Path', $target)
$marker      = '%MAVEN_HOME%\bin'

$hasMarker  = $currentPath -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ -ieq $marker } | Select-Object -First 1
$hasReal    = $currentPath -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ -ieq "$installDir\bin" } | Select-Object -First 1

if ($hasMarker -or $hasReal) {
    Write-Host "[ok] PATH already contains Maven bin (no change)"
} else {
    $newPath = $currentPath.TrimEnd(';') + ';' + $marker
    [Environment]::SetEnvironmentVariable('Path', $newPath, $target)
    Write-Host "[ok] PATH updated -> appended $marker"
}

# 3) Mirror: also reflect in current process for the rest of this script
$env:MAVEN_HOME = $installDir
$env:Path      = $env:Path + ';' + (Join-Path $installDir 'bin')

# 4) Verify
Write-Host ""
Write-Host "=== mvn -version ==="
& $mvnExe -version

Write-Host ""
Write-Host "=== where.exe mvn (in a fresh child process) ==="
$probe = Start-Process -FilePath 'where.exe' -ArgumentList 'mvn' -PassThru -WindowStyle Hidden -Wait
# where.exe writes to stdout, captured via OutputDataAvailable below
$probe | Out-Null
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = 'where.exe'
$psi.Arguments = 'mvn'
$psi.RedirectStandardOutput = $true
$psi.UseShellExecute = $false
$p = [System.Diagnostics.Process]::Start($psi)
$p.WaitForExit()
$p.StandardOutput.ReadToEnd().TrimEnd() | ForEach-Object { Write-Host "  $_" }
