// 0x39000: release queued global refs (optionally handing them to the world first)
#include "slinky/engine.h"
#include "slinky/jids.h"
extern "C" unsigned long long data_2fc780;
extern "C" jobject data_2fc680[];
FN(void *, java_call_minecraft_get_net_handler, (void *a));
FN(void *, java_get_net_handler_play_client_2, (void *a, void *x));
FN(void, java_call_generic_future_listener, (void *a, void *obj, void *x));
extern "C" bool release_global_refs(Jvm *j) {
    if (data_2fc780 == 0) return false;
    JNIEnv *env = j->env;
    void *x = java_call_minecraft_get_net_handler(j);
    void *y = java_get_net_handler_play_client_2(j, x);
    bool ok = (x != 0) & (y != 0);
    if (ok) {
        for (unsigned long long i = 0; i < data_2fc780; i++) {
            jobject o = data_2fc680[i];
            java_call_generic_future_listener(j, y, o);
            (*env)->DeleteGlobalRef(env, o);
        }
    } else {
        for (unsigned long long i = 0; i < data_2fc780; i++) (*env)->DeleteGlobalRef(env, data_2fc680[i]);
    }
    data_2fc780 = 0;
    return ok;
}
