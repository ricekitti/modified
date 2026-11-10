Current update: v1.8.1, `releases/rice-loader-v1.8.1-displace-photo-settings.exe`.
Screenshot values/layout replace the earlier defaults; see its `.changes.md`.

# Supplied manual Displace — 2026-11-04

Current executable: `../slinky-modified.exe` or `rice-loader.exe` in this folder.
Restart Minecraft before injecting the replacement build.

The supplied `KnockbackDisplaceModule.java`, `RavenBsVoidScanner.java` and
`myau/RotationState.java` compile verbatim into the native feature bridge.
The existing Knockback Displace registry entry now dispatches to that Java
state machine. The previous native OG/Void extension is inactive.

The handoff's Displace metadata and initial settings are merged into the
existing framework resources. All 12 settings are bridged; the screenshot controls are shown, with six direction
indices preserved. Void radius and wall settings appear only for Void.
Native event priorities, rotation priority 200, packet dispatch mode 1,
interaction phases, Timer Range and relationship checks are bridged through
the existing engine. Third-person poses use the supplied RotationState and
the existing scoped render hook. The host restores outstanding temporary yaw
on disable, GUI/input loss, world replacement and unload.

Validation: all 23 CTest checks passed. Native JVM integration runs with real
Minecraft classes under named, SRG and official mappings and covers manual
activation, five jittered directions, exact Void yaw, no-path rejection,
cooldown gates, Timer Range, movement-remap suppression, late-tick restore,
delayed attack replay, model pose, packet deferral and cleanup. Existing Bow,
Fog, Shaders, Scaffold and launcher regression checks also passed. All three
payloads contain Java 8 classes. The launcher was extracted without injecting;
its embedded DLL SHA-256 matches the rebuilt DLL.

Live Minecraft/Lunar gameplay has not been tested. Source kit and source-byte
hashes, executable/payload hashes and backup location are recorded in
`DISPLACE-HANDOFF-VERIFIED.json`. The regular edition and Forge project were
outside this native replacement.
