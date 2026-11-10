# Rice Slinky — Valen module integration: handoff for Codex

Cold-start brief for a coding agent with repo access and no prior session context. Goal: finish porting the
five selected Valen modules into the real Rice Slinky client. **Fireball Boost is DONE (v1.8.12).** Four remain.
Read the companion docs in this same folder first: `INTEGRATION-AUDIT.md`, `STAGE2-BUILD-VERIFIED.json`,
`STAGE3-FIREBALL-BOOST-SPEC.md`, `STAGE4-REMAINING-MODULES-VERDICT.md`, `SHA256SUMS.json`.

Hard rules (from the original request — do not violate): faithful recovery only; **do not invent algorithms,
add no-op stubs, or silently omit required behavior**; do not import the excluded Valen `Clutch` module; do not
modify Rice Slinky's existing Clutch / Scaffold / Stasis / Displace / Bedplates / Anticheat; keep branding
"RiceSlinky"; do not install Valen startup/GUI/auth/licensing/telemetry; **do not call a module finished until
it compiles across named/SRG/official AND passes CTest**; preserve all prior versioned EXEs.

---

## 0. Repository map

- **Active native engine** (builds `slinky.dll`): `rice slinky new/modified/slinkyreadablesource/slinkyreadablesource/`
  - `src/logic/combat/source_features.cpp` — the Java-feature-module bridge + menu descriptors (`descriptors[]`, `displace_install()`, templated `dispatch<I>`).
  - `src/engine/jni/module_manager.cpp` — registers descriptors into `m->mods[...]`, sets `m->count`.
  - `build/` — pre-configured Ninja build dir.
- **Feature-bridge Java overlay**: `rice slinky new/modified/feature-bridge/`
  - `java/NativeFeatureBridge.java` — the native↔Java bridge; holds `modules[]` (the ported feature modules).
  - `java/FireballBoostFeatureModule.java`, `java/fireball/*`, `java/pearl/ContinuousCondition.java` — ported code.
  - `build.py` — compiles the overlay `--release 8`, remaps named→SRG→official, embeds payload (`payloads.s`), runs fixtures. **Edit its `sources` list to add new files.**
  - `tests/` — headless tests (e.g. `FireballBoostLogicTest.java`).
  - `build/` — outputs (`features-{named,srg,official}.jar`, `classes/`, `fixture*`).
- **Canonical dev.rice reference source** (read-only reference for APIs; `OTHER` in build.py):
  `ricerakak slionky/Slinky-cross-platform-source/Slinky-source/java/src/main/java/dev/rice/`
  Key: `module/Module.java`, `module/ModuleCategory.java`, `client/SemanticEvent.java`, `client/ClientContext.java`,
  `client/MinecraftFields.java`, `setting/{ChoiceSetting,NumberSetting,BooleanSetting,RangeSetting}.java`,
  `module/impl/{VelocityModule,TimerRangeModule,ClutchModule,KnockbackDisplaceModule,BowAimbotModule}.java`,
  `client/rotation/RotationService.java`.
- **Launcher** (builds `rice-loader.exe`): `rice slinky new/modified/provided-injector/rice-launcher/`
  - `CMakeLists.txt` (`VERSION`), `src/rice-slinky.rc` (FILEVERSION/PRODUCTVERSION), `src/bundled/RiceNative.dll` (the DLL to embed), `build/` (pre-configured).
- **Releases**: `rice slinky new/modified/releases/` — `rice-loader-vX.Y.Z-<name>.{exe,changes.md,verified.json}`. Latest = **v1.8.12-fireball-boost**. Next = **v1.8.13**.
- **Valen source** (what to port): `rice slinky new/modified/valen-selected-ref-20261105/extracted/valen-selected-src/src/main/java/valen/`
- **Recovered collection with bytecode/jar evidence** (NOT the selected archive; use for provenance/reconstruction only): `ChatGPT/dfdfdf/valen-module-src/reference/` (`verification.json`, `requested-modules-bytecode.txt`, `auto-quick-buy-bytecode.txt`, `captured-input.jar`).

## 1. Toolchain (verified working this session; nothing is on PATH by default)

- JDK 21: `C:/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot/bin`
- clang/lld-link 23.1.4 (DLL cross-compile, no SDK): `rice slinky new/.build-tools/clang+llvm-23.1.4-x86_64-pc-windows-msvc/bin`
- VS2022 cmake: `C:/Program Files/Microsoft Visual Studio/18/Professional/Common7/IDE/CommonExtensions/Microsoft/CMake/CMake/bin`
- VS2022 ninja: `.../CMake/Ninja`
- vcvars64 (loader needs MSVC link.exe + Windows SDK): `C:/Program Files/Microsoft Visual Studio/18/Professional/VC/Auxiliary/Build/vcvars64.bat`
- **Always `unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS`** before any java/javac — a global classdump agent is attached; build.py already strips it.

