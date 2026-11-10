# v1.8.4 — Scaffold: all 14 controls — 2026-11-04

Retain the isolated Clutch-derived candidate-order, face-selection, clamped-aim
and support-continuity planner. Add the requested building and movement
behaviors around it. The original Clutch module remains unchanged.

| Setting | Modes / values | Default |
| --- | --- | --- |
| rotations | NONE, DEFAULT, BACKWARDS, SIDEWAYS | BACKWARDS |
| move-fix | NONE, SILENT | SILENT |
| sprint | NONE, VANILLA | NONE |
| ground-motion | 0–100% | 100% |
| air-motion | 0–100% | 100% |
| speed-motion | 0–100% | 100% |
| tower | NONE, VANILLA, EXTRA, TELLY | NONE |
| keep-y | NONE, VANILLA, EXTRA, TELLY | NONE |
| keep-y-on-press | On / Off | Off |
| multi-place | On / Off | On |
| safe-walk | On / Off | On |
| swing | On / Off | On |
| item-spoof | On / Off | Off |
| block-counter | On / Off | On |

- NONE rotations leave the camera/view ray alone; the other modes require a valid placement.
- SILENT move-fix preserves intended movement under placement rotations without changing physical keys.
- Apply ground/air/Speed input scaling and the Keep-Y/Telly sprint exceptions.
- Add the VANILLA and EXTRA tower cycles and TELLY takeoff behavior.
- Add stored-height Keep-Y, EXTRA recovery, TELLY delay, and the use-button activation gate. Show keep-y-on-press only when keep-y is enabled.
- Multi-place attempts the initial placement and up to three additional placements.
- Safe Walk clips grounded ledge movement. Swing off still sends an animation packet.
- Item spoof shows the saved selection in the hotbar and first/third-person renders. Render scopes restore placement inventory and cached equipment, including renderer failures.
- The toggleable block counter counts only suitable blocks in the nine hotbar slots and uses the existing movable HUD anchor.
- Preserve GUI/input-loss, world-change, disable and unload cleanup; preserve the earlier Displace settings.
- Use a separate scaffold_features_ profile namespace with exact requested keys and percentage tooltips.
- Upgrade the bootstrap hook schema to SlinkyHooks19 (36 JNI declarations) for the render hooks. Restart Minecraft before injecting this update.
- Set executable/product version to 1.8.4.0. Preserve previous versioned EXEs.

Validation: all 23 CTest checks passed. Each of named, SRG and official mappings
also passes 70 requested-feature assertions, 32 controller behavior assertions
and 159 Clutch planner geometry/bridging assertions. Native JVM checks cover
movement direction/speed, rotation/camera/packet separation, item spoof and
renderer-failure restoration, GUI pause, slot cleanup and reinjection. The
build preserves 31 controller methods against pinned Mayu source; targeting
is explicitly routed to the separate Clutch planner. Four original Clutch
source hashes match the pre-update copies. All three payloads use Java 8
bytecode. The extracted final EXE DLL matches the rebuilt DLL by SHA-256.

Live Minecraft/Lunar gameplay, visual appearance and server behavior have not
been tested.
