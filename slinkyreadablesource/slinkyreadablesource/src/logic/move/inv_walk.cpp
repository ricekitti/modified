// Inv Walk - keep moving while an inventory / container screen is open.
// Only the small hooks are recoded here; the big tick handler (inv_walk_tick_keep_movement)
//   +0x98: on disable restore the movement keys (0x39ff0)
//   0x39ce0: GUI click hook (mode "silent": queue clicks while the screen is closed)
//   0x39e30: tick: release queued input while a screen is open
#include "slinky/engine.h"

struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
struct GuiHook { char pad0[8]; void *a; void *screen_obj; void *screen; unsigned char handled; };

extern "C" char data_2ef3d0[];           // Inv Walk module descriptor
extern "C" unsigned data_2ef620;         // "mode"
extern "C" void *data_2fc680[0x20];      // queued clicks
extern "C" unsigned long long data_2fc780;   // queued count
extern "C" int data_2fc78c;              // tick counter
extern "C" int data_2fc790;              // state
extern "C" int data_2fc794;              // tick of last screen activity
extern "C" unsigned char data_2fc798;    // replaying queued clicks

typedef void *(*ScreenFn)(void *self, void *screen);

extern "C" unsigned data_2ef678;         // "stop_on_click"
extern "C" float data_2ef6b8;            // "stop_duration"
extern "C" unsigned data_2ef6f0;         // "stop_non_inv"
extern "C" unsigned char data_2fc788;    // keys released last tick
FN(void *, get_current_screen, (void *a, void *x));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_4, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_5, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_6, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_7, (void *a, void *x));
FN(void *, get_held_item, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_9, (void *a, void *x));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));
FN(unsigned char, java_jni_is_instance_of_gui_inventory, (void *a, void *screen));
FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(void *, java_call_minecraft_get_net_handler, (void *a, void *x));
FN(void *, java_get_net_handler_play_client_2, (void *a, void *x));
FN(void *, java_get_inventory_player, (void *a, void *inv));
FN(void *, java_get_entity_player, (void *a, void *p));
FN(void *, java_get_container_player, (void *a, void *x));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *inv, int slot));
FN(void *, java_new_c_init, (void *a, int x));
FN(void, java_call_entity_set_sprinting, (void *a, void *p, bool on));
FN(unsigned char, release_global_refs, (void *a, void *x));
FN(void *, java_jni_is_instance_of_c16_packet_client_status, (void *a, void *screen));
FN(int, java_get_c16_packet_client_status, (void *a, void *w));
FN(void *, java_jni_is_instance_of_c0_epacket_click_window, (void *a, void *screen));
FN(void *, java_get_static_init_2, (void *a));
FN(void *, java_new_c16_packet_client_status, (void *a, void *x));
FN(void, java_call_generic_future_listener, (void *a, void *obj, void *x));
FN(void *, java_jni_is_instance_of_c, (void *a, void *screen));
FN(unsigned char, keyboard_has_events, (void *a));
FN(unsigned char, keyboard_has_next, (void *a));

extern "C" void inv_walk_part(EvHook *h) {
    if (h->on || h->ev != data_2ef3d0)
        return;
    Ctx *c = h->ctx;
    release_global_refs(c->a, c->c18);
}

extern "C" void inv_walk_part_2(GuiHook *h) {
    if (__builtin_expect(data_2ef620 != 1, 0))
        goto end;
    unsigned long long n;
    void *w = java_jni_is_instance_of_c16_packet_client_status(h->a, h->screen);
    void *a = h->a;
    if (w) {
        if (java_get_c16_packet_client_status(a, w) == 2) {
            h->handled = 1;
            data_2fc790 = 2;
        }
        goto end;
    }
    if (java_jni_is_instance_of_c0_epacket_click_window(a, h->screen)) {
        if (data_2fc790 == 2) {
            java_call_generic_future_listener(h->a, h->screen_obj, java_new_c16_packet_client_status(h->a, java_get_static_init_2(h->a)));
            data_2fc790 = 3;
            data_2fc798 = 1;
            n = data_2fc780;
            goto queue;
        }
        goto check;
    }
    if (!java_jni_is_instance_of_c(h->a, h->screen))
        goto end;
    if (data_2fc790 == 2)
        goto mark;
    n = data_2fc780;
    if (n)
        goto queue;
    goto end;
check:
    n = data_2fc780;
    if (!(((n != 0) | data_2fc798) & 1)) {
        data_2fc794 = data_2fc78c;
        goto end;
    }
queue:
    if (n != 0x20) {
        void *obj = ((void **)h->a)[1];
        void *r = ((ScreenFn)(*(void ***)obj)[0xa8 / 8])(obj, h->screen);
        if (r) {
            unsigned long long k = data_2fc780;
            data_2fc680[k] = r;
            data_2fc780 = k + 1;
        }
    }
mark:
    h->handled = 1;
end:

}

extern "C" void inv_walk_part_3(Hook *h) {
    if (__builtin_expect(data_2fc798, 0))
        goto end;
    Ctx *c = h->ctx;
    if (!release_global_refs(c->a, c->c18))
        goto end;
    data_2fc794 = data_2fc78c;
    if (c->f40 != 1)
        goto end;
    while (keyboard_has_events(c->a))
        ;
    while (keyboard_has_next(c->a))
        ;
end:

}

