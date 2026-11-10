// Auto Tool - descriptor +0x1d8 (switch to the best tool for the block being
// mined, optionally switching back afterwards) and +0x1e8 (free the block filter).
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };

extern "C" int data_2ee368;              // slot to switch back to (-1 = none)
extern "C" int data_2ee36c;              // slot we switched to
extern "C" float data_2ee3a0;            // "activation_time"
extern "C" unsigned char data_2ee3c0;    // "allow_using_fists"
extern "C" unsigned char data_2ee3e0;    // "allow_using_sword"
extern "C" unsigned char data_2ee400;    // "allow_using_tools"
extern "C" unsigned char data_2ee420;    // "allow_using_other"
extern "C" unsigned char data_2ee440;    // "require_sneak"
extern "C" unsigned char data_2ee460;    // "restrict_allowed_blocks"
extern "C" unsigned char data_2ee480;    // "switch_back"
extern "C" char data_2ee158[];           // switch cooldown timer
extern "C" int data_2fc520;              // activation countdown
extern "C" void *data_2fc528[2];         // allowed-block set
extern "C" const float rdata_152420;     // minimum dig speed (1.0)

FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(int, java_get_inventory_player_current_item, (void *a, void *p));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(void *, get_net_handler, (void *a, void *x));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(void *, java_get_game_settings_key_bindings_8, (void *a, void *x));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(void *, java_get_game_settings_key_bindings_3, (void *a, void *x));
FN(unsigned char, is_key_down, (void *a, void *k));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(Vec3, java_get_moving_object_position_block_x, (void *a, void *t));
FN(void *, java_call_variant, (void *a, void *world, int x, int y, int z));
FN(int, get_block_id, (void *a, void *blk));
FN(unsigned char, id_vector_contains, (void *set, int id));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *p, int slot));
FN(float, java_call_item_stack_get_str_vs_block, (void *a, void *item, void *blk));
FN(unsigned char, claim_release_hotbar_module, (void *timer));
FN(void, release_owner, (void *timer));
FN(void, java_set_inventory_player_current_item, (void *a, void *p, int slot));
FN(void, libc_free, (void *p));

extern "C" void auto_tool_part(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    void *p = java_get_entity_player_2(c->a, c->b);
    int slot = java_get_inventory_player_current_item(c->a, p);
    int prev = data_2ee368;
    int n;
    void *blk;
    if (prev != -1 && data_2ee36c != slot) {
        data_2ee368 = -1;
        goto restore;
    }
    if (prev == -1) {
        const unsigned char *allow;
        void *held = item_held_hotbar_slot(c->a, c->b);
        if (!held) {
            allow = &data_2ee3c0;
        } else {
            switch (java_call_static_item_get_id_from_item(c->a, held) - 0x100) {
            case 0x0: case 0x1: case 0x2: case 0xd: case 0xe: case 0xf: case 0x11: case 0x12: case 0x13: case 0x15: case 0x16: case 0x17: case 0x1c: case 0x1d: case 0x1e: case 0x67:
                allow = &data_2ee400;
                break;
            case 0xb: case 0xc: case 0x10: case 0x14: case 0x1b:
                allow = &data_2ee3e0;
                break;
            default:
                allow = &data_2ee420;
                break;
            }
        }
        if (*allow != 1)
            goto reset;
    }
    {
        void *obj = get_net_handler(c->a, c->c18);
        if (java_call_entity_player_is_using_item(c->a, c->b))
            goto reset;
        if (!java_get_key_binding_key_code(c->a, java_get_game_settings_key_bindings_8(c->a, obj)))
            goto reset;
        if (data_2ee440 == 1 && !is_key_down(c->a, java_get_game_settings_key_bindings_3(c->a, obj)))
            goto reset;
        void *t = java_get_minecraft_5(c->a, c->c18);
        if (!t || java_get_moving_object_position_3(c->a, t) != 1)
            goto reset;
        Vec3 v = java_get_moving_object_position_block_x(c->a, t);
        blk = java_call_variant(c->a, c->p20, (int)v.x, (int)v.y, (int)v.z);
        if (!blk)
            goto reset;
        if (data_2ee460 == 1 && !id_vector_contains(data_2fc528, get_block_id(c->a, blk)))
            goto reset;
    }
    n = data_2fc520;
    if (n > 0) {
        n -= 1;
        goto store;
    }
    {
        void *a = c->a;
        int best = slot;
        float bestv = rdata_152420;
        #pragma clang loop unroll(disable)
        for (int i = 0; i < 9; i++) {
            int s = (slot + i) % 9;
            void *it = java_call_iinventory_get_stack_in_slot(a, p, s);
            if (!it)
                continue;
            float v = java_call_item_stack_get_str_vs_block(a, it, blk);
            if (v > bestv)
                best = s;
            bestv = v > bestv ? v : bestv;
        }
        if (slot == best)
            return;
        if (claim_release_hotbar_module(data_2ee158)) {
            java_set_inventory_player_current_item(c->a, p, best);
            data_2ee36c = best;
            if (data_2ee368 == -1 && data_2ee480)
                data_2ee368 = slot;
        }
        if (best != -1)
            return;
        goto restore;
    }
reset:
    n = (int)data_2ee3a0;
store:
    data_2fc520 = n;
restore:
    release_owner(data_2ee158);
    if (data_2ee368 != -1) {
        java_set_inventory_player_current_item(c->a, p, data_2ee368);
        data_2ee368 = -1;
    }
}

extern "C" void auto_tool_part_2(Hook *h) {
    libc_free(data_2fc528[0]);
    data_2fc528[0] = data_2fc528[1] = nullptr;
}
