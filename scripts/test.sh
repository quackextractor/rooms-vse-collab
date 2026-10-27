#!/bin/sh
set -e

script_dir=$(cd "$(dirname "$0")" && pwd)
cd "$(git -C "$script_dir" rev-parse --show-toplevel)"

junit=lib/junit-platform-console-standalone-1.13.4.jar

rm -rf out/test out/test-classes
mkdir -p out/test out/test-classes
cp rooms.csv out/test/rooms.csv

git ls-files --cached --others --exclude-standard -- 'src/*.java' 'test/*.java' > out/test-sources.txt
javac --release 17 -encoding UTF-8 -cp "$junit" -d out/test-classes @out/test-sources.txt

# Runs from out/test so log.txt and savegame.dat never land in the work tree.
cd out/test

# A test that reaches System.exit ends the JVM with code 0 before JUnit prints its summary.
if ! java -jar "../../$junit" execute --class-path ../test-classes --select-package tests \
        --fail-if-no-tests --disable-banner --disable-ansi-colors --details=summary > test.log 2>&1 \
    || ! grep -q 'tests successful' test.log; then
    grep -v '^stty:' test.log || true
    echo "[ERROR] tests failed, or a test ended the game with System.exit. See out/test/test.log."
    exit 1
fi

echo "[OK]   $(grep -o '[0-9]* tests successful' test.log)"
