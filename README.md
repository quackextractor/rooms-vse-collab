# Rooms

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A small text adventure in Java. Explore rooms, fight enemies, collect items and reach the village of Villedor.

## Play

```bash
sh mvnw package
java -jar target/rooms.jar
```

Or download a release zip and double-click `play.cmd` (`java -jar rooms.jar` from a terminal also works; double-clicking the jar itself does not, because it gets no terminal to read from).

`rooms.csv` must sit in the working directory. Its format is described in [map guide.txt](map%20guide.txt), the map itself in [map.png](map.png).

Goal, rules, controls and saving: [README.txt](README.txt), which also ships in the release zip.

## Docs

[Česká dokumentace (Google Docs)](https://docs.google.com/document/d/1T-GgtVTeWU5gGcM30NfqiNvnhVv8UQBvjJlyul5rVAU/edit?usp=sharing)

## Authors

- Miro Slezák (LostSoul / quackextractor)
- Natálie Benešová (nataliebenesova07)
- Klaudia Onderová (f4kfv6z8sb-star)
- Vu Anh (Vutify / VietnameseDelegation)

## Playtester
Martin Morávek VŠE

## Releases

Pushing to `master` publishes: `.github/workflows/release.yml` builds `rooms.jar`, tags the commit and attaches the zip to the GitHub release. Unzip it and run `java -jar rooms.jar`.

## Build

Maven, through the wrapper, so only a JDK 17+ is needed (`mvnw.cmd` on Windows without Git Bash):

- `sh mvnw test` compiles and runs the JUnit tests.
- `sh mvnw package` also builds `target/rooms.jar` and the release zip `target/rooms.zip`.

IntelliJ opens the project from `pom.xml`.

MIT licensed.