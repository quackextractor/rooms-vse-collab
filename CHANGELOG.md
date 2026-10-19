# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.1] - 2026-10-19

### Fixed

- Releases carry `map.png`, so a downloaded release can be played without a second visit to the repository
- The release zip no longer contains a `META-INF/` folder; it holds the jar, the map and the picture

## [0.1.0] - 2026-10-19

### Added

- Movement, look and inventory commands with undo
- Items, weapons, enemies and turn-based combat
- CSV map loader, 13-room castle level and secret chamber
- Stat-gated rooms and win condition
- `scripts/package.sh`: a runnable `rooms.jar` and a zip carrying it with `rooms.csv`
- GitHub Actions releases on push to master: tag, build, publish the artifact

<!--
The reference links at the foot of this file are written by hooks/post-commit. A version is linked
once it is tagged (published) or when it is the newest heading here, since the release is made by CI
from the next push (pending). `pre-commit` refuses any other link.
-->
[Unreleased]: https://github.com/quackextractor/rooms-vse-collab/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/quackextractor/rooms-vse-collab/releases/tag/v0.1.1
[0.1.0]: https://github.com/quackextractor/rooms-vse-collab/releases/tag/v0.1.0
