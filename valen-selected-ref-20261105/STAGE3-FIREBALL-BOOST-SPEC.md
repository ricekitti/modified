# Stage 3 — Fireball Boost (Valen) integration spec

Status: **IN PROGRESS.** Algorithm, event map, timer mechanism and native 5-slot architecture fully
reverse-engineered and verified. One native routing item is unresolved and must be pinned before the
module body is correct (see §6). First concrete code landed: `feature-bridge/java/fireball/FireballJumpTracker.java`
(faithful 1:1 port, source sha256 7149f202…e7bce). NOT compiled/wired/tested yet. Not "done".

## 1. Source → target
- Source: `valen/module/impl/movement/FireballBoost.java` (sha256 in SHA256SUMS.json) + helpers
  `valen.utility.{FireballJumpTracker, TimerRangeCycle, TimerController}`; `Utils.nullCheck()` only from Utils.
- Target: a NEW `dev.rice` feature module, distinguished from the existing `dev.rice.module.impl.FireballBoostModule`.

## 2. Collision resolution (verified)
- `FireballBoostModule` already exists (native `OpenJadeFireballBoostBackend`, "Boost Delay" only) but is
  **unregistered since v159** (CoreModules L205 comment). It is a *different* feature (native boost-delay),
  not Valen's timer-cycle boost. Keep its class untouched. The Valen port is a new, clearly distinguished
  module (proposed id `fireball_boost_valen`, display "Fireball Boost"; final name set at wiring time so the
  menu shows one unambiguous entry).

## 3. Settings (source-exact; screenshot deltas explicit)
| Setting | dev.rice type | default | min | max | step | notes |
|---|---|---|---|---|---|---|
| Mode | ChoiceSetting {Regular, Legit} | Regular(0) | — | — | — | **Legit recovered from source**; screenshot showed only Regular |
| Duration | NumberSetting "ms" | 200 | 50 | 2000 | 50 | matches screenshot value; range/step from source |
| Cooldown | NumberSetting "ms" | 1000 | 0 | 5000 | 50 | matches screenshot value; range/step from source |

Bind: shown unassigned in screenshot; do NOT auto-bind (preserve existing Rice Slinky keybindings).
`statusText()` = selected mode name (source `getInfo()` returns MODES[mode]).

## 4. Event mapping (Valen → dev.rice SemanticEvent type codes, verified against VelocityModule/TimerRangeModule)
| Valen handler | dev.rice type | detail |
|---|---|---|
| onSendPacket (C08 fire charge) | **3** send-packet | `e.packet instanceof C08PacketPlayerBlockPlacement`, stack item == fire_charge → `jump.onUse(nanoTime)` |
| onExplosion (S27) | **2** receive-packet | `e.packet instanceof S27PacketExplosion` → `jump.onExplosion(...)` with player distSq |
| onVelocity (S12) | **2** receive-packet | `e.packet instanceof S12PacketEntityVelocity`, entityId == player → `jump.onVelocity(...)` (motion /8000.0) |
| onReceivePacket (S08 PosLook) | **2** receive-packet | `e.packet instanceof S08PacketPlayerPosLook` → correctionPending = true (abort cycle) |
| onGameLoop (RunGameLoopEvent) | **see §6 (open)** | main cycle.update/begin + abort-on-correction + jump consume |
| onRenderTick (keep-held) | render tick | minor; re-assert during active cycle only if a feature render event exists, else omit with note |

## 5. Timer mechanism (verified — identical primitive to Valen)
dev.rice modules drive the timer directly, exactly like Valen's Lunar accessor:
- Valen `((IAccessorMinecraft)mc).valen$lunar$getTimer()` → dev.rice `MinecraftFields.timer(c.minecraft())`.
- freeze = `timer.timerSpeed = 0f`; catch-up burst = `timer.elapsedPartialTicks += ticks`; release = `timer.timerSpeed = 1f`.
- `TimerController.Owner` (shared enum) already includes FIREBALL_BOOST, STASIS, BRIDGE_EGG,
  FIREBALL_PEARL_CLUTCH, AUTO_PEARL → the SAME arbiter will coordinate all five ported modules. Port
  `TimerController` + `TimerRangeCycle` into dev.rice (pure logic; catch-up/applySpeed consumers bound to
  `MinecraftFields.timer`). Regular = immediate burst; Legit = freeze-until then burst. Duration→ticks = min(maxTicks=40, duration/50). Cooldown ms → nanos.
