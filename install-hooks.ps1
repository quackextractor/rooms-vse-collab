<#
.SYNOPSIS
    Installs the repository's git hooks.

.DESCRIPTION
    .git/hooks is not tracked by git, so a hook written there exists only on the machine that wrote
    it. This installs the tracked copy from hooks/ and makes it executable, which is what a fresh
    clone needs before its first commit.

    One hook: pre-commit runs `scripts/build.sh` and `scripts/test.sh` and refuses the commit if
    either fails. It is not meant to be bypassed with --no-verify: a gate that can be skipped is a
    suggestion, and --no-verify would switch off the build as well.

    Run it once per clone. Re-run it after pulling if anything under hooks/ changes.
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

    # Copied rather than symlinked: a symlink into the worktree breaks the moment the worktree is
    # moved, and the failure mode is a hook that silently stops existing.
    Copy-Item $source $target -Force

    # Windows has no executable bit, and git for Windows honours a hook whose shebang resolves through
    # its bundled sh, so this is a no-op here and matters on every other platform.
    #
    # -C and a relative pathspec, because neither was load-bearing by default. git update-index without
    # -C runs against the current directory, so from anywhere but a repository it exits 128 and changes
    # nothing; an absolute Windows pathspec is rejected the same way. The failure mode matters more than
    # it sounds: git skips a hook without the executable bit on POSIX, so these would commit cleanly
    # and simply never run on linux or mac, and never once say so.
    & git -C $repoRoot update-index --chmod=+x -- "hooks/$name" 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[WARN] could not set the executable bit on hooks/$name in the git index." -ForegroundColor Yellow
        Write-Host "[WARN] git skips a hook that is not executable on linux and mac, so it would never"
        Write-Host "[WARN] run there. Fix it with: git update-index --chmod=+x -- hooks/$name"
    }

    Write-Host "[OK]   installed $target"
}

Write-Host "[INFO] pre-commit runs scripts/build.sh and scripts/test.sh and refuses the commit if"
Write-Host "[INFO] either fails. Publishing is not local: pushing to master is, via"
Write-Host "[INFO] .github/workflows/release.yml."
Write-Host "[INFO] remove the hook with: Remove-Item .git/hooks/pre-commit"