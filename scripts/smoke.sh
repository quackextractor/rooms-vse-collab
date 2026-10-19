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
# would leave the tree dirty after every commit - which in turn makes post-commit skip its work and
# quietly turns the release flow into a no-op.
rm -rf out/smoke
mkdir -p out/smoke
cp rooms.csv out/smoke/rooms.csv

cd out/smoke

# Pick up the Magical Flower in the Castle Harbour (+10 wisdom, 10 -> 20), walk east into the Castle
# Lobby, which is gated at wisdom 15 and therefore unenterable without it, then inventory and quit.
printf 'P\nMagical Flower\nE\nI\nQ\n' | java -cp ../classes Main > smoke.log 2>&1

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

# The other way to start the game, and the one a player hits first: no stdin at all, which is what
# double-clicking the jar produces. It used to end in a NoSuchElementException printed as a bare
# "Exception caught:" with the stack trace on a stream nobody was watching. The check is that the
# game says what happened and how to get a terminal, and that it says it instead of throwing.
printf '' | java -cp ../classes Main > eof.log 2>&1

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

echo "[OK]   smoke run passed (out/smoke/smoke.log, out/smoke/eof.log)"