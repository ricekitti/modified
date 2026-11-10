// Chest Stealer - descriptor +0x1d8: when a container screen is open, (re)arm
// the steal delay with a random factor in [1.0, 1.5].
#include "slinky/engine.h"

S(155f9a);  // "container.chest"
S(155faa);  // "container.chestDouble"

extern "C" float data_2f54c8;                 // "delay_after_open" current value
extern "C" unsigned char data_2f54e8;         // "require_vanilla_title"
extern "C" float data_304a10;                 // steal delay (ms)

FN(void *, get_current_screen, (void *a, void *x));
FN(unsigned char, java_jni_is_instance_of_gui_chest, (void *a, void *scr));
FN(void *, java_get_gui_chest, (void *a, void *scr));
FN(void *, java_jni_is_instance_of_inventory_basic, (void *a, void *x));
FN(void *, java_get_inventory_basic, (void *a, void *x));
FN(unsigned char, java_string_equal_translation_key, (void *a, void *title, const char *name));
FN(void *, rot_make_floats, (float lo, float hi));
FN(float, random_rotation, (void *rng));
FN(void, slot_index, (int event));

extern "C" void chest_stealer_part(Hook *h) {
    Ctx *c = h->ctx;
    void *scr = get_current_screen(c->a, c->c18);
    if (scr && java_jni_is_instance_of_gui_chest(c->a, scr)) {
        if (data_2f54e8 != 1)
            return;
        void *a = c->a;
        void *w = java_jni_is_instance_of_inventory_basic(a, java_get_gui_chest(a, scr));
        if (w) {
            void *title = java_get_inventory_basic(c->a, w);
            if (java_string_equal_translation_key(c->a, title, rdata_155f9a))
                return;
            if (java_string_equal_translation_key(c->a, title, rdata_155faa))
                return;
        }
    }
    data_304a10 = data_2f54c8;
    data_304a10 = random_rotation(rot_make_floats(1.0f, 1.5f)) * data_304a10;
    slot_index(13);
}
