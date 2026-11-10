# v1.8.10 - RiceSlinky Anticheat and Fast Mine fatigue

- Added Anticheat under Utility, using the supplied Snowify 1.13.55 detector code with RiceSlinky-branded local alerts and popup. Disabled by default.
- Includes the original Autoblock, Keep-Y, Tower, Bridge Assist, Stasis, Fast Mine and No Break Delay checks. Retains per-check timing, total flag threshold, teammate exclusion, alert sounds, popup, tab icon, nametag warning and saved-blacklist settings.
- Blacklist storage is isolated at <Minecraft data directory>/riceslinky/anticheat_blacklist.json. Client-local /blacklist or .blacklist commands support adding/removing entries while Anticheat is enabled. These commands are consumed locally; ordinary chat is unchanged.
- Tab markers preserve server display-name changes and restore the latest server component when disabled. Nametag marks are a separate red warning above detected players. Popup position is configurable with Popup x/y sliders.
- Added Ignore fatigue to the existing Fast Mine controls: Off, Hold, Increase. Defaults to Off to preserve existing mining behavior.
- Hold suppresses only incoming Mining Fatigue effects for the local player. Server removal ends the hold; disabling/switching modes restores any still-current effect with its remaining duration. World cleanup discards stale holds. Other players, effects and packets pass through.
- Increase retains the visible effect and uses Snowify's fatigue-level multiplier. Dig speed ranges from 30% to 150%, default 75%; the original wooden axe/pickaxe adjustment is retained. It overrides only the existing Fast Mine late callback and restores the configured Speed immediately afterward. Without fatigue, normal Fast Mine settings apply.
- Original Fast Mine source, existing speed/delay settings and status remain unchanged. No foreign Snowify startup, mixins or client-wide configuration is loaded.
- Clutch, Scaffold, Displace, Bedplates and older versioned EXEs are preserved. Launcher and Windows file/product versions are 1.8.10.0.
- The supplied MIT notice is embedded in each payload and included beside this release.

Validation: 32/32 CTest tests passed. Anticheat/Fatigue passed 46 checks in each of named, SRG and official Minecraft mappings. Native bridge tests cover original Fast Mine delegation, default controls, fatigue packet verdicts and restoring Speed after the callback. Existing module regressions passed; extracted embedded DLL matches the rebuilt DLL exactly.

Static source recovery used JDK javap and CFR 0.152; reference classes, hashes, decompilation and adaptation notes are retained in modified/snowify-anticheat-port/reference. Detection accuracy against live players, live gameplay and actual GPU appearance remain untested.

Restart Minecraft before injecting. Use the versioned EXE; if Windows locks the running root slinky-modified.exe, it remains the previous copy.

Build: python modified/feature-bridge/build.py; build native CMake target; update launcher src/bundled/RiceNative.dll; build launcher/tests; run CTest and --check-runtime. prepare_rice_anticheat.py generates the locked reference source adaptation.
