// Stasis: native port of the freeze/release behavior, not Java bytecode injection.
// Reference: https://github.com/IamNespola/OpenMyau-Plus/blob/main/src/main/java/myau/module/modules/Stasis.java
#include "slinky/engine.h"
#include "slinky/jids.h"
#include "slinky/stasis_state.h"
#include <stddef.h>
#if defined(_MSC_VER) && !defined(__clang__)
#include <intrin.h>
#endif

using slinky::StasisMotion;
using slinky::StasisState;

struct HandlerSlot { void (*callback)(void*); int priority; int reserved; };
struct StasisDescriptor {
    const char* name;
    const char* description;
    unsigned category;
    unsigned char enabled;
    char pad15[3];
    unsigned flags;
    int key;
    unsigned char hold;
    char pad21[7];
    char animation[16];
    float height;
    char pad3c[4];
    unsigned char expanded;
    char pad41[7];
    HandlerSlot events[27];
    void (*settings)(void*, void*);
    void (*visit)(void*);
    void (*status)(void*);
};
static_assert(offsetof(StasisDescriptor, enabled) == 0x14, "Module enabled ABI");
static_assert(offsetof(StasisDescriptor, events) == 0x48, "Module event ABI");
static_assert(offsetof(StasisDescriptor, settings) == 0x1f8, "Module GUI ABI");
static_assert(sizeof(StasisDescriptor) == 0x210, "Module descriptor ABI");

struct Context {
    Jvm* java;
    void* manager;
    void* profiles;
    jobject minecraft;
    jobject world;
    jobject player;
    void* keys;
    void* targets;
    unsigned char inGame;
};
struct Event { long long type; Context* context; };
struct ToggleEvent { long long type; Context* context; void* module; unsigned char on; };
struct MoveEvent { long long type; Context* context; float strafe, forward, friction; };
struct PacketEvent { long long type; Jvm* java; jobject packet; long long time; int id, result; };
static_assert(offsetof(PacketEvent, result) == 0x24, "Packet verdict ABI");

static StasisDescriptor descriptor;
static StasisState cycle;
static jobject owner;
static jclass lookPacketClass;
static jfieldID movementInput, strafeInput, forwardInput, jumpInput, sneakInput;
static volatile unsigned char publishedFrozen;
static void publishFrozen(bool frozen) {
#if defined(_MSC_VER) && !defined(__clang__)
    _InterlockedExchange8((volatile char*)&publishedFrozen, frozen ? 1 : 0);
#else
    __atomic_store_n(&publishedFrozen, frozen ? 1 : 0, __ATOMIC_RELEASE);
#endif
}
static bool isFrozen() {
#if defined(_MSC_VER) && !defined(__clang__)
    return _InterlockedCompareExchange8((volatile char*)&publishedFrozen, 0, 0) != 0;
#else
    return __atomic_load_n(&publishedFrozen, __ATOMIC_ACQUIRE) != 0;
#endif
}


FN(void, queue_module_toggle, (void* manager, void* module, unsigned char on));
FN(jobject, get_minecraft, (Jvm* java));
FN(jobject, get_local_player, (Jvm* java, jobject minecraft));
FN(void, slinky_logf, (const char* format, ...));

static StasisMotion motion(Jvm* j, jobject player) {
    JNIEnv* env = j->env;
    return {(*env)->GetDoubleField(env, player, j->ids->Entity_motionX),
            (*env)->GetDoubleField(env, player, j->ids->Entity_motionY),
            (*env)->GetDoubleField(env, player, j->ids->Entity_motionZ)};
}

static void setMotion(Jvm* j, jobject player, StasisMotion value) {
    JNIEnv* env = j->env;
    (*env)->SetDoubleField(env, player, j->ids->Entity_motionX, value.x);
    (*env)->SetDoubleField(env, player, j->ids->Entity_motionY, value.y);
    (*env)->SetDoubleField(env, player, j->ids->Entity_motionZ, value.z);
}

static bool samePlayer(Context* c) {
    return c && c->java && c->player && owner &&
        (*c->java->env)->IsSameObject(c->java->env, owner, c->player);
}

// Probe standard MCP, SRG and obfuscated field names once, not on each movement event.
static jfieldID field(JNIEnv* env, jclass cls, const char* signature,
                     const char* mcp, const char* srg, const char* obfuscated) {
    const char* names[] = {mcp, srg, obfuscated};
    for (const char* name : names) {
        jfieldID id = (*env)->GetFieldID(env, cls, name, signature);
        if (id) return id;
        (*env)->ExceptionClear(env);
    }
    return nullptr;
}

