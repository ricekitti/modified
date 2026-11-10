// 0x2a000: rotation save/restore, hotbar slot switching, tagged-float entity flags, small id sets
#include "slinky/engine.h"
#include "slinky/jcall.h"

struct F2 { float a, b; };
FN(void *, get_minecraft, (void *j));
FN(void *, get_local_player, (void *j, void *mc));
FN(unsigned long long, get_entity_rotation, (void *j, void *p));               // packed Rot
FN(void, set_entity_rotation, (void *j, void *p, unsigned long long rot));
FN(void, set_entity_prev_rotation, (void *j, void *p, unsigned long long rot));
FN(float, java_get_entity_player_speed_on_ground, (void *j, void *e));                           // tagged flag float
FN(void, java_set_entity_player_speed_on_ground, (void *j, void *e, float v));
FN(void *, java_get_entity_player_2, (void *j, void *p));
FN(int, java_get_inventory_player_current_item, (void *j, void *inv));
FN(void *, java_get_inventory_player_main_inventory, (void *j, void *inv));
FN(void *, java_call_item_stack_get_item, (void *j, void *stack));
FN(int, java_call_static_item_get_id_from_item, (void *j, void *item));
FN(void, java_set_inventory_player_current_item, (void *j, void *inv, int slot));
FN(unsigned long long, libc_strlen, (const char *s));                // strlen
FN(void **, element_at, (void *vec, int elem, void *hdr, void *key));
FN(int, key_name_to_code, (void *key));
FN(void *, key_code_to_index, (int idx));
FN(void *, vector_find_insert, (void *t, int elem, void *hdr, void *e, void *cmp));
FN(void *, vector_erase, (void *vec, int elem, void *hdr, void *key));
FN(void *, binary_search, (void *vec, int elem, void *hdr, void *e, void *cmp));

extern "C" unsigned long long data_2f7b88, data_2f7b98;   // saved / current rotation
extern "C" unsigned char data_2f7b90, data_2f7b91, data_2f7ba0;
extern "C" void *data_2f7ba8;                             // slot owner
extern "C" void *data_2f7bb0;                             // module that switched the slot
extern "C" int data_2f7bb8, data_2f7bbc;
extern "C" unsigned char data_2f7bc0;

// apply the saved rotation to the local player (silent rotations)
extern "C" void tick_rotation(void *j) {
    void *mc = get_minecraft(j);
    void *p = get_local_player(j, mc);
    if (!p) return;
    data_2f7b98 = get_entity_rotation(j, p);
    if (data_2f7b91 == 1) {
        data_2f7b91 = 0;
        set_entity_rotation(j, p, data_2f7b88);
        set_entity_prev_rotation(j, p, data_2f7b88);
    }
    if (data_2f7b90 == 1) {
        data_2f7ba0 = 1;
        set_entity_rotation(j, p, data_2f7b88);
    }
}
// restore the real rotation
extern "C" void apply_rotation_to_player(void *j, void *p) {
    if (data_2f7ba0 != 1) return;
    data_2f7b88 = get_entity_rotation(j, p);
    data_2f7ba0 = 0;
    set_entity_rotation(j, p, data_2f7b98);
    set_entity_prev_rotation(j, p, data_2f7b98);
}

struct GRef { void *pad; jobject ref; };
extern "C" void tick_post_frame(Jvm **jj, GRef *g) {
    if (g->ref) JNI(*jj)->DeleteGlobalRef((*jj)->env, g->ref);
    g->ref = nullptr;
}
// claim / release the hotbar for a module
extern "C" bool claim_release_hotbar_module(void *owner) {
    void *cur = data_2f7ba8;
    if (cur == owner) return true;
    if (cur) return false;
    data_2f7ba8 = owner;
    return true;
}
extern "C" void release_owner(void *owner) {
    if (data_2f7ba8 == owner) data_2f7ba8 = nullptr;
}
// slot index -> (column, row) of the inventory grid; hotbar slots get row 1
extern "C" F2 slot_index(unsigned slot) {
    F2 r;
    if (slot <= 8) {
        r.b = 1.0f;
        r.a = (float)(unsigned)(slot - 5);
    } else {
        unsigned q = slot / 9;
        r.b = (float)(int)(slot - q * 9);
        r.a = (float)(q + 3);
    }
    return r;
}
// append a letter/digit/space to a 100-byte text buffer
extern "C" void key_name_append(char *buf, char c) {
    unsigned long long n = libc_strlen(buf);
    if (n > 99) return;
    if ((unsigned char)((c & 0xdf) + 0xbf) >= 0x1a && (unsigned char)(c - '0') >= 10 && c != ' ') return;
    buf[n] = c;
}

