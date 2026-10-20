#!/bin/sh
#
# The only automated check the game has: a scripted opening, played headless.
#
# A compile proves the code is well-formed and says nothing about whether the game runs. This covers
# the three things a compile cannot - rooms.csv parses into rooms, an item applies its stat, and a
# stat-gated room opens once the stat is there - which is exactly the path a broken map or a changed
# condition most often breaks.
#
# It is deliberately not a walkthrough of the whole castle. Winning needs weapons, damage rolls and
# enough input to fight six enemies, and a test that fails because a balance number moved is a test
# that gets deleted the first time the numbers are interesting.

set -e

# Resolved from this file's own location, not from the cwd, for the same reason as build.sh.
script_dir=$(cd "$(dirname "$0")" && pwd)
cd "$(git -C "$script_dir" rev-parse --show-toplevel)"

# Played from out/smoke rather than from the repository root. Logic.printToUser appends every line it
# prints to log.txt in the working directory, and log.txt is tracked, so running the game at the root
# would leave the tree dirty after every commit.
rm -rf out/smoke
mkdir -p out/smoke
cp rooms.csv out/smoke/rooms.csv

cd out/smoke

# Pick up the Magical Flower in the Castle Harbour (+10 wisdom, 10 -> 20), walk east into the Castle
# Lobby, which is gated at wisdom 15 and therefore unenterable without it, then inventory and quit.
printf 'P\nMagical Flower\nE\nI\nQ\n' | java -cp ../classes Main > smoke.log 2>&1 || true

if ! grep -q 'Picked up Magical Flower.' smoke.log; then
    echo "[ERROR] smoke: the opening item was never picked up - did rooms.csv or MapLoader change?"
    exit 1
fi

if ! grep -q 'You stand in the grand lobby' smoke.log; then
    echo "[ERROR] smoke: the Castle Lobby never opened. The wisdom gate rejected the player, which"
    echo "        means either the flower stopped granting wisdom or the gate stopped being met."
    exit 1
fi

if grep -qi 'exception' smoke.log; then
    echo "[ERROR] smoke: the game threw during the opening."
    grep -i -A 5 'exception' smoke.log
    exit 1
fi

# The map of discovered rooms. It is printed when the game starts and again after every move, so
# the scripted opening prints it twice: once for the room the player starts in, once for the room
# they walk into. The cell the player stands in carries a *, so it reads [*  1] after moving east.
map_count=$(grep -c 'Discovered map' smoke.log)
if [ "$map_count" -ne 2 ]; then
    echo "[ERROR] smoke: the map was printed $map_count times instead of twice. It belongs at the"
    echo "        start of the game and after every move - check that Player.move calls showMap."
    exit 1
fi

if ! grep -qF '[*  1]' smoke.log; then
    echo "[ERROR] smoke: the map does not mark the room the player walked into. The current room is"
    echo "        drawn with a *, so the cell should read [*  1] after moving east into room 1."
    exit 1
fi

# The other way to start the game, and the one a player hits first: no stdin at all, which is what
# double-clicking the jar produces. It used to end in a NoSuchElementException printed as a bare
# "Exception caught:" with the stack trace on a stream nobody was watching. The check is that the
# game says what happened and how to get a terminal, and that it says it instead of throwing.
printf '' | java -cp ../classes Main > eof.log 2>&1 || true

if ! grep -q 'Input ended' eof.log; then
    echo "[ERROR] smoke: the game no longer reports a missing terminal. It should say 'Input ended'"
    echo "        and name play.cmd; run it with no stdin and look at out/smoke/eof.log."
    exit 1
fi

if grep -qi 'exception' eof.log; then
    echo "[ERROR] smoke: the game threw on empty input instead of reporting it."
    grep -i -A 5 'exception' eof.log
    exit 1
fi

# Byte 7 of a class file is the major version, and 61 is Java 17. README.txt promises 17 or newer, and
# the only way that promise breaks is build.sh losing --release 17 - which nothing else would notice,
# because the JDK that builds it can always run its own output.
set -- $(od -An -tu1 -j6 -N2 ../classes/Main.class)
if [ "$2" -gt 61 ]; then
    echo "[ERROR] smoke: Main.class targets a Java newer than 17 (major version $2), so the release"
    echo "        would refuse to start on a Java 17 machine. Check --release 17 in scripts/build.sh."
    exit 1
fi

# Combat used to open its own Scanner over System.in, after the first one had buffered the piped input,
# so a scripted fight hit EOF at the first Y/N prompt and the game died with a NoSuchElementException.
# One shared reader means the prompt is reachable from the same stream as everything else, which is
# the only reason combat can be part of a headless check at all.
printf 'P\nMagical Flower\nE\nN\nE\nY\n' | java -cp ../classes Main > combat.log 2>&1 || true

