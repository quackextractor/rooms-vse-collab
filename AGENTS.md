# Agent Directives

Project rules for Rooms. Loaded automatically on every request.
Edit this file, not the README. `CHANGELOG.md` records what has actually shipped.

## Environment

- Windows 11. PowerShell 7 for shell work (`&&` is fine), Git Bash (`sh`) for everything under `hooks/` and `scripts/`.
- No emojis in code, logs, or output. Use `[OK]` / `[WARN]` / `[ERROR]`.
- Use the `edit` tool, not shell line-slicing, for editing files.
- `out/` is scratch and gitignored. Nothing a later session needs goes in the system temp directory.

## Validation

- `sh scripts/build.sh` compiles every file under `src/` with `javac`, tracked or not. `sh scripts/smoke.sh` runs three headless checks: the scripted opening, the no-terminal path, and one combat prompt. `sh scripts/package.sh` builds the release artifact and refuses to run without a version in the changelog.
- **Do not run the first two before committing.** `hooks/pre-commit` runs both and refuses the commit if either fails, so running them first spends the same two seconds twice and changes nothing. `package.sh` is not in the hook, so run it when the release artifact or the `Main-Class` matters.
- Run the smoke check when the change touches `MapLoader`, `rooms.csv`, items, stats, or `RoomCondition`, and watch it fail when you break what it guards. A gate you have not seen go red is a gate you have not tested.
- The smoke run covers the opening, the missing-terminal path and a combat prompt — not a whole game. There is no test framework and adding one for this project is not a trade worth making; extend `smoke.sh` instead when a change needs a check that does not exist.
- CI runs the same two scripts before it publishes anything, so a green local gate is not what makes the release safe — the workflow's is.

## Structure

- `src/` is split by package: `commands`, `enemy`, `items`, `player`, `room`, `utilities`, `myExceptions`. A class that does not fit one of those does not belong yet.
- 300-line limit. `Room.java` is the largest file at 199 and is at the edge of what a class should hold — most of it is getters.
- One purpose per class. No god objects.
- DRY, unless it produces an opaque helper.
- No dependencies. Not one. A library for CSV parsing or a test runner is a bigger cost here than the code it replaces.

## Code documentation

- Write no comments in code. Not `//`, not `/* */`, not Javadoc, not license headers, not prose annotations. Naming carries meaning; a comment restating the line below it is noise.
- No comment blocks in source. The prose belongs in this file and in `CHANGELOG.md`, which is where a reader looks for it.
- If a comment is unavoidable, one short line, and only for something the code cannot say: a non-obvious invariant, a workaround, a format note.
- When existing code carries lengthy comments, delete them as part of the change that touches that code. `ShowItemsCommand.undo()`, `Combat.warn`, and `Player` are the places to start.
- Do not create extra `.md` files to explain code.

## The map

`rooms.csv` is the level, and it is data — the same data as `map.png`, in a format `MapLoader` parses by column position.

- The format is `map guide.txt`. When the loader and the guide disagree, the guide is the specification and the loader is the bug.
- Line 1 is a room. Columns are positional: `id, name, north, east, south, west` are mandatory, then description, items, enemies, warning message, weapons, conditions, isWinRoom, winMessage, isSecret. Empty trailing columns are fine; a missing *leading* one silently shifts everything.
- `-1` is a wall. Room `0` is the start. Room `11` is the win room and its `strength:30` condition is the only one that can end a run.
- `isSecret` is the last column and defaults to false. A secret room stays off the map the player sees until the player walks into it, so the flag is the only thing hiding it - the name does not.
- **A comma followed by a space is fine; a comma followed by anything else is not.** `MapLoader` splits on `,(?!\s)` — a comma *not* followed by whitespace — specifically so that "a nice room, full of nice things." survives as one column, which is how the guide's own example is written. What silently corrupts the map is a comma with no space after it: the column splits there, everything after shifts left, and the room loads as something that makes no sense rather than as an error.
- Add a room to `rooms.csv` *and* to `map.png`. They are two views of one level and a map that disagrees with the game is worse than no map.
- Enemies are listed as `Name:health:strength:agility`, semicolon-separated. Agility decides who strikes first: the player opens when their agility is higher than the enemy's, otherwise the enemy does. `Imp Assistant:15:5:30` has 15 health, hits first against a fresh player, and dies in three hits.

## Commands

- A command is a `Command` implementing `execute()` and `undo()`. Movement, pick-up and discard are undoable through `PlayerController`'s stack; `L` is not, and its `undo()` says so rather than pretending.
- Adding a command means three edits: the class, the `switch` in `PlayerController`, and the help text — which is written out twice, in `Game` and again in the `H` case. Update both or neither; they are already two copies of the same sentence.
- All input goes through `utilities.Console.readLine()`, which owns the one `Scanner` over `System.in` and reports EOF instead of throwing. `PlayerController` and `Combat` share it. Do not create a second reader: one over the same stream buffers lines the other cannot see, which is what made scripted combat impossible and what the combat check in `smoke.sh` now guards.

## Commits

- Conventional Commits 1.0.0.
- Never `--no-verify` past `hooks/pre-commit`.
- After three failed attempts on a build or a fix, stop and report each attempt with the exact error and the diagnosis. Never disable the smoke check to force a pass.
- **Describe the change, not the author.** No `Co-Authored-By:`, no `Generated with`, no tool name, no model name, no `noreply@` address. Conventional Commits is a spec for the change; a trailer naming a tool is not part of it.

## Versioning

- `CHANGELOG.md` is the single source of truth. The newest `## [x.y.z]` heading is the version; `Unreleased` does not count.
- **Pushing to `master` publishes.** `.github/workflows/release.yml` gates on `scripts/build.sh` + `scripts/smoke.sh`, then tags `v<x.y.z>`, builds `scripts/package.sh` output and attaches it to the GitHub release. It is idempotent per release and never moves a tag. Nothing local publishes anything.
- Tags belong to CI alone. Do not create them in a hook: a local tag and a CI tag land on different commits and `git fetch --tags` refuses to reconcile them.
- `hooks/pre-commit` syncs the README `version-` badge to the changelog and refuses a link to a version that is neither tagged nor the newest heading. `hooks/post-commit` writes that link block — published versions from tags, the newest version as pending — and commits it separately. It touches no network.
- **`git fetch --tags` after CI publishes.** The block is built from local tags, so a release the clone has not fetched loses its link on the next commit, and the hook has no way to know it exists.
- To ship: rename `Unreleased` to the new version with today's date, write the section, commit, push. CI does the rest.
- `install-hooks.ps1` installs both hooks; `.git/hooks` is not tracked, so a fresh clone needs it once.

## Artefacts

- Everything a session produces goes under `out/`. Nothing meant to be read later goes in the system temp directory.
- `log.txt` is the game's own log: `Logic.printToUser` appends every line it prints, in the working directory, so the game writes there whenever it runs. `scripts/smoke.sh` therefore plays from `out/smoke` with its own copy of `rooms.csv` — a gate that dirties a tracked file is a gate that gets uninstalled.

## Response format

- No articles (`a`, `an`, `the`), no filler (`just`, `really`, `basically`), no pleasantries (`sure`, `certainly`).
- No preamble or postamble. Start with the answer, stop when the facts are delivered.
- Action first. Explain only when asked.
- Keep technical data exact and code blocks unchanged.