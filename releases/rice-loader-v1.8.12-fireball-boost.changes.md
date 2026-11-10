# v1.8.12 - Fireball Boost (Valen)

- Adds **Fireball Boost** under Movement: detects a fireball-boost jump from the packet stream and runs a
  freeze/burst client-timer cycle. Ported from the supplied Valen source
  (`valen.module.impl.movement.FireballBoost` + `FireballJumpTracker` / `TimerRangeCycle` / `TimerController`),
  adapted onto Rice Slinky's native feature bridge, event dispatcher and native/Java timer.
- Modes: **Regular** (immediate catch-up tick burst) and **Legit** (freeze, then burst). The Legit mode was
  recovered from source; the reference screenshot showed only Regular.
- Settings (source-exact): Mode {Regular, Legit}; Duration 200 ms [50-2000, step 50]; Cooldown 1000 ms
  [0-5000, step 50]. Bind unassigned - existing Rice Slinky keybindings are preserved; nothing is auto-bound.
- Detection preserved exactly: using a fire charge arms a 1.5s launch window; an explosion within 8 blocks and
  an upward velocity packet inside a 0.25s impact window confirm the jump while airborne with upward motion.
  Server pos-look corrections abort the cycle. Timer ownership shares one arbiter with the other ported
  modules and yields to the native Timer Range (`NativeFeatureBridge.timerBlocked()`).
- Distinguished from the pre-existing `dev.rice.module.impl.FireballBoostModule` (native OpenJade boost-delay,
  unregistered since v159), which is left untouched. Clutch, Scaffold, Stasis, Displace, Bedplates, Anticheat,
  Bow Aimbot, Fog, Shaders and Fireball Pearl are unchanged.
- Two Lunar-specific behaviors were intentionally dropped (documented here, not silent): the render-tick
  keybind re-press (Lunar hold-bind semantics; this client toggles) and the `ModuleManager.isBedMining()`
  mutual-exclusion guard (no equivalent accessor is exposed to feature modules). Core detection + timer cycle
  are unchanged.
- Launcher and Windows file/product versions are 1.8.12.0. All previous versioned EXEs are preserved.

Validation: 32/32 CTest passed, including `SourceFeaturesJvm` (named/SRG/official) booting the 6-module
feature bridge and `NativePayloadRoundtrip`. 35/35 headless logic assertions passed (detection windows, timer
arbitration, Regular/Legit cycle, cooldown gate, 40-tick cap). Java compiled `--release 8` and remapped across
named/SRG/official (1271 classes). The embedded DLL matches the rebuilt DLL exactly
(sha256 147114d0d0edea88f2b4b19e692e19a1343930b3edc133015fdcfd7da634adec).

Not tested: live Lunar/BedWars gameplay. Actual in-game boost height/feel, packet timing against a real
server, and anticheat interaction remain unverified - these are static/headless results only.

Restart Minecraft before injecting. Use the versioned EXE.
