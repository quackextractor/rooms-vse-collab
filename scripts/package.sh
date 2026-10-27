#!/bin/sh
set -e

script_dir=$(cd "$(dirname "$0")" && pwd)
cd "$(git -C "$script_dir" rev-parse --show-toplevel)"

if [ ! -d out/classes ]; then
    echo "[ERROR] out/classes is missing. Run sh scripts/build.sh first."
    exit 1
fi

mkdir -p out/dist
rm -f out/dist/rooms.jar out/dist/rooms.zip

# Oracle's javapath shims ship no jar, so fall back to the JDK that java reports.
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

"$jar_tool" --create --file out/dist/rooms.jar --main-class Main -C out/classes .

# rooms.csv ships beside the jar because MapLoader reads it from the working directory.
cp rooms.csv out/dist/rooms.csv
cp map.png out/dist/map.png
cp play.cmd out/dist/play.cmd
cp README.txt out/dist/README.txt

# Git for Windows has no zip; a jar without a manifest is a plain zip.
"$jar_tool" --create --file out/dist/rooms.zip --no-manifest \
    -C out/dist rooms.jar -C out/dist rooms.csv -C out/dist map.png \
    -C out/dist play.cmd -C out/dist README.txt

echo "[OK]   packaged out/dist/rooms.jar and out/dist/rooms.zip"
