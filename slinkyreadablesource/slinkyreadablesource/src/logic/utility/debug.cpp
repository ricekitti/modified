// Debug - chat/log diagnostics: hurt time, item-use state transitions, click counting.
#include "slinky/engine.h"

S(155136);   // hurt-time log format
S(15517d);   // item-use log format
S(15519d);   // "Start" label
S(1551a4);   // "Stop" label
S(1551b1);   // click log format
extern "C" const int rdata_155200[];   // hurt-time code table

extern "C" unsigned char data_2f4658;   // "hurt_time"
extern "C" unsigned char data_2f4678;   // "item_use_state"
extern "C" unsigned char data_2f4698;   // "clicks"
extern "C" unsigned char data_3049b8;   // last item-use state
extern "C" unsigned char data_3049b9;   // current item-use state
extern "C" int data_3049bc;             // left clicks this window
extern "C" int data_3049c0;             // right clicks this window
extern "C" int data_3049c4;             // ticks in window

struct EvHook { char pad0[8]; void *ctx; void *ev; char pad18[12]; int type; };   // +0x24

FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(void, jni_throw_format, (void *a, const char *fmt, ...));
FN(void *, java_jni_is_instance_of_c08_packet_player_block_placement, (void *ctx, void *ev));
FN(void *, java_jni_is_instance_of_c07_packet_player_digging, (void *ctx, void *ev));
FN(int, java_c07_packet_player_digging_status, (void *ctx, void *x));

extern "C" void debug_part(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 == 1) {
        if (data_2f4658 == 1) {
            int ht = java_get_entity_living_base_hurt_time(c->a, c->b);
            if (ht >= 7) {
                void *a = c->a;
                unsigned key = java_get_entity_ticks_existed(a, c->b);
                unsigned ms = key - key / 1000 * 1000;
                int code = 0x66;
                unsigned idx = ht - 7;

                if (idx <= 3)
                    code = rdata_155200[idx];
                int sprint = 0x63;
                if (java_call_entity_is_sprinting(c->a, c->b)) sprint = 0x38;
                int item = 0x38;
                if (java_call_entity_player_is_using_item(c->a, c->b)) item = 0x63;
                jni_throw_format(a, rdata_155136, ms, code, ht, sprint, item);
            }
        }
        if (data_2f4678 == 1) {
            unsigned cur = data_3049b9;
            unsigned char st = cur & 1;
            if (st != data_3049b8) {
                data_3049b8 = st;
                void *a = c->a;
                unsigned key = java_get_entity_ticks_existed(a, c->b);
                unsigned ms = key - key / 1000 * 1000;
                jni_throw_format(a, rdata_15517d, ms, (cur & 1) ? rdata_15519d : rdata_1551a4);
            }
        }
    }

}

extern "C" void debug_part_2(EvHook *h) {
    int t = h->type;
    unsigned char v;
    if (t == 3 || t == 0) {
        v = 1;
        if (!java_jni_is_instance_of_c08_packet_player_block_placement(h->ctx, h->ev)) {
            void *x = java_jni_is_instance_of_c07_packet_player_digging(h->ctx, h->ev);
            if (!x)
                return;
            if (java_c07_packet_player_digging_status(h->ctx, x) != 5)
                return;
            v = 0;
        }
        __atomic_exchange_n(&data_3049b9, v, __ATOMIC_SEQ_CST);
    }
}

extern "C" void debug_part_3(unsigned char *h) {
    if (data_2f4698 == 1) {
        int *p = &data_3049bc;
        int *q = &data_3049c0;
        if (!h[0x10])
            p = q;
        *p += 1;
        data_3049c4 = 0;
    }
}

extern "C" void debug_part_4(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 == 1) {
        unsigned n = data_3049c4;
        if (data_2f4698 == 1) {
            if (n <= 9) {
                int l = data_3049bc;
                int s = 0x32;
                if (l == 1) s = 0x61;
                if (l == 0) s = 0x38;
                int r = data_3049c0;
                int t = 0x33;
                if (r == 1) t = 0x62;
                if (r == 0) t = 0x38;
                void *a = c->a;
                unsigned key = java_get_entity_ticks_existed(a, c->b);
                unsigned ms = key - key / 1000 * 1000;
                int lc = data_3049bc, rc = data_3049c0;
                jni_throw_format(a, rdata_1551b1, ms, s, lc, t, rc);
                n = data_3049c4;
            }
            data_3049bc = 0;
            data_3049c0 = 0;
        }
        data_3049c4 = n + 1;
    }
}
