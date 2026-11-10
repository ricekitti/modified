// 0x6000: per-frame tick: rotation-delta tracking and the timed "tick" event (type 1)
#include "slinky/engine.h"
struct Rot { float yaw, pitch; };
extern "C" double data_2f6f00;
extern "C" Rot data_2f6ef8;
extern "C" const double rdata_13e010;
extern "C" char data_2f6f08[];
FN(void, update_frame_time, ());
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(Rot, rot_sub, (Rot a, Rot b));
FN(void, add_rotation_delta, (Rot r));
FN(float, get_partial_ticks, (void *a, void *x));
FN(void, set_rotation_active, (void *a, void *pl, bool on));
struct TickEvent { long long type; Ctx *ctx; float pt; int z; double dt; char rest[0x28]; };
FN(void, dispatch_event, (void *mgr, TickEvent *e));
extern "C" void tick_modules(Ctx *c, void *x, double now) {
    if (data_2f6f00 == 0.0) data_2f6f00 = now;
    update_frame_time();
    if (!c->p20 || !c->b) {
        data_2f6f00 = now;
        return;
    }
    Rot d = rot_sub(get_entity_rotation(c->a, c->b), data_2f6ef8);
    if (__builtin_bit_cast(unsigned long long, d) & 0x7fffffff7fffffffull) add_rotation_delta(d);
    double dt = now - data_2f6f00;
    if (rdata_13e010 > dt) return;
    float pt = get_partial_ticks(c->a, x);
    TickEvent e = {1, c, pt, 0, dt, {}};
    dispatch_event(data_2f6f08, &e);
    set_rotation_active(c->a, c->b, false);
    data_2f6f00 = now;
    data_2f6ef8 = get_entity_rotation(c->a, c->b);
}
