# Rise 6.9.7 Scaffold → Rice Slinky (Open Slinky) — Integration Plan

**Historical integration plan.** Implementation is in progress. See CURRENT-STATUS.md
and PARITY-CHECKLIST.md for current work and limitations. No confirmation request in
this historical plan is pending.

Destination: `C:\Users\ethan\Documents\rice slinky new\modified`
- Native C++ client: `slinkyreadablesource/slinkyreadablesource/` ("Open Slinky", one JNI/JVMTI shared lib in the MC JVM)
- Java feature layer: `feature-bridge/`
- Loader/injector + launcher: `injector/`, `rice-loader-*.exe`
- Preserve: `regular/` and every existing module/feature.

---

## 1. How the destination actually works (audited)

**Native C++ client** (`src/`):
- `src/engine` — JNI/JVMTI hooks, GUI, module manager, startup (`jni/module_manager.cpp`, `module_toggles.cpp`, `rotation_override.cpp`, `tick_rotation_event.cpp`, `settings_store.cpp`, `jni_*_accessors.cpp`).
- `src/logic` — one function per reconstructed routine.
- `src/modules/{block,combat,move,utility,visual}` — module descriptors (GUI, setting registration, status).
- `src/data` — generated string/table/descriptor `.rdata`.
- ABI contracts in `include/slinky/`: `module_toggle.h` (ModuleManagerView, 27 handler slots per module), `rotation_override.h` (`queue_rotation_override`, event-13 `kApplyRotationOverride`).
- Existing `block/bridge_assist.cpp` = the **old scaffold**. Per spec: **leave intact, do not use as an implementation source.** It may be read only as a reference for the menu/registration ABI — not for Scaffold behavior.

**Java feature bridge** (`feature-bridge/`): this is the key path.
- `build.py` copies the dev.rice framework jar (`…/ricerakak…/payload-101.jar`) as `vendor/rice-framework.jar`, compiles supplied dev.rice modules against the named 1.8.9 jar, and **patches `RotationService` method bodies to call `NativeFeatureBridge.rotation(op,a,b)`** so the Java framework's rotation delegates to the native controller.
- `RiceFeatureLoader` = isolated classloader for `dev.rice.*` / `myau.*`.
- `NativeFeatureBridge` = the adapter the native menu/dispatcher talks to:
  - `labels(i)` / `specification(i,s)` / `configure(i,v)` / `visible(i,s)` / `status(i)` → native menu reads/writes dev.rice settings.
  - `event(i,type,active,packet,float[] values)` → builds a dev.rice `SemanticEvent`, calls `module.onEvent`, writes mutated fields back. Current `values[]` = {cancelled, dispatchMode, yaw, pitch, remapMovement, reducing, sideways, forward, timerRenderPartialTicks, cameraX, cameraY, cameraZ, overrideRotation}.
  - Native-provided natives: `rotation`, `flags`, `guiOpen`, `timerBlocked`.

**Conclusion:** Rise's Scaffold is dev.rice-style Java. The lowest-impedance, most
faithful port is a **dev.rice `ScaffoldModule` (Java)** carrying the full behavior,
registered in `NativeFeatureBridge.initialize()`, surfaced through the existing
native menu, with the native+bridge layer **extended** to deliver the events and
setting types Scaffold needs. This reuses module registration, settings, menu,
config persistence, rotation arbitration, JNI accessors and packet dispatch as the
integration layer (per spec), and keeps Scaffold behavior fresh from the Rise
reference.

---

## 2. Required integration-layer extensions (the real work on the native/bridge side)

The bridge today does **not** carry everything Scaffold needs. Deliberate extensions:

1. **`RangeSetting` support** — `NativeFeatureBridge.specification()` handles
   Boolean/Number/Choice/Color only. Scaffold has 8 range settings. Add a range
   type code (e.g. `4` → {min, max, lo, hi}) in `specification()`, `configure()`,
   `labels()`, and the native menu renderer + `settings_store` persistence.
2. **Motion/position/ground event fields** — extend the `event()` `float[]`
   contract (and the native dispatch sites) to carry `posX/posY/posZ`, `onGround`,
   `motionX/motionY/motionZ` so PreMotion can rewrite position/ground and
   MoveInput/Strafe/Jump can rewrite motion/input before the C03 send.
3. **New event phases fired from native** mapped to dev.rice `SemanticEvent` types:
   - PreMotion (new type) — before outbound movement packet; mutable pos/ground.
   - PostMotion (new type) — after.
   - MoveInput → type 27 (exists in dev.rice) — fire from the native move-input site.
   - Strafe (new type) — yaw-driven strafe override.
   - Jump (new type) — cancellable/boostable jump.
   - Sprint (new type) — server-sprint decision.
   - PacketSend/Receive → types 3/2 (already bridged) — used for placement bookkeeping.
   Preserve dev.rice priority ordering (higher first) when multiple modules listen.
4. **Rotation** — reuse the existing controller: dev.rice `RotationService.claim/retain`
   (already patched to `NativeFeatureBridge.rotation`) + event-13 `overrideRotation`
   → `queue_rotation_override`. Scaffold server-facing rotations, visual-back-rotations
   and yaw-offset go through this; **verify the server-side rotation at the moment of
   placement** (ordering: rotation → movement packet → placement packet).
