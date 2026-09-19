#!/usr/bin/env bash
set -euo pipefail

rm -rf build/self-test
mkdir -p build/self-test

javac -d build/self-test \
  auth-core/src/main/java/com/kroxaboom/skazka/auth/*.java \
  tests/AuthCoreSelfTest.java

java -cp build/self-test AuthCoreSelfTest