// tick: keep the movement keys pressed while a screen is open ("mode" 0 =
// vanilla screens only, 1 = silent / any screen), stopping on clicks
extern "C" void inv_walk_tick_keep_movement(Hook *h) {
    Ctx *c = h->ctx;
    void *scr = get_current_screen(c->a, c->c18);
    void *obj = get_net_handler(c->a, c->c18);
    void *back = java_get_game_settings_key_bindings_5(c->a, obj);
    void *left = java_get_game_settings_key_bindings_6(c->a, obj);
    void *right = java_get_game_settings_key_bindings_7(c->a, obj);
    void *fwd = java_get_game_settings_key_bindings_4(c->a, obj);
    void *jump = get_held_item(c->a, obj);
    unsigned char kb = java_get_key_binding_key_code(c->a, back);
    unsigned char kl = java_get_key_binding_key_code(c->a, left);
    unsigned char kr = java_get_key_binding_key_code(c->a, right);
    unsigned char kf = java_get_key_binding_key_code(c->a, fwd);
    unsigned char kj = java_get_key_binding_key_code(c->a, jump);
    unsigned soc = data_2ef678;
    unsigned char queued = (data_2ef620 == 1 && soc != 2) ? data_2fc780 != 0 : 0;
    unsigned char release = (soc == 0) & queued;
    unsigned char act;
    data_2fc78c += 1;
    if (!scr) {
        if (data_2fc790 != 0) {
            java_set_key_binding_pressed(c->a, back, kb);
            java_set_key_binding_pressed(c->a, left, kl);
            java_set_key_binding_pressed(c->a, right, kr);
            java_set_key_binding_pressed(c->a, fwd, kf);
            java_set_key_binding_pressed(c->a, jump, kj);
        }
        data_2fc790 = 0;
        if (!c->f40)
            return;
        act = 0;
        goto apply;
    }
    {
        unsigned char pressed = kb | kl | kr | kf | kj;
        unsigned char busy;
        if (!java_jni_is_instance_of_gui_inventory(c->a, scr)) {
            data_2fc790 = 1;
            if (data_2ef620 == 1) {
                queued |= pressed ^ 1;
                unsigned char recent = 0;
                if (soc != 2)
                    recent = (unsigned)(data_2fc78c - data_2fc794) < (unsigned)(long long)data_2ef6b8;
                unsigned char stop = queued | recent;
                unsigned char other = (data_2ef6f0 != 2) | stop;
                queued = other;
                if (((soc == 0) & stop) || ((data_2ef6f0 == 0) & other))
                    goto release_all;
            } else if (release) {
                goto release_all;
            }
            goto physical;
        }
        if (data_2ef620 != 1) {
            data_2fc790 = 3;
            act = 1;
            busy = 0;
            goto recompute;
        }
        {
            void *inv = java_get_entity_player_2(c->a, c->b);
            void *cont = java_get_net_handler_play_client_2(c->a, java_call_minecraft_get_net_handler(c->a, c->c18));
            busy = 1;
            if (!java_get_inventory_player(c->a, inv)) {
                if (data_2fc790 == 1) {
                    busy = queued;
                } else {
                    void *grid = java_get_container_player(c->a, java_get_entity_player(c->a, c->b));
                    if (!java_call_iinventory_get_stack_in_slot(c->a, grid, 0) && !java_call_iinventory_get_stack_in_slot(c->a, grid, 1) &&
                        !java_call_iinventory_get_stack_in_slot(c->a, grid, 2) && !java_call_iinventory_get_stack_in_slot(c->a, grid, 3))
                        busy = queued;
                }
            }
            pressed &= busy ^ 1;
            int st = data_2fc790;
            if (st == 2 && !pressed) {
                void *x = java_new_c16_packet_client_status(c->a, java_get_static_init_2(c->a));
                java_call_generic_future_listener(c->a, cont, x);
                data_2fc790 = st = 3;
            } else if (pressed & (data_2fc780 == 0) & (st == 3)) {
                if (soc == 2 || !((unsigned)(data_2fc78c - data_2fc794) < (unsigned)(long long)data_2ef6b8)) {
                    void *x = java_new_c_init(c->a, 0);
                    java_call_generic_future_listener(c->a, cont, x);
                    data_2fc790 = st = 2;
                }
            }
            busy |= st == 3;
            act = st != 0;
        }
recompute:
        queued |= (soc != 2) & busy;
        release = (soc == 0) & queued;
    }
apply:
    if (!((act | data_2fc788 | release) & 1))
        goto done;
    if (!release)
        goto physical;
release_all:
    java_set_key_binding_pressed(c->a, back, 0);
    java_set_key_binding_pressed(c->a, left, 0);
    java_set_key_binding_pressed(c->a, right, 0);
    java_set_key_binding_pressed(c->a, fwd, 0);
    java_set_key_binding_pressed(c->a, jump, 0);
    release = 1;
    goto done;
physical:
    java_set_key_binding_pressed(c->a, back, kb);
    java_set_key_binding_pressed(c->a, left, kl);
    java_set_key_binding_pressed(c->a, right, kr);
    java_set_key_binding_pressed(c->a, fwd, kf);
    java_set_key_binding_pressed(c->a, jump, kj);
    release = 0;
done:
    if (queued) {
        java_call_entity_set_sprinting(c->a, c->b, false);
        void *a = c->a;
        java_set_key_binding_pressed(a, java_get_game_settings_key_bindings_9(a, obj), 0);
    }
    data_2fc788 = release;
    data_2fc798 = 0;
}
