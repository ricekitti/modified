#!/usr/bin/env bash
# Build libRiceSlinky.dylib for macOS / Apple Silicon (arm64) and stage it for the launcher.
#
# Run on the Mac. Prerequisites:
#   * Xcode Command Line Tools   (xcode-select --install)
#   * CMake 3.20+                 (brew install cmake)
#   * A JDK for jni.h            -- JAVA_HOME set, or /usr/libexec/java_home resolvable
# The produced dylib is loaded into Lunar's own JVM by the launcher via the Attach API; this build does not
# need Lunar's JVM, only JDK headers.
#
#   ./build-macos.sh                 # configure + build + stage
#   ./build-macos.sh --fresh         # wipe the build dir first
# Extra args after the script name are passed through to the configure step.
set -euo pipefail

here="$(cd "$(dirname "$0")" && pwd)"
build="$here/build/macos"
dist="$here/dist/macos-arm64"
toolchain="$here/cmake/toolchain-macos-arm64.cmake"

if [ "${1:-}" = "--fresh" ]; then rm -rf "$build"; shift; fi

cmake -S "$here" -B "$build" -DCMAKE_TOOLCHAIN_FILE="$toolchain" "$@"
cmake --build "$build" --parallel

mkdir -p "$dist"
cp "$build/libRiceSlinky.dylib" "$dist/"
echo "==> staged: $dist/libRiceSlinky.dylib"
file "$dist/libRiceSlinky.dylib" || true
lipo -archs "$dist/libRiceSlinky.dylib" 2>/dev/null || true
