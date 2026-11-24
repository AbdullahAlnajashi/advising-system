#!/usr/bin/env sh
# Compiles the sources and tests, then runs the full test suite.
set -e
cd "$(dirname "$0")"
rm -rf out
javac -encoding UTF-8 -d out $(find src test -name '*.java')
java -cp out AllTests
