// Friends - friend list kept in a hash table keyed by player UUID.
//   friends_friends_friend_list_2(a, ui): middle-click on a player toggles it in the list (+ notification)
//   friends_friends_friend_list_2_2(hook):  every tick tag friends in the entity list (and refresh last-seen)
//   friends_friends_friend_list_3(hook):  on disable untag everyone
//   friends_friends_friend_list_4(hook):  free the table
#include "slinky/engine.h"

struct Key { unsigned long long hi, lo, seen, pad; };          // 0x20 bytes
struct Entry { char pad0[0x10]; long long seen; };
struct Notice { int kind; int pad4; unsigned char added; char rest[0x3f]; };   // 0x48 bytes
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };

extern "C" unsigned char data_2f0260;   // "middle_click_add"
extern "C" char data_2f0038[];          // Friends module descriptor
extern "C" void *data_2fc8c0;           // friend table
extern "C" char data_2fc8c8[];          // table meta

typedef unsigned char (*EntityPred)(void *self, void *player, void *entity);

FN(void *, get_minecraft, (void *a));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(void *, java_get_moving_object_position_2, (void *a, void *x));
FN(void *, java_jni_is_instance_of_entity_other_player_mp, (void *a, void *e));
FN(void *, java_get_entity_2, (void *a, void *e));
FN(unsigned long long, java_jni_call_long_method_uuid_get_most_significant_bits, (void *a, void *uuid));
FN(unsigned long long, java_jni_call_long_method_uuid_get_least_significant_bits, (void *a, void *uuid));
FN(long long, libc_time64, (void *t));
FN(Entry *, binary_search, (void *table, int keysz, void *meta, void *key, void *hash));
FN(void, sorted_insert, (void *table, int keysz, void *meta, void *key, void *hash));
FN(void, vector_erase_value, (void *table, int keysz, void *meta, void *entry));
FN(unsigned char, entity_flags_has, (void *a, void *x, int mode));
FN(void, dispatch_event, (void *ui, Notice *n));
FN(void *, java_get_world, (void *a, void *x));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(void, entity_flags_set, (void *a, void *e, int tag));
FN(void, entity_flags_clear, (void *a, void *e, int tag));
FN(void, compare_friend_key, ());
FN(void, libc_free, (void *p));

extern "C" void friends_friends_friend_list_2(void *a, void *ui) {
    if (data_2f0260 == 1) {
        void *t = java_get_minecraft_5(a, get_minecraft(a));
        if (t) {
            void *e = java_get_moving_object_position_2(a, t);
            if (e) {
                void *ent = java_jni_is_instance_of_entity_other_player_mp(a, e);
                if (ent) {
                    void *uuid = java_get_entity_2(a, ent);
                    Key k;
                    k.hi = java_jni_call_long_method_uuid_get_most_significant_bits(a, uuid);
                    k.lo = java_jni_call_long_method_uuid_get_least_significant_bits(a, uuid);
                    k.seen = libc_time64(nullptr);
                    k.pad = 0;
                    Notice n = {};
                    Entry *f = binary_search(data_2fc8c0, 0x20, data_2fc8c8, &k, (void *)compare_friend_key);
                    if (f) {
                        n.kind = 0x13;
                        n.added = 0;
                        vector_erase_value(data_2fc8c0, 0x20, data_2fc8c8, f);
                    } else {
                        if (__builtin_expect(entity_flags_has(a, ent, 2), 0))
                            goto out;
                        n.kind = 0x13;
                        n.added = 1;
                        sorted_insert(&data_2fc8c0, 0x20, data_2fc8c8, &k, (void *)compare_friend_key);
                    }
                    dispatch_event(ui, &n);
                }
            }
        }
    }
out:;
}

extern "C" void friends_friends_friend_list_2_2(Hook *h) {
    Ctx *c = h->ctx;
    if (!c->b)
        return;
    long long now = libc_time64(nullptr);
    void *list = java_get_world(c->a, c->p20);
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        void *obj = ((void **)c->a)[1];
        EntityPred pred = (EntityPred)(*(void ***)obj)[0xc0 / 8];
        if (pred(obj, c->b, e))
            continue;
        void *uuid = java_get_entity_2(c->a, e);
        alignas(16) Key k;
        k.hi = java_jni_call_long_method_uuid_get_most_significant_bits(c->a, uuid);
        k.lo = java_jni_call_long_method_uuid_get_least_significant_bits(c->a, uuid);
        k.seen = 0;
        k.pad = 0;
        Entry *f = binary_search(data_2fc8c0, 0x20, data_2fc8c8, &k, (void *)compare_friend_key);
        if (f) {
            entity_flags_set(c->a, e, 4);
            f->seen = now;
        } else {
            entity_flags_clear(c->a, e, 4);
        }
    }
}

extern "C" void friends_friends_friend_list_3(EvHook *h) {
    if (__builtin_expect(h->ev != data_2f0038 || h->on, 0))
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
            entity_flags_clear(c->a, e, 4);
    }
}

extern "C" void friends_friends_friend_list_4(Hook *h) {
    libc_free(data_2fc8c0);
    *(__int128 *)&data_2fc8c0 = 0;
}
