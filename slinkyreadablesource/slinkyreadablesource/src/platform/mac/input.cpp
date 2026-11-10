// macOS input layer: the counterpart of src/engine/protected/input.cpp (Win32).
//
// WHAT WINDOWS DOES
//   The Windows layer subclasses the game's HWND (hook_game_window -> SetWindowLongPtr) so Win32 delivers
//   every WM_* message to game_window_proc. That proc turns each message into an InputEvent and hands it to
//   the client's handler (data_304ba8 == on_input_event, in base/java_natives_world.cpp); when the handler
//   returns true the message is swallowed and never reaches the game. hook_update_display() (the per-frame
//   native) calls hook_game_window() every frame, but the subclass installs once and the events then arrive
//   asynchronously on the window thread.
//
// WHY MAC IS DIFFERENT
//   Cocoa has no WndProc to subclass, and an OS-level CGEventTap that intercepts keys/clicks needs an
//   Accessibility grant (a user prompt) and would let us pre-empt the game -- a protection posture we are
//   told not to take. So this layer does NOT touch the OS. It reads LWJGL2's own polled input state through
//   JNI once per frame and emits the *changes* as InputEvents -- the exact same public Keyboard/Mouse state
//   Minecraft 1.8.9 itself reads every tick. No OS permission, no memory patching, no event injection, no
//   protection bypass. hook_game_window() is still called every frame by hook_update_display(); on Windows
//   that is an idempotent install, here it performs the per-frame poll (first call resolves the JNI handles).
//
// HONEST LIMITS OF A NON-DESTRUCTIVE POLL (vs the Windows message hook) -- all deferred to the Stage-2
// Java-side input-dispatch transform (installed through the client's existing JVMTI ClassFileLoadHook, the
// same mechanism as every other game hook, so still no OS/protection bypass):
//   * It cannot SWALLOW an event: the game has already read the same key/button this frame, so while the
//     client menu is open the game still sees the click/keystroke underneath. Fine for the load milestone
//     (open menu, move/click, close); matching the Windows "return true -> eaten" behaviour needs the
//     transform, which observes the events before the game's own dispatch.
//   * MOUSE WHEEL is omitted here: LWJGL2 Mouse.getDWheel() clears the accumulator on read, so reading it
//     would steal the game's hotbar/zoom scroll. Wheel arrives with the transform (which reads the event
//     stream the game is about to consume, not the shared accumulator).
//   * RAW mouse delta (IN_RAW / getDX/getDY) is omitted for the same destructive-read reason; it only feeds
//     combat rotation reads (Stage 2), not the menu.
//   * CHARACTER input is synthesised from the US layout + shift/caps so the client's own text fields work;
//     non-US layouts need the event-character stream the transform exposes.
#include "slinky/platform.h"

#include <jni.h>
#include <cstring>

// ---- InputEvent: byte-identical to the Windows definition (on_input_event / gui_part_2 read it by offset:
//      type@0, key.code@4, key.text@8, key.down@12, key.synthetic@13) ----
enum InputType { IN_MOVE = 1, IN_KEY = 2, IN_WHEEL = 3, IN_RAW = 4 };
struct InputEvent {
    int type;
    union {
        struct { int x, y; } move;                 // IN_MOVE: client-area position, y top-down
        struct {
            int code;                              // IN_KEY: client key code / mouse button (1 L, 2 R, 3 M, 4/5 X1/X2); 0 = character
            char text[4];                          //   character input: UTF-8 (code 0)
            unsigned char down;
            unsigned char synthetic;               //   posted by the auto clicker
            char pad[2];
        } key;
        struct { float delta; } wheel;             // IN_WHEEL: notches (unused on Mac for now; see header)
    };
};

extern "C" bool (*data_304ba8)(void *event);       // client input handler (on_input_event), installed by init.cpp
extern "C" JavaVM *data_2f6ee0;                    // the JavaVM the engine captured at start-up (base/java_natives_world.cpp)

