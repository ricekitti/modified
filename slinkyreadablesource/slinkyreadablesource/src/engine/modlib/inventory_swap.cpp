// 0x47000: pick the best inventory slot for an item category and plan the swap
#include "slinky/engine.h"
#include "slinky/jids.h"
FN(void *, java_get_entity_player_2, (void *a, void *b));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *p, int slot));
FN(void *, java_get_inventory_player_main_inventory, (void *a, void *x));
FN(unsigned, swap_delay, (Ctx *c, void *stack, unsigned kind));
struct Plan { char pad[0x10]; int mode; unsigned char from; unsigned char z15, z16, z17; char pad2[4]; unsigned char kind, to; };
extern "C" bool plan_inventory_swap(Ctx *c, Plan *pl) {
    unsigned char kind = pl->kind;
    void *p = java_get_entity_player_2(c->a, c->b);
    void *a = c->a;

    void *cur = java_call_iinventory_get_stack_in_slot(a, p, 0x27 - kind);
    void *inv = java_get_inventory_player_main_inventory(c->a, p);
    JNIEnv *env = ((Jvm *)c->a)->env;
    unsigned best = swap_delay(c, cur, kind);
    int idx = -1;
    for (int i = 0; i != 0x24; i++) {
        unsigned v = swap_delay(c, (*env)->GetObjectArrayElement(env, (jobjectArray)inv, i), kind);
        if (v > best) { idx = i; best = v; }
    }
    if (idx != -1) {
        if (!cur) {
            unsigned char to = (unsigned char)(idx + 0x24);
            if (idx >= 9) to = (unsigned char)idx;
            *(long long *)&pl->mode = 0;
            pl->to = to;
        } else if (idx <= 8) {
            pl->mode = 1;
            pl->from = (unsigned char)idx;
            pl->z15 = 0; pl->z16 = 0; pl->z17 = 0;
            pl->to = pl->kind + 5;
        } else {
            unsigned char k = pl->kind + 5;
            pl->mode = 2;
            pl->from = k;
            pl->z15 = 0; pl->z16 = 0; pl->z17 = 0;
            pl->to = (unsigned char)idx;
        }
    }
    return idx != -1;
}

// armour value of a stack for slot `kind` (armour points + protection enchantments)
FN(void *, java_call_item_stack_get_item, (void *a, void *x));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *x));
FN(void *, java_call_item_stack_get_enchantment_tag_list, (void *a, void *stack));
FN(int, java_call_nbttag_list_tag_count, (void *a, void *list));
FN(void *, java_call_nbttag_list_get_compound_tag_at, (void *a, void *list, int i));
FN(short, java_jni_call_short_method_nbttag_compound_get_short, (void *a, void *tag, void *key));
extern "C" const int rdata_155e70[];
extern "C" const long long rdata_155ec0[];
extern "C" const unsigned char rdata_155b80[];
extern "C" const char rdata_155b94[], rdata_155b97[];
extern "C" const double rdata_1555c0, rdata_1555c8, rdata_1555d0, rdata_1555e0, rdata_1555e8;
extern "C" const float rdata_1555d8, rdata_1555f0;
extern "C" unsigned swap_delay(Ctx *c, void *stack, unsigned kind) {
    if (!stack) return 0;
    void *item = java_call_item_stack_get_item(c->a, stack);
    if (!item) return 0;
    int id = java_call_static_item_get_id_from_item(c->a, item);
    unsigned k = id - 0x12a;
    if (k > 0x13) return 0;
    if ((unsigned)(id + rdata_155e70[k]) != kind) return 0;
    long long mat = rdata_155ec0[k];
    void *nbt = java_call_item_stack_get_enchantment_tag_list(c->a, stack);
    unsigned char points = rdata_155b80[mat * 4 + kind];
    double prot = 0.0;
    if (nbt) {
        JNIEnv *env = ((Jvm *)c->a)->env;
        jstring idS = (*env)->NewStringUTF(env, rdata_155b94);
        if (idS) {
            jstring lvlS = (*env)->NewStringUTF(env, rdata_155b97);
            if (lvlS) {
                int n = java_call_nbttag_list_tag_count(c->a, nbt);
                if (n > 0) {
                    int i = 0;
                    float extra = 0.0f;
                    double k0 = rdata_1555c0;

                    double k1 = rdata_1555c8;

                    double k2 = rdata_1555d0;

                    float k3 = rdata_1555d8;

                    float acc = 0.0f;
                    for (; i != n; i++) {
                        void *tag = java_call_nbttag_list_get_compound_tag_at(c->a, nbt, i);
                        unsigned short eid = (unsigned short)java_jni_call_short_method_nbttag_compound_get_short(c->a, tag, idS);
                        short lvl = java_jni_call_short_method_nbttag_compound_get_short(c->a, tag, lvlS);
                        if (lvl < 0) continue;
                        int L = lvl;
                        switch (eid) {
                        case 1: case 2: case 3: case 4:
                            acc = (float)((double)L * k0 + (double)acc);
                            break;
                        case 0: {
                            unsigned v = (unsigned)(long long)((double)(L * L + 6) * k1 / k2);
                            if (v >= 0x19) v = 0x19;
                            v = ((v + 1) & 0x3e) + (v >> 1);
                            extra = (float)(int)v * k3;
                            break;
                        }
                        }
                    }
                    prot = (double)(acc + extra);
                }
            }
        }
    }
    double c1 = rdata_1555e0, c2 = rdata_1555e8;
    double t = c2 - (double)points * c1;
    double r = c2 - (c2 - prot * c1) * t;
    return (unsigned)(long long)((float)r * rdata_1555f0);
}
