# Current Scaffold port status

Status: PARTIAL. The complete Rise Scaffold port is not finished and has not been tested in live gameplay.

Reference: ricekitti/rise-open commit c08acf7223ce9a6cde453cf86f97d77d92c742ba. Parent and all 26 repository submodes are retained under rise-source/. The local C:/classdump/out recovery is retained under recovered/; its exact version remains unverified. Empty historical rise-decompiled/V*Mode.java files are not source evidence.

## Implemented in this continuation

- Double-precision PreMotion position and independent packet-ground state. Field-read hooks are scoped to onUpdateWalkingPlayer; actual client position/bounding boxes are not temporarily replaced. Frame cleanup is scoped with a native TLS guard. PostMotion receives the outgoing coordinates.
- Movement-input hook after vanilla keyboard polling: mutable forward/strafe/jump/sneak and slowdown multiplier, preserving unchanged vanilla float values. Telly auto-jump now uses this phase; Watchdog Telly samples its configured ground delay. Sneaking Speed now changes the input multiplier.
- Silent packet identity markers survive the client/Netty thread handoff, bypass module dispatch once, clean up failed sends and clear on unload. Marker locking is separate from module dispatch locking.
- Air Jump, Matrix, Vulcan and NCP tower handlers, in addition to previously implemented Normal, Vanilla, Legit, MMC, Verus and Prediction 1.8 handlers. NCP emits its source sentinel packet without events and applies source fractional-Y thresholds. Watchdog tower remains unfinished. Suspicious empty branches in recovered tower source are not treated as proven original behavior.
- Vulcan Sprint: child speed setting (1.3 default, 0.9..1.3 step 0.05), potion/web/liquid/depth-strider distance rules, low-jump phase, placement reset and ten-tick packet cycle. Its setting is only visible for Vulcan. Cleanup restores a still-owned sneak action even beyond source cycle tick one; a newer action from another module relinquishes ownership.
- Watchdog Prediction sprint: three startup use-item packets, movement-heading camera snap, independent quantized server rotation, source 90-to-94 packet-pitch rewrite, incoming block-change hold/deadline logic and actual input freezing. Cleanup restores the initial camera snap only if the camera still matches that owned value. Rotation smoothing is adapted to the existing Rice arbitration/quantization layer; original FPS-dependent micro-jitter has not been established as exact parity.
- Matrix Sprint: source angular predicate and 0.95 motion reduction plus the physical-sneak input exception. Rise's separate Speed module is absent in this destination; unrelated native movement modules are not substituted for it.
- Existing basic sprint handlers (Normal, Disabled, Bypass, Legit, first/Vi Watchdog Jump), all six main-mode drivers, Normal Downwards, Up Side Down, Expand, Same-Y Auto Jump, base sneak scheduling, conditional visibility and range persistence remain integrated.
- Explicit final Scaffold cleanup before the native bridge reference is released. Pending silent markers, helper state, key claims and owned camera snap are released.
- Bootstrap class version is SlinkyHooks14 with 30 native declarations, avoiding the old SlinkyHooks13 schema when reinjecting an expanded interface. Extra registry hooks apply only to supported 1.8.9 mappings and are restored during hook removal.

Setting surface: 39 parent settings plus the source-backed Vulcan Sprint child setting = 40 settings, expanded to 48 native rows. The child was appended so existing parent setting order/keys remain stable. The second Watchdog Jump child setting is not exposed as a working implementation yet.

## Verification

- Java 8 target payloads built for named, SRG and official mappings.
- Behavioral and bridge fixtures pass in all three namespaces. They cover the new tower packet/ground predicates and precise position handling; movement input timing/scaling; Telly jump delay; Vulcan distance, potion, web, placement-window, packet-cycle and ownership cases; Prediction startup packets, pitch filters, block deadlines, input freeze and camera ownership; Matrix angular/input behavior; range/child-setting visibility and persistence; null/unload handling.
- Static hook-contract checks validate the actual walking-player call, four field-read sites, input-polling call and bootstrap native signatures in all three game namespaces.
- All 20 native regression tests pass, including all three JVM namespaces. Native fixture checks cover motion-field passthrough and identity, no client-position writes, NCP source thresholds and once-only packet bypass, cross-thread marker cancellation, send-failure cleanup, native input wrapper, enabled-Scaffold final cleanup and reinjection. Existing Bow/Fog/Shaders and unrelated native tests remain green.
- DLL and launcher rebuilt; embedded DLL roundtrip is verified by the native resource test.
- No live injection, gameplay, server compatibility or bypass validation was performed. Fixtures do not prove these outcomes.

Logs: validation-build.log and validation-native.log. Output paths, source hashes and checksums: BUILD-RESULTS.json. Candidate: modified/rice-loader-scaffold-partial.exe; native DLL: modified/slinky-scaffold-partial.dll. regular/ and the root launcher were not replaced.

## Remaining work

- Second/Vk Watchdog Jump and helper, Watchdog sprint, Watchdog tower and their Blink/Disabler/Speed/Jump/Teleport dependencies.
- Watchdog Telly and prediction rotation/placement branches, visual rendering and full conditional-mode combinations.
- Silent server hotbar selection and block-history tracking. The original slot gate is now traced: SlotUtil-aik reads ItemStack-co previous-count state; PlayerController-d resets its age and decrements the snapshot when an eligible block placement is attempted. Source snapshots and evidence are retained under rise-source/helpers/. This is not implemented by guessing or visibly changing currentItem.
- Shared timer ownership, Safe Walk, Verus/Watchdog Downwards, helper pause/flag sneaking and remaining parent toggles.
- Exact native event timing/packet traces, repeated live injection/world/disconnect testing and representative combined modes/settings.