static bool resolveInput(Jvm* j, jobject player) {
    JNIEnv* env = j->env;
    if ((*env)->PushLocalFrame(env, 64) != 0) return false;
    bool found = false;
    jclass classClass = (*env)->FindClass(env, "java/lang/Class");
    jclass fieldClass = (*env)->FindClass(env, "java/lang/reflect/Field");
    if (classClass && fieldClass) {
        jmethodID fields = (*env)->GetMethodID(env, classClass, "getDeclaredFields", "()[Ljava/lang/reflect/Field;");
        jmethodID type = (*env)->GetMethodID(env, fieldClass, "getType", "()Ljava/lang/Class;");
        jmethodID modifiers = (*env)->GetMethodID(env, fieldClass, "getModifiers", "()I");
        jmethodID primitive = (*env)->GetMethodID(env, classClass, "isPrimitive", "()Z");
        jmethodID array = (*env)->GetMethodID(env, classClass, "isArray", "()Z");
        if (fields && type && modifiers && primitive && array) {
            // Identify the field by its movement-input type, so no guessed player-field signature is needed.
            for (jclass cls = (*env)->GetObjectClass(env, player); cls && !found; cls = (*env)->GetSuperclass(env, cls)) {
                auto declared = (jobjectArray)(*env)->CallObjectMethod(env, cls, fields);
                if (!declared || (*env)->ExceptionCheck(env)) break;
                jsize count = (*env)->GetArrayLength(env, declared);
                for (jsize i = 0; i < count && !found; ++i) {
                    jobject reflected = (*env)->GetObjectArrayElement(env, declared, i);
                    if (!reflected) continue;
                    if (((*env)->CallIntMethod(env, reflected, modifiers) & 8) == 0) {
                        jclass inputType = (jclass)(*env)->CallObjectMethod(env, reflected, type);
                        if (inputType && !(*env)->CallBooleanMethod(env, inputType, primitive) &&
                            !(*env)->CallBooleanMethod(env, inputType, array)) {
                            jfieldID strafe = field(env, inputType, "F", "moveStrafe", "field_78902_a", "a");
                            jfieldID forward = field(env, inputType, "F", "moveForward", "field_78900_b", "b");
                            jfieldID jump = field(env, inputType, "Z", "jump", "field_78901_c", "c");
                            jfieldID sneak = field(env, inputType, "Z", "sneak", "field_78899_d", "d");
                            if (strafe && forward && jump && sneak) {
                                movementInput = (*env)->FromReflectedField(env, reflected);
                                strafeInput = strafe; forwardInput = forward; jumpInput = jump; sneakInput = sneak;
                                found = movementInput != nullptr;
                            }
                        }
                        if (inputType) (*env)->DeleteLocalRef(env, inputType);
                    }
                    (*env)->DeleteLocalRef(env, reflected);
                }
                (*env)->DeleteLocalRef(env, declared);
            }
        }
    }
    if ((*env)->ExceptionCheck(env)) { (*env)->ExceptionClear(env); found = false; }
    (*env)->PopLocalFrame(env, nullptr);
    return found;
}

static bool resolveLookPacket(Jvm* j) {
    if (lookPacketClass) return true;
    JNIEnv* env = j->env;
    if ((*env)->PushLocalFrame(env, 32) != 0) return false;
    jclass classClass = (*env)->FindClass(env, "java/lang/Class");
    jmethodID nested = classClass ? (*env)->GetMethodID(env, classClass, "getDeclaredClasses", "()[Ljava/lang/Class;") : nullptr;
    auto classes = nested ? (jobjectArray)(*env)->CallObjectMethod(env, j->ids->C03PacketPlayer, nested) : nullptr;
    if (classes && !(*env)->ExceptionCheck(env)) {
        jsize count = (*env)->GetArrayLength(env, classes);
        for (jsize i = 0; i < count && !lookPacketClass; ++i) {
            jclass candidate = (jclass)(*env)->GetObjectArrayElement(env, classes, i);
            // C05PacketPlayerLook is the C03 subclass with the (yaw, pitch, onGround) constructor.
            if (candidate && (*env)->IsAssignableFrom(env, candidate, j->ids->C03PacketPlayer)) {
                jmethodID constructor = (*env)->GetMethodID(env, candidate, "<init>", "(FFZ)V");
                if (constructor) lookPacketClass = (jclass)(*env)->NewGlobalRef(env, candidate);
                else (*env)->ExceptionClear(env);
            }
        }
    }
    if ((*env)->ExceptionCheck(env)) (*env)->ExceptionClear(env);
    (*env)->PopLocalFrame(env, nullptr);
    return lookPacketClass != nullptr;
}

static void stop(Context* c, bool restore) {
    publishFrozen(false);
    if (cycle.active) {
        StasisMotion saved = cycle.disable();
        if (restore && samePlayer(c)) setMotion(c->java, c->player, saved);
    }
    if (owner && c && c->java) {
        (*c->java->env)->DeleteGlobalRef(c->java->env, owner);
        owner = nullptr;
    }
}

static void disable(Context* c, bool restore) {
    bool wasActive = cycle.active;
    stop(c, restore);
    if (wasActive && c) queue_module_toggle(c->manager, &descriptor, 0);
}

