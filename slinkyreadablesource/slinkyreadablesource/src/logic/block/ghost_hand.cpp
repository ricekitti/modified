// Ghost Hand - descriptor +0xc8: raise the active flag; 0x46d10: may the
// attack go "through" (ignore) the entity `e` with the held item?
#include "slinky/engine.h"

struct Setting { char pad0[0x18]; unsigned char on; };

extern "C" unsigned char data_2f48c4;    // module enabled
extern "C" unsigned char data_2f4ad8;    // "require_mouse"
extern "C" unsigned char data_2f4af8;    // allow with fists
extern "C" unsigned char data_2f4b18;    // swords
extern "C" unsigned char data_2f4b38;    // tools
extern "C" unsigned char data_2f4b58;    // bows
extern "C" unsigned char data_2f4b78;    // flint and steel
extern "C" unsigned char data_2f4b98;    // cobweb
extern "C" unsigned char data_2f4bb8;    // other items
extern "C" unsigned char data_2f4bd8;    // non-living entities
extern "C" Setting data_2f4be0;          // bots
extern "C" Setting data_2f4c00;          // friends / teammates
extern "C" Setting data_2f4c20;          // others

FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(void *, java_jni_is_instance_of_entity_other_player_mp, (void *a, void *e));
FN(unsigned, entity_flags_get, (void *a, void *t));

extern "C" unsigned char ghost_hand_part_2(Ctx *c, void *e) {
    if (data_2f48c4 != 1)
        return 0;
    if (data_2f4ad8 == 1 && !vertex_pointer_current(c))
        return 0;
    const unsigned char *allow;
    void *held = item_held_hotbar_slot(c->a, c->b);
    if (!held) {
        allow = &data_2f4af8;
    } else {
        int id = java_call_static_item_get_id_from_item(c->a, held);
        switch (id) {
        case 0x100: case 0x101: case 0x102: case 0x10d: case 0x10e: case 0x10f: case 0x111: case 0x112: case 0x113: case 0x115: case 0x116: case 0x117: case 0x11c: case 0x11d: case 0x11e: case 0x167: allow = &data_2f4b38; break;
        case 0x10b: case 0x10c: case 0x110: case 0x114: case 0x11b: allow = &data_2f4b18; break;
        case 0x145: case 0x146: case 0x147: allow = &data_2f4b58; break;
        case 0x103: allow = &data_2f4b78; break;
        case 0x1e: allow = &data_2f4b98; break;
        default: allow = &data_2f4bb8; break;
        }
    }
    if (*allow != 1)
        return 0;
    void *t = java_jni_is_instance_of_entity_other_player_mp(c->a, e);
    if (!t)
        return data_2f4bd8;
    unsigned fl = entity_flags_get(c->a, t);
    Setting *s = (fl & 5) ? &data_2f4c00 : &data_2f4c20;
    s = (fl & 2) ? &data_2f4be0 : s;
    return s->on;
}

extern "C" void ghost_hand_part_2_2(char *s) {
    s[0x10] = 1;
}