## 2. Build + package pipeline (exact, proven order)

1. **Java payload**: `cd modified/feature-bridge && unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS && python build.py`
   (compiles overlay, remaps to SRG/official, regenerates `build/payloads.s` + feature jars, runs fixtures).
2. **Native DLL**: with clang+cmake+ninja on PATH →
   `cmake --build "modified/slinkyreadablesource/slinkyreadablesource/build"` → `build/slinky.dll`.
3. **Stage DLL into loader**: copy that `slinky.dll` → `modified/provided-injector/rice-launcher/src/bundled/RiceNative.dll`.
4. **Bump version**: `rice-launcher/CMakeLists.txt` `VERSION x.y.z`; `src/rice-slinky.rc` `FILEVERSION x,y,z,0`, `PRODUCTVERSION x,y,z,0`, `VALUE "FileVersion" "x.y.z.0"`, `VALUE "ProductVersion" "x.y.z.0"`.
5. **Loader**: under vcvars64 (plus cmake/ninja/clang on PATH) →
   `call vcvars64.bat && cmake --build "modified/provided-injector/rice-launcher/build"` → recompresses `RiceNative.lzms`, relinks `rice-loader.exe`.
6. **CTest**: `call vcvars64.bat && (PATH incl. JDK) && ctest --test-dir "modified/provided-injector/rice-launcher/build" --output-on-failure` → must be **100% (currently 32/32)**. `SourceFeaturesJvm` boots the feature bridge in a real JVM across named/SRG/official.
7. **Package**: assert `releases/rice-loader-vX.Y.Z-<name>.exe` does NOT exist (preserve priors); copy loader → that path + `modified/rice-loader.exe` + `slinky-modified.exe`; copy `slinky.dll` → `modified/slinky.dll`; copy `slinky.dll` → `.build-tools/<name>-vX.Y.Z-runtime-check/RiceNative.dll`; write `.changes.md` + `.verified.json`; verify launcher-bundle DLL == built DLL (sha256) — "embedded==rebuilt". (PE link timestamps are non-deterministic; that is expected — each release records its own runtime-check DLL.)

Use `package-rice-anticheat-v1810.py` / the v1.8.12 artifacts as templates.

## 3. Feature-module integration recipe (the Fireball Boost pattern — replicate exactly)

A ported module is a `dev.rice.module.Module` subclass added to `NativeFeatureBridge.modules[]` at the next index,
surfaced to the native menu by a `Descriptor` with a `dispatch<I>` template. Fireball Boost = index **5**, native
slot **0x4b**. Next = index **6/0x4c**, **7/0x4d**, **8/0x4e**.

Per new module:
1. **Java module** in `feature-bridge/java/<Name>FeatureModule.java`, package `dev.rice.module.impl`. Extend `Module(id,name,desc,ModuleCategory)`; settings via `add(new NumberSetting(key,label,def,min,max,step))` / `ChoiceSetting(key,label,idx,opts...)` / `BooleanSetting(key,label,def)`; implement `listens(int)`, `onEvent(ClientContext,SemanticEvent)`, lifecycle, `settingVisible(key)` for conditional rows, `statusText()`.
2. **build.py**: add the module + its helper dir to `sources` (see the fireball lines already added).
3. **NativeFeatureBridge.initialize()**: grow `modules=new Module[N+1]`, set `modules[N]=new <...>()`, `manager.register(modules[N])`. `labels/specification/configure/visible/event/status` are index-generic — no change.
4. **source_features.cpp** `displace_install()`: add `static Descriptor <name>;`, grow `descriptors[]`, `settings[][64]`, `counts[]`, `statuses[][128]`, cleanup loop bound, and `metadata(e,0..N)`; set name/description/category, `settings=show<N>`, `visit=visit<N>`, `status=status<N>`, register `events[t]={dispatch<N>,prio,0}` for each type the module uses; add the `source_<name>_` persistence prefix in `metadata()`; add `extern "C" void* <name>_descriptor(){displace_install();return &sourcefeatures::<name>;}`.
5. **module_manager.cpp**: `extern "C" void *<name>_descriptor();`, bump `m->count` (0x4c → 0x4d → …), `m->mods[0x4c] = (Module*)<name>_descriptor();`.
6. **Headless test** in `feature-bridge/tests/<Name>LogicTest.java` for the pure logic; compile against `build/classes` and run with `-Xverify:all`.

