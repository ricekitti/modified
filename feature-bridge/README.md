Current release: v1.8.18 makes Bridge Egg wait strictly for the enabled Fireball Pearl Clutch attempt/flight to end. Pending conditions or missing items cannot bypass the wait. See ../releases/rice-loader-v1.8.18-bridge-egg-strict-wait.exe and matching changelog/proof. Full CTest is 32/32, including all three mappings. Live gameplay remains unverified. Restart Minecraft before injecting.

Earlier releases (historical):

Current release: v1.8.17 fixes Edit HUD/settings overlap, adds default-on Bridge Egg waiting for Fireball Pearl Clutch, removes the regular Fireball Pearl menu entry, tightens selected-tile pearl aim and stops unconfirmed flight timeouts/ground contact from disabling Pearl Clutch. All 32 CTest tests pass; 28 Auto Pearl + 13 FPC + 30 Egg + 21 Quick Buy helper checks per mapping. See ../releases/rice-loader-v1.8.17-rescue-priority-accuracy-fix.exe and matching changelog/proof. Live gameplay/visual verification remains outstanding. Restart Minecraft before injecting.

Earlier releases (historical):

Current release: v1.8.16 matches the supplied Valen numeric display/defaults, moves Auto Pearl / Fireball Pearl Clutch / Bridge Egg Clutch into Utility, restores native conditional visibility, and applies rescue aim to real walking packets before item use. Above void launch is verified end-to-end under named/SRG/official; full CTest is 32/32. See ../releases/rice-loader-v1.8.16-valen-utility-rescue-fix.exe and its changelog/proof. Auto Quick Buy remains blocked on PurchaseConfirmation/Status. Restart Minecraft before injecting.

Current release: v1.8.15 Bridge Egg Clutch, including Auto Pearl (v1.8.13) and Fireball Pearl Clutch (v1.8.14). All 32 CTest tests pass; 77 rescue/independent Quick Buy checks per mapping across named/SRG/official. Versioned EXEs/changelogs/proof are in ../releases/. Quick Buy automatic purchases remain blocked by missing PurchaseConfirmation/Status; see ../quickbuy-port/CURRENT-STATUS.md. Restart Minecraft before injecting. See the execution update in ../../CODEX-HANDOFF.md for the current registry and completed work.

Current release: v1.8.12 adds Fireball Boost under Movement (Valen port): packet-driven fireball-jump detection + a Regular/Legit freeze-burst client-timer cycle. Distinguished from the unregistered native FireballBoostModule. Clutch, Scaffold, Stasis, Displace, Bedplates and Anticheat are unchanged. See ../valen-selected-ref-20261105/STAGE3-FIREBALL-BOOST-SPEC.md and ../releases/rice-loader-v1.8.12-fireball-boost.exe. Restart Minecraft first.

Current release: v1.8.11 fixes Bedplates auto-disabling after a LWJGL render-state buffer error. Only Bedplates production logic changed. See ../bedplates-port/CURRENT-STATUS.md and ../releases/rice-loader-v1.8.11-bedplates-enable-fix.exe. Restart Minecraft first.

Current release: v1.8.10 adds RiceSlinky Anticheat under Utility and Off/Hold/Increase fatigue settings to Fast Mine. Unrelated Clutch, Scaffold, Displace and Bedplates sources are preserved. See ../snowify-anticheat-port/CURRENT-STATUS.md and ../releases/rice-loader-v1.8.10-riceslinky-anticheat-fatigue.exe. Restart Minecraft first.

Current release: v1.8.9 adds an opt-in Legit condition under Displace Void: left/right only, angle capped at 90; off keeps the unrestricted scan. Clutch, Scaffold and Bedplates unchanged. See ../displace-legit-port/CURRENT-STATUS.md and ../releases/rice-loader-v1.8.9-displace-legit.exe. Restart Minecraft first.

Current release: v1.8.8 adds Bedplates under Visual with defense icons, block counts, scaling, distance/offset and display modes. Clutch and Scaffold unchanged. See ../bedplates-port/CURRENT-STATUS.md and ../releases/rice-loader-v1.8.8-bedplates.exe. Restart Minecraft first.

Current Scaffold: v1.8.7 Keep Y auto-jumps while moving, retaining bridge height. RMB gates the right-click variant; Space still builds upward. Original Clutch unchanged; Fast Place guard retained. See ../scaffold-port/CURRENT-STATUS.md and ../releases/rice-loader-v1.8.7-scaffold-keepy-auto-jump.exe. Restart Minecraft first.

Current Scaffold: v1.8.6 locked Regular / Keep Y modes, Space upward placement, and Fast Place suppression. Original Clutch unchanged. See ../scaffold-port/CURRENT-STATUS.md and ../releases/rice-loader-v1.8.6-scaffold-regular-keepy.exe. Restart Minecraft first.

Current Scaffold: v1.8.5 native walk-off edge activation; jumping is not
required. Original Clutch is unchanged. See ../scaffold-port/CURRENT-STATUS.md
and ../releases/rice-loader-v1.8.5-scaffold-walk-off.exe.

Current Scaffold: restored v1.8.3 direct native Clutch copy. The v1.8.4
features are inactive. Use ../releases/rice-loader-v1.8.3-scaffold-clutch-copy.exe.

