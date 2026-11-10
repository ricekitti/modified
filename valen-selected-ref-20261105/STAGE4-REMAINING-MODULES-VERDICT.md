# Stage 4+ — remaining four modules: evidence-based recoverability verdict

Execution update (2026-11-05): Auto Pearl shipped in v1.8.13, Fireball Pearl Clutch in v1.8.14, and Bridge Egg Clutch in v1.8.15. Each versioned EXE/changelog/proof is under `modified/releases/`. Latest suite: 32/32 CTest; rescue and independent Quick Buy helper checks pass across named/SRG/official. Quick Buy automatic purchases remain blocked by PurchaseConfirmation/Status; helpers/editor are ported but native launch/key/persistence integration is unfinished. See root CODEX-HANDOFF.md's execution update and `modified/quickbuy-port/`. The verdict table below records the pre-implementation assessment.

Authority: `C:\Users\ethan\Documents\ChatGPT\dfdfdf\valen-module-src\reference\verification.json` (the recovered
collection's own audit) + `captured-input.jar` contents + direct source inspection. The missing helpers have
**no source and no compiled .class** anywhere (jar holds only the module classes + PearlRescue); the bytecode
txts show only *call sites*, not helper bodies. Provenance note: these facts come from valen-module-src, which
is NOT the selected archive — the selected archive (valen-selected-src) contains no bytecode/provenance.

| Module | Verdict | Detail |
|---|---|---|
| Fireball Boost | **DONE (v1.8.12)** | no missing helpers |
| Auto Pearl (`PearlRescueModule(false)`) | **RECOVERABLE (large)** | ALL helpers present; none reference an unrecovered symbol |
| Fireball Pearl Clutch (`PearlRescueModule(true)`) | **RECOVERABLE (large)** | shares PearlRescue; combo via present `PearlFireballSequence` |
| Bridge Egg Clutch | **RECOVERABLE (adapted)** | `VoidProximity.isUnsupported` reconstructable from PearlRescue's recovered sibling `aboveVoid()` (same codebase/author/semantic); `SupportUpdate` is a data holder |
| Auto Quick Buy | **BLOCKED (core)** | `PurchaseConfirmation`+`$Status` = per-purchase confirm state machine; NO source, NO bytecode body, NO sibling to reconstruct from. The one genuine hard-block. |

### Pearl port scope (why it is the biggest single piece)
PearlRescueModule 1036 lines + helpers, all PRESENT: `SimulatedPlayer` **1151** (full MC fall physics),
`RotationUtils` **1058**, `RotationHelper` 367, `RescueFall` 133, `RescueItemUse` 74,
`PearlFireballSequence` 75, `ContinuousCondition` 27 (ported). ~4000 lines total. Needs dev.rice APIs
studied before the module body is correct: feature-module rotation injection (SemanticEvent type 13/14 +
moveFix), server-rotation access, inventory slot-use / item-use packet send, and a read-only host adapter
onto Rice Slinky's `ClutchModule` for `hasSafeLanding()/canSave()` (no edits to Clutch/Scaffold). `AntiBot`
(missing) adapts to Rice Slinky AntiBot. This is a multi-release effort, executed per-module, verified
(compile + CTest) like Fireball Boost — never marked done before it builds and tests.

## Recoverable missing helpers (data holders — determined by usage, not invented)
- `BridgeEgg$SupportUpdate(BlockPos, IBlockState)` + getters — reconstructable from exact construction/read sites.
- `QuickBuyPresetsSetting$Purchase(String itemId, int qty)` + `getItemId()` — reconstructable from exact usage
  in QuickBuyPresetsSetting.java (`new Purchase(id,1)`, `.add(new Purchase(string,n))`).

## Genuinely unrecoverable (algorithms — NO source, NO bytecode body — will NOT be invented)
- `valen.utility.VoidProximity.isUnsupported(World, double, double, double) -> boolean` — signature only.
  Exact void/support scan algorithm absent. Options: (a) document the gap; (b) adapt to an existing Rice Slinky
  void-detection service (RavenBsVoidScanner / native void check) as an explicit, non-identical adaptation.
- `valen.module.impl.minigames.quickbuy.PurchaseConfirmation(progress,long,long)` / `.status(progress,long)`
  / `Status{WAITING,...}` — the inventory-delta + timeout confirmation logic. No existing Rice Slinky analogue;
  shipping without it would be blind click-spam, which the request forbids. No faithful completion possible.
- `valen.utility.SnapbackRotation` — used by excluded Clutch, not by the five; ignore.
- `valen.utility.ResourceRelayClient` — decompiler failure; a telemetry/relay client, not needed (Valen
  startup/telemetry is explicitly not to be installed).

## PearlRescue (Auto Pearl + Fireball Pearl Clutch) — settings recovered (source-exact)
Range 40 [8-64/1]; Avoid enemies 10 [0-32/1]; Edge margin 2 [0-4/1];
Pre pearl timer 0.1x [0-1/0.01] (registered only when !fireball); After pearl timer 0.1x [0-1/0.01];
Swap back ON (only when !fireball); Disable afterwards ON (source default; Auto Pearl screenshot shows OFF = user override);
Damaged OFF; Above void OFF; Predict safe landing OFF (source default; Auto Pearl screenshot shows ON = user override);
In void for OFF + Time 1.0s [0.5-10/0.1]; Wait for Clutch ON; Wait for Bridge Egg ON.
Visibility: fireball mode (Fireball Pearl Clutch) hides Pre-pearl-timer + Swap-back — matches screenshots.
Coordination: `ModuleManager.clutch.hasSafeLanding()/canSave()` and `ModuleManager.bridgeEgg.canSave()` adapt
to Rice Slinky's existing Clutch via a scoped read-only host adapter (no edits to Clutch/Scaffold). `AntiBot`
(missing) adapts to Rice Slinky AntiBot or is documented as no bot-filtering in Avoid-enemies.

## Plan
- Stage 5: port PearlRescue + present helpers; host adapter for Clutch/Bridge-Egg coordination; 2 native feature
  slots (Auto Pearl, Fireball Pearl Clutch); build; test; package (v1.8.13 / v1.8.14 or combined).
- Stage 6: Bridge Egg — reconstruct SupportUpdate; port everything independent of the trigger; VoidProximity is
  the exact gap (adapt-to-service decision made explicitly at implementation, or documented as blocked).
- Auto Quick Buy: complete presets/editor/shop-matching/Purchase holder; document PurchaseConfirmation as the
  exact unrecoverable gap; do not ship blind click-spam.
