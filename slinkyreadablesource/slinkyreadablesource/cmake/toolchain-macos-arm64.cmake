# Build the macOS arm64 client dylib (libRiceSlinky.dylib) for Apple Silicon.
#
#   cmake -S . -B build/macos -DCMAKE_TOOLCHAIN_FILE=cmake/toolchain-macos-arm64.cmake
#   cmake --build build/macos
#
# This matches a native arm64 Lunar JVM: a JNI/JVMTI agent must share the JVM's architecture, and a 2023
# MacBook Pro runs an arm64 JVM. Building natively on the Mac does not strictly need a toolchain file; this
# one pins the architecture and deployment target so the result is reproducible and so a future x86_64 slice
# (for a Rosetta JVM) is a one-line change (CMAKE_OSX_ARCHITECTURES=x86_64).
set(CMAKE_SYSTEM_NAME Darwin)
set(CMAKE_SYSTEM_PROCESSOR arm64)
set(CMAKE_OSX_ARCHITECTURES arm64 CACHE STRING "" FORCE)

# 11.0 is the first macOS with arm64; the client uses nothing newer. Raise if a feature later needs it.
set(CMAKE_OSX_DEPLOYMENT_TARGET 11.0 CACHE STRING "" FORCE)

find_program(SLINKY_CLANG   clang   REQUIRED)
find_program(SLINKY_CLANGXX clang++ REQUIRED)
set(CMAKE_C_COMPILER   "${SLINKY_CLANG}")
set(CMAKE_CXX_COMPILER "${SLINKY_CLANGXX}")

# Agent entry points (Agent_OnAttach/Agent_OnLoad) must be exported; everything else can be hidden so the
# dylib does not collide with the game's own symbols. Individual exports are marked in the source with
# __attribute__((visibility("default"))).
set(CMAKE_C_VISIBILITY_PRESET   hidden)
set(CMAKE_CXX_VISIBILITY_PRESET hidden)
set(CMAKE_VISIBILITY_INLINES_HIDDEN ON)
