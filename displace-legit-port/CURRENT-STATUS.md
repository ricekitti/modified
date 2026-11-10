# v1.8.9 - Displace Void Legit condition

- Added a Legit toggle underneath the Void scan controls in Knockback Displace. It appears only when Direction is Void and defaults to off. Persisted as source_displace_legit_void in native profiles.
- With Legit on, evaluate only the left and right directions at the configured Angle, capped at 90 degrees from the current yaw. Use the existing void path scoring, scan radius and one-block-wall rules to choose the better valid side. No full-circle refinement, rear/180-degree fallback or jitter is applied to these candidates.
- If neither side has a valid void path, skip the displacement activation and leave the normal attack available.
- With Legit off, use the original unrestricted Raven Void scan and refinement unchanged. Other direction modes, cooldowns, conditions, sprint/replay handling and rotation restoration remain unchanged.
- Native handoff removes preexisting aim drift for an active Legit Void selection so it applies the selected side yaw exactly.
- Original supplied handoff files are preserved; the build generates adapted copies. Clutch, Scaffold, Bedplates and older versioned EXEs are unchanged.
- Launcher and Windows file/product versions are 1.8.9.0.

Validation: 29/29 CTest tests passed. Native/JVM checks passed in named, SRG and official mappings, including exact side selection, rear-only rejection, the 90-degree cap, native aim correction, default/visibility behavior and unrestricted off-state scan/activation equivalence. Existing Bedplates, Scaffold and other module regressions passed. Extracted embedded DLL matches the rebuilt DLL exactly.

Live gameplay and server behavior have not been tested. Restart Minecraft before injecting. Use this versioned EXE if the running root slinky-modified.exe is locked and could not be replaced.

Build: run modified/feature-bridge/build.py (calls prepare_displace_legit.py), build native CMake target, update launcher src/bundled/RiceNative.dll, build launcher/tests, run CTest and --check-runtime.
