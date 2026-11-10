// Auto Weapon - descriptor +0x1d8: switch to the best weapon slot against the
// current target once "activation_time" ticks have passed.
#include "slinky/engine.h"
#define UNLIKELY(c) __builtin_expect(!!(c), 0)

extern "C" float data_2f5d38;            // "activation_time"
extern "C" unsigned char data_2f5d58;    // "require_mouse_pressed"
extern "C" int data_304ae8;              // ticks since target acquired
extern "C" char data_2f5af8[];           // switch cooldown timer

struct WState { char pad0[8]; Ctx *ctx; };

FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *b));
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(void *, java_get_moving_object_position_2, (void *a, void *x));
FN(void *, java_jni_is_instance_of_entity_player, (void *a, void *x));
FN(unsigned char, entity_flags_has, (void *a, void *x, int mode));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(void *, java_get_entity_player_2, (void *a, void *b));
FN(int, java_get_inventory_player_current_item, (void *a, void *x));
FN(int, weapons_part_3, (Ctx *c, void *player));
FN(unsigned char, claim_release_hotbar_module, (void *timer));
FN(void, java_set_inventory_player_current_item, (void *a, void *player, int slot));
FN(void, release_owner, (void *timer));

extern "C" void auto_weapon_part(WState *s) {
    Ctx *c = s->ctx;
    void *t, *y, *p;
    if (UNLIKELY(c->f40 != 1)) goto reset;
    if (UNLIKELY(java_call_entity_player_is_using_item(c->a, c->b))) goto reset;
    if (UNLIKELY(data_2f5d58 == 1 && !vertex_pointer_current(c))) goto reset;
    t = java_get_minecraft_5(c->a, c->c18);
    if (UNLIKELY(!t)) goto reset;
    if (UNLIKELY(java_get_moving_object_position_3(c->a, t) != 2)) goto reset;
    y = java_jni_is_instance_of_entity_player(c->a, java_get_moving_object_position_2(c->a, t));
    if (UNLIKELY(!y)) goto reset;
    if (UNLIKELY(entity_flags_has(c->a, y, 7))) goto reset;
    if (click_with_helper(c->a, c->b)) goto reset;
    {
        p = java_get_entity_player_2(c->a, c->b);
        int cur = java_get_inventory_player_current_item(c->a, p);
        int best = weapons_part_3(c, p);
        if (best == -1 || cur == best) goto reset;
        int cnt = data_304ae8;
        if (data_2f5d38 > (float)cnt) {
            data_304ae8 = cnt + 1;
            return;
        }
        if (!claim_release_hotbar_module(data_2f5af8)) return;
        java_set_inventory_player_current_item(c->a, p, best);
        release_owner(data_2f5af8);
        return;
    }
reset:
    data_304ae8 = 0;
}
