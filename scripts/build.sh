#!/bin/sh
set -e

script_dir=$(cd "$(dirname "$0")" && pwd)
cd "$(git -C "$script_dir" rev-parse --show-toplevel)"

rm -rf out/classes
mkdir -p out/classes

# git ls-files, not find: Git Bash on Windows can resolve find to the Windows one.
git ls-files --cached --others --exclude-standard -- 'src/*.java' > out/sources.txt

javac --release 17 -encoding UTF-8 -d out/classes @out/sources.txt

# Byte 7 of a class file is its major version; 61 is Java 17, which README.txt promises.
set -- $(od -An -tu1 -j6 -N2 out/classes/Main.class)
if [ "$2" -gt 61 ]; then
    echo "[ERROR] Main.class targets a Java newer than 17 (major version $2). Keep --release 17."
    exit 1
fi

echo "[OK]   compiled $(wc -l < out/sources.txt | tr -d ' ') source file(s) into out/classes"
