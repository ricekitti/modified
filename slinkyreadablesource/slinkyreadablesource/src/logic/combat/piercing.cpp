// Piercing - decide which of two entities an attack should hit.
//   piercing_piercing_decide_which_3(ctx, e1, e2, inside_bb): 0 = e1 preferred, 1 = e2 preferred, 2 = no preference
//   piercing_piercing_decide_which_2_2(ctx): true when "blocks" piercing is active
//   piercing_piercing_decide_which_3_2(hook): arm flag
#include "slinky/engine.h"

extern "C" unsigned char data_2f3ac4;   // module enabled
extern "C" unsigned char data_2f3cd8;   // "require_inside_bb"
extern "C" unsigned char data_2f3cf8;   // "require_weapon"
extern "C" unsigned char data_2f3d18;   // "non_players"
extern "C" bool data_2f3d38;   // "friendlies"
extern "C" bool data_2f3d58;   // "bots"
extern "C" unsigned char data_2f3d78;   // "enemies"
extern "C" int data_2f3dc0;             // "sort_by"
extern "C" unsigned char data_2f3de8;   // "blocks"

FN(unsigned char, click_with_helper, (void *a, void *b));
FN(void *, java_jni_is_instance_of_entity_other_player_mp, (void *a, void *e));
FN(unsigned, entity_flags_get, (void *a, void *t));
FN(float, java_call_entity_living_base_get_health, (void *a, void *t));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *t));
FN(int, java_get_entity_ticks_existed, (void *a, void *t));
FN(int, java_get_entity_entity_id, (void *a, void *t));

static inline unsigned mix(unsigned x) {
    x = (x ^ (x >> 16)) * 0x45d9f3b;
    x = (x ^ (x >> 16)) * 0x45d9f3b;
    return x ^ (x >> 16);
}

extern "C" int piercing_piercing_decide_which_3(Ctx *c, void *e1, void *e2, unsigned char inside_bb) {
    int ret = 2;
    void *t1, *t2;
    unsigned fl;
    if (data_2f3ac4 != 1) goto done;
    if (!inside_bb && data_2f3cd8) goto done;
    if (data_2f3cf8 == 1 && !click_with_helper(c->a, c->b)) goto done;
    t2 = java_jni_is_instance_of_entity_other_player_mp(c->a, e2);
    if (!t2) {
        ret = 2 - data_2f3d18;
        goto done;
    }
    fl = entity_flags_get(c->a, t2);
    if ((data_2f3d38 & ((fl & 5) != 0)) || (data_2f3d58 & ((fl & 2) >> 1))) {
        ret = 1;
        goto done;
    }
    t1 = java_jni_is_instance_of_entity_other_player_mp(c->a, e1);
    if (!t1) {
        ret = 0;
        goto done;
    }
    if (data_2f3d78 != 1) goto done;
    if (data_2f3dc0 == 1) {
        float s1 = java_call_entity_living_base_get_health(c->a, t1);
        float s2 = java_call_entity_living_base_get_health(c->a, t2);
        if (s2 > s1) { ret = 1; goto done; }
        if (s1 > s2) { ret = 0; goto done; }
    } else if (data_2f3dc0 == 0) {
        int s1 = java_get_entity_living_base_hurt_time(c->a, t1);
        int s2 = java_get_entity_living_base_hurt_time(c->a, t2);
        if (s2 > s1) { ret = 1; goto done; }
        if (s2 < s1) { ret = 0; goto done; }
    }
    {
        unsigned h1 = mix(java_get_entity_ticks_existed(c->a, t1) + java_get_entity_entity_id(c->a, t1));
        unsigned h2 = mix(java_get_entity_ticks_existed(c->a, t2) + java_get_entity_entity_id(c->a, t2));
        ret = (int)h2 <= (int)h1;
    }
done:
    return ret;
}

extern "C" unsigned char piercing_piercing_decide_which_2_2(Ctx *c) {
    unsigned char r = 0;
    if (data_2f3ac4 != 1 || !data_2f3de8)
        goto end;
    if (data_2f3cf8 == 1 && __builtin_expect(!click_with_helper(c->a, c->b), 1)) {
        r = 0;
        goto end;
    }
    r = 1;
end:

    return r;
}

extern "C" void piercing_piercing_decide_which_3_2(unsigned char *h) {
    h[0x10] = 1;
}
