# v1.8.14 - Fireball Pearl Clutch

- Adds the separate Fireball Pearl Clutch under Player, preserving the existing native Fireball Pearl. Uses the recovered PearlRescueModule fireball-mode wrapper and PearlFireballSequence, rather than replacing either feature with a renamed menu entry.
- Shares the pearl candidate search/physics and source conditions from v1.8.13. Fireball mode registers twelve source controls: Pre pearl timer and Swap back are excluded. The remaining source defaults/ranges are retained, with no automatic key bind.
- When a falling rescue cannot find a normal pearl solution, the recovered combo aims upward, sends the pearl, waits at least one player tick, sends the fireball, restores the prior selected slot, and applies the source 0.03 flight timer. Item presence, rotation agreement, coordination, deadlines and correction/landing/disable cleanup gate each phase.
- Native AntiFireball receives a rescue-launch notification that reapplies its existing three-block near-origin exclusion to current tracked projectiles; future arrivals use the unchanged first-observation exclusion. This explicitly adapts to Rice's native proximity rule. It does not reconstruct Valen's missing separate OwnLaunch directional/time heuristic. Native AntiFireball behavior outside this notification remains unchanged.
- Retains v1.8.13's documented native Clutch planned-target query, bed-target/mining guard, native timer yielding, relationship flags and toggle-binding adaptations. The shared Java timer coordinates Boost and both pearl variants.
- Corrected the test world to support full controlled-player fall simulation and outgoing use-packet observation; these fixtures are not embedded. Bridge Egg and working Auto Quick Buy purchases are not registered by this release.
- Launcher and Windows file/product versions are 1.8.14.0. Previous versioned EXEs are preserved.

Validation: full 32-test CTest suite; 22 Auto Pearl checks and 13 Fireball Pearl Clutch checks in each of named, SRG and official mappings. Combo checks exercise full controlled fall physics, upward aim, actual vanilla item-use packets, one-tick separation, exact packet order, slot restoration, flight-timer ownership, native notification and correction cleanup. The versioned EXE's decoded DLL is checked against the rebuilt DLL.

Not verified: live Lunar/BedWars gameplay, real-server packet timing, actual boost/pearl outcomes or anticheat interaction. Restart Minecraft before injecting.
