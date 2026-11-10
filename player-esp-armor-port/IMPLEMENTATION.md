# PlayerESP 1:1 port — armor/held-item/enchant icons (implementation spec)

Status: **grounding complete, APIs/template/data-mechanism all verified against disk.
Next = mechanical edits below + full build/CTest + versioned EXE.** No code written yet;
nothing in the tree is half-edited.

## What the foreign decomp adds vs rice slinky's current PlayerESP

rice `logic/visual/player_esp.cpp` already matches the foreign box/healthbar/hurt-tint/
2D-3D/team-friend-target behaviour. The foreign `fn_1800aa540` extra = an **armor + held-item
icon column beside each box**, with **enchantment abbreviation labels**, plus box-thickness math.

## Key finding — no new JNI plumbing needed

`logic/visual/name_tags.cpp` (lines 419-463) ALREADY renders that exact column using APIs that
exist and are proven:
- `java_call_entity_player_get_equipment_in_slot(a, ent, k)` for k=0..4 (held + 4 armor)
- `java_new_render_item`, `java_render_item_init` (renderItemAndEffectIntoGUI),
  `java_render_item_render_item_overlay_into_gui` (count/durability overlay)
- `java_get/set_render_item_z_level`, `RenderHelper.enable/disableStandardItemLighting`
- enchant tags: `java_call_item_stack_get_enchantment_tag_list` + NBT list walk +
  `rdata_153870[id]` abbreviation table + `compare_key_table` sort
The port lifts this block into PlayerESP's gather+render. **name_tags.cpp is the literal template.**

## Hard constraint and the chosen approach

`data_2f7e20[100]` (the Player record, `.zero 8000` = 100*0x50 in data.s) is fixed-layout and
referenced by offset; it has NO room for armor refs or an entity ref. So:
- Do NOT grow the Player struct.
- Add a **parallel scratch array** at a NEW appended symbol, indexed 1:1 with data_2f7e20,
  holding {entity globalref, Slot[5]} per player (mirror name_tags' Tag.slot[5]).

## Exact edits

### 1. `src/data/rdata.s` (append at END — new strings, referenced only by new .quad)
```
.globl rdata_espShowArmor
rdata_espShowArmor:   .asciz "show_armor"
.globl rdata_espShowEnch
rdata_espShowEnch:    .asciz "show_enchants"
```

### 2. `src/data/data.s` (append at END — shifts nothing)
Two bool settings (descriptor + name ptr + value byte, matching the data_2ea160/2ea178 pattern):
```
.globl data_espArmorDesc
data_espArmorDesc:  .zero 16
.quad rdata_espShowArmor
.globl data_espArmorVal
data_espArmorVal:   .byte 0x00,0,0,0,0,0,0,0     # default OFF
.globl data_espEnchDesc
data_espEnchDesc:   .zero 16
.quad rdata_espShowEnch
.globl data_espEnchVal
data_espEnchVal:    .byte 0x00,0,0,0,0,0,0,0     # default OFF
```
Parallel scratch (entity ref + 5 slots * {has, count, refs}); 100 entries. Use a plain byte blob
sized generously and model it as a struct in C++:
```
.globl data_espSlots
data_espSlots:      .zero 6400                   # 100 * 0x40  (EspSlots struct, see below)
```

### 3. `src/logic/visual/player_esp.cpp`
- Declare: `extern "C" unsigned char data_espArmorVal, data_espEnchVal;`
  and `struct EspSlots { void *ent; struct { unsigned char has; int count; void **refs; } slot[5]; };`
  `extern "C" EspSlots data_espSlots[100];` plus the equipment/enchant/render-item FN() decls
  copied verbatim from name_tags.cpp (lines 105-134) and the render-item globals.
- In `player_esp_tick`: after a player is accepted into data_2f7e20[idx], populate
  `data_espSlots[idx]` exactly like name_tags lines 305-355 (NewGlobalRef the entity; for k=0..4
  get_equipment_in_slot -> has; if data_espEnchVal, build enchant label globalrefs). Release the
  prior tick's refs at the top of tick (mirror name_tags_disable / item_esp_tick cleanup) to avoid
  JNI localref/globalref leaks. Keep the qsort — but the parallel array must be reordered WITH it,
  so either (a) sort an index and gather, or (b) store slots keyed by the same compare. Simplest:
  move the qsort to sort a small index array, then write data_2f7e20 + data_espSlots in sorted order.
- In `player_esp_render_classic_3d` and `player_esp_render_mode_0`: after the box/healthbar for
  player i, if `data_espArmorVal` and `data_espSlots[i].ent`, reproduce name_tags lines 419-463
  (enableStandardItemLighting; translate to the box's top-left; loop slots drawing icons at 16px
  steps; overlay; if data_espEnchVal draw the enchant labels; disableStandardItemLighting; restore
  GL blend/color + render-item z-level). Guard on h->shader paths as name_tags does.

### 4. `src/modules/visual/player_esp.cpp`
- `D`/extern the new settings; in `player_esp_register` add
  `R_bool(m, rdata_espShowArmor, data_espArmorDesc); R_bool(m, rdata_espShowEnch, data_espEnchDesc);`
- In `player_esp_gui` add `W_bool` rows for both (place under the box section; show enchants row
  only when armor is on).

## Build + verify (from CODEX-HANDOFF §3, run in order, stop on failure)
```
python modified/feature-bridge/build.py
cmake --build modified/slinkyreadablesource/slinkyreadablesource/build
copy build/slinky.dll -> provided-injector/rice-launcher/src/bundled/RiceNative.dll
VsDevCmd x64 && cmake --build provided-injector/rice-launcher/build
ctest --test-dir provided-injector/rice-launcher/build --output-on-failure   # must stay green
```
Then per `AGENTS.md`: cut `modified/releases/rice-loader-vX.Y.Z-player-esp-armor.exe` +
matching `.changes.md` / `.verified.json`, bump launcher CMakeLists + src/rice-slinky.rc to X.Y.Z,
refresh root `slinky-modified.exe`.

## Still blocked (source gap — unchanged)
Full 1:1 of **NameTags / ItemESP / Chams** needs their foreign decomp .c files. PlayerESP is the
only one with complete foreign source. NameTags' icon column is already native (above), so a
NameTags "1:1" mostly needs the foreign file to diff against.
```
