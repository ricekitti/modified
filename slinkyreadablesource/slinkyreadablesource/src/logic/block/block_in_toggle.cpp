// Block In lifecycle runs during profile restoration and disconnect as well as in-game.
#include "slinky/engine.h"
struct Rot { float yaw, pitch; };
struct EvHook { char pad0[8]; Ctx* ctx; void* ev; unsigned char on; };
extern "C" char data_2f6108[];
extern "C" void* data_304b10;
extern "C" int data_304b18, data_304b20;
extern "C" unsigned char data_304b1c;
extern "C" const float rdata_156870;
FN(Rot, get_entity_rotation, (void*, void*));
FN(float, libc_roundf, (float));
FN(void*, get_net_handler, (void*, void*));
FN(void*, java_get_game_settings_key_bindings_4, (void*, void*));
FN(void*, java_get_game_settings_key_bindings_5, (void*, void*));
FN(void*, java_get_game_settings_key_bindings_6, (void*, void*));
FN(void*, java_get_game_settings_key_bindings_7, (void*, void*));
FN(unsigned char, java_get_key_binding_key_code, (void*, void*));
FN(void, java_set_key_binding_pressed, (void*, void*, unsigned char));

extern "C" void block_in_enable_remember_facing(EvHook* h) {
    if (!h || h->ev != data_2f6108) return;
    data_304b10 = nullptr;
    data_304b1c = 0;
    data_304b20 = 0;
    Ctx* c = h->ctx;
    if (h->on == 1) {
        data_304b18 = 0;
        // Pending enables are normally deferred by the manager until join.
        // Never pass a missing player to JNI, including a direct lifecycle call.
        if (!c || !c->a || !c->p20 || !c->b) return;
        int q = (int)libc_roundf(get_entity_rotation(c->a, c->b).yaw / rdata_156870);
        data_304b18 = q % 4;
        return;
    }
    // Disabling after disconnect still clears state. Restore physical key state
    // only when the Minecraft settings object remains available.
    if (!c || !c->a || !c->c18) return;
    void* settings = get_net_handler(c->a, c->c18);
    if (!settings) return;
    void* keys[] = {
        java_get_game_settings_key_bindings_4(c->a, settings),
        java_get_game_settings_key_bindings_5(c->a, settings),
        java_get_game_settings_key_bindings_6(c->a, settings),
        java_get_game_settings_key_bindings_7(c->a, settings)
    };
    for (void* key : keys)
        if (key) java_set_key_binding_pressed(c->a, key, java_get_key_binding_key_code(c->a, key));
}

