#include "slinky/module_toggle.h"
#include <iostream>
#include <stdexcept>

extern "C" { ModuleView data_2eb740{}; }
static int guiOpened;
static int enabledCallbacks, disabledCallbacks, released;
static void require(bool condition, const char* text) { if (!condition) throw std::runtime_error(text); }
static void onToggle(void* event) {
    auto* e = static_cast<ModuleToggleEvent*>(event);
    if (e->mod == &data_2eb740) return;
    if (e->on) {
        // This models Block In's enable-time player read, which used to crash at the main menu.
        require(e->ctx->a && e->ctx->world && e->ctx->player, "Enable callback received a null world/player");
        ++enabledCallbacks;
    } else ++disabledCallbacks;
}
static void onGui(void* event) {
    auto* e = static_cast<ModuleToggleEvent*>(event);
    if (e->mod != &data_2eb740 || !e->on) return;
    require(e->ctx->a && e->ctx->minecraft, "Menu GUI needs Minecraft context");
    ++guiOpened;
}
static void tick(void*) {}
extern "C" void release_owner(void*) { ++released; }

int main() {
    try {
        ModuleView module{};
        module.h[1].f = tick;
        module.h[5].f = onToggle;
        ModuleManagerView manager{};
        ModuleView* registered[] = {&module};
        Handler handlers[27][2]{};
        for (unsigned i = 0; i < 27; ++i) {
            manager.reg[i] = {registered, module.h[i].f ? 1ull : 0ull};
            manager.lists[i] = handlers[i];
        }
        ModuleView* enables[] = {&module};
        ModuleView* disables[] = {&module};
        manager.en_list = enables; manager.en_count = 1;
        manager.dis_list = disables;
        int java, world, player;
        ModuleToggleContext context{&java, &manager, nullptr, nullptr, nullptr, nullptr};
        apply_module_toggles(&context);
        require(manager.en_count == 1 && !module.enabled && enabledCallbacks == 0, "Main-menu enable was not deferred");
        context.world = &world;
        apply_module_toggles(&context);
        require(manager.en_count == 1 && enabledCallbacks == 0, "Missing-player enable was not deferred");
        context.world = nullptr; context.player = &player;
        apply_module_toggles(&context);
        require(manager.en_count == 1 && enabledCallbacks == 0, "Missing-world enable was not deferred");
        context.world = &world;
        apply_module_toggles(&context);
        require(manager.en_count == 0 && module.enabled && enabledCallbacks == 1, "Join did not apply pending enable exactly once");
        require(manager.lists[1][0] == tick && manager.lists[5][0] == onToggle, "Enabled handlers were not rebuilt");
        context.world = context.player = nullptr;
        manager.dis_count = 1;
        apply_module_toggles(&context);
        require(!module.enabled && manager.dis_count == 0 && disabledCallbacks == 1 && released == 1, "Disconnect prevented cleanup");
        require(manager.lists[1][0] == nullptr && manager.lists[5][0] == nullptr, "Disabled handlers were retained");
        apply_module_toggles(&context);
        require(enabledCallbacks == 1 && disabledCallbacks == 1, "Callbacks repeated on idle frame");
        // A real menu GUI request must work without enabling a queued gameplay module.
        ModuleView& gui = data_2eb740;
        gui.h[5].f = onGui;
        gui.h[9].f = tick;
        ModuleManagerView menuManager{};
        ModuleView* menuRegistered[] = {&module, &gui};
        Handler menuHandlers[27][3]{};
        for (unsigned i = 0; i < 27; ++i) {
            menuManager.reg[i] = {menuRegistered, 2};
            menuManager.lists[i] = menuHandlers[i];
        }
        ModuleView* menuEnables[] = {&module, &gui};
        ModuleView* menuDisables[] = {&gui};
        menuManager.en_list = menuEnables; menuManager.en_count = 2;
        menuManager.dis_list = menuDisables;
        int minecraft;
        ModuleToggleContext menuContext{&java, &menuManager, nullptr, &minecraft, nullptr, nullptr};
        apply_module_toggles(&menuContext);
        require(gui.enabled && guiOpened == 1 && !module.enabled && menuManager.en_count == 1,
            "Title screen must open only GUI and preserve gameplay enables");
        require(menuManager.en_list[0] == &module && menuManager.lists[9][0] == tick,
            "Pending gameplay queue and menu GUI draw handler must remain intact");
        apply_module_toggles(&menuContext);
        require(guiOpened == 1 && enabledCallbacks == 1, "Menu callback repeated or gameplay enabled early");
        menuContext.world = &world; menuContext.player = &player;
        apply_module_toggles(&menuContext);
        require(module.enabled && enabledCallbacks == 2 && menuManager.en_count == 0 && guiOpened == 1,
            "Joining must consume gameplay enable once without reopening GUI");
        menuContext.world = menuContext.player = nullptr;
        menuManager.dis_count = 1;
        apply_module_toggles(&menuContext);
        require(!gui.enabled && menuManager.dis_count == 0, "GUI must close safely back at menu");
        std::cout << "PASS: main-menu enables deferred, join initializes once, disconnect still disables and cleans up.\n";
        return 0;
    } catch (const std::exception& error) { std::cerr << error.what() << '\n'; return 1; }
}
