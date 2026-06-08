$ErrorActionPreference = 'Stop'
try {
    $resp = Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/' -UseBasicParsing
    $versions = $resp.Links |
        ForEach-Object { $_.href } |
        Where-Object { $_ -match '^3\.\d+\.\d+/?$' } |
        ForEach-Object { $_.TrimEnd('/') } |
        Sort-Object { [version]($_ -replace '^3\.','') } -Descending
    Write-Host "Available versions on dlcdn.apache.org:"
    $versions | Select-Object -First 8 | ForEach-Object { Write-Host "  $_" }
} catch {
    Write-Host "dlcdn failed: $($_.Exception.Message)"
    Write-Host "Trying archive.apache.org..."
    try {
        $resp = Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/' -UseBasicParsing
        $versions = $resp.Links |
            ForEach-Object { $_.href } |
            Where-Object { $_ -match '^3\.\d+\.\d+/?$' } |
            ForEach-Object { $_.TrimEnd('/') } |
            Sort-Object { [version]($_ -replace '^3\.','') } -Descending
        Write-Host "Available versions on archive.apache.org:"
        $versions | Select-Object -First 8 | ForEach-Object { Write-Host "  $_" }
    } catch {
        Write-Host "archive.apache.org also failed: $($_.Exception.Message)"
    }
}
