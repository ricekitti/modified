// 0x37000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
extern "C" char data_2edef0[]; extern "C" unsigned char data_2fc508; extern "C" void *data_2fc510;
extern "C" int data_2fc518, data_2ee148;
struct Ev { char pad[8]; Ctx *ctx; void *tag; unsigned char on; };
FN(unsigned char, is_silent_rotation_active, ());
FN(void *, get_silent_rotation, ());
FN(void *, get_entity_rotation, (void *a, void *p));
FN(void *, get_net_handler, (void *a, void *b));
FN(int, java_get_game_settings_third_person_view, (void *a, void *x));
FN(void, java_set_game_settings_third_person_view, (void *a, void *x, int v));
FN(void, set_render_rotation, (void *cam));
FN(void *, java_call_item_stack_get_enchantment_tag_list, (void *a, void *stack));
FN(int, java_call_nbttag_list_tag_count, (void *a, void *list));
FN(void *, java_call_nbttag_list_get_compound_tag_at, (void *a, void *list, int i));
FN(short, java_jni_call_short_method_nbttag_compound_get_short, (void *a, void *tag, void *key));
struct Jvm2 { void *ids; struct JE **env; };
struct JE { char pad[0x538]; void *(*NewStringUTF)(void *, const char *); };
extern "C" const char rdata_15292e[], rdata_152931[];
extern "C" void rotation_cache_toggle(Ev *ev) {
    if (ev->tag != data_2edef0) return;
    Ctx *c = ev->ctx;
    if (!c->p20) return;
    void *a;
    if (ev->on == 1) {
        data_2fc508 = 1;
        void *r;
        if (is_silent_rotation_active()) r = get_silent_rotation(); else r = get_entity_rotation(c->a, c->b);
        data_2fc510 = r;
    }
    void *w = get_net_handler(c->a, c->c18);
    if (ev->on == 0) {
        java_set_game_settings_third_person_view(c->a, w, data_2fc518);
        if (data_2fc508 == 1) { set_render_rotation(data_2fc510); data_2fc508 = 0; }
        return;
    }
    data_2fc518 = java_get_game_settings_third_person_view(c->a, w);
    java_set_game_settings_third_person_view(c->a, w, data_2ee148);
}
extern "C" void store_cached_rotation(char *p) { p[8] = 1; *(void **)(p + 0xc) = data_2fc510; }

// enchantment flags of an item stack: 1 = id 16, 2 = id 19, 4 = id 20
extern "C" unsigned enchantment_flags_item_stack(Jvm2 *j, void *stack) {
    void *list = java_call_item_stack_get_enchantment_tag_list(j, stack);
    unsigned flags = 0;
    if (!list) return flags;
    JE **env = j->env;
    void *idS = (*env)->NewStringUTF(env, rdata_15292e);
    if (!idS) return flags;
    if (!(*env)->NewStringUTF(env, rdata_152931)) return flags;
    int n = java_call_nbttag_list_tag_count(j, list);
    if (n <= 0) return flags;
    flags = 0;
    int i = 0;
    do {
        void *tag = java_call_nbttag_list_get_compound_tag_at(j, list, i);
        switch (java_jni_call_short_method_nbttag_compound_get_short(j, tag, idS)) {
        case 0x10: flags |= 1; break;
        case 0x13: flags |= 2; break;
        case 0x14: flags |= 4; break;
        }
    } while (++i != n);
    return flags;
}