The original repository has failed decompilations/unstructured parent branches. Its tree and releases do not include the restored/original JAR named by RESTORATION.md. The user confirmed that artifact is unavailable. Repository source and the unverified local dump support continued reconstruction, but cannot establish exact 6.9.7 provenance or restore missing original behavior with certainty.


---

# Rise 6.9.7 Scaffold → Rice Slinky (Open Slinky) — Source-to-Port Parity Checklist

Status legend: `[ ]` not started · `[~]` partial / needs source recovery · `[x]` verified in source · `GAP` decompilation/reachability gap to resolve before claiming parity.

All source paths are relative to the Rise clone:
`rise-open/sources/{deobfuscated,cleaned,raw}/`. Primary = deobfuscated;
`cleaned`/`raw` + bytecode (`javap`/CFR/Vineflower) are the gap-recovery sources.

Parent module: `rise/module/impl/player/Scaffold.java` — **4077 lines**, heavily
string-encryption-obfuscated (setting labels resolved at runtime from an
AES/PBE-decrypted `Object[]` pool `o0Oo000O0oO[...]`, so labels are NOT literal
in source). Human-readable names below are taken from the task spec and will be
confirmed against the decrypted pool / bytecode during implementation.

---

## 0. PHASE 1 — integration layer (BUILT + VERIFIED)

Done and verified this pass (Scaffold registered as native feature slot **4**; NoSlow deferred per D5):

- **Java bridge** `feature-bridge/java/NativeFeatureBridge.java`: registers `ScaffoldModule` at
  index 4; adds **RangeSetting** support (each range surfaced as two endpoint rows `_lo`/`_hi`,
  native-menu-compatible, independently persisted; sampling stays in the module). Non-range
  settings emit byte-identically → existing displace/bow/fog/shader menus unchanged.
- **Scaffold module** `feature-bridge/java/ScaffoldModule.java`: all 8 dropdowns (option counts
  6/2/3/10/12/3/4/3), full provisional setting surface, dual "Watchdog Jump" with distinct
  stable ids (`SPRINT_WATCHDOG_JUMP_VI`=5, `_VK`=8), lifecycle/reset, event entrypoints.
  **Per-mode behavior is Phase 2 — not implemented, not aliased.** `statusText()` reports
  "Phase 1 (integration only; behavior pending)".
- **Native** `src/logic/combat/source_features.cpp` (4→5 feature slots, `scaffold_descriptor()`,
  dispatch/show/visit/status<4>, `source_scaffold_` persistence prefix) +
  `src/engine/jni/module_manager.cpp` (module table `0x48`→`0x49`, `mods[0x48]=scaffold_descriptor()`).
- **Builds:** Java payload (`feature-bridge/build.py`, named/SRG/official — ScaffoldModule.class
  embedded in all three); native `slinky.dll` relinked clean via ninja/clang-cl (ABI static_asserts
  held). Headless test `scratchpad/ScaffoldBridgeTest.java`: **ALL PHASE-1 CHECKS PASSED**
  (dropdown counts, dual-WJ ids, range expansion 41→49 native rows, specification/configure
  round-trip, range write-back).

## 0a. PHASE 2 — FAITHFUL SOURCE RECOVERED (decompiler on the clean classdump)

Fetched CFR 0.152 and decompiled the classdump `com.alan.clients` classes (clean recompiled bytecode →
**0 scramble markers**, unlike the repo's CFR-of-obfuscated source). Saved to
`scaffold-port/rise-decompiled/` (31 files): `Scaffold.java` (1446 lines, readable), all 26 submodes,
and helpers `MoveUtil`/`PlayerUtil`/`RotationUtil`/`RayCastUtil`/`SlotComponent`/`EnumFacingOffset`/
`PacketUtil`/`RotationComponent`. This is the authoritative port reference: the whole Scaffold
behavior (6 modes' `calculateRotations`, `D`, `placeBlock`, `getHitVec`, `findPlacementRotation`,
`findPitchNear`, the event-lambda driver, sneak/tower/downwards) is now readable Java, still version-
unconfirmed per §0b. **Remaining = adapt `com.alan.clients` APIs → dev.rice/native** (`aEg`→context,
`RotationComponent.setRotations`→RotationService/native controller, `RayCastUtil`→raycast,
`ModeValue.wo()/wA()`→settings, player obf fields `tR`/`cqL`/`Zl`→MC fields) + the submodes.

## 0a-driver. PHASE 2 — NORMAL main-mode driver IMPLEMENTED + COMPILED (live-pending)

Ported from the decompiled source into the dev.rice/native framework (all against named `net.minecraft.*`):
- `ScaffoldGeometry.java` — faithful `PlayerUtil` target-finder (`a(x,y,z,floorY)`) + face-finder
  (`a(vec,allowDown)`), `RayCastUtil.c/a/overBlock`, `RotationUtil.c/a/h` (rotation-to-face/point),
  and a reconstruction of Rise's patched `EntityPlayerSP.rayTraceCustom` (eyes + `getVectorForRotation`
  inlined — that method is protected — + `World.rayTraceBlocks`).
- `ScaffoldMath.stepDelta` — Rise `RotationUtil.a` proportional 2-axis rotation-speed limiter.
- `ScaffoldDriver.java` — the onPreUpdate Normal pipeline: target/face/blockFace selection,
  `ticksOnAir`/`readyToPlace` gating, `calculateRotations` "Normal" case, `D(int)` non-prediction
  yaw-sweep raycast, the raycast placement gate, `placeBlock` (Normal/Strict/Grim), `getHitVec`.
  Rotation applied via the native type-13 finalize event (server-facing only; camera stays separate).

