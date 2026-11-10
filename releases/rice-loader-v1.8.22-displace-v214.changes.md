# v1.8.22 - Knockback Displace aligned to regular v2.14

Brought the EXE's Knockback Displace in line with the supplied regular **Rice Slinky v2.14**
Displace source (`displace.KnockbackDisplace` kit). Legit Void is dropped (it was never in the
native build to begin with - it only existed in the forge tree, which is out of scope here).

## What changed

The native build already compiled the clean 2026-11-04 handoff `KnockbackDisplaceModule`, whose
core logic and `RavenBsVoidScanner` are byte-for-byte the regular-v2.14 behaviour. The only
observable gaps vs the uploaded v2.14 were three activation guards and an angle clamp, now added
to the compiled source (`modified/displace-handoff-20261104/displace/KnockbackDisplaceModule.java`):

- **No displace while mounted / spectating / in a detached camera view.** Activation now also
  requires `!player.isRiding()`, `!player.isSpectator()`, and
  `minecraft.getRenderViewEntity() == player` - exactly v2.14's extra `beforeAttack` guards.
- **Angle clamp 45-180.** The displacement angle is clamped to `[45, 180]` before it is used for
  both the Void scan and the left/right/strafe sign math, matching v2.14's
  `MathHelper.clamp_float(angle, 45, 180)`.

Everything else was already v2.14-identical and is unchanged:
- Directions Left / Right / Random / Strafe / Strafe(inverted) / Void; the +/-5 deg jitter on
  non-Void directions; 9-tick input gate and cooldown; `require_no_kb` / `require_weapon` /
  `require_kb_enchant` / `require_facing_away`; sprint ownership + cleanup; hide-rotation packet
  hold; third-person head-flick.
- `RavenBsVoidScanner` geometry is behaviourally identical to the v2.14 scanner (diff is only the
  package/`ClientContext`-vs-`Minecraft` shell; the scan logic is the same 402 lines).

Plumbing note: `require_no_kb` uses the native `reducing` flag and the rotation uses the native
RotationService - these are the native client's reconstruction of the same regular-v2.14
`reducesRecoil` and yaw handling, so the observable behaviour matches v2.14.

## Files
- edited: `modified/displace-handoff-20261104/displace/KnockbackDisplaceModule.java`
  (the source `feature-bridge/build.py` compiles for the EXE)
- No Legit Void sources are referenced by the native build.

## Validation
- `feature-bridge/build.py`: Displace recompiled and remapped for **named, SRG and official**
  (getRenderViewEntity / isRiding / isSpectator all resolved + remapped); all Java checks green.
- Native DLL + launcher rebuilt; **31/31 CTest passed** (incl. SourceFeaturesJvm named/SRG/official,
  ModuleStartup, LauncherRegression, NativePayloadRoundtrip).
- `--check-runtime`: decoded embedded DLL SHA-256 equals the freshly built `build/slinky.dll`
  (`68ab66fc...`).
- Launcher/Windows file+product versions 1.8.22.0. Prior versioned EXEs (v1.8.0-v1.8.21) preserved.
- **NOT tested:** live Lunar/BedWars gameplay (no live client here, per this project's standard).

## Scope
Forge is untouched (out of scope). Only the EXE/native Displace was aligned to v2.14.
