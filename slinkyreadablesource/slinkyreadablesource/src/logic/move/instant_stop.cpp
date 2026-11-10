// Instant Stop - descriptor +0x148: when the forward key is released on the
// ground, briefly press back (plus left or right when that cancels the motion
// better) to kill the remaining horizontal velocity, then restore the keys.
//   state 0 = idle, 1 = forward held, 2 = counter-keys pressed this tick.
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Rot { float yaw, pitch; };

extern "C" int data_3043d0;               // state
extern "C" const double rdata_1547a8;     // sprint speed divisor
extern "C" const float rdata_1547b0;      // full input (1.0)
extern "C" const float rdata_1547b4;      // opposite strafe input (-1.0)

FN(unsigned char, is_in_game, (void *a, void *x));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_4, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_5, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_6, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_7, (void *a, void *x));
FN(unsigned char, is_key_down, (void *a, void *k));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(float, java_get_entity_living_base_land_movement_factor, (void *a, void *p));
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(Vec3, horizontal_motion, (Rot r, float strafe, float forward, float speed));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));

extern "C" void instant_stop_part(Hook *h) {
    Ctx *c = h->ctx;
    int next = 0;
    if (is_in_game(c->a, c->c18)) {
        void *obj = get_net_handler(c->a, c->c18);
        void *fwd = java_get_game_settings_key_bindings_4(c->a, obj);
        void *back = java_get_game_settings_key_bindings_5(c->a, obj);
        void *left = java_get_game_settings_key_bindings_6(c->a, obj);
        void *right = java_get_game_settings_key_bindings_7(c->a, obj);
        int st = data_3043d0;
        if (st == 2) {
            java_set_key_binding_pressed(c->a, back, java_get_key_binding_key_code(c->a, back));
            java_set_key_binding_pressed(c->a, left, java_get_key_binding_key_code(c->a, left));
            java_set_key_binding_pressed(c->a, right, java_get_key_binding_key_code(c->a, right));
        } else if (st == 1) {
            if (is_key_down(c->a, fwd))
                return;
            if (!java_get_entity_on_ground(c->a, c->b))
                return;
            Vec3 v = java_get_entity_motion_x(c->a, c->b);
            Rot rot = get_entity_rotation(c->a, c->b);
            float speed = java_get_entity_living_base_land_movement_factor(c->a, c->b);
            if (java_call_entity_is_sprinting(c->a, c->b))
                speed = (float)((double)speed / rdata_1547a8);
            double vx = v.x, vz = v.z;
            float k = rdata_1547b0;
            Vec3 t = horizontal_motion(rot, k, k, speed);
            t = vec3_add(v, t);
            double s1 = t.x * t.x + t.z * t.z;
            double s0 = vx * vx + vz * vz;
            int dir = s0 > s1 ? -1 : 999;
            double best = s1 < s0 ? s1 : s0;
            t = horizontal_motion(rot, k, 0.0f, speed);
            t = vec3_add(v, t);
            double s2 = t.x * t.x + t.z * t.z;
            if (best > s2)
                dir = 0;
            best = s2 < best ? s2 : best;
            t = horizontal_motion(rot, k, rdata_1547b4, speed);
            t = vec3_add(v, t);
            double s3 = t.x * t.x + t.z * t.z;
            dir = best > s3 ? 1 : dir;
            if (dir != 999) {
                java_set_key_binding_pressed(c->a, back, 1);
                java_set_key_binding_pressed(c->a, left, best > s3);
                java_set_key_binding_pressed(c->a, right, dir == -1);
                next = 2;
            }
        } else {
            if (!is_key_down(c->a, fwd))
                return;
            next = 1;
        }
    }
    data_3043d0 = next;
}