VERIFIED: compiles against the real MC 1.8.9 + dev.rice jar; full `build.py` builds the named/SRG/
official payloads (1207 classes each) with the driver included; FogHookCheck green (25 natives);
`tests/ScaffoldBridgeTest` — 39-setting parity + range bridge + event routing (incl. type 17→driver)
all pass; `statusText` reports Normal as "driver wired (live-unvalidated)".

DEFERRED (base path taken, NOT faked): Telly/Snap/Eagle/Breesily/Godbridge cases; all Sprint/Tower/
Downwards submodes; `expand` bridge-ahead; auto hotbar switch (SlotComponent); sneak scheduling; the
`flag5` falling-fast optimisations that need Rise's unmapped player field `tR` (air-tick counter —
needs the EntityPlayerSP classdump). LIVE-PENDING: hook firing, packet ordering, the `D` probe's
temporary Y-offset, bookkeeping — none exercisable headless (LIVE-VALIDATION.md).

## 0a-fields. RESOLVED — Rise obfuscated player timing fields

From the deobfuscated patched entity base tick `net/minecraft/entity/c.java::CY()` (rise-open):
```
if (this.aO) { this.tR = 0; ++this.cqP; }   // aO = onGround
else         { this.cqP = 0; ++this.tR;  }
```
→ **`tR` = air-ticks** (ticks since last on ground), **`cqL`/`cqP` = ground-ticks** (since last airborne).
Reproduced exactly in `ScaffoldDriver.updateCounters` (airTicks/groundTicks), once per game tick. NOT an
approximation. Also confirmed field types from the Scaffold constant pool: `pl:F`=rotationYaw,
`tR/cqL/Zl/ae:I`, `crI/crK:D`, `crd:Z`, `cHT:rip.vantage…StopWatch` (a Rise/Vantage addition).
STILL UNRESOLVED (different obf letters between the rise-open build and the classdump build; appear only
in watchdog edge-optimisations): `ae` (`<10` gate), `Zl` (`<43` sprint/flag gate), `crI`/`crK`
(`hypot>0.2` motion gate). These are confined to the deferred watchdog branches below.

## 0a-telly. PHASE 2 — TELLY base driver IMPLEMENTED + COMPILED (live-pending)

Ported `ScaffoldMoveUtil` (isMoving / enoughMovementForSprinting / speed / stop / direction×2 — `direction()`
base yaw `pp` taken as rotationYaw, documented) and `ScaffoldGeometry.rotationUtilM` (sensitivity-quantise).
`ScaffoldDriver` now dispatches calculateRotations by mode and implements **base Telly** (Rise lines
1339-1352: timed right-click at airTicks 0/2, aim-down early/late via `D`, mid-arc raycast refine, arm
readyToPlace only when airTicks>3) + the Telly jump (movementInput.jump when grounded + full input;
Watchdog Telly waits groundTicks>=tellyJumpDelay). Shared `applyFinalRotation` (speed-limited step, skips
Grim / Sprint=Watchdog-Prediction-grounded). VERIFIED: compiles vs real MC+framework; full `build.py`
builds named/SRG/official (1208 classes each); FogHookCheck green; `ScaffoldBridgeTest` all pass.

DEFERRED (documented; base path taken so Telly still bridges): the Watchdog Telly rotation branch
(getTellyRotation / findPlacementRotation / rotation-boost + the `ae`/`crI`/`crK` raycast-toggle) and the
Watchdog Prediction branch (needs the prediction variant of `D`). Telly jump phase is a documented stand-in
for Rise's MoveInputEvent. LIVE-PENDING as above.

## 0b. PHASE 2 — bytecode recovery + EXACT setting parity (DONE + VERIFIED)

**Source inspected:** `C:\classdump\out\com.alan.clients.module.impl.player.Scaffold.class` (+ the
`...player.scaffold.*.class` set) — classdump-agent output of a loaded build. Analyzed STATICALLY
with `javap` only (never executed).
**Version NOT verified:** the dump's exact version is unconfirmed. Structural signals are consistent
with the 6.9.7 source in this repo (residual-obf field names `age`/`agt`/`agx`, the 8 ModeValues, the
listed settings, readable methods `placeBlock`/`runMode`/`calculateRotations`/…), but that is
circumstantial, not proof it is 6.9.7. All ports below are **reconstructions from the inspected
disassembly**, not verified-faithful to 6.9.7. Because this is a clean recompile (not CFR output of
the obfuscated jar) its control flow is not scrambled, which removes the §11 decompile blocker for
reconstruction — it does not establish version fidelity.

**Setting parity now BYTECODE-EXACT** (from the `<init>` disassembly) — 8 Mode + 20 Bool + 3 Number
+ 8 Range = **39**. Corrected `ScaffoldModule.java` accordingly (built + test-verified):
- Number (default,min,max,step): `timer`(1,0.1,10,0.1) · `expand`(0,0,4,1) · `sneakingSpeed`(0.2,0.2,1,0.05)
- Range (lower,upper,min,max; step 1): `rotationSpeed`(5,10,0,10) · `placeDelay`(0,0,0,5) ·
  `age`="Watchdog Telly Jump Delay"(0,0,0,5) · `watchdogTellyRotationSpeed`(35,38,0,180) ·
  `rotationBlockRotationSpeed`(122,128,0,180) · `startSneaking`(0,0,0,5) · `stopSneaking`(0,0,0,5) ·
  `sneakEvery`(1,1,1,10)
- Bool (defaults): Safe Walk/Keep-Y/Visual Back Rotations/Block Diagonal Ascend/Extend Block Reach
  WD Telly/Disable On Flag/Render = **ON**; all others OFF. `agt`="Ignore Speed Effect".
- Confirmed Vulcan Sprint Speed / Place Extra blocks are SUBMODE settings (VjMode/VkMode), not parent.

## 0c. PHASE 2 — rotation/geometry primitives (IMPLEMENTED + TESTED)

