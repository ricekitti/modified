// JNI bridge 0x22000
#include "slinky/engine.h"
#include "slinky/jcall.h"
struct Rot { float yaw, pitch; };
struct Vec3 { double x, y, z; };

extern "C" jobject get_minecraft(Jvm *j) {
    return JCALL(j, GetStaticObjectField, ids->Minecraft, ids->Minecraft_Lbao_);
}

extern "C" jobject get_local_player(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->id_188);
}

extern "C" jobject get_world(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lbjf_);
}

extern "C" jobject get_timer(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lbbr_);
}

extern "C" jobject get_net_handler(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lbbj_);
}

extern "C" jobject get_render_manager(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lbma_);
}

extern "C" jobject java_get_minecraft(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lblt_);
}

extern "C" jobject java_get_minecraft_2(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lbjh_);
}

extern "C" jobject java_get_minecraft_3(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lbqf_);
}

extern "C" jobject java_get_minecraft_4(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lbje_);
}

extern "C" jobject java_get_minecraft_5(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lazu_);
}

extern "C" void java_set_minecraft(Jvm *j, void *a1, void *a2) {
    JCALL(j, SetObjectField, a1, ids->Minecraft_Lazu_, a2);
}

extern "C" bool is_in_game(Jvm *j, void *a1) {
    return JCALLN(j, GetBooleanField, a1, ids->Minecraft_inGameHasFocus) != 0;
}

extern "C" jint java_get_minecraft_left_click_counter(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Minecraft_leftClickCounter);
}

extern "C" void java_set_minecraft_left_click_counter(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->Minecraft_leftClickCounter, a2);
}

extern "C" jobject get_render_view_entity(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Minecraft_Lpk_);
}

extern "C" jobject java_call_minecraft_get_net_handler(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->Minecraft_getNetHandler);
}

extern "C" void java_call_minecraft_click_mouse(Jvm *j, void *a1) {
    JCALLN(j, CallVoidMethod, a1, ids->Minecraft_clickMouse);
}

extern "C" void java_call_minecraft_right_click_mouse(Jvm *j, void *a1) {
    JCALLN(j, CallVoidMethod, a1, ids->Minecraft_rightClickMouse);
}

// ---- hand-written
FN(jobject, java_get_static_render_manager, (Jvm *j, jobject o));

extern "C" jobject get_current_screen(Jvm *j, jobject o) {
    jobject x = JCALLN(j, GetObjectField, o, (jfieldID)ids->Minecraft_Lbdw_);
    return JCALLN(j, IsInstanceOf, x, (jclass)ids->GuiContainer) ? x : nullptr;
}
extern "C" jobject java_get_minecraft_6(Jvm *j, jobject o) {
    if (j->ids->variant != 0)
        return JCALL(j, GetObjectField, o, (jfieldID)ids->Minecraft_Lbiu_);
    return java_get_static_render_manager(j, o);
}

// ---- ClassFileLoadHook: rewrite the classes listed in the hook table, injecting "SlinkyHooks18"
struct MethodKey { char b[32]; };
struct MethodPat { long long a, b; };
struct HookTab { char pad[0x18]; MethodPat *pats; unsigned long long n; };
struct HookCls { jobject cls; void *data; int len; char pad[0x14]; };   // 0x28 bytes each

extern "C" volatile int data_2f7a48;          // first error (sticky)
extern "C" unsigned char data_2f78c8;         // 1 while the hooks are installed
extern "C" void slinky_logf(const char *fmt, ...);
static unsigned char g_disarming;             // 1 while remove_class_hooks restores the original bytecode
extern "C" HookCls data_2f78e0, data_2f7908, data_2f7930, data_2f7958, data_2f7980,
    data_2f79a8, data_2f79d0, data_2f79f8, data_2f7a20;
extern "C" HookCls g_hotbar;
extern "C" HookCls g_inventory;
extern "C" HookCls g_extra;   // separate registry (EntityPlayerSP), overlay of hook_installer's HookRec g_extra
extern "C" const char rdata_14f710[];         // "SlinkyHooks18"
FN(bool, compare_member_name, (MethodKey k, MethodPat p));
struct HookTable; struct CP; struct RBuf22; struct WBuf22;
FN(int, rewrite_instruction, (HookTable *t, CP *cp, RBuf22 *in, WBuf22 *out));   // instruction patcher (below)
FN(int, rewrite_class_file, (const unsigned char *data, int len, unsigned char **out, int *outLen, const char *hookCls,
                void *hookData, int hookLen, void *match, HookCls *h));
FN(void, libc_free, (void *p));                  // free
FN(void *, libc_memcpy_alt, (void *d, const void *s, long long n));   // memcpy

struct JvmtiEnv_ { void *fn[64]; };
typedef int (*JvmtiAllocate)(JvmtiEnv_ **, long long, unsigned char **);

