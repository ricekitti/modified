// Client start-up, hook control and shutdown.
//
// The original runs this from protector-virtualised code (see docs/init.md); the bodies could not be translated,
// so this file is NEW code written from what the rest of the client expects to find when it runs: every global the
// readable code reads but nothing readable writes is created here, in the order its dependencies imply, using the
// readable helpers (fn_*) the original left behind for exactly these steps. The JNI/JVMTI call sequences are those
// of the readable counterparts (install_class_hooks installs, remove_class_hooks removes). Anything inferred is marked "inferred".
// Licence checking is not part of this client.
#include "slinky/engine.h"
#include "slinky/jids.h"
#include "slinky/platform.h"
#include <stdarg.h>

// ---- state created here (the data_* globals of the image; all read elsewhere) ----
struct VmTable {   // JNIInvokeInterface
    void *reserved[3];
    int (*DestroyJavaVM)(void *vm);
    int (*AttachCurrentThread)(void *vm, void **env, void *args);
    int (*DetachCurrentThread)(void *vm);
    int (*GetEnv)(void *vm, void **env, int version);
};
struct KeyMap {   // key -> items map (see gui/e_e.cpp: key_map_add / key_map_remove / key_map_get / key_map_dispatch / key_map_destroy)
    void (*handler)(void *);   // fn(item, key, down), called by the input handler
    void (*release)(void *);   // called on an item when it is replaced or removed
    unsigned long long count, cap;
    void *data;
    unsigned char multi;       // 1: several items may share a key
    char pad[7];
};
struct LoaderRef { jobject loader; jmethodID loadClass; };
struct HClass { jclass cls; jmethodID ctor; };
extern "C" void *data_2f6ed8;               // our module handle
extern "C" unsigned char data_2f7259;       // client shutting down: the natives check this and go inert
extern "C" VmTable **data_2f6ee0;           // JavaVM*
extern "C" JIds *data_2f6ef0;               // cached JNI IDs
extern "C" char data_2f6f08[];              // module manager (0x310 bytes, in place)
extern "C" unsigned char *data_2f7218;      // key state table, one byte per client key code
extern "C" KeyMap *data_2f7230;             // profile key binds: key -> profile name
extern "C" KeyMap *data_2f7238;             // module key binds: key -> module
extern "C" void *data_2f7240;               // profile list
extern "C" HClass data_2f7248;              // SlinkyChannelHandler class + constructor
extern "C" bool (*data_304ba8)(void *event);   // input handler hooked into the game window (protected/input.cpp)
extern "C" const char rdata_13ec08[], rdata_13ec10[], rdata_13ec47[], rdata_13ec52[], rdata_13ec60[];   // ".slinky", "settings", "name", "profile_binds", "bind"