Reconstructed from the inspected classdump disassembly (`javap -c`) into
`feature-bridge/java/ScaffoldMath.java` (pure) + `ScaffoldModule`. "Tested" below means the
listed behavioral checks passed — it does not assert fidelity to 6.9.7 (version unverified, §0b).

| Rise method | Reconstruction | Status |
|---|---|---|
| `rotationsBetween` | `yaw=wrap180((float)toDeg(atan2(dz,dx))-90)`; `pitch=clamp((float)(-toDeg(atan2(dy,dist))),-90,90)` (float casts placed as in disassembly) | TESTED: +Z=0,+X=-90,-X=90,down=+90,diag=-45 |
| `clampPitch` | ±89.9 clamp | TESTED |
| `snapTo45` | `round(wrap180(f)/45)*45` | TESTED (23→45,22→0,70→90) |
| `b(cur,target,max)` → `stepToward` | shortest-arc step limiter | TESTED (incl ±180 seam) |
| `wrapAngleTo180_float`/`clamp_float` | reimplemented per the 1.8 algorithm | TESTED |
| `isReplaceableBelow(n)` | floor posX, floor posY−n, floor posZ → `Block.isReplaceable` | IMPLEMENTED (compile-only; not behaviorally tested) |

`scratchpad/ScaffoldMathTest.java`: 26/26 math checks pass (establishes the tested math behavior
only). Note: the disassembly uses Rise's MC mapping (`pl`=rotationYaw); reconstructions use the
framework's MCP names.

## 0d. PHASE 2 — native event phases (PARTIALLY INTEGRATED)

> Status is **partial**: routing 11 event IDs through `listens()`/`onEvent` without error verifies
> **dispatch only** — NOT event timing, mutable-field write-back under load, priority ordering, or
> behavior. Required call-site hooks for the Scaffold driver (pre-C03 PreMotion, and Strafe/Jump/
> Sprint) are still **missing** (see trace + gaps below). No end-to-end claim.


Native engine → slot-4 phase mapping (hooks in `java_natives_world.cpp`/`java_natives_frame.cpp`;
delivery in `source_features.cpp dispatch<4>`; routing in `ScaffoldModule.onEvent`). Scaffold
registered for `{2,3,5,13,14,16,17,18,21,22,26}`.

| Native type | Hook | Rise phase | Status |
|---|---|---|---|
| 16/17/18 | `onUpdate` pre/mid/post | PreUpdate / driver tick (ctx: player/world) | delivered (ctx) |
| 13 | `onUpdate` rotation finalize → `queue_rotation_override` | calculateRotations output (writes yaw/pitch + override) | delivered |
| 21/22 | `moveFlying` | Strafe/MoveInput (`sideways`=x, `forward`=y, jump flag) — **added** this pass to `dispatch<4>` + writeback | delivered + route-tested |
| 14 | left/right click (flag) | RightClickEvent (flag==0) / attack (flag==1); cancel via `v[0]` | delivered |
| 2 / 3 | packet in / out | PacketReceive / PacketSend | delivered |
| 5 / 26 | toggle / unload | lifecycle | delivered |

Route test: `ScaffoldModule.listens()`+`onEvent` dispatch all 11 wired types without error and do
not over-subscribe. Native rebuilt + relinked clean; `moveFlying` ABI (`Vel` struct) matches
`VelEvent`. **IMPLEMENTED = routing + data delivery**; the per-phase placement behavior is the
next unit and is intentionally empty (no faked behavior).

**UNRESOLVED native-phase gaps:** no dedicated hooks for pre-C03 PreMotion position-mutation,
Strafe-*yaw* override, Jump, Sprint, or Teleport. These map to the nearest phase (tick/moveFlying)
or must be done inside a tick handler; full fidelity for Telly/Godbridge/sprint modes may need new
JVMTI hooks — to be assessed when those modes are built.

**Still OPEN (behavior):** `getHitVec`, `findPlacementRotation`, `findPitchNear`,
`calculateRotations`, `placeBlock`, `runMode` driver, `isOffsetReplaceableBelow`/
`isReplaceableAtStartY`; Normal end-to-end validation; mode-dropdown default indices; all submodes.

---

(superseded open item kept for history:)
- Setting **type split** was provisional (20/5/8); **now resolved** bytecode-exact to 20/3/8 above.
- Event phases **MoveInput / Strafe / Jump / Sprint / PreMotion position-mutation** are not yet
  fired to slot 4 by the native engine (it currently drives slot 4 with the confirmed types
  2/3/5/13/16/17/18/26). Native hook additions are a Phase-2 integration task (PORT-PLAN §2).
- Existing-feature regression: build-level verified (ABI asserts, clean relink). The real-JVM
  `SourceFeaturesJvmTest` fixture (built by build.py) was **not executed this pass** — pending.

---

## 0e. PHASE 2 — Rise phase → Minecraft call-site trace + ordering (ANALYSIS)

From the inspected disassembly (`hook_tables.inc` 1.8.9 `v1_*`; Rise `placeBlock`/`runMode`). Hooks
rewrite a specific call-site instruction inside an MC method into a `SlinkyHooks13` native call.

