# v1.8.3 — Scaffold is a Clutch copy — 2026-11-04

- Scaffold now uses the original native Clutch logic and settings directly, with the module name Scaffold.
- Give the copy its own settings, runtime state, random-walk state and callbacks.
- Preserve Clutch's original behavior, conditions, targeting, clicking, rotations, snap-back, block selection, controls and defaults.
- Register this native copy in place of the Mayu-style Scaffold. The previous Java implementation remains inactive.
- Leave the original Clutch module unchanged.
- Preserve prior Displace fixes and all earlier versioned EXEs.
- Set executable/product version to 1.8.3.0.

Verification: 5,981 compiled logic bytes and 829 compiled settings-GUI bytes are identical to original Clutch before relocation. Relocations match after redirecting the clone's 58 mutable aliases and private callbacks. All 1,400 descriptor/settings bytes match after accounting for the Scaffold name and private pointers; 208 bytes of runtime state are independent. The registry selects the new native copy. Four original Clutch source files retain their pre-update SHA-256 values. All 23 existing regression checks pass. The EXE's extracted DLL matches the rebuilt DLL by SHA-256.

This restores Clutch behavior, including its falling/condition gates; it does not retain the earlier Mayu-style bridging behavior. Live Minecraft gameplay has not been tested.