- Must respect existing native timer ownership: abort if `NativeFeatureBridge.timerBlocked()` (another owner holds the timer), mirroring Valen's `timer.hasRequestOtherThan(FIREBALL_BOOST)` guard.

## 6. Per-frame event routing — RESOLVED (evidence-backed)
`struct Descriptor` carries `Slot events[27]` (types 0–26), and `src/logic/combat/anti_fireball.cpp`
registers `descriptor.events[1]={onFrame,0}` — i.e. **type 1 is the per-frame run-loop event and is
routable to any native module slot**. The 5 Java feature descriptors simply never opted into it.
Resolution: Fireball Boost registers `fireballBoost.events[1]={dispatch<5>, prio, 0}`; `dispatch<5>`
routes to `NativeFeatureBridge.event(5,1,…)` → module `onEvent(ctx, SemanticEvent{type=1})`, which runs
the nanotime-driven cycle. Valen's `onGameLoop` is `System.nanoTime()`-driven, so no reliance on
`elapsedSeconds` (which `event()` does not populate) — only that the callback fires each loop. **No new
native hook required.** Option (a) confirmed; (b)/(c) rejected.

## 7. Native 6th-slot recipe (verified from source_features.cpp displace_install)
1. `static Descriptor fireballBoost;` add `&fireballBoost` → `descriptors[6]`; grow `settings[6][64]`,
   `counts[6]`, `statuses[6][128]`; cleanup loops `i<5` → `i<6`.
2. In `displace_install()`: set name/description/category(1=Movement)/`settings=show<5>`/`visit=visit<5>`/
   `status=status<5>`; register `fireballBoost.events[t]={dispatch<5>,prio,0}` for its types (3,2,5,26 + the §6 frame type).
3. `metadata()` persistence prefix for index 5: `"source_fireball_boost_"`.
4. `extern "C" void* fireball_boost_descriptor(){displace_install();return &sourcefeatures::fireballBoost;}`
   and register that descriptor where `bow_aimbot_descriptor()/fog_descriptor()/…` are consumed by the menu.
5. `source_features_initialize`: `metadata(e,0..5)` (add 5).
6. Java `NativeFeatureBridge.initialize()`: `modules=new Module[6]; modules[5]=new <FireballBoostValen>(); manager.register(modules[5]);`
   `labels/specification/configure/visible/status/event` are already index-generic → no change.
7. `build.py`: add the new module + `dev.rice.module.impl.fireball.*` + ported TimerController/TimerRangeCycle sources to the compile list.

## 8. Remaining Stage-3 steps
- [x] §6 frame-event routing resolved: register `events[1]` on the feature descriptor (evidence: anti_fireball.cpp).
- [x] `FireballJumpTracker` ported (feature-bridge/java/fireball/FireballJumpTracker.java, faithful 1:1).
- [x] Ported `TimerController` + `TimerRangeCycle` to dev.rice (feature-bridge/java/fireball/, pure logic).
- [x] Module body `FireballBoostFeatureModule` written (§4 handlers, §5 timer, native-timer ownership guard).
- [x] Native 6th descriptor added (source_features.cpp) + registered at m->mods[0x4b] (module_manager.cpp, count 0x4c).
- [x] `build.py` sources added; compiled `--release 8` clean across named/SRG/official (1271 classes).
- [x] Headless test `FireballBoostLogicTest` (35 assertions) passed; CTest 32/32 passed incl. SourceFeaturesJvm x3.
- [x] Packaged v1.8.12-fireball-boost: exe ca780d2a, dll 147114d0, embedded==rebuilt MATCH, priors preserved.

STATUS: **DONE (static/headless verified).** Untested: live Lunar/BedWars gameplay + anticheat interaction.
