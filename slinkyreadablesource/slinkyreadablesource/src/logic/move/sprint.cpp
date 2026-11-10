// Sprint - descriptor +0x158 / +0x198.
// +0x158: keep the sprint key state in sync (and re-assert sprint on the player)
//         unless a GUI is open and "while_in_gui" is off.
// +0x198: boost motion for the directions allowed by the settings.
#include "slinky/engine.h"
#define H0(c) (c)
#define H1(c) __builtin_expect(!!(c), 1)
#define H2(c) (c)

extern "C" unsigned char data_2eda68;   // "while_in_gui"
extern "C" unsigned char data_2eda88;   // "while_using_item"
extern "C" unsigned char data_2edaa8;   // "while_backwards"
extern "C" unsigned char data_2edac8;   // "while_sideways"
extern "C" unsigned char data_2fc448;   // sprint currently forced

struct SprintState { char pad0[8]; Ctx *ctx; };
struct MoveState { char pad0[8]; Ctx *ctx; float strafe; float forward; float speed; };  // +0x10/+0x14/+0x18

FN(void *, get_current_screen, (void *a, void *x));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_9, (void *a, void *x));
FN(void, java_set_key_binding_pressed, (void *a, void *x, unsigned char on));
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(void, java_call_entity_set_sprinting, (void *a, void *p, bool on));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *b));

extern "C" void sprint_part(SprintState *s) {
    Ctx *c = s->ctx;
    unsigned char allowed = ((get_current_screen(c->a, c->c18) == 0) | data_2eda68) & 1;
    java_set_key_binding_pressed(c->a, java_get_game_settings_key_bindings_9(c->a, get_net_handler(c->a, c->c18)), allowed);
    void *p = c->b;
    if (p) {
        if (!allowed) {
            if (data_2fc448 == 1)
                java_call_entity_set_sprinting(c->a, p, false);
            return;
        }
        if (java_call_entity_is_sprinting(c->a, p))
            java_call_entity_set_sprinting(c->a, c->b, true);
    }
    data_2fc448 = allowed;
}

extern "C" void sprint_part_2(MoveState *s) {
    Ctx *c = s->ctx;
    if (s->forward == 0.0f && s->strafe == 0.0f)
        return;
    unsigned char using_item = java_call_entity_player_is_using_item(c->a, c->b);
    if (using_item && !data_2eda88)
        return;
    if (s->forward < 0.0f) {
        if (H0(!data_2edaa8))
            return;
    } else if (s->forward == 0.0f) {
        if (s->strafe != 0.0f) {
            if (H1(!data_2edac8))
                return;
        } else if (H2(!using_item))
            return;
    } else if (!using_item)
        return;
    s->speed = (float)(s->speed * 1.3);
    java_call_entity_set_sprinting(c->a, c->b, true);
}
