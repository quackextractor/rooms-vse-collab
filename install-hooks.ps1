<#
.SYNOPSIS
    Installs hooks/pre-commit into .git/hooks. Run once per clone.
#>

[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not (Test-Path (Join-Path $repoRoot '.git'))) {
    Write-Host "[ERROR] $repoRoot is not a git repository." -ForegroundColor Red
    exit 1
}

$installed = @('pre-commit')

foreach ($name in $installed) {
    $source = Join-Path $repoRoot "hooks/$name"
    $target = Join-Path $repoRoot ".git/hooks/$name"

    if (-not (Test-Path $source)) {
        Write-Host "[ERROR] no tracked hook at $source" -ForegroundColor Red
        exit 1
    }

    Copy-Item $source $target -Force

    # git skips a hook without the executable bit on Linux and macOS.
    & git -C $repoRoot update-index --chmod=+x -- "hooks/$name" 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[WARN] could not set the executable bit on hooks/$name in the git index." -ForegroundColor Yellow
        Write-Host "[WARN] Fix it with: git update-index --chmod=+x -- hooks/$name"
    }

    Write-Host "[OK]   installed $target"
}

Write-Host "[INFO] pre-commit runs 'mvnw test' and refuses the commit if the build or the tests fail."
Write-Host "[INFO] remove the hook with: Remove-Item .git/hooks/pre-commit"
