# slinky — modified

The Slinky Minecraft 1.8.9 client reconstruction (see the sibling `regular` repository
for the base) **extended with ported feature modules**. This tree keeps the readable
client source and injector from the base build and adds a set of module ports, a
feature-integration bridge, and versioned releases.

> Reverse-engineering reconstruction for research and personal use. Private repository.

## Core

| Path | What it is |
| --- | --- |
| `slinkyreadablesource/slinkyreadablesource/` | The client (`slinky.dll`) — readable reconstruction, built with clang-cl. See the base repo for the `src/` layout. |
| `injector/` | C# WinForms injector (`Injector.cs` → `RiceInjector.exe`). |
| `provided-injector/rice-launcher/` | C++ loader variant (CMake). |
| `feature-bridge/` | Integration layer wiring the ported modules into the client. |
| `releases/` | Built release EXEs (versioned). |
| `forge/` | Vendored Minecraft Forge, kept as reference material. |

## Ported modules

Each `*-port/` directory is a self-contained feature port staged for integration
through `feature-bridge/`.

- **Movement** — `displace-legit-port`, `displace-handoff-20261104`, `noslow-port`
- **Clutch / BedWars** — `auto-pearl-port`, `fireball-pearl-clutch-port`,
  `bridge-egg-clutch-port`, `bridge-egg-strict-wait-port`, `bedplates-port`,
  `rescue-priority-accuracy-fix-port`, `quickbuy-port`
- **Scaffold** — `scaffold-port`, `scaffold-supplied-20261103`,
  `scaffold-payload-check-*`
- **Visual** — `player-esp-armor-port`
- **Utility / config** — `valen-settings-port`, `valen-utility-rescue-fix-port`,
  `valen-selected-ref-20261105`
- **Anticheat reference** — `snowify-anticheat-port`

Feature notes and verification records sit at the tree root: `DISPLACE.md`,
`FIREBALL-PEARL.md`, `ANTIFIREBALL.md`, `HEAVY-GLOW.md`, `HUD-EDITOR.md`, and the
`*-VERIFIED.json` files.

## Build

### Client (`slinky.dll`)

Requires LLVM (clang-cl, lld-link, llvm-mt), no Windows SDK:

```powershell
cd slinkyreadablesource/slinkyreadablesource
cmake -S . -B build/cmake -DCMAKE_TOOLCHAIN_FILE=cmake/toolchain-windows-clang-cl.cmake
cmake --build build/cmake
```

### Injector (`RiceInjector.exe`)

```powershell
powershell -File injector/build.ps1
```

## Usage

1. Build, or use a prebuilt loader from `releases/`.
2. Launch Minecraft 1.8.9 (vanilla or Lunar).
3. Inject `slinky.dll` into the Java process.
4. Open the in-game GUI with the configured key bind and toggle modules.

## Release convention

Each client update ships as a separate versioned EXE (version + update name in the
filename) with a matching changelog; previous versioned EXEs are preserved, and the
launcher project/version resources stay consistent with the release version.