| Rise phase | MC call site | Native hook today | Status |
|---|---|---|---|
| Packet send | `NetHandlerPlayClient.addToSendQueue` → `NetworkManager.sendPacket` | `sendPacket_1_8` → type 3 | HOOKED |
| Packet recv | channel read | pipeline → type 2 | HOOKED |
| moveFlying (strafe/forward) | `EntityLivingBase.moveEntityWithHeading` → `moveFlying` call | `moveFlying` → 21/22 | HOOKED (delivery added) |
| per-tick update | `Minecraft.runTick` → `updateEntities` call | `updateEntities` → 16/17/18 | HOOKED |
| rotation finalize | inside `hook_update_entities` | → 13 (`queue_rotation_override`) | HOOKED |
| click / right-click | `Minecraft.clickMouse`/`rightClickMouse` | 14/15 | HOOKED |
| **PreMotion / PostMotion** | **`EntityPlayerSP.onUpdateWalkingPlayer`** (builds/sends C03/C04/C05/C06) | **none** | **MISSING** |
| **MoveInput** | **`MovementInput.updatePlayerMoveState`** | **none** | **MISSING** |
| **Strafe-yaw override** | inside `moveEntityWithHeading` before strafe calc | none (21 is after) | **MISSING** |
| **Jump** | **`EntityLivingBase.jump`** | **none** | **MISSING** |
| **Sprint** | sprint decision in `onLivingUpdate` | none (can `setSprinting` in tick) | **MISSING** |

### `placeBlock()` packet-ordering trace (from disassembly — NOT a runtime trace)
- **Grim** rotation mode: send `C06PacketPlayerPosLook(pos, targetYaw, targetPitch, onGround)` →
  `PlayerControllerMP.onPlayerRightClick(...)` (sends C08 block placement) → swing `C0APacketAnimation`
  → send `C06PacketPlayerPosLook` restoring real yaw (+rand) / pitch. ⇒ **rotation-packet → placement → restore**.
- **Normal** rotation mode: `onPlayerRightClick` (C08) + swing only — no explicit rotation packet;
  relies on the per-tick rotation (type-13) carried by vanilla's C03. `Strict` raycast defers to
  vanilla `rightClickMouse`.
- ⇒ Normal's rotation→movement→placement order is **NOT established**; it depends on when the driver
  (`onPreMotion`) runs vs vanilla's `onUpdateWalkingPlayer` C03 send. **Must be confirmed with an
  actual outbound-packet trace** (fixture capture or in-game) before any Normal end-to-end claim.

### Hook-installer capability — VERIFIED (`classfile_hooks.cpp::rewrite_instruction`)
The JVMTI ClassFileLoadHook patcher does **in-place, same-length instruction replacement ONLY**:
it matches one opcode — getstatic/putstatic/getfield/putfield (0xb2/b3/b4/b5) or
invokevirtual/invokespecial/invokestatic (0xb6/b7/b8) — against a `HookEnt{cls,name,desc,kind}` and
rewrites it to `invokestatic <hook>` (opcode+u2 → opcode+u2, no length change, no reflow).
**It has NO method-entry or method-return injection** (those add bytes → would require offset /
StackMapTable / exception-table / line-number reflow the patcher does not implement). Call-site
replacements alone did NOT establish entry/return support — now confirmed they don't have it.

### Hooked-class table — VERIFIED FIXED AT 9 (second blocker)
`data_2f78e0` is a **fixed image-data region** (`src/data/data.s`: 9 consecutive `HookCls` symbols
`data_2f78e0`,`data_2f7908`,…), and the installer hard-codes 9 (`HookRec data_2f78e0[9]`,
`jclass classes[9]`, the `TRY(...)` list of exactly 9, `g_natives[9][6]`, `g_pats[9][3]`, loops `<9`).
The 9 hooked classes are Minecraft/EntityRenderer/RenderManager/RendererLivingEntity/RenderPlayer/
NetHandlerPlayClient/WorldClient/Entity/EntityLivingBase — **EntityPlayerSP is NOT among them**. The
fog hook only EXTENDED an existing class (`c==1 && variant==1 ? +1` native), it did not add a class.
⇒ **A new `onUpdateWalkingPlayer` hooked class is NOT addable** without image-layout + installer
surgery that cannot be validated in this sandbox. So the clean PreMotion wrap hook is **dropped**.

### RESOLVED via a separate hook registry (IMPLEMENTED — compile/link-verified; firing is live-pending)
Rather than grow the fixed-9 image table, added a **native-owned 10th hook record** `g_extra`
(`hook_installer.cpp`: `build_extra` + a separate `RetransformClasses` call; `classfile_hooks.cpp`:
`TRY(g_extra)` in the ClassFileLoadHook + disarm retransform; `hook_tables.inc`: 25th `kNatives`
entry `walkingPlayer`; installer `natives[24]→[25]`). It wraps **`EntityPlayerSP.onUpdateWalkingPlayer`**
(call site in `onUpdate`; names obf `bew`/`t_`/`p`, MCP, SRG `func_70071_h_`/`func_175161_p`), giving
Rise's **real PreMotion/PostMotion timing** (events 32/33, dispatched scaffold-scoped — never the
`events[0..26]` array). `hook_walking_player` (`source_features.cpp`): **preserves the original
method's exception** (propagated), clears **only bridge** exceptions, **Win32-TLS re-entry guard with
RAII cleanup** (`/NODEFAULTLIB` rules out `thread_local`; `TlsAlloc` at install) — nested entry still
runs the original call, skips duplicate dispatch. DLL links clean; payload builds (named/SRG/official,
`walkingPlayer` native present; FogHookCheck now 25). **Unresolved:** whether the `Site` matches the
real `onUpdate` bytecode and the hook actually fires — LIVE-VALIDATION step A (needs running MC).

### (superseded) earlier fallback — map to 16/17/18 + sendPacket(3):
- Run the Scaffold driver on the **existing** `onUpdate` phases delivered by `hook_update_entities`:
  **16** (pre), **17** (brain = PreMotion-equivalent), **18** (post = PostMotion-equivalent); control
  **C03/C08 ordering at the existing outbound-packet hook (type 3)**. No new hook, no table surgery.
