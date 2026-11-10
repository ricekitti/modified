# v1.8.2 — Scaffold: Slinky planner, Mayu behavior — 2026-11-04

- Replace Scaffold's support search and face aiming with an independent Java port of Slinky Clutch targeting.
- Use Slinky's actual candidate table, dominant-axis face choice, clamped face points, angular cost and previous-support continuity.
- Adapt the planner to Scaffold's predicted feet and requested building height, with deterministic aim and ray/reach validation.
- Retain the existing Mayu-style Default, Backwards and Sideways modes, movement correction, Keep Y, Tower, Safe Walk, multi-place, hotbar switching, block counter and target display.
- Preserve the existing 12 settings and their saved profile keys.
- Keep Clutch independent: its source files, callbacks, settings and state are unchanged. Scaffold owns its own planner state and lifecycle.
- Keep v1.8.1 Displace controls and values.
- Update launcher and Windows file/product version to 1.8.2.0.

Validation: 23/23 CTest checks passed. Native JVM checks cover named, SRG and official mappings, placement, 1,040 movement/sneak combinations per mapping, packet/model/camera rotation separation, input routing, slot requests, GUI pause, disable and reinjection. A further 159 backend/bridging assertions pass per mapping, including all three Scaffold modes on all four sides, reach and unsupported-target rejection, warmup, free camera and slot restoration. The prior 17 source behavior assertions pass per mapping. The build verifies 25 retained Scaffold policy methods against the supplied source. Four Clutch source hashes match their pre-update copies. The extracted EXE payload matches the rebuilt DLL by SHA-256; payload classes target Java 8.

Live Minecraft/Lunar gameplay and server behavior have not been tested.