### Event-type codes (verified; `SemanticEvent` fields carry the data)
`0`=client tick · `1`=per-frame run loop (route via `events[1]`; nanotime-driven) · `2`=incoming packet (`e.packet`) ·
`3`=outgoing packet (`e.packet`, `e.dispatchMode`) · `5`=toggle (`active`) · `6`=world render · `13`=rotation
(`e.yaw/e.pitch/e.overrideRotation`, moveFix) · `14`/`15`=mouse-over/attack phases · `16`=tick start · `17`=preUpdate ·
`18`=postUpdate/clientTick · `21`/`22`=velocity/move (`e.factor`, cameraX/Y/Z) · `26`=world unload · `27-31` named in `SemanticEvent.java`.
Timer is driven directly: `MinecraftFields.timer(mc).timerSpeed` / `.elapsedPartialTicks`. `NativeFeatureBridge.timerBlocked()` = native Timer-Range owns the timer (yield to it).

Compile against **named** MC names (`minecraft-1.8.9-named.jar`); build.py auto-remaps to SRG/official.

## 4. Remaining modules

### v1.8.13 — Auto Pearl  (RECOVERABLE, large)
`valen/module/impl/player/AutoPearl.java` = `PearlRescueModule("Auto Pearl", false)`. Port the shared base +
all helpers (ALL PRESENT, none reference a missing symbol):
`PearlRescueModule.java` (1036) + `SimulatedPlayer.java` (1151, full MC fall physics) + `RotationUtils.java` (1058) +
`helper/RotationHelper.java` (367) + `utility/{RescueFall.java(133), RescueItemUse.java(74), PearlFireballSequence.java(75)}` +
`utility/ContinuousCondition.java` (**already ported** → `feature-bridge/java/pearl/ContinuousCondition.java`). Reuse the
already-ported `fireball/TimerController.java` + `TimerRangeCycle.java` (consider moving to a shared package).
- **Event mapping**: `onRenderTick(END)`→ render-tick (type 16 or the render path used by Shaders/Scaffold) for timer bookkeeping + abort; `onReceivePacket(S08PacketPlayerPosLook)`→ type 2 (sets `landed`); `onClientRotation`→ type 13 (inject `aimYaw/aimPitch` via `e.yaw/e.pitch/e.overrideRotation`; `RotationHelper.forceMovementFix`→ the rotation moveFix flag); `onPostMotion`→ type 18 (the actual throw).
  Study `KnockbackDisplaceModule` + `NativeFeatureBridge.event()` type-13/14 `displacePlayer` handling for exactly how a feature module overrides rotation, and `RotationService`/`BowAimbotModule` for server-rotation access (`RotationUtils.serverRotations[0/1]`).
- **Inventory**: `RescueItemUse.use(slot)` (swap to slot + right-click-use + send) / `restore(slot)` → port against dev.rice inventory + the use/packet path (`NativeFeatureBridge.sendNoEvent`, `context.player().inventory`). `isPearl` = `Items.ender_pearl`.
- **Coordination (host adapter, read-only, no edits to Clutch/Scaffold)**: `ModuleManager.clutch.hasSafeLanding()/canSave()` → adapt to Rice Slinky `ClutchModule` (id "clutch") — find equivalent predicates in `Slinky-source/.../ClutchModule.java`. `ModuleManager.bridgeEgg.canSave()` → false until Bridge Egg is ported (then wire). `ModuleManager.autoPearl/fireballPearlClutch.isRescueActive()` → cross-reference the two pearl instances. `ModuleManager.antiFireball.recordRescueLaunch(...)` → Rice Slinky AntiFireball (optional/no-op if absent, documented). `ModuleManager.antiBot` / `AntiBot.isBot` → Rice Slinky AntiBotModule or no bot-filter (documented). `ModuleManager.Timer` → Rice Slinky TimerModule.
- **Settings (source-exact)**: Range 40 [8-64/1]; Avoid enemies 10 [0-32/1]; Edge margin 2 [0-4/1]; Pre pearl timer 0.1x [0-1/0.01] (**register only when !fireball**); After pearl timer 0.1x [0-1/0.01]; Swap back ON (**!fireball only**); Disable afterwards ON; Damaged OFF; Above void OFF; Predict safe landing OFF; In void for OFF + Time 1.0s [0.5-10/0.1]; Wait for Clutch ON; Wait for Bridge Egg ON. **Visibility**: In void for + Time visible only when (Above void ON AND Predict safe landing OFF); In void for's Time visible only when In void for ON. Make captured-vs-source default diffs explicit in the changelog (Auto Pearl screenshot shows Disable afterwards OFF + Predict safe landing ON = user overrides of source defaults ON/OFF).
- **Timer owner**: `AUTO_PEARL`. Native slot: index 6 / `mods[0x4c]`; category = player (confirm the native category int for "player"; Movement was 1). `statusText`/`getInfo` = floor(distance to target)+"m"+(forced?" !":"").