struct IdVec { void *v; char hdr[4]; int count; };
struct IdEnt { void *p; int id; int z; };
extern "C" int id_vector_count(IdVec *s) { return s->count; }
extern "C" void *id_vector_get(IdVec *s, void *key) { return *element_at(s->v, 0x10, s->hdr, key); }
extern "C" int compare_id_entry(void *ctx, IdEnt *a, IdEnt *b) { return a->id - b->id; }
extern "C" bool id_vector_insert(IdVec *s, void *key) {
    int id = key_name_to_code(key);
    if (id != -1) {
        IdEnt e = {key_code_to_index(id), id, 0};
        vector_find_insert(s, 0x10, s->hdr, &e, (void *)compare_id_entry);
    }
    return id != -1;
}
extern "C" void *id_vector_remove(IdVec *s, void *key) { return vector_erase(s->v, 0x10, s->hdr, key); }
extern "C" void id_vector_clear(IdVec *s) { s->count = 0; }
extern "C" bool id_vector_contains(IdVec *s, int id) {
    IdEnt e = {nullptr, id, 0};
    return binary_search(s->v, 0x10, s->hdr, &e, (void *)compare_id_entry) != nullptr;
}

// entity flags kept in a float field as a NaN tagged 0xb315xxxx
#define TAG 0xb3150000u
static inline __attribute__((always_inline)) unsigned bits(float f) { return __builtin_bit_cast(unsigned, f); }
extern "C" unsigned entity_flags_get(void *j, void *e) {
    unsigned v = bits(java_get_entity_player_speed_on_ground(j, e));
    return (v & 0xffff0000) == TAG ? (v & 0xffff) : 0;
}
extern "C" bool entity_flags_has(void *j, void *e, int mask) {
    unsigned v = bits(java_get_entity_player_speed_on_ground(j, e));
    unsigned f = (v & 0xffff0000) == TAG ? (v & 0xffff) : 0;
    return (f & mask) != 0;
}
extern "C" void entity_flags_set(void *j, void *e, int mask) {
    unsigned v = bits(java_get_entity_player_speed_on_ground(j, e));
    unsigned cur = (v & 0xffff0000) == TAG ? v : TAG;
    java_set_entity_player_speed_on_ground(j, e, __builtin_bit_cast(float, ((cur | mask) & 0xffff) | TAG));
}
extern "C" void entity_flags_clear(void *j, void *e, int mask) {
    unsigned v = bits(java_get_entity_player_speed_on_ground(j, e));
    unsigned cur = (v & 0xffff0000) == TAG ? (v & 0xffff) : TAG;
    java_set_entity_player_speed_on_ground(j, e, __builtin_bit_cast(float, (~mask & cur) | TAG));
}

// switch to the best hotbar slot (exact item id first, else best score), once per module
extern "C" void switch_best_hotbar_slot(Ctx *c, void *owner, unsigned char force, int (*score)(int)) {
    if (data_2f7bb0 != nullptr) return;
    if (!force && data_2f7bb8 == 0) return;
    void *inv = java_get_entity_player_2(c->a, c->b);
    int want = data_2f7bb8;
    int start = java_get_inventory_player_current_item(c->a, inv) + data_2f7bc0;
    void *items = java_get_inventory_player_main_inventory(c->a, inv);
    JNIEnv *env = ((Jvm *)c->a)->env;
    int best = -1, bestScore = -1;
    #pragma clang loop unroll(disable)
    for (int i = 0; i != 9; i++) {
        int slot = (start + i) % 9;
        jobject st = (*env)->GetObjectArrayElement(env, (jobjectArray)items, slot);
        if (!st) continue;
        void *it = java_call_item_stack_get_item(c->a, st);
        if (!it) continue;
        int id = java_call_static_item_get_id_from_item(c->a, it);
        if (want != 0 && id == want) { best = slot; break; }
        int s = score(id);
        if (s > bestScore) { bestScore = s; best = slot; }
    }
    if (best == -1) return;
    if (!claim_release_hotbar_module(owner)) return;
    java_set_inventory_player_current_item(c->a, inv, best);
    data_2f7bb0 = owner;
}
// block-id preference for scaffolding: 2 = solid building block, 1 = acceptable, -1 = no
extern "C" int block_id_preference_scaffolding(int id) {
    switch (id) {
    case 1: case 2: case 3: case 35: case 45: return 2;
    case 5: case 17: case 20: case 24: case 87: case 121: case 159: case 162: return 1;
    default: return -1;
    }
}
extern "C" void track_owner(Ctx *c, void *owner, unsigned char force) {
    switch_best_hotbar_slot(c, owner, force, block_id_preference_scaffolding);
}
extern "C" void release_tracked_owner() {
    if (data_2f7bb0) {
        release_owner(data_2f7bb0);
        data_2f7bb0 = nullptr;
    }
    data_2f7bbc++;
    data_2f7bb8 = 0;
    data_2f7bc0 = 0;
}