if ! grep -q 'You attack Gargoyle' combat.log; then
    echo "[ERROR] smoke: piped input no longer reaches combat. The Gargoyle prompt was never answered"
    echo "        by a real fight - check that PlayerController and Combat share one reader."
    exit 1
fi

if grep -qi 'exception' combat.log; then
    echo "[ERROR] smoke: combat threw on piped input."
    grep -i -A 5 'exception' combat.log
    exit 1
fi

# Rooms 4 and 6 carried an extra comma, which put the Ghost and the Horseless Headless Knight in the
# warning-message column; the rooms loaded with no enemies and the strings were never parsed. Walking
# to them is the only check that proves a row is parsed as data: the prompt names the enemy that was
# loaded, so a missing one is visible even though the fight is never finished.
printf 'P\nMagical Flower\nE\nP\nShield\nP\nSword\nN\nE\nY\nE\nY\nS\nY\nE\n' \
    | java -cp ../classes Main > enemies.log 2>&1 || true

if ! grep -q "name='Ghost'" enemies.log; then
    echo "[ERROR] smoke: room 4 loaded no Ghost. Its row in rooms.csv is shifted - the enemy list"
    echo "        belongs in column 8, the warning message in column 9."
    exit 1
fi

if ! grep -q "name='Horseless Headless Knight'" enemies.log; then
    echo "[ERROR] smoke: room 6 loaded no Horseless Headless Knight. Same shift as room 4."
    exit 1
fi

if grep -qi 'exception' enemies.log; then
    echo "[ERROR] smoke: walking the castle threw."
    grep -i -A 5 'exception' enemies.log
    exit 1
fi

# The guard that keeps the two rows fixed. A warning message shaped exactly like an enemy definition
# cannot be a warning message, so the map is refused instead of loading a room that never fights.
mkdir -p bad
sed 's/Map:wisdom:5,Ghost:40:6:7/Map:wisdom:5,,Ghost:40:6:7/' rooms.csv > bad/rooms.csv

if ( cd bad && java -cp ../../classes Main > bad.log 2>&1 ); then
    echo "[ERROR] smoke: a map with an enemy in the warning column loaded anyway."
    exit 1
fi

if ! grep -q 'warning message is an enemy definition' bad/bad.log; then
    echo "[ERROR] smoke: the shifted map was refused, but not for the reason it should be."
    tail -5 bad/bad.log
    exit 1
fi

# The Secret Chamber is flagged secret in rooms.csv, so it stays off the map until the player walks
# into it. This walk ends in room 5, right next to it: the room is not on the map, and its id is
# nowhere in the log. The flower, the scroll and the map item bring the wisdom to 30, which is what
# the Secret Chamber asks for.
printf 'P\nMagical Flower\nE\nP\nShield\nP\nSword\nN\nP\nScroll\nE\nY\nE\nY\nP\nMap\nS\nY\nQ\n' \
    | java -cp ../classes Main > secret.log 2>&1 || true

if grep -q '101' secret.log; then
    echo "[ERROR] smoke: the Secret Chamber was drawn on the map before the player entered it."
    echo "        The isSecret column in rooms.csv is what keeps it hidden - check that MapView"
    echo "        leaves a secret room out and that the loader reads the flag."
    exit 1
fi

# The Secret Chamber is the cell between the Castle Lobby and the Crossroads. It is right next to
# the room the player stands in, so it is the one cell a broken flag gives away: a hint, an exit the
# player does not know about yet. The cell has to stay blank - the map draws an empty cell as space.
if ! grep -qE '\[   1\] +\[\*  5\]' secret.log; then
    echo "[ERROR] smoke: the Secret Chamber is on the map. The cell between the Castle Lobby and the"
    echo "        Crossroads has to stay blank - a secret room is drawn only once the player is in it."
    exit 1
fi

if grep -qi 'exception' secret.log; then
    echo "[ERROR] smoke: walking to the Crossroads threw."
    grep -i -A 5 'exception' secret.log
    exit 1
fi

# The same walk one step further west. The player is in the Secret Chamber now, so it belongs on the
# map, drawn with the * that marks the current room.
printf 'P\nMagical Flower\nE\nP\nShield\nP\nSword\nN\nP\nScroll\nE\nY\nE\nY\nP\nMap\nS\nY\nW\nQ\n' \
    | java -cp ../classes Main > secret_found.log 2>&1 || true

if ! grep -qF '[*101]' secret_found.log; then
    echo "[ERROR] smoke: the Secret Chamber did not show up on the map after the player entered it."
    echo "        The current room is drawn with a *, so the cell should read [*101]."
    exit 1
fi

if grep -qi 'exception' secret_found.log; then
    echo "[ERROR] smoke: entering the Secret Chamber threw."
    grep -i -A 5 'exception' secret_found.log
    exit 1
fi

echo "[OK]   smoke run passed (smoke.log, eof.log, combat.log, enemies.log, secret.log, secret_found.log, bad/bad.log)"