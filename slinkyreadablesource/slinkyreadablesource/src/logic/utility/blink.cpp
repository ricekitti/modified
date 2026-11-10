// Blink - descriptors +0x48 / +0x68 / +0x78 / +0x98 / +0x128.
// +0x48:  per tick: auto-disable when "max_duration" runs out, or on attack
//         (own hurt-time rising) if "disable_on_attack".
// +0x68:  packet hook: decide whether the packet is held (3) or sent (1).
// +0x78:  force the packet verdict to 1 while blinking.
// +0x98:  on enable: load the duration, reset the counters, publish "direction".
// +0x128: disable when the player digs / interacts with a block.
#include "slinky/engine.h"

extern "C" char data_2ec600[];                 // Blink module descriptor
extern "C" unsigned char data_2ec828;          // "disable_on_attack"
extern "C" float data_2ec860;                  // "max_duration"
extern "C" unsigned char data_2ec8b8;          // "direction"
extern "C" unsigned char data_2ec8e0;          // "disable_on_block_dig"
extern "C" unsigned char data_2ec900;          // "disable_on_block_interact"
extern "C" int data_2fa354;                    // last hurt time
extern "C" int data_2fa358;                    // ticks left
extern "C" volatile unsigned char data_2fa35c;          // blinking / direction state

struct PktHook { char pad0[8]; Ctx *ctx; void *ev; char pad18[12]; int verdict; };   // +0x24
struct PhaseHook { char pad0[8]; Ctx *ctx; unsigned char phase; };                 // +0x10
struct StateHook { char pad0[16]; void *ev; unsigned char on; };                   // +0x10 / +0x18

FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));
FN(void, queue_module_toggle, (void *a, void *mod, int x));
FN(void *, java_jni_is_instance_of_s19_packet_entity_status, (void *a, void *ev));
FN(void *, java_jni_is_instance_of_s06_packet_update_health, (void *a, void *ev));
FN(void *, java_jni_is_instance_of_s29_packet_sound_effect, (void *a, void *ev));
FN(void *, java_jni_is_instance_of_s2_apacket_particles, (void *a, void *ev));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));

extern "C" void blink_part(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    int hurt = java_get_entity_living_base_last_attacker_time(c->a, c->b);
    unsigned char rose = hurt > data_2fa354;
            // keep the compare materialised (setg)
    unsigned char attack = data_2ec828;

    data_2fa354 = hurt;
    if (--data_2fa358 == 0 || (rose & attack))
        queue_module_toggle(c->p08, data_2ec600, 0);
}

extern "C" void blink_part_2(PktHook *h) {
    if (data_2fa35c != 1) {
        int verdict = 3;
        if (!java_jni_is_instance_of_s19_packet_entity_status(h->ctx, h->ev) && !java_jni_is_instance_of_s06_packet_update_health(h->ctx, h->ev) && !java_jni_is_instance_of_s29_packet_sound_effect(h->ctx, h->ev))
            verdict = (java_jni_is_instance_of_s2_apacket_particles(h->ctx, h->ev) != 0) * 2 + 1;
        h->verdict = verdict;
    }
}

extern "C" void blink_part_3(PktHook *h) {
    if (data_2fa35c)
        h->verdict = 1;
}

extern "C" void blink_part_4(StateHook *h) {
    if (h->ev != data_2ec600 || h->on != 1)
        return;
    data_2fa358 = (int)(long long)data_2ec860;
    data_2fa354 = 0x7fffffff;
    __atomic_exchange_n(&data_2fa35c, data_2ec8b8, __ATOMIC_SEQ_CST);
}

extern "C" void blink_part_5(PhaseHook *h) {
    Ctx *c = h->ctx;
    if (h->phase == 1 ? data_2ec8e0 == 1 : data_2ec900 == 1) {
        void *t = java_get_minecraft_5(c->a, c->c18);
        if (t && java_get_moving_object_position_3(c->a, t) == 1) {
            queue_module_toggle(c->p08, data_2ec600, 0);
            return;
        }
    }

}