// ---- "moving toward the target?" test, computed once per tick (state bit 0) ----
struct V3a { double x, y, z; };
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(void *, java_get_moving_object_position, (void *a, void *x));
FN(V3a, java_get_vec3_x_coord, (void *a, void *e));
FN(V3a, java_get_entity_pos_x, (void *a, void *e));
FN(unsigned char, java_variant, (void *a));
FN(V3a, vec3_add_eye_height, (V3a v));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *e));
FN(float, java_get_entity_living_base_land_movement_factor, (void *a, void *e));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_4, (void *a, void *e));
FN(void *, java_get_game_settings_key_bindings_5, (void *a, void *e));
FN(void *, java_get_game_settings_key_bindings_6, (void *a, void *e));
FN(void *, java_get_game_settings_key_bindings_7, (void *a, void *e));
FN(unsigned char, is_key_down, (void *a, void *x));
FN(float, java_get_entity_living_base_move_forward, (void *a, void *e));
FN(float, java_get_entity_living_base_move_strafing, (void *a, void *e));
FN(V3a, horizontal_motion, (unsigned long long rot, float fwd, float strafe, float speed));
FN(V3a, java_get_entity_motion_x, (void *a, void *e));
FN(V3a, vec3_add, (V3a a, V3a b));
FN(double, vec3_distance_sq, (V3a a, V3a b));
extern "C" const float rdata_14fd70;
extern "C" const double rdata_14fd78;
struct TickState { unsigned char done; char pad[0x17]; unsigned char result; };
extern "C" void tick_pre_click(Ctx *c, TickState *st) {
    if (st->done & 1) return;
    unsigned char r = 1;
    void *hit = java_get_minecraft_5(c->a, c->c18);
    if (hit && java_get_moving_object_position_3(c->a, hit) == 2) {
        V3a tp = java_get_vec3_x_coord(c->a, java_get_moving_object_position(c->a, hit));
        V3a eye = java_get_entity_pos_x(c->a, c->b);
        if (!java_variant(c->a)) eye = vec3_add_eye_height(eye);
        float speed = java_get_entity_on_ground(c->a, c->b) ? java_get_entity_living_base_land_movement_factor(c->a, c->b) : rdata_14fd70;
        void *a = c->a;
        void *inv = get_net_handler(a, c->c18);
        void *i0 = java_get_game_settings_key_bindings_4(a, inv), *i1 = java_get_game_settings_key_bindings_5(a, inv), *i2 = java_get_game_settings_key_bindings_6(a, inv), *i3 = java_get_game_settings_key_bindings_7(a, inv);
        is_key_down(a, i0); is_key_down(a, i1); is_key_down(a, i2); is_key_down(a, i3);
        unsigned long long rot = get_entity_rotation(c->a, c->b);
        float fwd = java_get_entity_living_base_move_forward(c->a, c->b);
        V3a mv = horizontal_motion(rot, fwd, java_get_entity_living_base_move_strafing(c->a, c->b), speed);
        V3a v1 = java_get_entity_motion_x(c->a, c->b);
        V3a v2 = java_get_entity_motion_x(c->a, c->b);
        v2.x *= rdata_14fd78;
        v2.z *= rdata_14fd78;
        V3a da = vec3_add(v1, mv);
        V3a db = vec3_add(v2, mv);
        V3a pa = vec3_add(eye, da);
        V3a pb = vec3_add(eye, db);
        double dB = vec3_distance_sq(pb, tp);
        r = vec3_distance_sq(pa, tp) > dB;
    }
    st->result = r;
    st->done |= 1;
}