- This is a **PHASE DEVIATION** from Rise's `onUpdateWalkingPlayer`; the emitted packet order is
  **UNRESOLVED** and must be confirmed with a live trace (`LIVE-VALIDATION.md` A/B). Reverted the
  earlier PreMotion=32 / PostMotion=33 wrap plan (the hook it needed is infeasible).
- **Jump** IS extensible later (EntityLivingBase is an existing hooked class with method room ≤3;
  `jump()V` = `bF`/`jump`/`func_70664_aZ`) via the fog-style `+1`-native special case. **MoveInput**
  (`MovementInput` not hooked) and **Strafe-yaw** are not cleanly addable → map to `moveFlying`
  (21/22) or document as gaps. **Sprint** via a tick handler remains an APPROXIMATION.

### (superseded) earlier plan assumed a new call-site wrap hook:
1. **PreMotion/PostMotion** — do NOT inject at `onUpdateWalkingPlayer` head/returns (unsupported).
   Instead **replace the `invokevirtual onUpdateWalkingPlayer` call site in its caller
   (`EntityPlayerSP.onUpdate`)** with `hook_walking_player`, which: fire PreMotion (mutable
   pos/yaw/pitch/onGround/cancel) → call the real `onUpdateWalkingPlayer` **once** via JNI (covers the
   method's every internal return path as one wrapper return; no recursion because the JNI call does
   not pass through the rewritten instruction) → fire PostMotion. Requires adding `EntityPlayerSP` +
   `onUpdate` to the patched set with `Site` rows for obf `v1_0` / MCP `v1_1` / SRG `v1_2`, a
   `kNatives` entry, and the real-method `methodID` per mapping.
   **Event-ID correction (VERIFIED):** the earlier 23/19 are WRONG — 23 is a dev.rice outbound-packet
   event (AutoBlock/AutoClutch/BedDefender/BlockIn/InvWalk/Stasis), 19 is used (Friends/AutoTool), and
   the native `ModuleView.events[0..26]` slot array is saturated. Use **PreMotion=32, PostMotion=33**
   (verified unused as event types) and dispatch them **scaffold-scoped** via a dedicated exported
   dispatcher calling `dispatch<4>` directly (the fog/type-30 precedent), NOT the `events[]` array.
   Names for the `Site`: `EntityPlayerSP` = `bew`/MCP/SRG-class; containing method `onUpdate` =
   `t_`/`onUpdate`/`func_70071_h_`; invoked `onUpdateWalkingPlayer()V` = `p`/`onUpdateWalkingPlayer`/`func_175161_p`.
   **Native fn hardening (required):** static re-entry guard; `ExceptionCheck`+clear after the JNI
   `CallVoidMethod` and after each dispatch; `PushLocalFrame`/`PopLocalFrame`; honor the `data_2f7259`
   unload guard.
2. **MoveInput** — same wrap on `updatePlayerMoveState`; **Jump** on `jump`. **Sprint via a tick
   handler is an APPROXIMATION** — not equivalent timing/cancellation to Rise's SprintEvent unless a
   matching sprint-decision site is hooked; flagged UNRESOLVED, not claimed equivalent.

### Rotation + bookkeeping constraints (design, to enforce in the driver)
- **Camera rotation ≠ server rotation.** Scaffold's server-facing rotation goes through the native
  rotation controller (type-13 / silent path → `queue_rotation_override`), NOT by writing
  `player.rotationYaw/Pitch` (which moves the camera). Keep them separate.
- **Don't corrupt movement-packet bookkeeping.** Any temporary position/rotation/ground change for a
  packet must be restored (or routed through the controller) so vanilla `onUpdateWalkingPlayer`
  dedup/bookkeeping (`lastReportedPosX/Y/Z`, prev-yaw/pitch, `positionUpdateTicks`) stays intact.

### Validation scope (explicit)
- A **headless packet fixture tests only the PORT's own emitted sequence** — it CANNOT establish
  Rise's original sequence (no runnable original build here, no independently captured reference
  trace). Establishing Rise's order requires executing the original bytecode or a captured trace.
- Hook firing, write-back under load, cancellation, priority, and real C03/C08 ordering require a
  **running MC JVM** (absent in this sandbox). Procedure to run them live:
  `scaffold-port/LIVE-VALIDATION.md`. **Normal C03/C08 ordering remains UNRESOLVED** until a live
  trace (and a reference-trace diff) is produced.

### Reconstructed this pass (IMPLEMENTED, compile-only — not behaviorally tested)
`getEyePosition` (`posX, posY+eyeHeight, posZ`). `placeBlock` ordering fully traced (above) but its
port is deferred until `getHitVec`/`calculateRotations`/`EnumFacingOffset` land (interdependent).

---

## 1. ModeValues (8 — source-confirmed by option counts)

| Parent field | Setting | Options (source count) | Option source | Status |
|---|---|---|---|---|
| `afJ` | **Mode** (main) | 6 | parent-internal logic (no Vx class) | `[x]` count=6 |
| `afK` | **Rotation Mode** | 2 | parent-internal | `[x]` count=2 |
| `afL` | **Ray Cast** | 3 | parent-internal | `[x]` count=3 |
| `afM` | **Sprint** | 10 | Normal(parent)+9 Vx | `[x]` count=10 |
| `afN` | **Tower** | 12 | Disabled(parent)+11 Vx | `[x]` count=12 |
| `afO` | **Same Y** | 3 | parent-internal | `[x]` count=3 |
| `afP` | **Downwards (Press Sneak)** | 4 | Off(parent)+3 Vx | `[x]` count=4 |
| `yawOffset` | **Yaw Offset** | 3 | parent-internal (literal name in source) | `[x]` |

Evidence: `grep "new ModeValue"` → 8 decls at lines 903/925/955/1043/1147/1177/1218/1722;
`.a(modeArrayN)` chain lengths = 6/2/3/10/12/3/4/(builder) respectively.