// ---- LWJGL2 DirectInput scancode (org.lwjgl.input.Keyboard.KEY_*) -> client key code (0 = no client code) ----
// Client codes are the same space windows_key_to_client_key / glfw_key_to_client_key target. Keypad *,+ and the
// Cmd/Apps/Pause keys have no client code (as on Windows) and are left out so they map to 0.
struct KeyMap { unsigned short dik, client; };
static const KeyMap kKeyMap[] = {
    {0x01, 0x57},                                                                           // escape
    {0x02, 0x07}, {0x03, 0x08}, {0x04, 0x09}, {0x05, 0x0a}, {0x06, 0x0b},                    // 1-5
    {0x07, 0x0c}, {0x08, 0x0d}, {0x09, 0x0e}, {0x0a, 0x0f}, {0x0b, 0x06},                    // 6-9, 0
    {0x0c, 0x37}, {0x0d, 0x35}, {0x0e, 0x53}, {0x0f, 0x54},                                  // - = backspace tab
    {0x10, 0x2a}, {0x11, 0x30}, {0x12, 0x1e}, {0x13, 0x2b}, {0x14, 0x2d},                    // Q W E R T
    {0x15, 0x32}, {0x16, 0x2e}, {0x17, 0x22}, {0x18, 0x28}, {0x19, 0x29},                    // Y U I O P
    {0x1a, 0x3b}, {0x1b, 0x3d}, {0x1c, 0x55}, {0x1d, 0x64},                                  // [ ] enter lctrl
    {0x1e, 0x1a}, {0x1f, 0x2c}, {0x20, 0x1d}, {0x21, 0x1f}, {0x22, 0x20},                    // A S D F G
    {0x23, 0x21}, {0x24, 0x23}, {0x25, 0x24}, {0x26, 0x25},                                  // H J K L
    {0x27, 0x34}, {0x28, 0x3e}, {0x29, 0x3a}, {0x2a, 0x62}, {0x2b, 0x3c},                    // ; ' ` lshift backslash
    {0x2c, 0x33}, {0x2d, 0x31}, {0x2e, 0x1c}, {0x2f, 0x2f}, {0x30, 0x1b},                    // Z X C V B
    {0x31, 0x27}, {0x32, 0x26}, {0x33, 0x36}, {0x34, 0x38}, {0x35, 0x39},                    // N M , . /
    {0x36, 0x63}, {0x38, 0x66}, {0x39, 0x58}, {0x3a, 0x56},                                  // rshift lalt space caps
    {0x3b, 0x43}, {0x3c, 0x44}, {0x3d, 0x45}, {0x3e, 0x46}, {0x3f, 0x47},                    // F1-F5
    {0x40, 0x48}, {0x41, 0x49}, {0x42, 0x4a}, {0x43, 0x4b}, {0x44, 0x4c},                    // F6-F10
    {0x45, 0x60}, {0x46, 0x61},                                                              // numlock scroll-lock
    {0x47, 0x17}, {0x48, 0x18}, {0x49, 0x19}, {0x4a, 0x37},                                  // KP7 KP8 KP9 KP-
    {0x4b, 0x14}, {0x4c, 0x15}, {0x4d, 0x16},                                                // KP4 KP5 KP6
    {0x4f, 0x11}, {0x50, 0x12}, {0x51, 0x13}, {0x52, 0x10}, {0x53, 0x38},                    // KP1 KP2 KP3 KP0 KP.
    {0x57, 0x4d}, {0x58, 0x4e},                                                              // F11 F12
    {0x9c, 0x55}, {0x9d, 0x65}, {0xb5, 0x39}, {0xb7, 0x5d}, {0xb8, 0x67},                    // KP-enter rctrl KP/ prtscrn ralt
    {0xc7, 0x5c}, {0xc8, 0x40}, {0xc9, 0x59}, {0xcb, 0x3f}, {0xcd, 0x41},                    // home up pageup left right
    {0xcf, 0x5b}, {0xd0, 0x42}, {0xd1, 0x5a}, {0xd2, 0x5e}, {0xd3, 0x5f},                    // end down pagedown insert delete
};

