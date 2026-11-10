# Rice Slinky v1.8.17 — rescue priority, pearl accuracy and menu overlap

- Reserve 76 logical pixels for the fixed Edit HUD footer. Settings cards intersect the content clip, their scroll range includes the reserved space, and controls behind the footer cannot receive initial clicks/hover.
- Add default-on **Wait for Fireball Pearl Clutch** to Bridge Egg Clutch. An independent readiness query gives eligible Fireball Pearl Clutch the first attempt without circular waiting. Bridge Egg can follow after fireball flight ends if the player is still falling and Egg's existing conditions pass. Missing items or unmet fireball conditions do not block Egg. The toggle is saved through the normal settings system.
- Remove the older regular **Fireball Pearl** menu registration. Keep **Fireball Pearl Clutch** in Utility. The older implementation remains dormant and covered by its existing regression test.
- Improve Auto Pearl targeting: require the selected landing tile, reject impacts within 0.1 blocks of its edges, and refine pitch in 0.01-degree increments. Revalidate actual packet aim immediately before item use, as before. This does not remove Minecraft's random projectile spread.
- Stop treating flight timeout or pre-teleport ground contact as successful pearl completion. Timeout cleans up the attempt without disabling the module. Confirmed server position/teleport completion still honors Disable afterwards. Normal pearl flight gets at least 10 seconds; upward fireball-combo flight gets 12 seconds plus measured ping instead of 3.5 seconds.

Validation: all 32 CTest tests passed, including named/SRG/official JVM integration. Added selected-tile/edge validation, airborne timeout/ground regressions, both-enabled rescue ordering, unavailable-item/condition fallback, follow-up Egg while falling and no follow-up after landing. HUD geometry checks cover footer separation, scaled heights, last-setting reachability and tiny windows. Versioned EXE runtime extraction matches the rebuilt DLL. Launcher project and Windows file/product versions are 1.8.17 / 1.8.17.0. Previous versioned EXEs are preserved.

Live Minecraft gameplay and visual rendering have not been verified in this session. Auto Quick Buy remains blocked on the missing PurchaseConfirmation/Status implementation and is not registered.

Restart Minecraft before injecting this update. Release: `rice-loader-v1.8.17-rescue-priority-accuracy-fix.exe`.
