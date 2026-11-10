// Free Look - descriptor +0x98 / +0x118: swap the camera perspective while
// the key is held and restore the previous one on release.
#include "slinky/engine.h"

extern "C" char data_2edef0[];                 // owner tag
extern "C" unsigned data_2ee148;               // "target_perspective" current value
extern "C" unsigned char data_2fc508;          // free-look active
extern "C" void *data_2fc510;                  // saved camera
extern "C" int data_2fc518;                    // saved perspective

struct LookState { char pad0[8]; Ctx *ctx; void *tag; unsigned char held; };  // held @ +0x18

FN(bool, is_silent_rotation_active, ());
FN(void *, get_silent_rotation, ());
FN(void *, get_entity_rotation, (void *a, void *b));
FN(void *, get_net_handler, (void *a, void *x));
FN(int, java_get_game_settings_third_person_view, (void *a, void *x));
FN(void, java_set_game_settings_third_person_view, (void *a, void *x, int perspective));
FN(void, set_render_rotation, (void *camera));

extern "C" void free_look_part(LookState *s) {
    if (s->tag != (void *)data_2edef0)
        return;
    Ctx *c = s->ctx;
    if (c->p20 == 0)
        return;
    if (s->held == 1) {
        data_2fc508 = 1;
        void *cam;
        if (is_silent_rotation_active())
            cam = get_silent_rotation();
        else
            cam = get_entity_rotation(c->a, c->b);
        data_2fc510 = cam;
    }
    void *x = get_net_handler(c->a, c->c18);
    void *a = c->a;
    if (s->held != 0) {
        data_2fc518 = java_get_game_settings_third_person_view(a, x);
        void *a2 = c->a;
        unsigned persp = data_2ee148;
        java_set_game_settings_third_person_view(a2, x, persp);
        return;
    }
    java_set_game_settings_third_person_view(a, x, data_2fc518);
    if (data_2fc508 == 1) {
        set_render_rotation(data_2fc510);
        data_2fc508 = 0;
    }
}

extern "C" void free_look_part_2(char *s) {
    s[8] = 1;
    *(void **)(s + 0xc) = data_2fc510;
}
