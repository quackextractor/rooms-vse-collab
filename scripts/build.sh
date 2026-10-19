#!/bin/sh
#
# The entire build: javac, no dependency manager, no build tool, no test framework.
#
# Every source file at once, because the package graph is small and the alternative - compiling only
# what changed - needs a dependency graph this project does not have. It takes about two seconds.
#
# Output goes to out/classes, which is gitignored, so running this can never dirty the tree.

set -e

# Resolved from this file's own location, not from the cwd, so `sh /path/to/scripts/build.sh` builds
# this repository rather than whichever one the shell is standing in.
script_dir=$(cd "$(dirname "$0")" && pwd)
cd "$(git -C "$script_dir" rev-parse --show-toplevel)"

rm -rf out/classes
mkdir -p out/classes

# git ls-files rather than find: git for windows ships sh with C:\Windows\System32 ahead of its own
# /usr/bin in PATH, so `find` is the Microsoft one that prints "File not found - *.java" and exits 0.
# A build script that silently compiles nothing is worse than one that fails.
git ls-files -- 'src/*.java' > out/sources.txt

# -encoding UTF-8 is not optional: the map, the changelog and any Czech text in a room description are
# all non-ASCII, and javac falls back to the platform default without it, which is a different answer
# on every machine.
#
# --release 17 and not the JDK that happens to be installed. Without it the class files carry the
# compiler's own version - 68 under JDK 24 - so the released jar refuses to start on the Java 17 or 21
# an ordinary machine has, for no reason the code asks for. The source uses nothing newer than 17, so
# 17 is the honest floor and the artifact says so.
javac --release 17 -encoding UTF-8 -d out/classes @out/sources.txt

echo "[OK]   compiled $(wc -l < out/sources.txt | tr -d ' ') source file(s) into out/classes"