# Scaffold — Reproducible Live Validation Procedure

Live validation has not been performed. Hook firing, mutable-field write-back under load,
cancellation, event priority and real packet ordering require the built DLL injected into a
live 1.8.9 client. Keep results labeled **live** and separate from the headless fixtures.

## Build artifacts under test
- Java payload: `feature-bridge/build/features-{named,srg,official}.jar` (via `feature-bridge/build.py`)
- Native DLL: `slinkyreadablesource/slinkyreadablesource/build/slinky.dll` (via ninja/clang-cl)
- Launcher/loader: the existing `rice-loader-*.exe` / `injector/`.

## A. Hook-firing + injection (prereq for any behavior claim)
1. Build payload + DLL; launch the loader into Minecraft 1.8.9; join a test world/server.
2. Open the native menu; confirm **Scaffold** exposes 39 parent settings plus the two submode children
   (41 settings / 49 native rows). Confirm Vk and Wa run their independent experimental handlers.
3. With a temporary `slinky_logf` in `hook_walking_player` (and the PreMotion/PostMotion dispatch),
   enable Scaffold and confirm the log shows the wrap hook firing once per tick (PreMotion before,
   PostMotion after `onUpdateWalkingPlayer`). **If it does not fire, the `Site` row for the
   `onUpdateWalkingPlayer` invoke in `EntityPlayerSP.onUpdate` (obf `t_` / MCP `onUpdate` / SRG
   `func_70071_h_`) did not match — fix the mapping and rebuild before proceeding.** Repeat under all
   three naming formats (named/SRG/official payloads).

## B. Packet ordering (the C03/C08 question — currently UNRESOLVED)
4. Attach a packet logger (client-side: log every outbound packet in `addToSendQueue`; or a proxy/
   Wireshark with the session key) while bridging on flat ground, mode = Normal.
5. Record the per-tick outbound sequence and classify: `C03/C04/C05/C06PacketPlayer*` (movement, carries
   yaw/pitch) vs `C08PacketPlayerBlockPlacement` (placement) vs `C0APacketAnimation` (swing).
6. **Establish the order empirically** — do not assume rotation→movement→placement. Record what the
   port actually emits.
7. To compare against Rise's ORIGINAL sequence, capture an independent reference trace from the
   original Rise 6.9.7 build under the same scenario (the headless fixture cannot stand in for this —
   it only tests the port's own emitted order). Diff the two traces.

## C. Behavior checks (per mode, live)
8. Normal: walk off an edge; confirm blocks place under the path, reach/raycast respected, no ghost
   placements, correct hotbar block selected/synced.
9. Rotation separation: in silent/server mode the camera must NOT snap while the server receives the
   placement rotation (verify via the packet trace vs the on-screen camera).
10. Bookkeeping: confirm no rubber-banding from corrupted movement dedup — i.e. temporary
    position/rotation/ground changes are restored so `lastReportedPosX/Y/Z` / prev-yaw bookkeeping in
    `onUpdateWalkingPlayer` is intact.
11. Repeat per mode (Godbridge/Breesily/Snap/Telly/Eagle) and per submode as each lands.

## D. Regression
12. Confirm Knockback Displace / Bow Aimbot / Fog / Shaders still enable, show settings, and behave as
    before (Scaffold is additive feature slot 4; existing slots 0–3 unchanged).

## Reporting rule
Report A–D results as **live** findings. Do not claim any server bypass or end-to-end success from
the headless fixtures or source analysis alone.
