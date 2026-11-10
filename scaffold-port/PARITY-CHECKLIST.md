# Scaffold source-to-port parity checklist

This is the current checklist. Historical notes are retained under history/; their earlier completeness/provenance claims are superseded. A checked handler means implemented and covered by fixtures, not exact original-client or live-server parity.

Reference commit: c08acf7223ce9a6cde453cf86f97d77d92c742ba (ricekitti/rise-open). Repository source and recovered dump are separately identified in SOURCE-RECOVERY.md.

## Main modes

| Stable id | Mode | Implementation / remaining boundary |
|---|---|---|
| 0 | Normal | Shared placement pipeline, camera/face sweep, Strict/Grim branches; shared guards integrated |
| 1 | Godbridge | Rotation, key remap, timed sneak and placement handler; live combinations pending |
| 2 | Breesily | Rotation and movement/key predicates; live combinations pending |
| 3 | Snap | Source snapping and movement predicates; live combinations pending |
| 4 | Telly | Base, Watchdog and Prediction rotation branches; input jump, right-click/flag fallback, boost and visuals; remaining parent prediction combinations pending |
| 5 | Eagle | Source rotation/input/placement handler; destination rendering/event adaptations remain |

## Sprint modes

| Stable id | Source mode | Status |
|---|---|---|
| 0 | Normal | No extra sprint handler, as source |
| 1 | Disabled / Vf | Implemented |
| 2 | Legit / Vg | Implemented |
| 3 | Bypass / Ve | Implemented |
| 4 | Vulcan / Vj | Implemented with child speed, distance/potion/liquid/web/depth-strider and owned packet cycle |
| 5 | First Watchdog Jump / Vi | Independent implemented handler (named VerusSprint in the readable dump) |
| 6 | Matrix / Vh | Angular/input handler implemented; absent Rise Speed module is not replaced by an unrelated destination module |
| 7 | Watchdog Prediction / Vm | Startup packets, pitch rewrite, correction/block deadlines, input freeze and camera ownership implemented; arbitration adapted |
| 8 | Second Watchdog Jump / Vk | Readable Vk/vo movement, packet, jump-cancel and cleanup handlers implemented; child level predicate retained, missing source/dependencies unresolved |
| 9 | Watchdog / Vn | Source ground speed, signed correction penalty, air additive drag and sprint-key cleanup implemented |

## Tower and downward modes

| Stable ids | Modes | Status |
|---|---|---|
| Tower 0 | Disabled | No tower behavior |
| Tower 1,2,3,4 | Vulcan, Vanilla, Normal, Air Jump | Known handlers implemented |
| Tower 5 | Watchdog / Wa | Readable Wa stages, alignment/rotation, input/strafe, packet counter and deferred bind implemented; original empty branches/cadence unresolved |
| Tower 6,7,8,9 | MMC, NCP, Matrix, Legit | Known handlers implemented; NCP uses once-only no-event packet path |
| Tower 10 | Verus | Known motion handler implemented; suspicious empty packet/input branches remain unproven |
| Tower 11 | Watchdog Prediction 1.8 | Known source handler implemented; Via target checked when available |
| Down 0,1,2,3 | Off, Normal, Watchdog, Verus | Offset/input/timing predicates implemented; Watchdog Downwards also releases the parent's one-use edge request |

## Setting coverage

