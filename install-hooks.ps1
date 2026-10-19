<#
.SYNOPSIS
    Installs the repository's git hooks.

.DESCRIPTION
    .git/hooks is not tracked by git, so a hook written there exists only on the machine that wrote
    it. This installs the tracked copies from hooks/ and makes them executable, which is what a fresh
    clone needs before its first commit.

    Two hooks, with deliberately different authority.

      pre-commit - syncs the README version badge to CHANGELOG.md, refuses a changelog link naming a
        version that was never published, then runs `scripts/build.sh` and `scripts/smoke.sh`.
        Corrects the badge and re-stages it rather than refusing; refuses everything else. Aborts the
        commit.

      post-commit - rewrites the reference link block at the foot of CHANGELOG.md, so every version
        links to its real release, and commits it separately so it reverts on its own. It does not
        tag and it does not publish: that is GitHub Actions' job, because a local post-commit fires
        before the commit is on the remote and a tag pushed from here has no source for GitHub to
        show. Neither hook is meant to be bypassed with --no-verify: a gate that can be skipped is a
        suggestion, and --no-verify would switch off the build as well as the version checks.

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

$installed = @('pre-commit', 'post-commit')

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

Write-Host "[INFO] pre-commit syncs the README badge to CHANGELOG.md, refuses a link to a release"
Write-Host "[INFO] that was never published, then runs scripts/build.sh and scripts/smoke.sh."
Write-Host "[INFO] post-commit rewrites the changelog links and commits them separately. Publishing"
Write-Host "[INFO] is not local: pushing to master is, via .github/workflows/release.yml."
Write-Host "[INFO] remove the hooks with: Remove-Item .git/hooks/pre-commit, .git/hooks/post-commit"