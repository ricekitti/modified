#include "slinky/module_toggle.h"

// apply pending enables/disables: rebuild the handler lists of the affected event types, fire event 5
extern "C" void release_owner(void *timer);
extern "C" char data_2eb740[]; // UI-only descriptor: safe to open on the title screen.
static bool can_enable(ModuleToggleContext* c, ModuleView* mod) {
    return c->a && ((c->world && c->player) ||
        (c->minecraft && reinterpret_cast<void*>(mod) == data_2eb740));
}
static inline unsigned long long handler_mask(ModuleView *m) {
    unsigned long long mask = 0;
    for (unsigned t = 0; t < 27; t++)
        if (m->h[t].f) mask |= 1u << t;
    return mask;
}
static inline void rebuild(ModuleManagerView *m, unsigned long long mask) {
    for (unsigned long long t = 0; t != 27; t++) {
        if (!((mask >> t) & 1)) continue;
        Handler *out = m->lists[t];
        ModuleRegistryView *r = &m->reg[t];
        for (unsigned long long i = 0; i < r->n; i++) {
            ModuleView *mod = r->list[i];
            if (mod->enabled == 1 && mod->h[t].f) *out++ = mod->h[t].f;
        }
        *out = 0;
    }
}
extern "C" void apply_module_toggles(ModuleToggleContext *c) {
    if (!c || !c->mgr) return;
    ModuleManagerView *m = c->mgr;
    // Saved and always-on modules can be queued while the client is still at its main menu.
    // Their enable handlers require real world/player objects; leave the queue pending until then.
    // Disables below must still run after disconnect so modules can release state.
    if (m->en_count) {
        const unsigned long long queued = m->en_count;
        unsigned long long mask = 0;
        for (unsigned long long i = 0; i != queued; i++) {
            ModuleView *mod = m->en_list[i];
            if (!can_enable(c, mod)) continue;
            mod->enabled = 1;
            mask |= handler_mask(mod);
        }
        rebuild(m, mask);
        for (unsigned long long i = 0; i < queued; i++) {
            if (!can_enable(c, m->en_list[i])) continue;
            ModuleToggleEvent e = {5, c, m->en_list[i], 1, {}};
            Handler *h = c->mgr->lists[5];
            for (Handler f = *h; f; f = *++h) f(&e);
        }
        // Preserve deferred gameplay enables and any new requests queued by a
        // callback, while consuming the GUI's enable exactly once.
        unsigned long long pending = 0;
        for (unsigned long long i = 0; i < queued; ++i)
            if (!can_enable(c, m->en_list[i])) m->en_list[pending++] = m->en_list[i];
        const unsigned long long total = m->en_count;
        for (unsigned long long i = queued; i < total; ++i)
            m->en_list[pending++] = m->en_list[i];
        m->en_count = pending;
        m = c->mgr;
    }
    if (m->dis_count) {
        unsigned long long mask = 0;
        for (unsigned long long i = 0; i != m->dis_count; i++) {
            ModuleView *mod = m->dis_list[i];
            mod->enabled = 0;
            mask |= handler_mask(mod);
        }
        for (unsigned long long i = 0; i < m->dis_count; i++) {
            ModuleView *mod = m->dis_list[i];
            ModuleToggleEvent e = {5, c, mod, 0, {}};
            Handler *h = c->mgr->lists[5];
            for (Handler f = *h; f; f = *++h) f(&e);
            release_owner(mod);
        }
        rebuild(m, mask);
        m->dis_count = 0;
    }
}