// ---- readable engine functions used below ----
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));   // calloc
FN(void, libc_free, (void *p));                                             // free
FN(int, libc_vsnprintf, (char *buf, unsigned long long n, const char *fmt, va_list ap));   // vsnprintf
FN(int, get_java_vm, (void **vm));                                              // JNI_GetCreatedJavaVMs
FN(int, init_jni_ids, (JIds *ids, JNIEnv *env, LoaderRef *out));                // JNI bootstrap: loader, mapping, all IDs
FN(void, release_jni_ids, (JIds *ids, JNIEnv *env));                               // release the cached class refs
FN(int, create_module_manager, (void *mgr, void (*setter)(void *mod, int key)));         // module manager from the static module list
extern "C" bool source_features_initialize(Jvm*);
FN(void, destroy_module_manager, (void *mgr));                                            // ... and its destructor
FN(unsigned char, queue_module_toggle, (void *mgr, void *mod, unsigned char on));      // queue a module enable/disable
extern "C" void enable_startup_modules(void *manager);
FN(void, set_module_bind, (void *mod, int key));                                    // set a module's key bind
FN(unsigned char, on_profile_bind, (const char *name, int key, bool down));        // profile key pressed
FN(unsigned char, on_module_bind, (char *mod, int key, bool down));               // module key pressed
FN(void, bind_profile_to_key, (char *profile, int key));                                // bind a profile to a key
FN(int, load_profiles, (void *profiles));                                         // (re)read the profile folder
FN(void, key_map_destroy, (void *map));                                             // destroy a key map
FN(void, destroy_profiles, (void *profiles));                                        // free the profile list
FN(void *, find_profile, (void *profiles, const char *name));                    // profile entry by name
FN(int, init_chat_commands, ());                                                       // built-in chat commands
FN(void, destroy_chat_commands, ());                                                      // ... and their free
FN(int, init_glfw_entrypoints, ());                                                       // GLFW entry points (0x3e8: no glfw)
FN(int, install_packet_channel, (Jvm *j, jobject loader, HClass *out, void *onPacket));    // define SlinkyChannelHandler
FN(void, release_packet_channel, (JNIEnv *env, jclass *cls));                              // ... and drop it
FN(int, on_packet_local, (JNIEnv *env, jobject, void *pkt, unsigned char out, long long time, int id)) __asm__("on_packet");
FN(bool, on_input_event, (void *event));                                           // the client's input handler
FN(int, install_class_hooks, (Jvm *j, void **jvmti));                                  // install the class hooks
FN(int, remove_class_hooks, (JNIEnv *env, void **jvmti));                             // remove them again
FN(void, clear_saved_string, ());                                                      // free the current profile name
FN(void, free_allocator_pages, ());                                                     // free the block-search mesh cache
FN(int, settings_path, (char **path));                                           // %USERPROFILE%
FN(int, settings_path_append, (char **path, const char *suffix));                       // append "\suffix"
FN(int, read_text_file, (char **path, char **data));                              // read a whole file as text
FN(void, free_path, (char **path));
FN(unsigned char, load_module_settings, (void *root, void *mods, void *mod));            // apply the saved settings of one module
FN(void *, cjson_parse, (const char *text));                                   // cJSON_Parse
FN(void *, cjson_get_object_item, (const void *o, const char *name));                    // cJSON_GetObjectItemCaseSensitive
FN(char *, cjson_get_string_value, (const void *o));                                      // cJSON_GetStringValue
FN(void, cjson_delete, (void *o));                                              // cJSON_Delete

// ---- random numbers ----
// xoshiro128+ state used by math/e_a.cpp. The image leaves it zeroed and its virtualised start-up (not
// recovered) seeded it. With an all-zero state rng_next() always returns 0, so walk_randomize's
// `while (epsilon > |a - b|)` and gauss_random's `while (s >= 1)` rejection loops never terminate: the game
// freezes the first time a module uses a random walk (Aim Assist, Clutch, ...). Seed it from the TSC.
extern "C" unsigned data_2f7320[4];
static void seed_rng() {
    unsigned long long x = (unsigned long long)__builtin_ia32_rdtsc();
    x ^= reinterpret_cast<unsigned long long>(&data_2f7320) << 17;
    unsigned long long acc = 0;
    for (int i = 0; i < 4; i++) {
        x += 0x9e3779b97f4a7c15ULL;                    // splitmix64
        unsigned long long z = x;
        z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9ULL;
        z = (z ^ (z >> 27)) * 0x94d049bb133111ebULL;
        z ^= z >> 31;
        data_2f7320[i] = (unsigned)z;
        acc |= z;
    }
    if (!acc) data_2f7320[0] = 0x9e3779b9;             // xoshiro must not be all zero
}

// ---- logging ----
extern "C" void slinky_logf(const char *fmt, ...) {
    char line[512];
    va_list ap;
    va_start(ap, fmt);
    libc_vsnprintf(line, sizeof line, fmt, ap);
    va_end(ap);
    slinky_log_write(line);
}

