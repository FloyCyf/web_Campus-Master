#requires -Version 5.1
$ErrorActionPreference = 'Stop'

$version    = '3.9.16'
$zipUrl     = "https://dlcdn.apache.org/maven/maven-3/$version/binaries/apache-maven-$version-bin.zip"
$destRoot   = Join-Path $env:USERPROFILE 'Apps\Apache Maven'
$installDir = Join-Path $destRoot "apache-maven-$version"

if (Test-Path $installDir) {
    Write-Host "[skip] Already installed: $installDir"
    exit 0
}

if (-not (Test-Path $destRoot)) {
    Write-Host "[create] $destRoot"
    New-Item -ItemType Directory -Path $destRoot -Force | Out-Null
}

$tmpZip = Join-Path $env:TEMP "apache-maven-$version-bin.zip"
if (-not (Test-Path $tmpZip) -or (Get-Item $tmpZip).Length -lt 5MB) {
    Write-Host "[1/3] Downloading: $zipUrl"
    Invoke-WebRequest -Uri $zipUrl -OutFile $tmpZip -UseBasicParsing
    Write-Host "        size = $([math]::Round((Get-Item $tmpZip).Length/1MB,2)) MB"
} else {
    Write-Host "[1/3] Reusing cached zip: $tmpZip"
}

Write-Host "[2/3] Extracting to: $destRoot"
Expand-Archive -Path $tmpZip -DestinationPath $destRoot -Force

if (-not (Test-Path $installDir)) {
    throw "Extraction failed: $installDir not found."
}

Write-Host "[3/3] Cleaning up zip"
Remove-Item $tmpZip -Force

Write-Host ""
Write-Host "========================================"
Write-Host "  Maven installed at: $installDir"
Write-Host "========================================"