| Settings | Status / boundary |
|---|---|
| Eight mode dropdowns, yaw offset | Stable ids, defaults, options, visibility and keyed persistence covered; all choices retain independent source handlers; incomplete original-source parity remains explicit |
| Timer | Source target predicate and owned timer lifecycle implemented; foreign speed is preserved |
| Expand, Up Side Down | Source vector/offset order implemented, including Watchdog Downwards overriding the earlier upward offset |
| Rotation Speed, Place Delay | Sampling and placement gates implemented, including falling/prediction delay branches |
| Sneak, Start/Stop Sneaking, Sneak Every x Blocks, Sneaking Speed | Source scheduler, packet counters and real input multiplier implemented; pause counters and placement threshold covered (external writers absent) |
| Movement Correction | Integrated through existing native rotation arbitration; exact source phase/jitter not established |
| Safe Walk | Primary Entity.crh one-use X/Z/diagonal clipping implemented, including in-air behavior; source session gate adapted to valid destination context; no shared native Safe Walk flag writes |
| New Watchdog Rots | Independent source displacement/step/key/speed branch implemented for both stable Watchdog Jump ids; Vk now uses its own motion rotation scope |
| Keep-Y bypass | Source setting retained; alternate 147/117-degree branch depends on absent Rise Speed module. The independent 140-degree default is implemented |
| Watchdog Prediction | Telly arc, sweep/reach and delay branches implemented; broader parent placement combinations incomplete |
| Watchdog Telly, Jump Delay | Ascent/placement rotation branches and actual input-phase sampled jump delay implemented |
| Watchdog Telly Rotation Speed | Source target step range implemented |
| Rotation Block Boost, Rotation Block Rotation Speed, Boost Only While Holding Jump | Pending boost, independent range and held-key predicate implemented |
| Visual Back Rotations | Source visual state plus scoped native render adaptation implemented; appearance/runtime trace still pending |
| Block Diagonal Ascend | Source diagonal held-jump predicate implemented |
| Don't Force Raycast on Watchdog Telly | Force predicate and cleanup ownership implemented; velocity-alias inference is recorded |
| Extend Block Reach on Watchdog Telly | Source 4.5/5.5 search probes and Prediction 6.0 sweep implemented; no general reach-module substitution |
| Disable On Flag | One-tick-after-correction trigger, ten eligible-placement recovery, Normal fallback and toggle cleanup implemented |
| Telly Only on Right Click | Effective-mode selection and non-Strict manual-click cancellation implemented |
| Render | Selected item icon, source hotbar total, centered measured panel and 900 ms animation implemented; GL/cache/matrix/depth cleanup covered. Destination fonts/theme and rounded underlay replace Rise shader queues |
| Advanced | Source conditional visibility implemented; hidden values retain source behavior |
| Ignore Speed Effect | Source allowed-distance helper predicate implemented |
| Bypass Raycast When Falling | Source falling placement gate implemented |
| Vulcan Sprint Speed child | Implemented, appended after the 39 parents to preserve existing ids/order |
| Second Watchdog Jump Place Extra Blocks child | Child default/visibility/bridge and known level predicate implemented; source placement-output branch is empty and remains unresolved |

## Shared dependencies and validation

- [x] Walking-player Pre/PostMotion, precise packet fields and client-position separation.
- [x] Movement input after vanilla polling, scaling and unchanged-float preservation.
- [x] Slot identity/history, held-item/controller hooks, eligible-attempt tracking and cleanup.
- [x] Global early packet-order guard and late accepted-packet timing; source priority/cancellation differences preserved.
- [x] Silent packet identity across thread handoff; failure/unload cleanup.
- [x] Scoped model pose and original-render-exception preservation.
- [x] Slot/count HUD, retained-item release and actual painter checks using a GL command recorder.
- [x] One-use Safe Walk collision probes, airborne behavior, unchanged foreign movement, virtual-call/exception passthrough and all three movement hook sites.
- [x] Parent enable targets, pre-block input cache, pause scheduler/placement gate, Watchdog early rotations and common placement fallbacks.
- [x] Vk/vo jump cancellation and Wa deferred bind; native input thread queues requests without JNI.
- [x] Scoped walking-packet rotations preserve camera ownership and original exceptions.
- [x] Native menu pause releases Scaffold-owned state while preserving global packet timing.
- [x] Java 8 payload and fixtures for named/SRG/official mappings; native regression and resource checks (see BUILD-RESULTS.json).
- [ ] Complete recovery of failed/empty original handlers and exact 6.9.7 provenance.
- [ ] Full parent/submode combinations, exact Rise shader appearance, helper and cross-client dependencies.
- [ ] Live injection, packet traces, game geometry under load, disconnect/reconnect, repeated injection and representative combinations.

No server bypass or gameplay success is claimed. Passing fixtures do not supply missing original bytecode.
