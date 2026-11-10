// Fake Lag - descriptors +0x48 (tick) / +0x78 (packet hook).
// +0x48: manage the lag windows: per-direction counters ("inbound"/"outbound"
//        ms budgets), and decide whether lagging is currently allowed.
// +0x78: spend the budget while a packet is held; release (verdict 1) when it
//        runs out, or let it straight through (verdict 3) for protected packets.
#include "slinky/engine.h"
#define LIKELY(c) __builtin_expect(!!(c), 1)
#define H4(c) __builtin_expect(!!(c), 0)

extern "C" float data_2ebf00;            // "inbound"
extern "C" float data_2ebf38;            // "outbound"
extern "C" bool data_2ebf58;             // "realtime_damage"
extern "C" unsigned data_2ebfa0;         // "mode"
extern "C" unsigned char data_2ebfc8;    // "require_ingame"
extern "C" unsigned char data_2ebfe8;    // "require_weapon"
extern "C" unsigned char data_2ec008;    // "require_attack"
extern "C" int data_2fa304;              // last hurt time
extern "C" int data_2fa308;              // cooldown
extern "C" int data_2fa30c;              // inbound counter
extern "C" int data_2fa310;              // outbound counter
extern "C" unsigned char data_2fa314;
extern "C" unsigned char data_2fa315;             // inbound window open
extern "C" unsigned char data_2fa316;             // outbound window open
extern "C" volatile unsigned short data_2fa318;   // inbound budget
extern "C" volatile unsigned short data_2fa31a;   // outbound budget

struct PktHook { int type; char pad4[4]; Ctx *ctx; void *ev; long long budget; char pad20[4]; int verdict; };  // +0x00/+0x08/+0x10/+0x18/+0x24

FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(void *, java_jni_is_instance_of_s19_packet_entity_status, (void *a, void *ev));
FN(void *, java_jni_is_instance_of_s06_packet_update_health, (void *a, void *ev));
FN(void *, java_jni_is_instance_of_s29_packet_sound_effect, (void *a, void *ev));

extern "C" void fake_lag_part(Hook *h) {
    if (data_2fa308 > 0)
        data_2fa308--;
    __atomic_exchange_n(&data_2fa314, data_2ebf58, __ATOMIC_SEQ_CST);
    if (data_2ebfa0 == 1) {
        int n = data_2fa30c;
        bool open;
        if (n > 0) {
            data_2fa30c = n - 50;

            open = (unsigned)n < 51;
        } else {
            data_2fa30c = (int)((float)n + data_2ebf00);

            open = false;
        }

        __atomic_exchange_n(&data_2fa315, open, __ATOMIC_SEQ_CST);
        n = data_2fa310;
        if (n > 0) {
            data_2fa310 = n - 50;

            open = (unsigned)n < 51;
        } else {
            data_2fa310 = (int)((float)n + data_2ebf38);

            open = false;
        }

        __atomic_exchange_n(&data_2fa316, open, __ATOMIC_SEQ_CST);
    }
    Ctx *c = h->ctx;
    unsigned char ok;
    int hurt;
    if (!c->p20) goto off;
    if (data_2ebfc8 == 1 && !c->f40) goto off;
    hurt = java_get_entity_living_base_last_attacker_time(c->a, c->b);
    if (data_2ebfe8 == 1 && !click_with_helper(c->a, c->b)) {
        data_2fa308 = 0;
        ok = false;
    } else {
        bool busy = data_2fa308 != 0;
        unsigned char ra = data_2ec008;
        ok = (ra ^ 1) | busy;

    }
    if (hurt != data_2fa304)
        data_2fa308 = 20;
    data_2fa304 = hurt;
    if (ok) {
        __atomic_exchange_n(&data_2fa318, (unsigned short)(int)data_2ebf00, __ATOMIC_SEQ_CST);
        __atomic_exchange_n(&data_2fa31a, (unsigned short)(int)data_2ebf38, __ATOMIC_SEQ_CST);
        return;
    }
    goto clear;
off:
    data_2fa308 = 0;
clear:
    __atomic_exchange_n(&data_2fa31a, (unsigned short)0, __ATOMIC_SEQ_CST);
    __atomic_exchange_n(&data_2fa318, (unsigned short)0, __ATOMIC_SEQ_CST);
}

extern "C" void fake_lag_part_2(PktHook *h) {
    unsigned short v;
    if (h->type == 2) {
        if (LIKELY(__atomic_load_n(&data_2fa314, __ATOMIC_SEQ_CST) & 1)) {
            if (java_jni_is_instance_of_s19_packet_entity_status(h->ctx, h->ev) || java_jni_is_instance_of_s06_packet_update_health(h->ctx, h->ev) || java_jni_is_instance_of_s29_packet_sound_effect(h->ctx, h->ev)) {
                h->verdict = 3;
                return;
            }
        }
        if (__atomic_load_n(&data_2fa315, __ATOMIC_SEQ_CST) & 1)
            return;
        v = data_2fa318;

    } else {
        if (__atomic_load_n(&data_2fa316, __ATOMIC_SEQ_CST) & 1)
            return;
        v = data_2fa31a;

    }
    h->budget -= v;
    if (h->budget < 0) {
        h->budget = 0;
        h->verdict = 1;
    }
}