5. **Hotbar / held-item sync** — via native `rotation_hotbar.cpp` / JNI player
   accessors; Scaffold selects/held-syncs blocks through these.
6. **Module slot/menu growth** — `NativeFeatureBridge.modules[]` is a fixed array
   today (grown by hand). Add Scaffold (and NoSlow) without hardcoding indices that
   break other modules; audit `module_toggle.h` 27-slot ABI and menu index use.

Everything above is additive to the integration layer; no existing module behavior changes.

---

## 3. Scaffold behavior port (dev.rice `ScaffoldModule` + submode strategies)

- Parent `ScaffoldModule`: all 38 settings (with recovered defaults/ranges/visibility),
  the 8 mode dropdowns, main-mode placement/rotation pipeline (Normal/Godbridge/
  Breesily/Snap/Telly/Eagle), and the shared placement pipeline:
  candidate/support-block selection → valid/replaceable/face/reach → hit-vector +
  raycast (Off/Normal/Strict) → hotbar+held sync+availability → place delay/counter/
  expand/state → per-mode aim/yaw-offset/pitch/smoothing/quantization → server vs
  visual rotations + movement correction → Same-Y/jump/ascend/descend/tower →
  sprint/sneak/safe-walk/slowdown → timer change+restore → packet effects+bookkeeping →
  render (gated by `Render`).
- Submode strategies as independent, stably-identified selectable options
  (Sprint 10 / Tower 12 / Downwards 4), ported one-for-one from `Va..Wa`, including
  **both** Watchdog Jump impls (`ViMode`, `VkMode`) with distinct ids, and `vo.java`
  (VkMode's helper) ported and registered the way the source registers it
  (helper state that can outlive the parent during the source-defined transition —
  kept scoped + resettable).
- `VzMode` protocol check: route through available protocol info or document the
  specific compatibility gap.
- Decompilation gaps (§11 of the checklist): recover from cleaned/raw/bytecode;
  never copy failure stubs or treat empty branches as behavior.

Lifecycle: init/enable/disable/mode-switch/world-change/disconnect/unload/re-inject;
restore only Scaffold-owned input/timer/rotation/inventory/packet/helper state; guard
null world/player, missing blocks, empty inventory, slot changes, open menus.

---

## 4. Build pipeline

- Java payload: `feature-bridge/build.py` (JDK 21 → `--release 8`, named/SRG/official
  mappings). Scaffold + NoSlow compiled into the feature payload jar.
- Native DLL: `slinkyreadablesource/.../CMakeLists.txt` (clang-cl + lld-link,
  Windows). Extensions in §2 land here.
- Launcher/loader: existing `rice-loader-*.exe` pipeline / `injector/`.
- Target compat preserved (Java 8 payloads; named/SRG/official mappings).

---

## 5. Validation (behavior, not labels)

Fixture/behavioral checks (reported separately from live gameplay):
every main mode + registered submode; each setting default/range/visibility/persistence;
both Watchdog Jump impls; placement geometry/reach/raycast/**packet ordering**;
rotation + movement correction; Same-Y/downward/jump/tower; mode-switch + full cleanup;
all naming mappings; existing-module regression; representative setting combinations;
recorded event/packet traces where possible. **No server-bypass/gameplay claims from
source analysis or unit tests alone.**

---

## 6. Proposed phase order (each phase = a complete, verified unit)

1. **Integration layer** — bridge `RangeSetting` + motion/pos/ground fields + the new
   event phases + native dispatch sites + menu/persistence; regression-test existing modules.
2. **Scaffold parent skeleton** — module + all 38 settings (recovered defaults/ranges/
   visibility) + 8 dropdowns + registration + menu surfacing. No placeholders.
3. **Main modes** — Normal → Godbridge → Breesily → Snap → Telly → Eagle, each with its
   real placement/rotation/movement.
4. **Submodes** — Sprint (10), Tower (12), Downwards (4) + `vo` helper; both Watchdog Jumps.
5. **Watchdog Telly / Watchdog Prediction / Keep-Y / rotation-block-boost** behaviors.
6. **NoSlow** (Watchdog Prediction mode) via same path.
7. **Build + validation + parity sign-off**; document residual gaps.

---

## 7. OPEN DECISIONS — need your confirmation before code

**D1 — Scaffold home.** Recommended: **dev.rice Java `ScaffoldModule` via
`feature-bridge` + native/bridge extensions** (faithful to Rise's Java form, reuses
the menu/rotation/event infra). Alternative: a **pure-native C++ module**
(`src/modules/block` + `src/logic` + `src/data`) — more work, re-expresses Java logic
in C++, but no Java payload. Confirm D1 = **bridge/Java** (recommended) or **native/C++**.

**D2 — Integration-layer extensions.** OK to extend `NativeFeatureBridge` + the native
dispatch/menu/persistence for range settings and motion/pos/ground/jump/strafe/sprint
events (additive, no existing-module behavior change)?

**D3 — Orphaned `Vd/Vl/Vq`.** Confirm **exclude** as unregistered (not Rise Scaffold
modes), with a documented note — unless you have evidence they're reachable.

**D4 — Scope of first implementation deliverable.** Start with **Phase 1 (integration
layer) + Phase 2 (parent skeleton with all real settings)** as the first shippable unit,
then proceed down §6? Or a different starting point.

**D5 — NoSlow timing.** Fold NoSlow in at Phase 6 (recommended), or earlier/separately?
