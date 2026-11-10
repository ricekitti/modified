// Teams - descriptors +0x48 (per tick) / +0x98 (event) / +0x1e8 (shutdown).
// +0x48: work out the local player's team colour code, then sweep the entity
//        list and tag every entity whose team colour matches / is allowed.
// +0x98: on disable, release every tagged entity.
// +0x1e8: free the colour table.
#include "slinky/engine.h"

extern "C" unsigned char data_2eb628;   // "match_self_color"
extern "C" void *data_2f9e20;           // colour table
extern "C" char data_2f9e28[];

typedef unsigned char (*EntityPred)(void *self, void *player, void *entity);
struct Vtbl { void *slot[0x18]; };      // slot[0x18] (+0xc0): is-team-mate predicate

FN(void *, java_call_minecraft_get_net_handler, (void *a, void *x));
FN(void *, java_get_net_handler_play_client, (void *a, void *x));
FN(void *, java_get_entity_2, (void *a, void *x));
FN(void *, java_call_map_get, (void *a, void *x, void *y));
FN(void *, java_get_network_player_info, (void *a, void *x));
FN(void *, java_call_ichat_component_get_formatted_text, (void *a, void *x));
FN(unsigned short, name_prefix_code, (void *a, void *x));
FN(void *, java_call_network_player_info_get_player_team, (void *a, void *x));
FN(unsigned short, name_last_code, (void *a, void *x));
FN(unsigned short, entity_name_colour, (void *a, void *x));
FN(void *, java_get_world, (void *a, void *x));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(void, entity_flags_set, (void *a, void *e, int on));
FN(void, entity_flags_clear, (void *a, void *e, int on));
FN(void *, binary_search, (void *table, int n, void *key, void *val, void *cb));
FN(void, compare_signed_chars, ());
FN(void, libc_free, (void *p));

extern "C" void teams_part(Hook *h) {
    Ctx *c = h->ctx;
    if (c->b == nullptr)
        return;
    unsigned short team;
    if (**(int **)c->a) {
        void *t = java_call_minecraft_get_net_handler(c->a, c->c18);
        void *tt = java_get_net_handler_play_client(c->a, t);
        void *pid = java_get_entity_2(c->a, c->b);
        void *r = java_call_map_get(c->a, tt, pid);
        if (!r) {
            team = entity_name_colour(c->a, c->b);
        } else {
            void *q = java_get_network_player_info(c->a, r);
            if (q) {
                unsigned short v = name_prefix_code(c->a, java_call_ichat_component_get_formatted_text(c->a, q));
                team = v ? v : 0x72;
            } else {
                void *w = java_call_network_player_info_get_player_team(c->a, r);
                team = w ? name_last_code(c->a, w) : entity_name_colour(c->a, c->b);
            }
        }
    } else {
        team = entity_name_colour(c->a, c->b);
    }
    void *list = java_get_world(c->a, c->p20);
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        void *obj = ((void **)c->a)[1];
        EntityPred pred = (EntityPred)(*(void ***)obj)[0xc0 / 8];
        if (pred(obj, c->b, e))
            continue;
        unsigned short col = entity_name_colour(c->a, e);
        if (data_2eb628 == 1 && col == team) {
            entity_flags_set(c->a, e, 1);
            continue;
        }
        signed char b = (signed char)col;
        char key = (char)((b >= 0x61 ? 0xa9 : 0xd0) + b);
        if (binary_search(data_2f9e20, 1, data_2f9e28, &key, (void *)compare_signed_chars))
            entity_flags_set(c->a, e, 1);
        else
            entity_flags_clear(c->a, e, 1);
    }
}

struct EventHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };   // +0x10 / +0x18
extern "C" char data_2eb400[];

extern "C" void teams_part_2(EventHook *h) {
    if (__builtin_expect(h->ev != data_2eb400 || h->on, 0))
        return;
    Ctx *c = h->ctx;
    if (!c->p20)
        return;
    void *list = java_get_world(c->a, c->p20);
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        void *obj = ((void **)c->a)[1];
        EntityPred pred = (EntityPred)(*(void ***)obj)[0xc0 / 8];
        if (!pred(obj, c->b, e))
            entity_flags_clear(c->a, e, 1);
    }
}

extern "C" void teams_part_3(Hook *h) {
    libc_free(data_2f9e20);
    *(__int128 *)&data_2f9e20 = 0;
}