// ---- pick the aim target: crosshair entity, forced target, else nearest valid entity in FOV ----
struct AB { V3a min, max; };
struct Rot2 { float yaw, pitch; };
FN(void *, java_get_moving_object_position_2, (void *a, void *x));
FN(void *, java_jni_is_instance_of_entity_other_player_mp, (void *a, void *x));
FN(float, java_call_entity_living_base_get_health, (void *a, void *e));
FN(void *, get_refill_state, ());
FN(void *, java_get_world, (void *a, void *w));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(unsigned char, java_get_entity_player_sleeping, (void *a, void *e));
FN(void *, java_get_entity, (void *a, void *e));
FN(AB, java_get_axis_aligned_bb_min_x, (void *a, void *bb));
FN(AB, aabb_expand, (AB b, double d));
FN(V3a, aabb_clamp_point, (AB b, V3a p));
FN(Rot2, rot_look_at, (V3a from, V3a to));
FN(float, wrap_angle, (float x));
extern "C" const double rdata_14fd80, rdata_14fd88;
extern "C" const float rdata_14fd90;
struct Target { int z0; int pad; jobject ref; double dist; };
extern "C" bool target_clear(Ctx *c, Target *t) {
    t->z0 = 0;
    if (!c->p20) {
        if (t->ref) { JNIEnv *env = ((Jvm *)c->a)->env; (*env)->DeleteGlobalRef(env, t->ref); }
        t->ref = 0;
        return true;
    }
    V3a eye = java_get_entity_pos_x(c->a, c->b);
    if (!java_variant(c->a)) eye = vec3_add_eye_height(eye);
    void *ent;
    JNIEnv *env;
    void *hit = java_get_minecraft_5(c->a, c->c18);
    if (hit && java_get_moving_object_position_3(c->a, hit) == 2 && (ent = java_jni_is_instance_of_entity_other_player_mp(c->a, java_get_moving_object_position_2(c->a, hit))) != 0 &&
        !entity_flags_has(c->a, ent, 7) && java_call_entity_living_base_get_health(c->a, ent) > 0.0f) {
        env = ((Jvm *)c->a)->env;
    } else {
        ent = get_refill_state();
        env = ((Jvm *)c->a)->env;
        if (!ent) {
            Rot2 rot = __builtin_bit_cast(Rot2, get_entity_rotation(c->a, c->b));
            void *list = java_get_world(c->a, c->p20);
            void *best = 0;
            double bestD = rdata_14fd88;
            for (int i = 0; i < java_call_list_size(c->a, list); i++) {
                void *e = java_call_list_get(c->a, list, i);
                JNIEnv *e2 = ((Jvm *)c->a)->env;
                if ((*e2)->IsSameObject(e2, (jobject)c->b, (jobject)e)) continue;
                if (entity_flags_has(c->a, e, 7)) continue;
                if (!(java_call_entity_living_base_get_health(c->a, e) > 0.0f)) continue;
                if (java_get_entity_player_sleeping(c->a, e)) continue;
                AB bb = aabb_expand(java_get_axis_aligned_bb_min_x(c->a, java_get_entity(c->a, e)), rdata_14fd80);
                double d = vec3_distance_sq(eye, aabb_clamp_point(bb, eye));
                if (d > bestD) continue;
                Rot2 ang = rot_look_at(eye, java_get_entity_pos_x(c->a, e));
                float dp = ang.pitch - rot.pitch;
                float dy = wrap_angle(ang.yaw - rot.yaw);
                if (dy * dy + dp + dp > rdata_14fd90) continue;
                bestD = d;
                best = e;
            }
            if (best) t->dist = bestD;
            env = ((Jvm *)c->a)->env;
            if (t->ref) (*env)->DeleteGlobalRef(env, t->ref);
            if (!best) {
                t->ref = 0;
                return true;
            }
            jobject r = (*env)->NewGlobalRef(env, (jobject)best);
            if (!r) return false;
            t->ref = r;
            return true;
        }
    }
    if (t->ref) (*env)->DeleteGlobalRef(env, t->ref);
    jobject r = (*env)->NewGlobalRef(env, (jobject)ent);
    if (r) t->ref = r;
    AB bb = aabb_expand(java_get_axis_aligned_bb_min_x(c->a, java_get_entity(c->a, ent)), rdata_14fd80);
    t->dist = vec3_distance_sq(eye, aabb_clamp_point(bb, eye));
    return true;
}
