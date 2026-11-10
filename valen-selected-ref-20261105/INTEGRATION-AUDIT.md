# Rice Slinky ← Valen selected-module integration — AUDIT & STAGED PLAN

Author: Potato (Claude Opus 4.8) · Date: 2026-11-05
Status: **IN PROGRESS — recon + dependency audit complete; no module ported, built, or tested yet.**
Nothing in this file may be read as "module finished." A module is finished only when it
compiles in the overlay, passes a fixture/hook-check test, and ships in a versioned EXE whose
embedded DLL byte-matches the rebuilt DLL.

---

## 0. Provenance (verified)

| Item | Value |
|---|---|
| Source archive | `C:\Users\ethan\Documents\ChatGPT\dfdfdf\valen-selected-src.zip` |
| Archive SHA-256 | `127F23AED8E9920CDD774F4C269F9FAA330A7674C5846E8ADE35144ACAD3A3BF` |
| Archive bytes | 361748 · 263 entries · single root `valen-selected-src/` |
| Extracted to | `...\modified\valen-selected-ref-20261105\extracted\` (quarantined, read-only use) |
| Per-file hash manifest | `...\modified\valen-selected-ref-20261105\SHA256SUMS.json` (263 entries) |

The archive is recovered/decompiled source (CFR 0.152 headers), **not** a verified build.
No reference bytecode / provenance files beyond the Java are present — none will be claimed.

## 1. The five modules (MODULES.json — confirmed verbatim)

| Display name | Archive source | Notes |
|---|---|---|
| Fireball Boost | `valen/module/impl/movement/FireballBoost.java` (8066 B) | self-contained; no named-missing helpers |
| Bridge Egg Clutch | `valen/module/impl/player/BridgeEgg.java` (18407 B) | needs `BridgeEgg$SupportUpdate`, `VoidProximity` |
| Fireball Pearl Clutch | `valen/module/impl/player/FireballPearlClutch.java` (275 B) | **thin wrapper** |
| Auto Pearl | `valen/module/impl/player/AutoPearl.java` (245 B) | **thin wrapper** |
| Auto Quick Buy | `valen/module/impl/minigames/AutoQuickBuy.java` (11182 B) | needs `PurchaseConfirmation(+Status)`, `QuickBuyPresetsSetting$Purchase` + quickbuy/ helpers |

### Shared base — proven, not assumed
- `AutoPearl extends PearlRescueModule { super("Auto Pearl", false); }`
- `FireballPearlClutch extends PearlRescueModule { super("Fireball Pearl Clutch", true); }`
- `PearlRescueModule.java` is 45170 B and holds the real logic. The boolean = **fireball mode**.
  This is why the Fireball Pearl Clutch screenshot hides the *Pre pearl timer* / *Swap back* rows:
  those are visibility-gated for the pearl-only (`false`) path. Visibility rules come from
  `PearlRescueModule`, not from giving both modules identical controls.

## 2. Target framework contract (verified in Slinky-source)

Canonical editable tree: `C:\Users\ethan\Documents\ricerakak slionky\Slinky-cross-platform-source\Slinky-source`
(this is `OTHER` in the feature-bridge build). Package root `dev.rice`.

- `dev.rice.module.Module(String id, String name, String desc, ModuleCategory)`.
  Settings: `add(new NumberSetting(key,label,def,min,max,step))`, `BooleanSetting(key,label,def)`,
  `ChoiceSetting`, `RangeSetting`, `ColorSetting`, `StringListSetting`, `NamedColorListSetting`.
  Lifecycle: `onEnable/onDisable(ClientContext)`, `onClientTick(Start)`, `onRenderTick(Start)`,
  `onWorldUnload`, `onEvent(ClientContext, SemanticEvent)`, `listens(int type)`,
  `settingVisible(String key)`, `statusText()`, `hudName()`.
- `ModuleCategory`: COMBAT, MOVEMENT, RENDER, WORLD, UTILITY, UNKNOWN.
- Event dispatch is `SemanticEvent{ int type; ... }`. **OPEN ITEM:** `SemanticEvent` only *names*
  codes 27–31; existing fireball modules fire on raw 13/17/26 → the low event-type table lives in
  native dispatch (`src/engine/jni/hook_tables.inc` / `dev.rice.client.openjade.JadeRuntime`).
  Must be read and pinned before wiring new modules — no guessing event codes.
- Native work is bridged through `dev.rice.client.openjade.OpenJade*Backend` + `OpenJadeHost`
  (present: `OpenJadeFireballBoostBackend`, `OpenJadeFireballPearlBackend`, `OpenJadeHost`,
  `JadeModuleAdapter`, `JadeRuntime`, `OpenJadeCompat`).
- Registration: `dev.rice.module.CoreModules.register(ModuleManager)`. Catalogue modules gate on
  `EvidenceCatalog`; source-extension modules are added with explicit `manager.register(...)`.
  New Valen ports register the same explicit way.

## 3. Name-collision resolution (per request rule)

Rule: *preserve the current module and add a clearly distinguished variant.*

| Concept | Existing Slinky module | State today | Action |
|---|---|---|---|
| Fireball Boost | `FireballBoostModule` ("Fireball Boost", native `OpenJadeFireballBoostBackend`, "Boost Delay" only) | imported, **unregistered** since v159 | keep class untouched; add Valen port under a distinguished id/name |
| Fireball Pearl | `FireballPearlModule` ("Fireball Pearl", "Real Rotations", native backend) | **registered** (CoreModules L204) | keep untouched & registered; add Valen "Fireball Pearl Clutch" as a separate module |
| Clutch | `ClutchModule` (id "clutch", CoreModules L222) | registered | **do not touch**; "Wait for Clutch" coordinates via scoped host adapter only |
| Stasis | `StasisModule` (L194) | registered | do not touch; "Use Stasis instead" coordinates, not replaces |
| Scaffold | existing scaffold | registered | do not touch |

The separate Valen `Clutch.java` block-placement module is **excluded** and absent from the archive
(README confirms). "Wait for Clutch" / "Wait for Bridge Egg" are coordination references only.

## 4. Dependency audit — present vs. missing

PRESENT in archive (shared framework + helpers the modules need), e.g.:
`valen.utility.{FireballJumpTracker, TimerController, TimerRangeCycle, Utils}`,
`valen.event.*` (full set), `valen.module.setting.impl.SliderSetting`, `valen.module.{Module,ModuleManager}`,
`valen.mixin.impl.accessor.IAccessorMinecraft`, quickbuy `{BedWarsShopItem, QuickBuyPresetScreen, QuickBuyPresetsSetting}`.
These are adapted to `dev.rice` equivalents — Valen's full framework is **not** installed
(no Valen startup/GUI/auth/licensing/telemetry), per request.

MISSING (grep-confirmed referenced, absent from archive) — these gate specific behavior:
- `BridgeEgg$SupportUpdate`  → Bridge Egg Clutch support/placement update state
- `VoidProximity`            → Bridge Egg Clutch / rescue void detection
- `PurchaseConfirmation` + `PurchaseConfirmation$Status` → Auto Quick Buy purchase confirmation
- `QuickBuyPresetsSetting$Purchase` → Auto Quick Buy preset purchase entry
- `valen.utility.ResourceRelayClient` → **present but contains a decompiler failure** (unusable as-is)

Recovery policy: recover each only where archive/bytecode evidence supports it. No invented
algorithms, no no-op stubs, no silent omission. Where a behavior cannot be recovered, it is named
exactly and the independent modules are completed around it.

## 5. Mapping / timing deltas already recovered (evidence-backed)

Fireball Boost (`FireballBoost.java`), source vs. screenshot:
- Modes = {"Regular","Legit"} — screenshot showed only "Regular". **Legit mode recovered from source.**
- Duration: default 200 ms, range 50–2000, step 50 (screenshot showed 200 ms only).
- Cooldown: default 1000 ms, range 0–5000, step 50 (screenshot showed 1000 ms only).
- Timer integration via `TimerController.Owner.FIREBALL_BOOST` + `TimerRangeCycle` →
  maps to Slinky's native timer ownership (`NativeFeatureBridge.timer*` / `TimerRangeModule`),
  not a second timer hook.

Screenshot-only Auto Pearl / Fireball Pearl Clutch / Bridge Egg values are recorded in the request
and will be reconciled against `PearlRescueModule` / `BridgeEgg` source defaults, with every
difference made explicit in the changelog.

## 6. Build pipeline (verified from feature-bridge/build.py + .build-tools)

1. Adapt selected `dev.rice` / ported module sources; wire native entry points through
   `NativeFeatureBridge` (`feature-bridge/java/NativeFeatureBridge.java`).
2. `javac --release 8 -proc:none` against `rice-framework.jar` (payload-101.jar) + `minecraft-1.8.9-named.jar` + `deps/libraries/*`.
   **Must strip `JAVA_TOOL_OPTIONS`/`_JAVA_OPTIONS`** (classdump agent is attached globally; build.py pops them).
3. Generate `SlinkyHooks*.java` from `src/engine/jni/hook_tables.inc`; run hook-check tools (ASM) to assert hook signatures.
4. Build `features-named.jar`; `RemapJar` → `features-srg.jar`, `features-official.jar` (MC 1.8.9 named→SRG→official).
5. Convert payload jars to STORED; emit `payloads.s` (`.incbin`) for named/srg/official + loader + hooks.
6. clang+llvm 23.1.4 compiles engine + payloads → `slinky.dll` (~28 MB).
7. Loader EXE built via `.build-tools` package scripts (`package-*.py`, `build-current-launchers.cmd`),
   keeping Windows file/product version resources consistent with the release version.
8. Fixture + mapping tests (`-Xverify:all`) across named/srg/official.

Toolchain verified present & runnable: JDK 21.0.12 (Temurin), clang 23.1.4, MC 1.8.9 SRG mappings,
`java/mappings/{srg,official}.txt`.

## 7. Release target

Latest release = **v1.8.11** (`modified/releases/rice-loader-v1.8.11-bedplates-enable-fix.exe`).
Next unused = **v1.8.12**. Deliver: versioned EXE + `.changes.md` + `.verified.json` under `modified/releases/`,
preserve all prior EXEs, verify embedded DLL == rebuilt `modified/slinky.dll`.

## 8. Staged plan (each stage ends in a verifiable artifact)

- **S0 (done)** recon, extract+hash, mappings, framework contract, collisions, blockers → this doc.
- **S1** pin the native event-type table + read OpenJadeHost/backends, PearlRescueModule, BridgeEgg,
  AutoQuickBuy + quickbuy helpers, the present valen helpers; finalize per-module adaptation spec.
- **S2 (done)** reproduced the current v1.8.11 build chain on this box: native `slinky.dll` relinks
  content-identical (2-byte PE-timestamp diff) via clang-cl/lld-link; loader `rice-loader.exe` relinks
  content-identical (4-byte PE-timestamp diff) via MSVC link.exe+SDK under vcvars64. Tree left pristine.
  Evidence: `STAGE2-BUILD-VERIFIED.json`. The DLL+EXE chain is **reproducible**; no build blocker.
  Not yet exercised: full `build.py` Java payload compile + CTest (Stage 3+ when module code exists).
- **S3** Fireball Boost (independent, no missing helpers) — port, wire, compile, test.
- **S4** Auto Quick Buy core (presets/editor/matching/currency) — recover `PurchaseConfirmation*`,
  `QuickBuyPresetsSetting$Purchase` from evidence or document the exact gap.
- **S5** PearlRescueModule shared base + Auto Pearl + Fireball Pearl Clutch — recover `VoidProximity`;
  wire Wait-for-Clutch/Bridge-Egg via scoped host adapters (no edits to Clutch/Scaffold/Stasis).
- **S6** Bridge Egg Clutch — recover `BridgeEgg$SupportUpdate` + void-time visibility.
- **S7** full test + regression, package v1.8.12, verify DLL match, changelog, final report.