// method matcher passed to the class rewriter: (-1,-1) entries match everything
extern "C" int match_method(HookTab *t, MethodKey *k, void **outFn, MethodPat **outPat) {
    for (unsigned long long i = 0; i < t->n; i++) {
        MethodPat *p = &t->pats[i * 2];
        if ((p->a == -1 && p->b == -1) || compare_member_name(*k, *p)) {
            *outFn = (void *)rewrite_instruction;
            *outPat = p;
            break;
        }
    }
    return 0;
}

extern "C" void class_file_load_hook(JvmtiEnv_ **jvmti, JNIEnv *env, jclass redefined, jobject loader, const char *name,
                         jobject pd, int len, const unsigned char *data, int *newLen, unsigned char **newData) {
    if (data_2f7a48 != 0) return;
    // remove_class_hooks sets g_disarming before it retransforms: during that call the JVM may replay this hook
    // for the classes being restored, and re-patching them would silently undo the disarm. Handing the
    // bytes back untouched makes the retransform a true restore. (The install also runs with the hook
    // active and g_disarming clear, so it still patches.)
    if (g_disarming) return;
    HookCls *h;
#define TRY(x) if ((*env)->IsSameObject(env, x.cls, redefined)) { h = &x; goto found; }
    TRY(data_2f78e0) TRY(data_2f7908) TRY(data_2f7930) TRY(data_2f7958) TRY(data_2f7980)
    TRY(data_2f79a8) TRY(data_2f79d0) TRY(data_2f79f8) TRY(data_2f7a20)
    TRY(g_hotbar)
    TRY(g_inventory)
    TRY(g_extra)   // separate registry; g_extra.cls is null (never matches) when the extra hook is disabled
#undef TRY
    return;
found:
    unsigned char *buf = nullptr;
    int outLen;
    int err = rewrite_class_file(data, len, &buf, &outLen, rdata_14f710, h->data, h->len, (void *)match_method, h);
    if (err == 0) {
        unsigned char *mem = nullptr;
        JvmtiAllocate alloc = (JvmtiAllocate)(*jvmti)->fn[45];
        if (alloc(jvmti, outLen, &mem) != 0) {
            err = ((JvmtiAllocate)(*jvmti)->fn[45])(jvmti, outLen, &mem) + 12000;
            if (err != 0) {
                libc_free(buf);
                __sync_val_compare_and_swap(&data_2f7a48, 0, err);
                return;
            }
        }
        libc_memcpy_alt(mem, buf, outLen);
        *newData = mem;
        *newLen = outLen;
        libc_free(buf);
        return;
    }
    __sync_val_compare_and_swap(&data_2f7a48, 0, err);
}

// ---- JNI bridge functions found by the gap scan (no caller outside the virtualised init) ----
extern "C" jint java_get_minecraft_right_click_delay_timer(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Minecraft_rightClickDelayTimer);
}

extern "C" jobject java_call_minecraft_add_scheduled_task(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->Minecraft_addScheduledTask, a2);
}

// ---- JVMTI: switch the class hooks off again (undo of the install) ----
// Disables the ClassFileLoadHook event, retransforms the 9 hooked classes so the game gets its original bytecode
// back, gives the capabilities up and drops the global ref of the transformer class. Errors are JVMTI error + 12000.
struct JvmtiTable { void *fn[160]; };
typedef int (*JvmtiSetEventMode)(JvmtiTable **, int mode, int event, void *thread, ...);
typedef int (*JvmtiRetransform)(JvmtiTable **, int count, jclass *classes);
typedef int (*JvmtiRelinquish)(JvmtiTable **, const void *caps);
#define JVMTI(env, idx, T) ((T)(*(env))->fn[idx])
extern "C" unsigned char data_2f78c8;       // 1 while the hooks are installed
extern "C" jobject data_2f78d0;              // global ref of the transformer class
extern "C" const char rdata_14f720[];        // JVMTI capabilities that were added
#define JVMTI_CHECK(call)                       \
    do {                                        \
        if (call) {                             \
            err = (call) + 0x2ee0;              \
            if (err) goto out;                  \
        }                                       \
    } while (0)
