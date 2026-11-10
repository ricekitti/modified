// Timer - descriptor +0x58 / +0x98: set the game timer speed.
// The first hook applies "speed" when every enabled condition holds, else 1.0.
#include "slinky/engine.h"

extern "C" float data_2ebc50;             // "speed" current value
extern "C" unsigned char data_2ebc70;     // "require_mouse"
extern "C" unsigned char data_2ebc90;     // "require_falling"
extern "C" unsigned char data_2ebcb0;     // "require_damage"
extern "C" char data_2eba10[];            // owner tag checked by hook +0x98

struct V3 { double x, y, z; };
struct TimerState { char pad0[8]; Ctx *ctx; void *tag; bool flag; };

FN(void *, get_timer, (void *a, void *x));
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *b));
FN(void, java_get_entity_motion_x, (V3 *out, void *a, void *b));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *b));
FN(void, java_set_timer_timer_speed, (void *a, void *timer, float speed));

extern "C" void timer_part(TimerState *s) {
    Ctx *c = s->ctx;
    void *t = get_timer(c->a, c->c18);
    float speed;
    void *a;
    if (data_2ebc70 == 1 && !vertex_pointer_current(c))
        goto normal;
    if (data_2ebc90 == 1) {
        if (java_get_entity_on_ground(c->a, c->b))
            goto normal;
        V3 v;
        java_get_entity_motion_x(&v, c->a, c->b);
        if (v.y >= 0.0)
            goto normal;
    }
    if (data_2ebcb0 == 1) {
        if (c->f40 != 1)
            goto normal;
        if (java_get_entity_living_base_hurt_time(c->a, c->b) == 0)
            goto normal;
    }
    a = c->a;

    speed = data_2ebc50;
    goto apply;
normal:
    a = c->a;

    speed = 1.0f;
apply:
    java_set_timer_timer_speed(a, t, speed);

}

extern "C" void timer_part_2(TimerState *s) {
    if (s->tag != (void *)data_2eba10 || s->flag != 0)
        return;
    Ctx *c = s->ctx;
    java_set_timer_timer_speed(c->a, get_timer(c->a, c->c18), 1.0f);
}
