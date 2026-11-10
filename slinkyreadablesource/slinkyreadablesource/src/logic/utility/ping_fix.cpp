// Ping Fix - descriptor +0x68 / +0x78: when the lookup succeeds, force the
// packet state field to 3.
#include "slinky/engine.h"

struct PingState {
    char pad0[8];
    void *p08;
    void *p10;
    char pad1[0xc];
    int state;  // +0x24
};

FN(void *, java_jni_is_instance_of_s00_packet_keep_alive, (void *a, void *b));
FN(void *, java_jni_is_instance_of_c00_packet_keep_alive, (void *a, void *b));

extern "C" void ping_fix_part(PingState *s) {
    if (java_jni_is_instance_of_s00_packet_keep_alive(s->p08, s->p10))
        s->state = 3;
}

extern "C" void ping_fix_part_2(PingState *s) {
    if (java_jni_is_instance_of_c00_packet_keep_alive(s->p08, s->p10))
        s->state = 3;
}
