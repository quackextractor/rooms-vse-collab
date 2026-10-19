#!/bin/sh
#
# Packages what a release actually contains: a runnable jar and the map beside it.
#
# Two files, not one, because Main opens rooms.csv from the *working directory* rather than from the
# classpath (`new FileReader(filePath)` in MapLoader). A jar that carried the csv inside it would need
# a loader change - read the working directory first, fall back to a classpath resource - before it
# ran anywhere but next to its source. The other option was chosen against for now; a single
# downloadable file is the better artifact and the loader fix is about six lines, so revisit it when
# there is a reason to.
#
# `jar` builds the zip. Git for Windows ships no `zip`, and a .zip made by `jar` is a zip: the format
# is the container, the manifest is just one more entry in it, and both Windows Explorer and `unzip`
# open the result without being told anything about how it was made.

set -e

# Resolved from this file's own location, not from the cwd, for the same reason as build.sh.
script_dir=$(cd "$(dirname "$0")" && pwd)
cd "$(git -C "$script_dir" rev-parse --show-toplevel)"

if [ ! -d out/classes ]; then
    echo "[ERROR] out/classes is missing. Run sh scripts/build.sh first."
    exit 1
fi

version=$(grep -a -o '^## \[[0-9][^]]*\]' CHANGELOG.md \
    | head -1 \
    | sed 's/^## \[//; s/\]$//')

if [ -z "$version" ]; then
    echo "[ERROR] CHANGELOG.md has no released version heading, so there is nothing to package as."
    exit 1
fi

mkdir -p out/dist
rm -f out/dist/rooms.jar "out/dist/rooms-$version.zip"

# `jar` is not always on PATH. Oracle's javapath shims ship java, javac and javaw but no jar, so a
# machine with a perfectly good JDK installed can fail on "jar: command not found". Ask the JVM where
# it lives rather than guessing: java.home is the JDK root, and the backslashes it prints become
# forward slashes because MSYS resolves those and not the other kind.
jar_tool=""
if command -v jar > /dev/null 2>&1; then
    jar_tool=$(command -v jar)
else
    java_home=${JAVA_HOME:-$(java -XshowSettings:properties -version 2>&1 \
        | sed -n 's/^ *java\.home = //p' \
        | tr '\\' '/')}

    for candidate in "$java_home/bin/jar" "$java_home/bin/jar.exe"; do
        if [ -x "$candidate" ]; then
            jar_tool="$candidate"
            break
        fi
    done
fi

if [ -z "$jar_tool" ]; then
    echo "[ERROR] no jar tool found. Set JAVA_HOME to a JDK, or put jar on PATH."
    exit 1
fi

# --main-class is what turns a pile of class files into something `java -jar` will start.
"$jar_tool" --create --file out/dist/rooms.jar --main-class Main -C out/classes .

cp rooms.csv out/dist/rooms.csv

"$jar_tool" --create --file "out/dist/rooms-$version.zip" -C out/dist rooms.jar -C out/dist rooms.csv

echo "[OK]   packaged out/dist/rooms.jar and out/dist/rooms-$version.zip (version $version)"