Current Scaffold update: v1.8.4 adds all 14 requested controls around the
isolated Clutch planner. Original Clutch is unchanged. See
../scaffold-port/CURRENT-STATUS.md and ../releases/. Restart Minecraft first.

Current Scaffold update: v1.8.3 registers a direct native Clutch copy named
Scaffold. The Mayu-style Java Scaffold is inactive. Original Clutch is unchanged.
See ../scaffold-port/CURRENT-STATUS.md and ../releases/.

Current Scaffold update: v1.8.2 uses the isolated Slinky targeting port with
existing Mayu-style movement and behavior. Clutch is unchanged. See
../scaffold-port/CURRENT-STATUS.md and ../releases/ for the versioned EXE.

Current Displace implementation (2026-11-04): the supplied manual Java module
replaces the native OG/Void extension. See ../DISPLACE-HANDOFF-STATUS.md.
The following describes the earlier bridge history.

The modified client uses original native Slinky Knockback Displace with an
optional Raven bS Void yaw selector. See ../OG-DISPLACE-STATUS.md for the active
implementation and validation. Original attack/timing/restore logic and source
hashes remain intact. Only Raven's scanner is imported, not its Displace module.
The extension preserves Slinky's cooldown values, scale and milliseconds format.

The bridge adds Bow Aimbot, Fog, Shaders, and Scaffold. Bow's supplied aiming
and prediction source remains intact, and its native enable-crash fix remains
active: rotation dispatch obtains the current thread's JNI environment and
scopes callback context rather than caching hook-stack pointers.

The adapter owns menu controls, configuration persistence, event conversion,
and lifecycle. Framework-only adaptations connect rotation priority/deltas,
relationship flags, GUI state, and Timer Range freeze checks to the current
native engine. Source settings/defaults are read from the original Java modules
and their initial configuration resource, rather than copied by hand.

Build `build.py` with Python, then build the modified native DLL, copy it to the
launcher bundle, and build the launcher. Java compilation targets Java 8. The
vendored framework JAR provides the original client's dependencies; build.py
also uses that project's local Minecraft 1.8.9/tooling dependencies. It emits
named, SRG, and official payloads plus a source/payload hash manifest.

SourceFeaturesJvmTest starts a separate JVM with real Minecraft classes and
test-only game/input objects. It exercises initialization, full settings,
Timer Range interoperation, actual Displace attack yaw/restoration, original
bow prediction and native rotation handoff, cleanup, and repeated injection.
It runs under all three naming formats. These checks do not replace a live
Lunar gameplay test. Test Minecraft JARs and input stubs are never embedded.

Legit Void appears under Knockback Displace when Direction is Void and is off
by default. Tests cover rear-only void rejection, both side paths, wrapped
yaw boundaries, normal Void behavior, and queued Aim Assist during activation.

Fog is compiled unchanged from the separately supplied fog-full-source archive
(SHA-256 recorded in the build manifest). It adds six controls under Visual,
including a native RGB picker. The existing native hook class is extended with
one setupFog wrapper; the wrapper calls vanilla first, then the supplied Fog
render handler. All five setupFog call sites in renderWorldPass are covered.
The generated hook class has 24 JNI declarations, checked against named, SRG,
and official Minecraft classes. Restart Minecraft when moving from older
loaders because the hook class gained a method.

Tests record Fog's GL writes without launching a game. They cover all four
falloff modes, color, opacity, zero opacity, water/lava, native settings/event
handoff, and normal distance restoration on disable. GL recorders and test
library copies are not embedded in the delivered client.

Shaders adds the separately supplied ShaderModule under Visual, with its
20 built-in Minecraft post-processing choices. It runs on the existing late
tick, lets Minecraft manage its shader pipeline and resizing, and releases the
effect on disable/world unload. A capability failure is reflected back into
the native module toggle so unsupported systems do not leave it enabled.

The preserved reference is unchanged; its build adaptation fixes the missing
scan_pixel resource to Minecraft's scan_pincushion resource and label. Tests
verify all 20 bundled asset paths, private loader reflection under every naming
format, preset changes, stable ticks without redundant reloads, pipeline loss
recovery, disable, and unsupported capability auto-disable. Shader application
is recorded at the vanilla renderer API boundary; live GPU shader execution
and gameplay remain unverified.

Scaffold now compiles the four user-supplied Java files unchanged, using the
matching compiled Snowify helpers with package/member mapping only. See
../scaffold-port/CURRENT-STATUS.md and build/supplied-source-manifest.json for
the active adapters and preserved source hashes. The former Mayu and continuous
movement implementations are inactive. SuppliedScaffoldTest and the native
JVM suite validate the replacement under all three game naming formats.
Restart Minecraft before loading rice-loader-supplied-scaffold.exe.

Bow enable crash fix (2026-11-03): source_features.cpp no longer retains
hook-local Jvm pointers or deleted local player/world references. Rotation
events get the current thread's JNIEnv from JavaVM and create fresh references;
scoped callback contexts restore outer state after nested dispatch. Native JVM
tests cover idle enable with expired prior context and actual aiming under
named, SRG, and official mappings. This fixes the native null-environment path
found in Lunar hs_err_pid34692.log; live gameplay has not been re-tested.

Current Displace build: ../rice-loader-void-direction.exe. Select Mode -> Raven bS
Void to enable the optional yaw selection. There is no separate Mode selector.
