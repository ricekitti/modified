#include <stddef.h>

struct StartupModule {
    char header[0x14];
    unsigned char enabled;
    char pad[3];
    unsigned flags;
};
struct StartupModules { StartupModule** data; unsigned long long count; };
static_assert(offsetof(StartupModule, flags) == 0x18, "Module flags ABI");
extern "C" char data_2eb740[]; // GUI descriptor. Its persistent settings exclude toggle state.
extern "C" bool queue_module_toggle(void*, void*, unsigned char);

extern "C" void enable_startup_modules(void* manager) {
    auto* modules = static_cast<StartupModules*>(manager);
    if (!modules) return;
    for (unsigned long long i = 0; i < modules->count; ++i) {
        auto* module = modules->data[i];
        // The GUI shares the non-persistent-state flag with always-on helpers,
        // but opening it is a user action, not session initialization.
        if (module && reinterpret_cast<void*>(module) != data_2eb740 &&
            (module->flags & 2) && module->enabled != 1)
            queue_module_toggle(manager, module, 1);
    }
}
