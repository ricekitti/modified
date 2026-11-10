# Modules by category

Category = descriptor field `+0x10` (0 Combat, 1 Move, 2 Visual, 3 Block,
4 Utility), matching the GUI tab strings at `.rdata` `0x151cc8`. Paths are
relative to the repo root.  The first five sections are the 61 modules with a
GUI callback; the six engine-only modules are listed at the end.

### Combat (17)

| module | file | functions | description |
|---|---|---|---|
| Aim Assist | [aim_assist.cpp](../src/modules/combat/aim_assist.cpp) | cb, reg, aux | Pulls your crosshair towards other players. |
| Auto Block | [auto_block.cpp](../src/modules/combat/auto_block.cpp) | cb, reg | Blocks your sword for you in combat. |
| Auto Clicker | [auto_clicker.cpp](../src/modules/combat/auto_clicker.cpp) | cb, reg, aux | Clicks for you when holding down left-click. |
| Auto Weapon | [auto_weapon.cpp](../src/modules/combat/auto_weapon.cpp) | cb, reg | Selects your weapon when aiming on players. |
| Backtrack | [backtrack.cpp](../src/modules/combat/backtrack.cpp) | cb, reg, aux | Uses lag to make players easier to hit. |
| Criticals | [criticals.cpp](../src/modules/combat/criticals.cpp) | cb, reg, aux | Increases chance of landing critical hits. |
| Hit Select | [hit_select.cpp](../src/modules/combat/hit_select.cpp) | cb, reg, aux | Perfectly times your hits. |
| Hitboxes | [hitboxes.cpp](../src/modules/combat/hitboxes.cpp) | cb, reg, aux | Expands hitboxes of other players. |
| Knockback Delay | [knockback_delay.cpp](../src/modules/combat/knockback_delay.cpp) | cb, reg, aux | Delays your knockback in certain scenarios. |
| Knockback Displace | [knockback_displace.cpp](../src/modules/combat/knockback_displace.cpp) | cb, reg, aux | Allows dealing knockback at a different angle. |
| Lag Range | [lag_range.cpp](../src/modules/combat/lag_range.cpp) | cb, reg, aux | Uses lag to stay out of target range. |
| Piercing | [piercing.cpp](../src/modules/combat/piercing.cpp) | cb, reg | Allows hitting entities through obstructions. |
| Reach | [reach.cpp](../src/modules/combat/reach.cpp) | cb, reg, aux | Increases your melee range. |
| Sprint Reset | [sprint_reset.cpp](../src/modules/combat/sprint_reset.cpp) | cb, reg, aux | Resets your sprint state to deal more knockback. |
| Timer Range | [timer_range.cpp](../src/modules/combat/timer_range.cpp) | cb, reg, aux | Abuses timer lag to teleport you. |
| Velocity | [velocity.cpp](../src/modules/combat/velocity.cpp) | cb, reg, aux | Reduces the amount of knockback you take. |
| Weapons | [weapons.cpp](../src/modules/combat/weapons.cpp) | cb, reg | Configure which items are considered weapons. |

### Move (7)

| module | file | functions | description |
|---|---|---|---|
| Fast Accel | [fast_accel.cpp](../src/modules/move/fast_accel.cpp) | cb, reg | Speeds up acceleration. |
| Inv Walk | [inv_walk.cpp](../src/modules/move/inv_walk.cpp) | cb, reg, aux | Allows moving in inventory. |
| Keep Sprint | [keep_sprint.cpp](../src/modules/move/keep_sprint.cpp) | cb, reg, aux | Keeps your sprint speed when attacking players. |
| No Slow | [no_slow.cpp](../src/modules/move/no_slow.cpp) | cb, reg | Removes slowdown while using items. |
| Safe Walk | [safe_walk.cpp](../src/modules/move/safe_walk.cpp) | cb, reg | Prevents you from walking off ledges. |
| Sprint | [sprint.cpp](../src/modules/move/sprint.cpp) | cb, reg | Automatically sprints for you. |
| Strafe | [strafe.cpp](../src/modules/move/strafe.cpp) | cb, reg | Allows faster strafing. |

### Visual (14)

| module | file | functions | description |
|---|---|---|---|
| Anti Debuff | [anti_debuff.cpp](../src/modules/visual/anti_debuff.cpp) | cb, reg | Removes negative visual effects. |
| Block ESP | [block_esp.cpp](../src/modules/visual/block_esp.cpp) | cb, reg | Highlights blocks. |
| Chams | [chams.cpp](../src/modules/visual/chams.cpp) | cb, reg | Renders players behind blocks. |
| Chest ESP | [chest_esp.cpp](../src/modules/visual/chest_esp.cpp) | cb, reg | Highlights chests. |
| Free Look | [free_look.cpp](../src/modules/visual/free_look.cpp) | cb, reg | Freely move the camera around. |
| GUI | [gui.cpp](../src/modules/visual/gui.cpp) | cb, reg | Graphical interface for the cheat. |
| Indicators | [indicators.cpp](../src/modules/visual/indicators.cpp) | cb, reg | Points to incoming projectiles. |
| Item ESP | [item_esp.cpp](../src/modules/visual/item_esp.cpp) | cb, reg | Highlights dropped items. |
| Mod Overlay | [mod_overlay.cpp](../src/modules/visual/mod_overlay.cpp) | cb, reg | Displays a list of all active mods. |
| Name Tags | [name_tags.cpp](../src/modules/visual/name_tags.cpp) | cb, reg | Makes player name tags easier to see. |
| Notifications | [notifications.cpp](../src/modules/visual/notifications.cpp) | cb, reg | Informs you when certain actions are performed. |
| Player ESP | [player_esp.cpp](../src/modules/visual/player_esp.cpp) | cb, reg, aux | Highlights other players. |
| Pointers | [pointers.cpp](../src/modules/visual/pointers.cpp) | cb, reg, aux | Points to other players. |
| Trajectories | [trajectories.cpp](../src/modules/visual/trajectories.cpp) | cb, reg | Predicts the trajectory of the item in your hand. |

