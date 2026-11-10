// Chams - entity render hook wrapper.  Draws the entity with GL polygon offset
// (so it shows through geometry) unless it is filtered out (friendlies when
// "hide_friendlies" is set).  Forwards all render arguments to the original 0x274f0.
#include "slinky/engine.h"
#define H0(c) __builtin_expect(!!(c), 0)
#define H1(c) (c)

extern "C" unsigned char data_2f0ff4;            // module enabled
extern "C" unsigned char data_2f1208;            // "hide_friendlies"
extern "C" void (**rdata_13e458)(unsigned);       // glEnable
extern "C" void (**rdata_13e450)(unsigned);       // glDisable
extern "C" void (**rdata_153f78)(float, float);   // glPolygonOffset
extern "C" volatile float rdata_153f24;             // 1.0f
extern "C" volatile float rdata_153f28;             // -3000000.0f

FN(unsigned char, entity_flags_has, (void *a, void *x, int mode));
FN(void, java_jni_call_nonvirtual_void_method_renderer_living_entity_2, (void *a, void *b, void *c, double x, double d0, double d1, float f0, float f1));

extern "C" void chams_part_2(void *a, void *b, void *c, double x, double d0, double d1, float f0, float f1) {
    if (__builtin_expect(data_2f0ff4 != 1, 0) || entity_flags_has(a, c, data_2f1208 * 5 + 2)) {
        java_jni_call_nonvirtual_void_method_renderer_living_entity_2(a, b, c, x, d0, d1, f0, f1);
           // keeps clang from turning this call into a tail call (the original is not)
        return;
    }
    (*rdata_13e458)(0x8037);
    auto po = *rdata_153f78;
    float f = rdata_153f24;
    float u = rdata_153f28;
    po(f, u);
    java_jni_call_nonvirtual_void_method_renderer_living_entity_2(a, b, c, x, d0, d1, f0, f1);
    (*rdata_13e450)(0x8037);
}