// US-layout character for a key-down, so the client's text fields receive text (menu search, HUD editor, ...).
static char dik_to_char(unsigned dik, bool shift, bool caps) {
    if (dik >= 0x10 && dik <= 0x19) { const char *r = "qwertyuiop"; char c = r[dik - 0x10]; return (shift ^ caps) ? (char)(c - 32) : c; }
    if (dik >= 0x1e && dik <= 0x26) { const char *r = "asdfghjkl";  char c = r[dik - 0x1e]; return (shift ^ caps) ? (char)(c - 32) : c; }
    if (dik >= 0x2c && dik <= 0x32) { const char *r = "zxcvbnm";    char c = r[dik - 0x2c]; return (shift ^ caps) ? (char)(c - 32) : c; }
    switch (dik) {
    case 0x02: return shift ? '!' : '1';  case 0x03: return shift ? '@' : '2';
    case 0x04: return shift ? '#' : '3';  case 0x05: return shift ? '$' : '4';
    case 0x06: return shift ? '%' : '5';  case 0x07: return shift ? '^' : '6';
    case 0x08: return shift ? '&' : '7';  case 0x09: return shift ? '*' : '8';
    case 0x0a: return shift ? '(' : '9';  case 0x0b: return shift ? ')' : '0';
    case 0x0c: return shift ? '_' : '-';  case 0x0d: return shift ? '+' : '=';
    case 0x1a: return shift ? '{' : '[';  case 0x1b: return shift ? '}' : ']';
    case 0x27: return shift ? ':' : ';';  case 0x28: return shift ? '"' : '\'';
    case 0x29: return shift ? '~' : '`';  case 0x2b: return shift ? '|' : '\\';
    case 0x33: return shift ? '<' : ',';  case 0x34: return shift ? '>' : '.';
    case 0x35: return shift ? '?' : '/';  case 0x39: return ' ';
    case 0x47: return '7'; case 0x48: return '8'; case 0x49: return '9';   // keypad
    case 0x4b: return '4'; case 0x4c: return '5'; case 0x4d: return '6';
    case 0x4f: return '1'; case 0x50: return '2'; case 0x51: return '3';
    case 0x52: return '0'; case 0x53: return '.'; case 0x4a: return '-'; case 0xb5: return '/';
    default: return 0;
    }
}

// ---- cached JNI handles (resolved once from the game's classloader, then held as global refs) ----
static bool g_resolved = false, g_resolve_failed = false, g_installed = false;
static jclass cls_keyboard, cls_mouse, cls_display;
static jmethodID m_key_down, m_key_created, m_mouse_down, m_mouse_x, m_mouse_y, m_mouse_created, m_display_h;

// ---- last-seen state, diffed each frame to produce edge events ----
static unsigned char g_key_state[256];            // indexed by DIK scancode
static unsigned char g_btn_state[8];              // indexed by LWJGL mouse button (0 L, 1 R, 2 M, 3/4 side)
static int g_last_x = -1, g_last_y = -1;
static bool g_caps = false;                        // tracked from the caps-lock key edges (0x3a)

static JNIEnv *current_env() {
    if (!data_2f6ee0) return nullptr;
    JNIEnv *env = nullptr;
    // hook_game_window() runs on the JVM render thread (hook_update_display's caller), so the thread is attached.
    if ((*data_2f6ee0)->GetEnv(data_2f6ee0, (void **)&env, JNI_VERSION_1_6) != JNI_OK) return nullptr;
    return env;
}

static jclass find_global(JNIEnv *env, const char *name) {
    jclass local = env->FindClass(name);
    if (env->ExceptionCheck()) { env->ExceptionClear(); return nullptr; }
    if (!local) return nullptr;
    jclass global = (jclass)env->NewGlobalRef(local);
    env->DeleteLocalRef(local);
    return global;
}

static bool resolve(JNIEnv *env) {
    if (g_resolved) return true;
    if (g_resolve_failed) return false;
    cls_keyboard = find_global(env, "org/lwjgl/input/Keyboard");
    cls_mouse    = find_global(env, "org/lwjgl/input/Mouse");
    cls_display  = find_global(env, "org/lwjgl/opengl/Display");
    if (!cls_keyboard || !cls_mouse || !cls_display) { g_resolve_failed = true; slinky_logf("mac input: LWJGL classes not found; input poll disabled"); return false; }
    m_key_down      = env->GetStaticMethodID(cls_keyboard, "isKeyDown", "(I)Z");
    m_key_created   = env->GetStaticMethodID(cls_keyboard, "isCreated", "()Z");
    m_mouse_down    = env->GetStaticMethodID(cls_mouse,    "isButtonDown", "(I)Z");
    m_mouse_x       = env->GetStaticMethodID(cls_mouse,    "getX", "()I");
    m_mouse_y       = env->GetStaticMethodID(cls_mouse,    "getY", "()I");
    m_mouse_created = env->GetStaticMethodID(cls_mouse,    "isCreated", "()Z");
    m_display_h     = env->GetStaticMethodID(cls_display,  "getHeight", "()I");
    if (env->ExceptionCheck()) env->ExceptionClear();
    if (!m_key_down || !m_key_created || !m_mouse_down || !m_mouse_x || !m_mouse_y || !m_mouse_created || !m_display_h) {
        g_resolve_failed = true; slinky_logf("mac input: LWJGL methods not found; input poll disabled"); return false;
    }
    g_resolved = true;
    slinky_logf("mac input: LWJGL Keyboard/Mouse/Display bound");
    return true;
}

static void send(InputEvent *e) { data_304ba8(e); }   // return value (swallow) has no effect on a poll; see header

