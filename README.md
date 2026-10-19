# Rooms

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-0.1.0-blue.svg)](CHANGELOG.md)

A small text adventure in Java. Explore rooms, fight enemies, collect items and reach the village of Villedor.

## Play

```bash
sh scripts/build.sh
java -cp out/classes Main
```

Or download a release zip and run `java -jar rooms.jar`.

`rooms.csv` must sit in the working directory. Its format is described in [map guide.txt](map%20guide.txt), the map itself in [map.png](map.png).

Controls: `N/E/S/W` move, `L` look, `I` inventory, `P` pick up, `D` discard, `H` help, `Q` quit.

## Docs

[Česká dokumentace (Google Docs)](https://docs.google.com/document/d/1T-GgtVTeWU5gGcM30NfqiNvnhVv8UQBvjJlyul5rVAU/edit?usp=sharing)

## Releases

`CHANGELOG.md` is the version. Pushing to `master` publishes: `.github/workflows/release.yml` tags `v<version>`, builds `rooms.jar`, and attaches the zip to the GitHub release. Unzip it and run `java -jar rooms.jar`.

MIT licensed.