// ---- JVM / JVMTI access ----
enum { JNI_VERSION_8 = 0x00010008, JVMTI_VERSION_1_2 = 0x30010200 };
static JNIEnv *attach(bool *attached) {
    VmTable **vm = data_2f6ee0;
    void *env = 0;
    *attached = false;
    if ((*vm)->GetEnv(vm, &env, JNI_VERSION_8) == 0) return (JNIEnv *)env;   // already attached (the game's thread)
    if ((*vm)->AttachCurrentThread(vm, &env, 0) != 0) return 0;
    *attached = true;
    return (JNIEnv *)env;
}
static void detach(bool attached) {
    if (attached) (*data_2f6ee0)->DetachCurrentThread(data_2f6ee0);
}
// turn the class hooks on (1) or off (0); 0 or the error code of the step that failed
// GetEnv(..., JVMTI_VERSION_1_2) makes the VM create a NEW JVMTI environment on every call
// (JvmtiExport::get_jvmti_interface -> JvmtiEnv::create_a_jvmti). A fresh environment has neither the
// capabilities the install added nor the event callbacks nor the enabled ClassFileLoadHook event, so a
// disarm on a re-fetched environment does nothing and RetransformClasses fails with
// JVMTI_ERROR_MUST_POSSESS_CAPABILITY (99). The original keeps one environment; reuse the cached one.
static void **g_jvmti;
static int hooks_set(JNIEnv *env, bool enable) {
    void **jvmti;
    int err;
    if (g_jvmti) {
        jvmti = g_jvmti;
    } else {
        err = (*data_2f6ee0)->GetEnv(data_2f6ee0, (void **)&jvmti, JVMTI_VERSION_1_2);
        if (err) return 12000 + err;
    }
    Jvm j = {data_2f6ef0, env};
    err = enable ? install_class_hooks(&j, jvmti) : remove_class_hooks(env, jvmti);
    if (enable && !err) g_jvmti = jvmti;
    return err;
}
// set_java_hooks(env, enable): called by the unload sequence (base/e_7.cpp, hook_update_display) with enable = 0.
// Returns the error so the unload can be aborted while every global is still valid: if the Java
// hooks cannot be removed, tearing the client down and unloading the DLL while the game keeps
// calling into it is a guaranteed use-after-free (see the serialize_settings crash on unload).
extern "C" int set_java_hooks(JNIEnv *env, int enable) {
    int err = hooks_set(env, enable != 0);
    if (err) slinky_logf(enable ? "Initialization failed: %u" : "Disabling hooks failed: %u", err);
    return err;
}

// ---- the saved global settings: ~/.slinky/settings (written by save_settings) ----
// inferred: the counterpart of serialize_settings (save). Module settings of the global modules (flags & 0x10) go through the
// per-module loader load_module_settings; "profile_binds" is a list of {name, bind} that rebinds the profiles.
struct Json { Json *next, *prev, *child; int type; char *valuestring; int valueint; double valuedouble; char *string; };
struct Mods { struct Mod { char pad0[0x14]; unsigned char enabled; char pad1[3]; unsigned flags; } **data; unsigned long long count; };
static void load_settings() {
    char *path;
    if (settings_path(&path)) return;
    char *text = 0;
    if (!settings_path_append(&path, rdata_13ec08) && !settings_path_append(&path, rdata_13ec10) && !read_text_file(&path, &text) && text) {
        Json *root = (Json *)cjson_parse(text);
        if (root) {
            Mods *mods = (Mods *)data_2f6f08;
            for (unsigned long long i = 0; i < mods->count; i++)
                if (mods->data[i]->flags & 0x10) load_module_settings(root, mods, mods->data[i]);
            Json *binds = (Json *)cjson_get_object_item(root, rdata_13ec52);
            for (Json *b = binds ? binds->child : 0; b; b = b->next) {
                const char *name = cjson_get_string_value(cjson_get_object_item(b, rdata_13ec47));
                Json *key = (Json *)cjson_get_object_item(b, rdata_13ec60);
                char *profile = name ? (char *)find_profile(data_2f7240, name) : 0;
                if (profile && key) bind_profile_to_key(profile, (int)key->valuedouble);
            }
            cjson_delete(root);
        }
        libc_free(text);
    }
    free_path(&path);
}

