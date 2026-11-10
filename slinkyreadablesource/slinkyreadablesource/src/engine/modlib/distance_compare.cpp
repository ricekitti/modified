// 0x36000: distance comparisons (vec3_distance takes its two Vec3 by value = pointers to caller copies)
#include "slinky/engine.h"
struct Vec3 { double x, y, z; };
FN(double, vec3_distance, (Vec3 *a, Vec3 *b));
FN(Vec3, java_get_entity_pos_x, (void *a, void *e));
FN(Vec3, java_get_entity_other_player_mp_other_player_mpx, (void *a, void *e));
extern "C" double rdata_1521b0;
extern "C" double segment_distance(Vec3 *a, Vec3 *b, Vec3 *c) {
    Vec3 t1, t2;
    t1 = *a; t2 = *b;
    double d1 = vec3_distance(&t1, &t2);
    t1 = *a; t2 = *c;
    return vec3_distance(&t1, &t2) - d1;
}
extern "C" bool entity_reach_check(Ctx *c, void *ent, Vec3 *p) {
    Vec3 eye = java_get_entity_pos_x(c->a, ent);
    Vec3 prev = java_get_entity_other_player_mp_other_player_mpx(c->a, ent);
    Vec3 p1 = *p, e1 = eye, q1 = prev, p2 = *p;
    double d1 = vec3_distance(&p2, &e1);
    return vec3_distance(&p1, &q1) - d1 > rdata_1521b0;
}

// ---- smooth the tracked server position/yaw over 3 ticks after every change ----
extern "C" Vec3 data_2fc4e8, data_2fc4a8, data_2fc4c0;
extern "C" float data_2fc500, data_2fc4d8, data_2fc4dc;
extern "C" int data_2fc504;
extern "C" const double rdata_1521a8;   // 1.0
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(Vec3, vec3_scale, (Vec3 a, double s));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(float, wrap_angle, (float x));
extern "C" void is_same_position(const Vec3 *pos, float yaw) {
    int n;
    if (__builtin_memcmp(pos, &data_2fc4e8, sizeof(Vec3)) == 0 && yaw == data_2fc500) {
        n = data_2fc504;
    } else {
        data_2fc504 = 3;
        n = 3;
    }
    data_2fc4e8 = *pos;
    data_2fc4a8 = data_2fc4c0;
    data_2fc500 = yaw;
    data_2fc4d8 = data_2fc4dc;
    float ny;
    if (n > 0) {
        double f = rdata_1521a8 / (double)n;
        Vec3 d = vec3_scale(vec3_sub(*pos, data_2fc4c0), f);
        data_2fc4c0 = vec3_add(data_2fc4c0, d);
        ny = (float)((double)wrap_angle(data_2fc500 - data_2fc4dc) * f) + data_2fc4dc;
        data_2fc504--;
    } else {
        data_2fc4c0 = *pos;
        ny = yaw;
    }
    data_2fc4dc = ny;
}

// ---- track the target entity's position from move / teleport packets ----
// undo=true reverts the packet's delta (skipped when the packet is older than the last teleport)
struct PktCtx { void *j; void *pkt; void *p10; int tick; };
FN(Vec3, vec3_make, (double x, double y, double z));
FN(void *, java_jni_is_instance_of_s14_packet_entity, (void *j, void *pkt));
FN(int, java_get_s14_packet_entity_entity_id, (void *j, void *p));
FN(signed char, java_jni_get_byte_field_s14_packet_entity_pos_x, (void *j, void *p));
FN(signed char, java_jni_get_byte_field_s14_packet_entity_pos_y, (void *j, void *p));
FN(signed char, java_jni_get_byte_field_s14_packet_entity_pos_z, (void *j, void *p));
FN(signed char, java_jni_get_byte_field_s14_packet_entity_yaw, (void *j, void *p));
FN(bool, java_get_s14_packet_entity_g, (void *j, void *p));
FN(void *, java_jni_is_instance_of_s18_packet_entity_teleport, (void *j, void *pkt));
FN(int, java_get_s18_packet_entity_teleport_entity_id, (void *j, void *p));
FN(int, java_get_s18_packet_entity_teleport_pos_x, (void *j, void *p));
FN(int, java_get_s18_packet_entity_teleport_pos_y, (void *j, void *p));
FN(int, java_get_s18_packet_entity_teleport_pos_z, (void *j, void *p));
FN(signed char, java_jni_get_byte_field_s18_packet_entity_teleport_yaw, (void *j, void *p));
extern "C" int data_2fc468, data_2fc49c;
extern "C" Vec3 data_2fc478;
extern "C" bool data_2fc490, data_2fc498;
extern "C" float data_2fc494;
extern "C" const double rdata_152198;   // 1/32
extern "C" const float rdata_1521a0, rdata_1521a4;
extern "C" void check_entity_distance(PktCtx *c, bool undo) {
    if (undo && data_2fc49c > c->tick) return;
    if (void *m = java_jni_is_instance_of_s14_packet_entity(c->j, c->pkt)) {
        if (java_get_s14_packet_entity_entity_id(c->j, m) != data_2fc468) return;
        double x = (double)java_jni_get_byte_field_s14_packet_entity_pos_x(c->j, m) * rdata_152198;
        double y = (double)java_jni_get_byte_field_s14_packet_entity_pos_y(c->j, m) * rdata_152198;
        double z = (double)java_jni_get_byte_field_s14_packet_entity_pos_z(c->j, m) * rdata_152198;
        if (undo) {
            data_2fc478 = vec3_sub(data_2fc478, vec3_make(x, y, z));
            return;
        }
        data_2fc478 = vec3_add(data_2fc478, vec3_make(x, y, z));
        if (!java_get_s14_packet_entity_g(c->j, m)) return;
        data_2fc494 = (float)java_jni_get_byte_field_s14_packet_entity_yaw(c->j, m) * rdata_1521a0 * rdata_1521a4;
        data_2fc498 = 1;
        return;
    }
    void *t = java_jni_is_instance_of_s18_packet_entity_teleport(c->j, c->pkt);
    if (!t) return;
    if (java_get_s18_packet_entity_teleport_entity_id(c->j, t) != data_2fc468) return;
    double x = (double)java_get_s18_packet_entity_teleport_pos_x(c->j, t) * rdata_152198;
    double y = (double)java_get_s18_packet_entity_teleport_pos_y(c->j, t) * rdata_152198;
    double z = (double)java_get_s18_packet_entity_teleport_pos_z(c->j, t) * rdata_152198;
    if (undo) {
        data_2fc478 = vec3_sub(data_2fc478, vec3_make(x, y, z));
        data_2fc490 = 0;
        return;
    }
    data_2fc478 = vec3_make(x, y, z);
    data_2fc490 = 1;
    data_2fc494 = (float)java_jni_get_byte_field_s18_packet_entity_teleport_yaw(c->j, t) * rdata_1521a0 * rdata_1521a4;
    data_2fc498 = 1;
    data_2fc49c = c->tick;
}