static void poll_keyboard(JNIEnv *env) {
    if (!env->CallStaticBooleanMethod(cls_keyboard, m_key_created)) return;
    for (const KeyMap *k = kKeyMap; k != kKeyMap + sizeof(kKeyMap) / sizeof(*kKeyMap); k++) {
        unsigned char cur = env->CallStaticBooleanMethod(cls_keyboard, m_key_down, (jint)k->dik) ? 1 : 0;
        if (cur == g_key_state[k->dik]) continue;
        g_key_state[k->dik] = cur;
        if (k->dik == 0x3a && cur) g_caps = !g_caps;                     // caps-lock toggles on press
        InputEvent e = {};
        e.type = IN_KEY;
        e.key.code = k->client;
        e.key.down = cur;
        if (k->client) send(&e);
        if (cur) {                                                       // on key-down, also feed a character to text fields
            bool shift = g_key_state[0x2a] || g_key_state[0x36];
            char ch = dik_to_char(k->dik, shift, g_caps);
            if (ch) { InputEvent c = {}; c.type = IN_KEY; c.key.code = 0; c.key.text[0] = ch; c.key.down = 1; send(&c); }
        }
    }
}

static void poll_mouse(JNIEnv *env) {
    if (!env->CallStaticBooleanMethod(cls_mouse, m_mouse_created)) return;
    static const int kBtnToClient[5] = {1, 2, 3, 4, 5};                 // LWJGL 0/1/2/3/4 -> client L/R/M/X1/X2
    for (int b = 0; b < 5; b++) {
        unsigned char cur = env->CallStaticBooleanMethod(cls_mouse, m_mouse_down, (jint)b) ? 1 : 0;
        if (cur == g_btn_state[b]) continue;
        g_btn_state[b] = cur;
        InputEvent e = {};
        e.type = IN_KEY;
        e.key.code = kBtnToClient[b];
        e.key.down = cur;
        send(&e);
    }
    int x = env->CallStaticIntMethod(cls_mouse, m_mouse_x);
    int gly = env->CallStaticIntMethod(cls_mouse, m_mouse_y);           // LWJGL origin is bottom-left
    int h = env->CallStaticIntMethod(cls_display, m_display_h);
    int y = h > 0 ? h - 1 - gly : gly;                                  // -> top-down, matching the Win32 client-area y
    if (x != g_last_x || y != g_last_y) {
        g_last_x = x; g_last_y = y;
        InputEvent e = {};
        e.type = IN_MOVE;
        e.move.x = x;
        e.move.y = y;
        send(&e);
    }
}

// ---- called every frame by hook_update_display(); first call binds the JNI handles, every call polls ----
extern "C" void hook_game_window() {
    if (!data_304ba8) return;
    JNIEnv *env = current_env();
    if (!env) return;
    if (!resolve(env)) return;
    g_installed = true;
    poll_keyboard(env);
    poll_mouse(env);
}

// ---- counterpart of the Windows un-subclass; nothing is attached to the OS, so just go inert and let a
//      reload re-resolve against a fresh game (global refs dropped so FindClass runs again) ----
extern "C" void restore_game_window() {
    if (!g_installed) return;
    JNIEnv *env = current_env();
    if (env) {
        if (cls_keyboard) env->DeleteGlobalRef(cls_keyboard);
        if (cls_mouse)    env->DeleteGlobalRef(cls_mouse);
        if (cls_display)  env->DeleteGlobalRef(cls_display);
    }
    cls_keyboard = cls_mouse = cls_display = nullptr;
    g_resolved = g_resolve_failed = g_installed = false;
    __builtin_memset(g_key_state, 0, sizeof g_key_state);
    __builtin_memset(g_btn_state, 0, sizeof g_btn_state);
    g_last_x = g_last_y = -1;
    g_caps = false;
}

// ---- auto clicker: deliver a synthetic click to the client's own handler (synthetic = 1). On Windows this
//      also round-trips through the window so the game registers a real click; posting a real click to the
//      game on Mac is the Stage-2 CGEventPost piece (posting needs no Accessibility grant, unlike a tap) and
//      is not part of the load milestone. Signature matches the caller (auto_clicker.cpp: int button). ----
extern "C" void post_synthetic_click(int button, unsigned char down) {
    InputEvent e = {};
    e.type = IN_KEY;
    e.key.code = (button == 2) ? 2 : 1;           // client button: L unless the caller asked for right
    e.key.down = down ? 1 : 0;
    e.key.synthetic = 1;
    if (data_304ba8) data_304ba8(&e);
}
