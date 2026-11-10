// Weapons - item classification shared by Auto Weapon / Auto Tool style modules.
//   weapons_part_4: (cached per hotbar key) is the held item a weapon we accept?
//   weapons_part_2_2: does item id `id` of this item belong to an enabled weapon class?
//   weapons_part_3_2: best hotbar slot (0..8) or -1
//   weapons_part_4_2: clear the id cache
#include "slinky/engine.h"

struct SlotSetting { char pad0[0x18]; unsigned char on; char pad1[7]; };
struct IdCache { void *p, *q; };

extern "C" unsigned char data_2eeb58;    // "fist"
extern "C" SlotSetting data_2eeb60[];    // per-slot "allow" toggles
extern "C" unsigned char data_2eec98;    // "enchant_sharpness"
extern "C" unsigned char data_2eecb8;    // "enchant_knockback"
extern "C" unsigned char data_2eecd8;    // "enchant_fire_aspect"
extern "C" unsigned char data_2eecf8;    // "axes"
extern "C" unsigned char data_2eed18;    // "swords"
extern "C" int data_2fc558;              // cached key
extern "C" unsigned char data_2fc55c;    // cached verdict
extern "C" unsigned data_2fc560;         // cached enchant flags
extern "C" IdCache data_2fc568;

FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(int, java_get_inventory_player_current_item, (void *a, void *p));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *p, int slot));
FN(unsigned, enchantment_flags_item_stack, (void *a, void *item));
FN(void *, java_call_item_stack_get_item, (void *a, void *item));
FN(unsigned char, weapons_part_2, (void *a, void *item));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(unsigned char, id_vector_contains, (IdCache *cache, int id));
FN(void, libc_free, (void *p));

extern "C" unsigned char weapons_part_4(void *a, void *b, unsigned *out) {
    int key = java_get_entity_ticks_existed(a, b);
    if (key != data_2fc558) {
        data_2fc558 = key;
        data_2fc560 = 0;
        void *p = java_get_entity_player_2(a, b);
        int slot = java_get_inventory_player_current_item(a, p);
        void *item = java_call_iinventory_get_stack_in_slot(a, p, slot);
        unsigned char r;
        if (!item) {
            r = data_2eeb58;
        } else {
            unsigned fl = enchantment_flags_item_stack(a, item);
            data_2fc560 = fl;
            r = 1;
            if (data_2eeb60[slot].on)
                goto store;
            if ((fl & 1) && data_2eec98)
                goto store;
            if ((fl & 2) && data_2eecb8)
                goto store;
            if (fl >= 4 && data_2eecd8)
                goto store;
            r = weapons_part_2(a, java_call_item_stack_get_item(a, item));
        }
store:
        data_2fc55c = r;
    }
    if (out)
        *out = data_2fc560;
    return data_2fc55c;
}

extern "C" unsigned char weapons_part_2_2(void *a, void *item) {
    if (item) {
        int id = java_call_static_item_get_id_from_item(a, item);
        switch (id) {
        case 0x10b: case 0x10c: case 0x110: case 0x114: case 0x11b:
            return data_2eed18;
        case 0x102: case 0x10f: case 0x113: case 0x117: case 0x11e:
            return data_2eecf8;
        default:
            if (id)
                return id_vector_contains(&data_2fc568, id);
        }
    }
    return data_2eeb58;
}

extern "C" int weapons_part_3_2(void **ctx, void *player) {
    long i = 0;
    for (; i < 9; i++) {
        if (data_2eeb60[i].on)
            return (int)i;
        void *item = java_call_iinventory_get_stack_in_slot(*ctx, player, (int)i);
        if (!item)
            continue;
        void *a = *ctx;
        unsigned fl = enchantment_flags_item_stack(a, item);
        if ((fl & 1) && data_2eec98)
            return (int)i;
        if ((fl & 2) && data_2eecb8)
            return (int)i;
        if (fl >= 4 && data_2eecd8)
            return (int)i;
        if (weapons_part_2(a, java_call_item_stack_get_item(a, item)))
            return (int)i;
    }
    return -1;
}

extern "C" void weapons_part_4_2(Hook *h) {
    libc_free(data_2fc568.p);
    data_2fc568 = IdCache{};
}
