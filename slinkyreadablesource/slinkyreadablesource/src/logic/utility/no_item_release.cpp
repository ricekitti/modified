// No Item Release - descriptor +0x78.
// Tracks (data_2fa350) whether the held item is one whose use must not be
// released; swords/bow/0x15a handling depends on the "mode" setting.
#include "slinky/engine.h"

extern "C" unsigned data_2ec5e8;          // "mode" current value
extern "C" unsigned char data_2fa350;     // release-lock flag

struct RelState { char pad0[8]; void *w; void *x; char pad1[0xc]; int state; };  // +0x24

FN(void *, java_jni_is_instance_of_c08_packet_player_block_placement, (void *w, void *x));
FN(void *, java_get_c08_packet_player_block_placement, (void *w, void *x));
FN(void *, java_call_item_stack_get_item, (void *w, void *x));
FN(int, java_call_static_item_get_id_from_item, (void *w, void *item));
FN(void *, java_jni_is_instance_of_c07_packet_player_digging, (void *w, void *x));
FN(int, java_c07_packet_player_digging_status, (void *w, void *x));

extern "C" void no_item_release_part(RelState *s) {
    unsigned char lock = 0;
    void *w = java_jni_is_instance_of_c08_packet_player_block_placement(s->w, s->x);
    void *rw = s->w;
    if (w) {
        void *t = java_get_c08_packet_player_block_placement(rw, w);
        if (t) {
            void *item = java_call_item_stack_get_item(rw, t);
            if (item) {
                int id = java_call_static_item_get_id_from_item(rw, item);
                bool compute;
                switch (id) {
                case 267: case 268: case 272: case 276: case 283:  // swords
                    compute = data_2ec5e8 != 1;
                    break;
                case 261:  // bow
                case 346:
                    compute = false;
                    break;
                default:
                    compute = data_2ec5e8 != 0;
                    break;
                }
                if (compute) {
                    int id2 = java_call_static_item_get_id_from_item(rw, item);
                    lock = (id2 != 0x15a) & (id2 != 0x105);
                }
            }
        }
        data_2fa350 = lock;
        return;
    }
    w = java_jni_is_instance_of_c07_packet_player_digging(rw, s->x);
    if (!w || data_2fa350 == 0)
        return;
    if (java_c07_packet_player_digging_status(s->w, w) == 5)
        s->state = 2;
    data_2fa350 = 0;
}