// ---- start-up ----
static void *calloc1(unsigned long long size) { return libc_calloc(1, size); }
static KeyMap *new_key_map(void (*handler)(void *), void (*release)(void *), bool multi) {
    KeyMap *m = (KeyMap *)calloc1(sizeof(KeyMap));
    if (!m) return 0;
    m->handler = handler;
    m->release = release;
    m->multi = multi;
    return m;
}
enum { ERR_NO_MEMORY = 0x3ed, ERR_NO_ENV = 0x7d3 };
static int setup(JNIEnv *env) {
    int err;
    seed_rng();
    // 1. the JNI ID table: game class loader, mapping detection (1.7.10 / 1.8.9, obf / MCP / SRG), all IDs
    JIds *ids = (JIds *)calloc1(sizeof(JIds));
    if (!ids) return ERR_NO_MEMORY;
    data_2f6ef0 = ids;
    LoaderRef secure;   // SecureClassLoader child of the game loader (the game loader itself is ids + 0x10)
    if ((err = init_jni_ids(ids, env, &secure))) return err;
    Jvm j = {ids, env};
    source_features_initialize(&j);
    jobject game_loader = (jobject)((char *)ids + 0x10);

    // 2. state the frame code and the input handler read
    if ((err = create_module_manager(data_2f6f08, set_module_bind))) return err;           // module manager, key-bind setter = set_module_bind
    if (!(data_2f7218 = (unsigned char *)calloc1(0x100))) return ERR_NO_MEMORY;
    if (!(data_2f7230 = new_key_map((void (*)(void *))on_profile_bind, libc_free, false))) return ERR_NO_MEMORY;
    if (!(data_2f7238 = new_key_map((void (*)(void *))on_module_bind, 0, true))) return ERR_NO_MEMORY;
    if (!(data_2f7240 = calloc1(0x20))) return ERR_NO_MEMORY;
    *(void **)((char *)data_2f7240 + 0x18) = (void *)bind_profile_to_key;          // profile bind button setter
    if ((err = load_profiles(data_2f7240))) return err;                      // profiles found in ~/.slinky/profiles
    if ((err = init_chat_commands())) return err;                                 // chat commands
    init_glfw_entrypoints();                                                         // GLFW entry points (absent on LWJGL 2: not an error)
    load_settings();                                                   // global settings, profile binds, modules' own state
    // Modules with flags bit 1 (Notifications, Auto Rod, Block In, Weapons) have no toggle and are
    // never read from the settings file; they are on for the whole session. queue_module_toggle only queues the
    // enable, apply_module_toggles applies it on the first frame.
    enable_startup_modules(data_2f6f08);
    data_304ba8 = on_input_event;                                             // window hook (installed by the frame code, hook_game_window)

    // 3. Java side: the netty packet tap, then the hooks that make the game call into the client
    if ((err = install_packet_channel(&j, game_loader, &data_2f7248, (void *)on_packet_local))) return err;
    return hooks_set(env, true);
}

// Entry of the start-up thread. Returns 0 or the error that stopped it (reported; the caller then unloads).
extern "C" int slinky_setup(void *self) {
    data_2f6ed8 = self;
    int err = get_java_vm((void **)&data_2f6ee0);
    if (!err) {
        bool attached;
        JNIEnv *env = attach(&attached);
        err = env ? setup(env) : ERR_NO_ENV;
        detach(attached);
    }
    if (err) slinky_logf("Initialization failed: %u", err);
    return err;
}

// ---- shutdown: everything slinky_setup built, once the hooks are gone (runs on the unload thread) ----
extern "C" void stasis_cleanup(JNIEnv *env);
extern "C" void displace_cleanup(JNIEnv *env);
extern "C" void anti_fireball_cleanup(JNIEnv *env);
extern "C" void fireball_pearl_cleanup(JNIEnv *env);
extern "C" void slinky_teardown(void) {
    // from here the packet tap and every Java native are inert: they check data_2f7259 first, and the
    // frees below must not race one (this covers the start-up failure path too, where hook_update_display never ran)
    data_2f7259 = 1;
    bool attached;
    JNIEnv *env = attach(&attached);
    if (env) {
        stasis_cleanup(env);
        displace_cleanup(env);
        anti_fireball_cleanup(env);
        fireball_pearl_cleanup(env);
        if (data_2f7248.cls) release_packet_channel(env, &data_2f7248.cls);
        if (data_2f6ef0) release_jni_ids(data_2f6ef0, env);
    }
    detach(attached);
    destroy_module_manager(data_2f6f08);
    key_map_destroy(data_2f7230);
    key_map_destroy(data_2f7238);
    destroy_profiles(data_2f7240);
    destroy_chat_commands();
    clear_saved_string();
    free_allocator_pages();
    libc_free(data_2f7218);
    libc_free(data_2f6ef0);
    data_2f7230 = data_2f7238 = 0;
    data_2f7240 = 0;
    data_2f7218 = 0;
    data_2f6ef0 = 0;
}