---

## 2. Main Modes (Mode dropdown `afJ`) — logic in parent

| # | Mode | Placement/rotation logic location | Status |
|---|---|---|---|
| 0 | Normal | parent | `[ ]` |
| 1 | Godbridge | parent | `[ ]` |
| 2 | Breesily | parent | `[ ]` |
| 3 | Snap | parent | `[ ]` |
| 4 | Telly | parent | `[ ]` |
| 5 | Eagle | parent | `[ ]` |

> Main-mode aim/yaw/pitch/placement differ per mode and live in the parent's
> PreUpdate/PreMotion/Strafe/MoveInput handlers switched on `afJ` index.
> **Do not alias any of these to Normal.**

---

## 3. Sprint submodes (`afM`, registration order)

| Idx | Display | Class | File bytes | Role notes | Status |
|---|---|---|---|---|---|
| 0 | Normal | parent | — | default | `[ ]` |
| 1 | Disabled | `VfMode` | 548 | | `[ ]` |
| 2 | Legit | `VgMode` | 704 | | `[ ]` |
| 3 | Bypass | `VeMode` | 488 | | `[ ]` |
| 4 | Vulcan | `VjMode` | 2086 | uses "Vulcan Sprint: Speed" setting | `[ ]` |
| 5 | **Watchdog Jump** | `ViMode` | 1417 | **impl #1** (distinct from Vk) | `[ ]` |
| 6 | Matrix | `VhMode` | 976 | | `[ ]` |
| 7 | Watchdog Prediction | `VmMode` | 3500 | | `[ ]` |
| 8 | **Watchdog Jump** | `VkMode` | 11198 | **impl #2**; creates/registers `vo` helper; "Place Extra blocks" setting | `[ ]` |
| 9 | Watchdog | `VnMode` | 1292 | | `[ ]` |

> **Dual "Watchdog Jump":** `ViMode` and `VkMode` share the display name but are
> different implementations. Assign distinct stable internal ids
> (`sprint_watchdog_jump_vi`, `sprint_watchdog_jump_vk`). Do NOT merge.

---

## 4. Tower submodes (`afN`, registration order)

| Idx | Display | Class | File bytes | Status |
|---|---|---|---|---|
| 0 | Disabled | parent | — | `[ ]` |
| 1 | Vulcan | `VyMode` | 1166 | `[ ]` |
| 2 | Vanilla | `VxMode` | 594 | `[ ]` |
| 3 | Normal | `VvMode` | 1164 | `[ ]` |
| 4 | Air Jump | `VpMode` | 677 | `[ ]` |
| 5 | Watchdog | `WaMode` | 18692 | largest submode | `[ ]` |
| 6 | MMC | `VsMode` | 958 | `[ ]` |
| 7 | NCP | `VuMode` | 1203 | `[ ]` |
| 8 | Matrix | `VtMode` | 729 | `[ ]` |
| 9 | Legit | `VrMode` | 611 | `[ ]` |
| 10 | Verus | `VwMode` | 872 | `[ ]` |
| 11 | Watchdog Prediction 1.8 | `VzMode` | 1190 | **checks target protocol version** | `[ ]` |

---

## 5. Downwards submodes (`afP`, registration order)

| Idx | Display | Class | File bytes | Status |
|---|---|---|---|---|
| 0 | Off | parent | — | `[ ]` |
| 1 | Normal | `VaMode` | 691 | `[ ]` |
| 2 | Watchdog | `VcMode` | 1283 | `[ ]` |
| 3 | Verus | `VbMode` | 1856 | `[ ]` |

---

## 6. Parent-internal submodes (no Vx classes)

- **Rotation Mode:** Normal, Grim `[ ]`
- **Ray Cast:** Off, Normal, Strict `[ ]`
- **Same Y:** Off, On, Auto Jump `[ ]`
- **Yaw Offset:** 0, 45, -45 `[ ]`

---

## 7. Orphaned submode files — `GAP` (reachability)

| Class | File bytes | Finding |
|---|---|---|
| `VdMode` | 2482 | **Not imported by Scaffold.java**; `git grep` (deobfuscated) finds only self-reference. Not statically registered. |
| `VlMode` | 2267 | same |
| `VqMode` | 1899 | same |

Decision: **excluded from selectable options.** Before final sign-off, re-check
`cleaned`/`raw` + any reflective/string-pool construction. If no caller exists,
document as dead directory classes (not Rise-registered Scaffold modes).

---

## 8. Settings (38 total: 8 Mode + 19 Boolean + 3 Number + 8 Range)

Counts from source: `new BooleanValue`×19, `new NumberValue`×3, `new RangeValue`×8,
`new ModeValue`×8. Named list below from task spec; **defaults / min / max / step /
visibility predicates must be recovered** (string-crypto pool + bytecode) — each is
`[~]` until recovered.

### General
Rotation Speed · Place Delay · Timer · Expand · Movement Correction · Safe Walk ·
Sneak · Start Sneaking · Stop Sneaking · Sneak every x blocks · Sneaking Speed ·
Telly Only on Right Click · Render · Advanced · Yaw Offset (Mode) ·
Ignore Speed Effect · Up Side Down · Bypass Raycast When Falling

### Watchdog / related
New Watchdog Rots · Keep-Y bypass · Watchdog Prediction (bool) · Watchdog Telly (bool) ·
Visual Back Rotations · Block Diagonal Ascend · Don't force raycast on Watchdog Telly ·
Extend Block Reach on Watchdog Telly · Watchdog Telly Jump Delay · Disable On Flag ·
Watchdog Telly Rotation Speed · Rotation Block Boost · Rotation Block Rotation Speed ·
Boost Only While Holding Jump