### v1.8.14 — Fireball Pearl Clutch  (RECOVERABLE)
`FireballPearlClutch.java` = `PearlRescueModule("Fireball Pearl Clutch", true)`. Same base; fireball/combo path
(`beginCombo`/`throwCombo` via `PearlFireballSequence`, finds `Items.fire_charge` slot). Hides Pre pearl timer +
Swap back (the !fireball-gated settings). Timer owner `FIREBALL_PEARL_CLUTCH`. Native slot index 7 / `mods[0x4d]`.
Screenshot defaults (Disable afterwards ON, Predict safe landing OFF) match source — note it.

### v1.8.15 — Bridge Egg Clutch  (RECOVERABLE, adapted)
`valen/module/impl/player/BridgeEgg.java` (18 KB). Display name exactly **"Bridge Egg Clutch"**.
- **Reconstruct `BridgeEgg$SupportUpdate`** (data holder): `new SupportUpdate(BlockPos pos, IBlockState state)` + `getPos()`/`getBlockState()` — fully determined by its construction (from S23/S22 block-change packets) and reads in `refresh()`. Evidence-backed, not invented.
- **Reconstruct `valen.utility.VoidProximity.isUnsupported(World,double,double,double)->boolean`**: NO source/bytecode exists, BUT mirror PearlRescue's **recovered** `aboveVoid()` (PearlRescueModule L512-535: scan from `floor(y)` down to 0, return true iff no solid block). Document this as a reconstruction from the recovered sibling, not the exact original.
- Settings: Lowest Y 40; In void for OFF (+ conditional void-time — determine visibility from source); Wait for Clutch ON; Pre timer speed 1x; After timer speed 0.03x; Use Stasis instead OFF; bind unassigned. Coordinate "Use Stasis instead" with Rice Slinky `StasisModule` (read-only) and "Wait for Clutch" with `ClutchModule`. Uses S23/S22 (type 2) + bridge-egg throw. Native slot index 8 / `mods[0x4e]`.

### Auto Quick Buy  (BLOCKED at core — complete the rest, document the gap, DO NOT ship blind click-spam)
`valen/module/impl/minigames/AutoQuickBuy.java` (11 KB) + helpers `quickbuy/{BedWarsShopItem.java(17KB), QuickBuyPresetScreen.java(17KB), QuickBuyPresetsSetting.java(9KB)}` (all present).
- **Reconstruct `QuickBuyPresetsSetting$Purchase`** (data holder): `new Purchase(String itemId, int qty)` + `getItemId()` — determined by usage in QuickBuyPresetsSetting.java.
- **HARD BLOCK — `PurchaseConfirmation` + `PurchaseConfirmation$Status`**: the per-purchase confirmation state machine (`new PurchaseConfirmation(inventoryProgress, startMs, 3000)`, `.status(inventoryProgress, now) -> Status{WAITING,...}`). NO source, NO bytecode class body, NO compiled class in `captured-input.jar`, NO sibling to reconstruct from (confirmed by `valen-module-src/reference/verification.json`). Its exact inventory-delta/timeout success logic is unrecoverable. **Shipping without it = blind click-spam, which the request forbids.** Build everything recoverable (presets, editor via Rice Slinky's existing controls/style NOT the screenshot design, cycle-preset keybind, shop-item matching against live shop/inventory/currency, the `Purchase` holder, quantity +/- and Clear) and **leave the module unregistered / clearly marked incomplete** with `PurchaseConfirmation` named as the exact missing behavior. Shop content/prices are in the original request (Blocks/Melee/Armor/Tools/Ranged/Potions/Utility); treat prices as server-mode-aware, confirm against actual shop/inventory/currency. "Solo: Free" meaning must come from source, not guessed.

## 5. Per-release verification checklist (every module)
- [ ] `build.py` green; feature jars contain the new `dev/rice/module/impl/<...>.class` across named/SRG/official.
- [ ] Native DLL rebuilds; module appears in `source_features.cpp` log line.
- [ ] Loader rebuilds at the new version; `RiceNative.lzms` recompressed; launcher-bundle DLL sha256 == built DLL.
- [ ] `ctest` 100% incl. `SourceFeaturesJvm` (named/srg/official) and `NativePayloadRoundtrip`.
- [ ] Headless `<Name>LogicTest` passes (`-Xverify:all`).
- [ ] No edits to Clutch/Scaffold/Stasis/Displace/Bedplates/Anticheat; prior EXEs preserved.
- [ ] `changes.md` states recovered-vs-source default diffs + any documented difference/omission + `live_gameplay_tested:false`.
- [ ] Never mark a module finished unless the above pass. Auto Quick Buy stays explicitly incomplete until `PurchaseConfirmation` is recovered.
