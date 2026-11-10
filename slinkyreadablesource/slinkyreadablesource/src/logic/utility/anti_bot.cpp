// Anti Bot - tag entities that look like server-side bots (tag 2) so other
// modules can ignore them.  Per-player info is kept in a table keyed by UUID.
//   +0x48 tick:  re-evaluate every player
//   +0x98 event: on disable clear the table and untag everyone
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Key { unsigned long long hi, lo, pad0, pad1; };
struct Info {
    unsigned long long hi, lo;
    double moved;            // +0x10 accumulated horizontal movement^2
    unsigned char aged;      // +0x18 passed "min_age"
    unsigned char visible;   // +0x19 seen with armour while invisible / old enough
    unsigned char name_ok;   // +0x1a name has a valid character
};
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
struct JEnv { void **vtbl; };

extern "C" float data_2eaf10;            // "min_age"
extern "C" unsigned char data_2eaf30;    // "always_stationary"
extern "C" int data_2eaf88;              // "invalid_uuid"
extern "C" unsigned char data_2eafb0;    // "sleeping"
extern "C" unsigned char data_2eafd0;    // "always_invis"
extern "C" unsigned char data_2eaff0;    // "invalid_username"
extern "C" char data_2eacd0[];           // Anti Bot module descriptor
extern "C" int data_2f9de8;              // world id the table belongs to
extern "C" void *data_2f9df0[2];         // info table
extern "C" char data_2f9df8[];           // table meta
extern "C" const double rdata_1510c8;    // "stationary" threshold

typedef unsigned char (*EntityPred)(void *self, void *player, void *entity);

FN(int, java_call_object_hash_code, (void *a, void *world));
FN(void, libc_free, (void *p));
FN(void *, java_get_world, (void *a, void *x));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(void *, java_get_entity_2, (void *a, void *e));
FN(unsigned long long, java_jni_call_long_method_uuid_get_most_significant_bits, (void *a, void *uuid));
FN(unsigned long long, java_jni_call_long_method_uuid_get_least_significant_bits, (void *a, void *uuid));
FN(Info *, vector_find_insert, (void *table, int keysz, void *meta, void *key, void *hash));
FN(void, compare_u128, ());
FN(Vec3, java_get_entity_prev_pos_x, (void *a, void *e));
FN(Vec3, java_get_entity_pos_x, (void *a, void *e));
FN(unsigned char, java_get_entity_player_sleeping, (void *a, void *e));
FN(int, java_get_entity_ticks_existed, (void *a, void *e));
FN(unsigned char, java_call_entity_player_is_invisible_to_player, (void *a, void *e, void *viewer));
FN(void *, java_get_entity_player_2, (void *a, void *e));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *inv, int slot));
FN(void *, java_call_item_stack_get_item, (void *a, void *stack));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(void *, java_call_entity_get_command_sender_name, (void *a, void *e));
FN(void, entity_flags_set, (void *a, void *e, int tag));
FN(void, entity_flags_clear, (void *a, void *e, int tag));

static inline bool has_item(void *a, void *inv, int slot) {
    void *it = java_call_iinventory_get_stack_in_slot(a, inv, slot);
    if (!it)
        return false;
    void *item = java_call_item_stack_get_item(a, it);
    return item && java_call_static_item_get_id_from_item(a, item) != 0;
}

extern "C" void anti_bot_part(Hook *h) {
    Ctx *c = h->ctx;
    int wid = c->p20 ? java_call_object_hash_code(c->a, c->p20) : 0;
    if (wid != data_2f9de8) {
        libc_free(data_2f9df0[0]);
        data_2f9df0[0] = data_2f9df0[1] = nullptr;
        data_2f9de8 = wid;
    }
    if (!c->p20)
        return;
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
        k.pad0 = k.pad1 = 0;
        Info *inf = vector_find_insert(data_2f9df0, 0x20, data_2f9df8, &k, (void *)compare_u128);
        Vec3 now = java_get_entity_prev_pos_x(c->a, e);
        Vec3 last = java_get_entity_pos_x(c->a, e);
        double dx = now.x - last.x, dz = now.z - last.z;
        double moved = dx * dx + dz * dz + inf->moved;
        inf->moved = moved;
        if (data_2eaf30 == 1 && rdata_1510c8 > moved)
            goto bot;
        if (data_2eaf88 != 0) {
            unsigned char still = rdata_1510c8 > moved;
            unsigned char v2 = data_2eaf88 == 2;
            if (((unsigned)inf->hi & 0xf000) == 0x2000 && (v2 | still))
                goto bot;
        }
        if (data_2eafb0 == 1 && java_get_entity_player_sleeping(c->a, e))
            goto bot;
        {
            int age = java_get_entity_ticks_existed(c->a, e);
            if (data_2eafd0 == 1 && !inf->visible) {
                void *a = c->a;
                if (java_call_entity_player_is_invisible_to_player(a, e, c->b)) {
                    void *inv = java_get_entity_player_2(a, e);
                    if (!has_item(a, inv, 0x24) && !has_item(a, inv, 0x25) &&
                        !has_item(a, inv, 0x26) && !has_item(a, inv, 0x27))
                        goto bot;
                }
                if (age >= 21)
                    inf->visible = 1;
            }
            float min_age = data_2eaf10;
            if (min_age > 0.0f && !inf->aged) {
                if (min_age > (float)age)
                    goto bot;
                inf->aged = 1;
            }
        }
        if (data_2eaff0 == 1 && !inf->name_ok) {
            JEnv *env = ((JEnv **)c->a)[1];
            void *name = java_call_entity_get_command_sender_name(c->a, e);
            int len = ((int (*)(JEnv *, void *))env->vtbl[0x520 / 8])(env, name);
            unsigned short *ch = ((unsigned short *(*)(JEnv *, void *, void *))env->vtbl[0x700 / 8])(env, name, nullptr);
            for (int j = 0; j < len; j++) {
                unsigned d = ch[j];
                if (d == 0xa7) {   // colour code: skip the code character too
                    j++;
                    continue;
                }
                if ((unsigned short)((d & 0xffdf) - 0x41) < 0x1a || d == 0x5f || (unsigned short)(d - 0x30) <= 9) {
                    inf->name_ok = 1;
                    break;
                }
            }
            ((void (*)(JEnv *, void *, void *))env->vtbl[0x708 / 8])(env, name, ch);
            if (!inf->name_ok)
                goto bot;
        }
        entity_flags_clear(c->a, e, 2);
        continue;
bot:
        entity_flags_set(c->a, e, 2);
    }
}

extern "C" void anti_bot_part_2(EvHook *h) {
    if (__builtin_expect(h->ev != data_2eacd0 || h->on, 0))
        return;
    void *tbl = data_2f9df0[0];

    libc_free(tbl);
    data_2f9df0[0] = data_2f9df0[1] = nullptr;
    Ctx *c = h->ctx;
    if (!c->p20)
        return;
    void *list = java_get_world(c->a, c->p20);
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        void *obj = ((void **)c->a)[1];
        EntityPred pred = (EntityPred)(*(void ***)obj)[0xc0 / 8];
        if (!pred(obj, c->b, e))
            entity_flags_clear(c->a, e, 2);
    }
}