extern "C" int remove_class_hooks(JNIEnv *env, JvmtiTable **jvmti) {
    int err = 0;
    // Not armed = the hooks were never installed or another pass already consumed the flag (a failed
    // pass left the game's patched calls live). Reporting success here made the unload sequence tear
    // everything down while the JVM was still dispatching natives into this DLL: hs_err pid6708
    // (java_set_render_manager_player_view_y via setRenderY) and pid2260 (serialize_settings via a second hook_update_display pass) look exactly like that.
    if (!__sync_bool_compare_and_swap(&data_2f78c8, 1, 0)) return 12000 + 49 /* JVMTI_ERROR_NOT_AVAILABLE */;
    g_disarming = 1;
    JVMTI_CHECK(JVMTI(jvmti, 1, JvmtiSetEventMode)(jvmti, 0 /* JVMTI_DISABLE */, 0x36 /* CLASS_FILE_LOAD_HOOK */, 0));
    {
        jclass classes[9] = {data_2f78e0.cls, data_2f7908.cls, data_2f7930.cls, data_2f7958.cls, data_2f7980.cls,
                             data_2f79a8.cls, data_2f79d0.cls, data_2f79f8.cls, data_2f7a20.cls};
        JVMTI_CHECK(JVMTI(jvmti, 151, JvmtiRetransform)(jvmti, 9, classes));
        if (g_extra.cls) { jclass ex[1] = {g_extra.cls}; JVMTI_CHECK(JVMTI(jvmti, 151, JvmtiRetransform)(jvmti, 1, ex)); }
        if (g_inventory.cls) { jclass ex[1] = {g_inventory.cls}; JVMTI_CHECK(JVMTI(jvmti, 151, JvmtiRetransform)(jvmti, 1, ex)); }
        if (g_hotbar.cls) { jclass ex[1] = {g_hotbar.cls}; JVMTI_CHECK(JVMTI(jvmti, 151, JvmtiRetransform)(jvmti, 1, ex)); }
    }
    g_disarming = 0;
    if (JVMTI(jvmti, 142, JvmtiRelinquish)(jvmti, rdata_14f720))
        err = JVMTI(jvmti, 142, JvmtiRelinquish)(jvmti, rdata_14f720) + 0x2ee0;
    else
        err = 0;
out:
    g_disarming = 0;
    if (data_2f78d0) {
        (*env)->DeleteGlobalRef(env, data_2f78d0);
        data_2f78d0 = 0;
    }
    // A failed step leaves the client resident (the unload sequence cancels on errors): re-arm the flag
    // so the next click gets a whole new pass. Retransform is idempotent, so resuming is safe.
    slinky_logf("disarm: err=%d", err);
    if (err) __sync_bool_compare_and_swap(&data_2f78c8, 0, 1);
    return err;
}

// ---- class-file patcher: rewrites a call/field instruction that matches a hook entry into `invokestatic hook` ----
// Instruction callback of the class transformer (rewrite_span/copy_methods_table_constant_pool `pick`). `t` lists the hooks; each has the
// owner class, member name + descriptor to catch, the kind of instruction and where its new Methodref index is.
#include "slinky/classfile.h"
struct RBuf22 { const unsigned char *p, *end; };
struct WBuf22 { unsigned char *p, *end; };
struct HookEnt { const char *cls; const char *name, *desc; int kind; int pad; unsigned short *ref; };   // 0x28
struct HookTable { char pad[0x10]; HookEnt *e; unsigned long long n; };
struct MemberName { const char *name, *desc; };
FN(int, classfile_read_u1, (RBuf22 *r, unsigned char *out));
FN(int, classfile_read_u2, (RBuf22 *r, unsigned short *out));
FN(int, classfile_write_u1, (WBuf22 *w, unsigned char b));
FN(int, classfile_write_u2, (WBuf22 *w, unsigned short v));
FN(int, constant_pool_member_ref, (CP *cp, unsigned short idx, MemberRef *out));
FN(int, constant_pool_member_ref2, (CP *cp, unsigned short idx, MemberRef *out));
extern "C" int compare_utf8_local(Utf8 u, const char *s) __asm__("compare_utf8");
extern "C" bool compare_member_name_local(NameType nt, MemberName want) __asm__("compare_member_name");
extern "C" int rewrite_instruction(HookTable *t, CP *cp, RBuf22 *in, WBuf22 *out) {
    unsigned char op;
    int r = classfile_read_u1(in, &op);
    if (r) return r;
    int kind;
    bool field = true;
    switch (op) {
    case 0xb2: kind = 4; break;               // getstatic
    case 0xb3: kind = 6; break;               // putstatic
    case 0xb4: kind = 3; break;               // getfield
    case 0xb5: kind = 5; break;               // putfield
    case 0xb6: case 0xb7: kind = 1; field = false; break;   // invokevirtual / invokespecial
    case 0xb8: kind = 2; field = false; break;               // invokestatic
    default: return 0;
    }
    unsigned short idx;
    r = classfile_read_u2(in, &idx);
    if (r) return r;
    MemberRef ref;
    r = field ? constant_pool_member_ref(cp, idx, &ref) : constant_pool_member_ref2(cp, idx, &ref);
    if (r) return r;
    for (unsigned long long i = 0; i < t->n; i++) {
        HookEnt *h = &t->e[i];
        if (h->kind != kind) continue;
        if (compare_utf8_local(ref.cls, h->cls) != 0) continue;
        if (!compare_member_name_local(ref.nt, MemberName{h->name, h->desc})) continue;
        r = classfile_write_u1(out, 0xb8);   // invokestatic
        if (r) return r;
        return classfile_write_u2(out, *h->ref);
    }
    return 0;
}

// ---- functions the first gap scan missed (padding / tail-jump neighbours) ----
extern "C" void java_set_minecraft_right_click_delay_timer(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->Minecraft_rightClickDelayTimer, a2);
}

extern "C" jint java_get_minecraft_display_width(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Minecraft_displayWidth);
}

extern "C" jint java_get_minecraft_display_height(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Minecraft_displayHeight);
}
