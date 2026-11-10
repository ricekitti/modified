// 0x3c000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
#include "slinky/heavy_glow.h"
extern "C" void *data_2fc8c0; extern "C" char data_2fc8c8[]; extern "C" int data_2fc8cc; extern "C" unsigned char data_2f02e4;
FN(void, vector_erase, (void *vec, int elem, void *hdr, void *key));
FN(void *, java_jni_is_instance_of_entity_player, (void *a, void *x));
FN(unsigned char, entity_flags_has, (void *a, void *x, int mode));
FN(void *, java_call_renderer_living_entity, (void *a, void *b, void *c));
extern "C" void remove_id_key(void *, void *key) { vector_erase(data_2fc8c0, 0x20, data_2fc8c8, key); }
extern "C" void clear_id_flag() { data_2fc8cc = 0; }
extern "C" void *should_render_player_name(void *a, void *x, void *p) {
    if (heavy_glow_capture_active()) return 0;
    if (data_2f02e4 == 1 && java_jni_is_instance_of_entity_player(a, p) != 0 && !entity_flags_has(a, p, 2)) return 0;
    return java_call_renderer_living_entity(a, x, p);
}
