// 0x33000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
extern "C" int data_2ec5e8; extern "C" unsigned char data_2fa350; extern "C" float data_2ecee8;
struct Ev { char pad[8]; void *a; void *pkt; char pad2[0xc]; int f24; };
FN(void *, java_jni_is_instance_of_c08_packet_player_block_placement, (void *w, void *x));
FN(void *, java_get_c08_packet_player_block_placement, (void *w, void *x));
FN(void *, java_call_item_stack_get_item, (void *a, void *x));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *x));
FN(void *, java_jni_is_instance_of_c07_packet_player_digging, (void *w, void *x));
FN(int, java_c07_packet_player_digging_status, (void *w, void *x));
extern "C" void packet_entity_lookup(Ev *ev) {
    void *a = ev->a;
    unsigned char v;
    void *x = java_jni_is_instance_of_c08_packet_player_block_placement(a, ev->pkt);
    if (x) {
        void *y = java_get_c08_packet_player_block_placement(a, x);
        if (!y) { v = 0; goto store; }
        void *s = java_call_item_stack_get_item(a, y);
        if (!s) { v = 0; goto store; }
        int id = java_call_static_item_get_id_from_item(a, s);
        unsigned e = id - 0x105;
        if (e <= 0x16) {
            if ((0x4088c0u >> e) & 1) {
                if (data_2ec5e8 == 1) { v = 0; goto store; }
            } else if (e == 0) { v = 0; goto store; }
            else goto other;
        } else {
other:
            if (id == 0x15a) { v = 0; goto store; }
            if (data_2ec5e8 == 0) { v = 0; goto store; }
        }
        int id2 = java_call_static_item_get_id_from_item(a, s);
        v = (id2 != 0x15a) & (id2 != 0x105);
store:
        data_2fa350 = v;
        return;
    }
    void *z = java_jni_is_instance_of_c07_packet_player_digging(a, ev->pkt);
    if (!z || !data_2fa350) return;
    if (java_c07_packet_player_digging_status(ev->a, z) == 5) ev->f24 = 2;
    data_2fa350 = 0;
}
extern "C" void activate_hold(char *p) { p[0x10] = 1; *(double *)(p + 0x20) = data_2ecee8; }