### Block (7)

| module | file | functions | description |
|---|---|---|---|
| Auto Tool | [auto_tool.cpp](../src/modules/block/auto_tool.cpp) | cb, reg | Selects the best tool when digging. |
| Block In | [block_in.cpp](../src/modules/block/block_in.cpp) | cb, reg | Places blocks around you. |
| Bridge Assist | [bridge_assist.cpp](../src/modules/block/bridge_assist.cpp) | cb, reg | Sneaks and unsneaks for you while bridging. |
| Clutch | [clutch.cpp](../src/modules/block/clutch.cpp) | cb, reg | Clutches for you when falling. |
| Fast Mine | [fast_mine.cpp](../src/modules/block/fast_mine.cpp) | cb, reg, aux | Increases block mining speed. |
| Fast Place | [fast_place.cpp](../src/modules/block/fast_place.cpp) | cb, reg, aux | Allows placing blocks faster. |
| Ghost Hand | [ghost_hand.cpp](../src/modules/block/ghost_hand.cpp) | cb, reg | Allows interacting with blocks through entities. |

### Utility (16)

| module | file | functions | description |
|---|---|---|---|
| Anti Bot | [anti_bot.cpp](../src/modules/utility/anti_bot.cpp) | cb, reg | Ignores NPCs for specific modules. |
| Auto Pot | [auto_pot.cpp](../src/modules/utility/auto_pot.cpp) | cb, reg | Uses healing potions for you. |
| Auto Rod | [auto_rod.cpp](../src/modules/utility/auto_rod.cpp) | cb, reg | Throws your fishing rod for you. |
| Auto Soup | [auto_soup.cpp](../src/modules/utility/auto_soup.cpp) | cb, reg | Uses soups for you. |
| Blink | [blink.cpp](../src/modules/utility/blink.cpp) | cb, reg, aux | Simulates teleporting using lag. |
| Chest Stealer | [chest_stealer.cpp](../src/modules/utility/chest_stealer.cpp) | cb, reg | Automatically loots chests for you. |
| Debug | [debug.cpp](../src/modules/utility/debug.cpp) | cb, reg | Prints debug information in chat. |
| Fake Lag | [fake_lag.cpp](../src/modules/utility/fake_lag.cpp) | cb, reg, aux | Adds an artificial delay to your packets. |
| Friends | [friends.cpp](../src/modules/utility/friends.cpp) | cb, reg | Lets you mark other players as friendly. |
| Inv Manager | [inv_manager.cpp](../src/modules/utility/inv_manager.cpp) | cb, reg | Organises your inventory for you. |
| No Hit Delay | [no_hit_delay.cpp](../src/modules/utility/no_hit_delay.cpp) | cb, reg, aux | Removes 1.8 swing delay. |
| No Item Release | [no_item_release.cpp](../src/modules/utility/no_item_release.cpp) | cb, reg, aux | Hides item release from server. |
| No Use Delay | [no_use_delay.cpp](../src/modules/utility/no_use_delay.cpp) | cb, reg | Removes vanilla delay when consuming items. |
| Refill | [refill.cpp](../src/modules/utility/refill.cpp) | cb, reg | Automatically moves items into your hotbar. |
| Teams | [teams.cpp](../src/modules/utility/teams.cpp) | cb, reg | Ignores teammates for specific modules. |
| Timer | [timer.cpp](../src/modules/utility/timer.cpp) | cb, reg, aux | Modifies the speed the game runs at. |

### Engine-only (6)

These descriptors exist in the image but have no GUI callback and no settings
pane. They are kept in `manifest.json` so the module table is exactly the 67
descriptors from the image.

| module | category | file | description |
|---|---|---|---|
| No Jump Delay | move | [no_jump_delay.cpp](../src/modules/move/no_jump_delay.cpp) | Removes cooldown when holding jump key. |
| Ping Fix | utility | [ping_fix.cpp](../src/modules/utility/ping_fix.cpp) | Shows your real ping while using lag mods. |
| No Hurt Cam | visual | [no_hurt_cam.cpp](../src/modules/visual/no_hurt_cam.cpp) | Removes camera shake when taking damage. |
| Instant Stop | move | [instant_stop.cpp](../src/modules/move/instant_stop.cpp) | Instantly stops moving when move keys are released. |
| Item Use Fix | utility | [item_use_fix.cpp](../src/modules/utility/item_use_fix.cpp) | Allows using items while breaking blocks. |
| Null Move | move | [null_move.cpp](../src/modules/move/null_move.cpp) | Makes it easier to switch move direction instantly. |