static void clearInput(Context* c) {
    Jvm* j = c->java;
    JNIEnv* env = j->env;
    (*env)->SetFloatField(env, c->player, j->ids->EntityLivingBase_moveForward, 0);
    (*env)->SetFloatField(env, c->player, j->ids->EntityLivingBase_moveStrafing, 0);
    jobject input = movementInput ? (*env)->GetObjectField(env, c->player, movementInput) : nullptr;
    if (input) {
        (*env)->SetFloatField(env, input, strafeInput, 0);
        (*env)->SetFloatField(env, input, forwardInput, 0);
        (*env)->SetBooleanField(env, input, jumpInput, 0);
        (*env)->SetBooleanField(env, input, sneakInput, 0);
        (*env)->DeleteLocalRef(env, input);
    }
}

static void onToggle(void* event) {
    auto* e = (ToggleEvent*)event;
    if (e->module != &descriptor) return;
    Context* c = e->context;
    if (!e->on) { stop(c, true); return; }
    if (!c || !c->java || !c->player || !c->world ||
        !resolveInput(c->java, c->player) || !resolveLookPacket(c->java)) {
        slinky_logf("Stasis: movement input or look-packet mapping unavailable");
        if (c) queue_module_toggle(c->manager, &descriptor, 0);
        return;
    }
    owner = (*c->java->env)->NewGlobalRef(c->java->env, c->player);
    if (!owner) { queue_module_toggle(c->manager, &descriptor, 0); return; }
    cycle.enable(motion(c->java, c->player));
    publishFrozen(true);
}

static void onUpdate(void* event) {
    Context* c = ((Event*)event)->context;
    if (!cycle.active) return;
    if (!samePlayer(c) || !c->world) { disable(c, false); return; }
    setMotion(c->java, c->player, cycle.update(motion(c->java, c->player)));
    publishFrozen(!cycle.release);
    if ((*c->java->env)->GetBooleanField(c->java->env, c->player, c->java->ids->Entity_onGround)) {
        disable(c, true);
        return;
    }
    if (!cycle.release) clearInput(c);
}

static void enforceFreeze(void* event) {
    Context* c = ((Event*)event)->context;
    if (!cycle.active || cycle.release) return;
    if (!samePlayer(c) || !c->world) { disable(c, false); return; }
    setMotion(c->java, c->player, {0, 0, 0});
    clearInput(c);
}

static void onStrafe(void* event) {
    if (!cycle.active || cycle.release) return;
    auto* e = (MoveEvent*)event;
    e->strafe = e->forward = 0;
    enforceFreeze(event);
}

static void onPacket(void* event) {
    auto* e = (PacketEvent*)event;
    if (!isFrozen() || e->result == 2) return;
    Jvm* j = e->java;
    JNIEnv* env = j->env;
    if (!(*env)->IsInstanceOf(env, e->packet, j->ids->C03PacketPlayer)) return;
    if (lookPacketClass && (*env)->IsInstanceOf(env, e->packet, lookPacketClass)) return;
    jobject player = get_local_player(j, get_minecraft(j));
    if (!player) return;
    int hurt = (*env)->GetIntField(env, player, j->ids->EntityLivingBase_hurtTime);
    // SlinkyChannelHandler verdict 2 drops a packet; 1 holds it. Stasis drops, never queues.
    if (StasisState::cancelMovementPacket(true, true, false, hurt)) e->result = 2;
}

static void onFrame(void* event) {
    Context* c = ((Event*)event)->context;
    if (cycle.active && (!samePlayer(c) || !c->world)) disable(c, false);
}

static void onShutdown(void* event) { stop(((Event*)event)->context, true); }
static void showSettings(void* a, void* b) {
    W_label(a, b, "Freeze: 45 ticks / Release: 1 tick");
    W_label(a, b, "Disables on landing.");
}
static void showStatus(void* self) {
    log_fmt(self, 0x20, "%s", isFrozen() ? "Frozen" : "Release");
}

extern "C" void* stasis_descriptor() {
    descriptor.name = "Stasis";
    descriptor.description = "Freezes you in the air with a brief movement release.";
    descriptor.category = 1; // Move
    descriptor.events[1] = {onFrame, -1000, 0};
    descriptor.events[3] = {onPacket, -1000, 0};
    descriptor.events[5] = {onToggle, 0, 0};
    descriptor.events[16] = {onUpdate, -1000, 0}; // once per World.updateEntities, not render frames
    descriptor.events[18] = {enforceFreeze, -1000, 0};
    descriptor.events[21] = {onStrafe, -1000, 0};
    descriptor.events[22] = {enforceFreeze, -1000, 0};
    descriptor.events[26] = {onShutdown, 0, 0};
    descriptor.settings = showSettings;
    descriptor.status = showStatus;
    return &descriptor;
}

// Called after packet hooks are detached and before the engine releases its JNI IDs.
extern "C" void stasis_cleanup(JNIEnv* env) {
    publishFrozen(false);
    if (owner) { (*env)->DeleteGlobalRef(env, owner); owner = nullptr; }
    if (lookPacketClass) { (*env)->DeleteGlobalRef(env, lookPacketClass); lookPacketClass = nullptr; }
    movementInput = strafeInput = forwardInput = jumpInput = sneakInput = nullptr;
    cycle = {};
}
