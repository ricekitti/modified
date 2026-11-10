// Criticals - descriptors +0x48 (decide), +0x58 (timer), +0x78 (packet hook), +0x98 (disable).
//   mode 0 ("packet"): hold outgoing packets for up to "max_delay" ms around a hit
//   mode 1 ("timer"):  slow the game timer while falling onto a target so the hit crits
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct PktHook { char pad0[8]; void *ctx; void *ev; long long budget; int seq; int verdict; };
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
struct TimerHook { char pad0[8]; Ctx *ctx; char pad10[8]; double dt; };          // +0x18

extern "C" float data_2ec250;            // "max_delay"
extern "C" float data_2ec288;            // "chance"
extern "C" unsigned char data_2ec2a8;    // "require_mouse_pressed"
extern "C" unsigned char data_2ec2c8;    // "require_weapon"
extern "C" unsigned data_2ec310;         // "mode"
extern "C" float data_2ec350;            // "timer_speed"
extern "C" float data_2ec388;            // "boost"
extern "C" char data_2ec010[];           // Criticals module descriptor
extern "C" unsigned char data_2fa320;    // packet mode armed
extern "C" unsigned short data_2fa322;   // packet delay (ms)
extern "C" unsigned char data_2fa324;    // timer mode armed
extern "C" int data_2fa328;              // roll cooldown
extern "C" unsigned char data_2fa32c;    // last roll
extern "C" unsigned char data_2fa32d;    // timer modified
extern "C" double data_2fa330;           // accumulated boost
extern "C" int data_2fa338;              // current hold budget
extern "C" double data_2fa340;           // last y
extern "C" int data_2fa348;              // last sequence
extern "C" unsigned char data_2fa34c;    // falling
extern "C" const double rdata_151808;
extern "C" float rdata_151810;
extern "C" double rdata_151818;
extern "C" float rdata_151820;

FN(unsigned char, random_chance, (float chance));
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(void *, get_timer, (void *a, void *x));
FN(void, java_set_timer_timer_speed, (void *a, void *timer, float speed));
FN(float, java_get_timer_elapsed_partial_ticks, (void *a, void *timer));
FN(void, java_set_timer_elapsed_partial_ticks, (void *a, void *timer, float v));
FN(void *, java_jni_is_instance_of_c03_packet_player, (void *ctx, void *ev));
FN(unsigned char, java_get_c03_packet_player_moving, (void *ctx, void *p));
FN(double, java_get_c03_packet_player_b, (void *ctx, void *p));
FN(unsigned char, java_get_c03_packet_player_on_ground, (void *ctx, void *p));

#define AXCHG(x, v) __atomic_exchange_n(&(x), (v), __ATOMIC_SEQ_CST)
#define ALOAD(x) __atomic_load_n(&(x), __ATOMIC_SEQ_CST)

extern "C" void criticals_part(Hook *h) {
    Ctx *c = h->ctx;
    bool ok = false;
    if (c->f40 == 1) {
        int n = data_2fa328;
        if (n != 0) {
            unsigned char last = data_2fa32c;
            data_2fa328 = n - 1;
            if (last != 1)
                goto decided;
        } else {
            data_2fa328 = 10;
            unsigned char r = random_chance(data_2ec288);
            data_2fa32c = r;
            data_2fa328 += -1;
            if (!r)
                goto decided;
        }
        if (data_2ec2a8 == 1 && !vertex_pointer_current(c))
            goto decided;
        if (data_2ec2c8 == 1 && !click_with_helper(c->a, c->b))
            goto decided;
        int ht = java_get_entity_living_base_last_attacker_time(c->a, c->b);
        ok = java_get_entity_ticks_existed(c->a, c->b) - ht < 11;
    }
decided:

    unsigned char v;
    if (data_2ec310 == 0) {
        AXCHG(data_2fa320, (unsigned char)ok);
        AXCHG(data_2fa322, (unsigned short)(int)data_2ec250);
        goto zero;
    }
    if (data_2ec310 != 1)
        return;
    AXCHG(data_2fa320, (unsigned char)0);
    if (!ok)
        goto zero;
    if (java_get_entity_on_ground(c->a, c->b))
        goto zero;
    {
        Vec3 m = java_get_entity_motion_x(c->a, c->b);
        if (!(m.y < 0.0))
            goto zero;
    }
    if (java_get_entity_living_base_hurt_time(c->a, c->b) == 10)
        goto zero;
    {
        void *t = java_get_minecraft_5(c->a, c->c18);
        if (!t)
            goto zero;
        v = java_get_moving_object_position_3(c->a, t) == 2;
        goto store;
    }
zero:
    v = 0;
store:
    data_2fa324 = v;
}

extern "C" void criticals_part_2(TimerHook *h) {
    Ctx *c = h->ctx;
    void *timer = get_timer(c->a, c->c18);
    if (data_2fa324 == 1) {
        void *a = c->a;
        java_set_timer_timer_speed(a, timer, data_2ec350);
        data_2fa32d = 1;
        double dt = h->dt * rdata_151818;
        double acc = (double)(rdata_151810 - data_2ec350) * dt + data_2fa330;
        data_2fa330 = acc;
        if (acc > (double)data_2ec388)
            data_2fa330 = data_2ec388;
        return;
    }
    double acc = data_2fa330;
    if (acc > rdata_151808) {
        float cur = java_get_timer_elapsed_partial_ticks(c->a, timer);
        void *a = c->a;
        int whole = (int)acc;
        java_set_timer_elapsed_partial_ticks(a, timer, (float)whole + cur);
        data_2fa330 -= (double)whole;
    }
    if (data_2fa32d == 1) {
        void *a = c->a;
        java_set_timer_timer_speed(a, timer, rdata_151810);
        data_2fa32d = 0;
    }
}

extern "C" void criticals_part_3(PktHook *h) {
    void *p = java_jni_is_instance_of_c03_packet_player(h->ctx, h->ev);
    if (p && java_get_c03_packet_player_moving(h->ctx, p) && h->seq > data_2fa348) {
        double y = java_get_c03_packet_player_b(h->ctx, p);
        unsigned char g = java_get_c03_packet_player_on_ground(h->ctx, p);
        if (g || data_2fa340 > y)
            data_2fa34c = g ^ 1;
        int hold = 0;
        if ((ALOAD(data_2fa320) & 1) && (data_2fa34c & 1)) {
            hold = ALOAD(data_2fa322) / 5 + data_2fa338;
            data_2fa338 = hold;
            if (hold <= (int)ALOAD(data_2fa322))
                goto keep;
            hold = ALOAD(data_2fa322);
        }
        data_2fa338 = hold;
keep:
        data_2fa340 = y;
        data_2fa348 = h->seq;
    }
    if (h->budget < (long long)data_2fa338)
        h->verdict = 1;
}

extern "C" void criticals_part_4(EvHook *h) {
    if (h->ev == data_2ec010 && !h->on && (data_2fa32d & 1)) {
        Ctx *c = h->ctx;
        void *timer = get_timer(c->a, c->c18);
        void *a = c->a;
        java_set_timer_timer_speed(a, timer, rdata_151820);
        data_2fa32d = 0;
    }

}
