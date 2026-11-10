// 0x2b000: refill-stack tracking for the held block, accumulated rotation delta
#include "slinky/engine.h"
#include "slinky/jcall.h"

struct Rot { float yaw, pitch; };
FN(void *, java_get_entity_player_2, (void *j, void *p));
FN(int, java_get_inventory_player_current_item, (void *j, void *inv));
FN(void *, java_get_inventory_player_main_inventory, (void *j, void *inv));
FN(void *, java_call_item_stack_get_item, (void *j, void *stack));
FN(int, java_call_static_item_get_id_from_item, (void *j, void *item));
FN(int, java_get_item_stack_stack_size, (void *j, void *stack));      // stack size
FN(bool, block_entity_id, (void *j, void *item));      // is a block item
FN(Rot, rot_add, (Rot a, Rot b));

extern "C" int data_2f7bb8, data_2f7bbc, data_2f7bc4, data_2f7bc8, data_2f7bcc, data_2f7bd0;
extern "C" unsigned char data_2f7bc0;
extern "C" unsigned long long data_2f7be0;
extern "C" Rot data_2f7c18;

// remember the held block (id, count, slot) before placing
extern "C" void tick_click(Ctx *c) {
    if (data_2f7bc4 != 0) return;
    void *inv = java_get_entity_player_2(c->a, c->b);
    int slot = java_get_inventory_player_current_item(c->a, inv);
    void *items = java_get_inventory_player_main_inventory(c->a, inv);
    JNIEnv *env = ((Jvm *)c->a)->env;
    jobject st = (*env)->GetObjectArrayElement(env, (jobjectArray)items, slot);
    if (!st) return;
    void *it = java_call_item_stack_get_item(c->a, st);
    if (!block_entity_id(c->a, it)) return;
    data_2f7bc4 = java_call_static_item_get_id_from_item(c->a, it);
    int n = java_get_item_stack_stack_size(c->a, st);
    data_2f7bc8 = n;
    if ((unsigned)data_2f7bbc > 20 || data_2f7bcc != slot) data_2f7bd0 = n;
    data_2f7bbc = 0;
    data_2f7bcc = slot;
}
// after placing: if the stack ran out, request a switch to the same block id
extern "C" void tick_click_done(Ctx *c) {
    if (data_2f7bc4 == 0) return;
    void *inv = java_get_entity_player_2(c->a, c->b);
    int slot = java_get_inventory_player_current_item(c->a, inv);
    void *items = java_get_inventory_player_main_inventory(c->a, inv);
    JNIEnv *env = ((Jvm *)c->a)->env;
    jobject st = (*env)->GetObjectArrayElement(env, (jobjectArray)items, slot);
    if (st) {
        void *it = java_call_item_stack_get_item(c->a, st);
        if (it && java_call_static_item_get_id_from_item(c->a, it) != 0) {
            if (java_get_item_stack_stack_size(c->a, st) >= data_2f7bc8) goto clear;
            if (--data_2f7bd0 != 0) goto clear;
        }
    }
    data_2f7bb8 = data_2f7bc4;
    data_2f7bc0 = 1;
clear:
    data_2f7bc4 = 0;
}
extern "C" unsigned long long get_refill_state() { return data_2f7be0; }
extern "C" void add_rotation_delta(Rot r) { data_2f7c18 = rot_add(data_2f7c18, r); }