### Mode-specific
Vulcan Sprint: Speed · VkMode Watchdog Jump: Place Extra blocks

> **Range settings:** preserve BOTH endpoints and the source's randomized sampling
> (`RangeValue` draws between lo/hi per use). Do not collapse to one slider.
> **Visibility predicates:** several `BooleanValue(..., () -> {...})` lambdas (lines
> 1325/1355/1375/1394/1437/1492/1555/1686/1768/1788…). Determine per-predicate whether
> it means "visible when true" or "hide when true" by reading the GUI consumer —
> **do not blind-invert.**

---

## 9. Events used by parent (`Listener<T>`)

| Rise event | dev.rice SemanticEvent | Native bridge today | Action |
|---|---|---|---|
| `PreUpdateEvent` (prio 2) | type 17 | bridged | map |
| `PreMotionEvent` | — | **NOT bridged** (no pos/ground fields) | **extend** |
| `PostMotionEvent` | — | **NOT bridged** | **extend** |
| `MoveInputEvent` ×2 | type 27 | sideways/forward bridged; needs native fire | **extend fire** |
| `StrafeEvent` | — (yaw field exists) | partial | **extend** |
| `PacketSendEvent` | type 3 | bridged | map |
| `PacketReceiveEvent` | type 2 | bridged | map |
| `ea` = JumpEvent | — | **NOT bridged** | **extend** |
| `en` = SprintEvent | — | **NOT bridged** | **extend** |
| `fd` = ? | — | unknown | **identify** |

Submodes register their own additional `Listener`s — enumerated per-submode during
implementation.

---

## 10. Helper / dependency classes to port or bridge

| Rise class | Role (inferred) | Status |
|---|---|---|
| `hackclient/rise/vo.java` | `VkMode`'s separately-registered helper | `[ ]` must port |
| `bs` | rotation manager (silent rotations, GCD) | map → native rotation controller |
| `cb` | rotation priority enum (Rise/Traditional/…) | map → claim priority |
| `bu` | hotbar/slot + held-item sync | map → native hotbar |
| `aih` | world/block lookup | map → JNI world accessors |
| `aif` / `aiu` | movement/rotation math (yaw/pitch, GCD, direction) | port |
| `aib` | block/placement helper | port |
| `ahj` | packet send | map → native packet dispatch |
| `aka` / `aef` / `fd` / `ea` / `en` | events/util — **identify** | `GAP` |
| `Flight`, `Speed` | module interactions (imported) | audit interaction, do not fake |
| `Disabler`, `LongJump`, potion effects, Via protocol | referenced behavior | audit; `VzMode` needs protocol-version check |

---

## 11. Decompilation gaps — `GAP` (BLOCKER for faithful parent-pipeline port)

Measured Phase-2 (scramble markers = `** GOTO` / `lblNN` / `blockNN:` / orphan expr):

| Unit | Markers | Decompilation | Portable now? |
|---|---|---|---|
| **Parent `Scaffold.java`** (placement pipeline, 6 main modes, rotation, movement, timer) | **128** | FAILED (CFR emitted scrambled goto/opaque control flow; core method `a(b,float)`@3487 is 369 lines of `block62{…}`/`** GOTO lbl72`/`7 & 1;`) | **NO** |
| All 26 submodes `Va..Wa` | 0 (VkMode 2) | clean | **YES** — faithfully portable |

Findings:
- The parent's `raw`/`cleaned` variants are **absent** from the repo (only `deobfuscated`),
  and all three are CFR output of the same `.class`, so they share the same control-flow scramble.
- **No Rise bytecode exists** in the clone or any workspace: no `.class`, no
  `client-restored.jar`, no `RisePortable.exe`, no `rise.vm`. RESTORATION.md confirms build
  inputs (`artifacts/client-restored.jar`, `libraries.jar`) are **supplied separately**, not shipped.
- Therefore the scrambled parent methods **cannot** be faithfully recovered here: there is no
  bytecode to re-decompile (with Vineflower/Procyon) or to read via `javap` (whose control flow
  cannot be scrambled). Readable *fragments* survive (e.g. `bs.a(new Vector2f(acs-30, act), …, afU?gH:gG)`
  @3586; `aif.speed()` thresholds; `cgF/cgH/cgI` key checks), but not whole-method control flow.

**Consequence (task rule compliance):** the block-placement/movement/main-mode behavior lives in the
parent and is an **unresolved source gap**. It will NOT be filled with a generic scaffold algorithm,
guessed control flow, or Normal-aliases. The clean submodes are portable but hook into this parent
pipeline, so end-to-end placement is gated on recovering it.

**Unblock paths:**
1. **(Recommended) Supply the Rise bytecode** — `client-restored.jar`, the extracted `.class` set,
   or `RisePortable.exe`. Then the scrambled methods are recovered faithfully from `javap`/a cleaner
   decompiler, and the full parent pipeline + main modes become a true 1:1 port.
2. Authorize an explicitly-labeled **reconstruction** of the scrambled parent methods from the
   readable fragments + MC 1.8 semantics — documented as reconstruction, NOT verbatim 1:1.

Faithful work that does NOT depend on the scrambled parent and can proceed now:
- Native integration-layer event phases (MoveInput/Strafe/Jump/Sprint/PreMotion) wired to slot 4.
- Clean self-contained submodes (e.g. `VfMode` sprint-disable) ported verbatim onto those events.

---

## 12. NoSlow (folded in — same integration path)

Watchdog-Prediction NoSlow (already fully analyzed against the dev.rice framework)
ports as a dev.rice `NoSlowModule` "Watchdog Prediction" mode and registers through
the same `NativeFeatureBridge` path. Tracked separately; not part of Scaffold parity
but shares the bridge/native extensions below.
