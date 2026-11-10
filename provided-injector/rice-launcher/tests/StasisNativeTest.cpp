#include "slinky/jids.h"
#include "slinky/stasis_state.h"
#include <cstring>
#include <iostream>
#include <stdexcept>

using slinky::StasisMotion;
namespace {
struct Player { StasisMotion motion; bool grounded; int hurt; } player, replacement;
int minecraft, world, input, playerClass, inputClass, classClass, reflectedFieldClass;
int reflectedInput, fields, nested, c03Class, lookClass, packet, lookPacket, otherPacket;
int globals = 0, queued = 0;
bool jump = true, sneak = true;
float forward = 1, strafe = 1;
jobject currentPlayer = &player;
void* lastQueued = nullptr;
enum Id { X = 1, Y, Z, Ground, Hurt, MoveForward, MoveStrafe, Input, Forward, Strafe, Jump, Sneak,
          DeclaredFields, FieldType, Modifiers, NestedClasses, Constructor, Primitive, Array };
jfieldID id(int value) { return reinterpret_cast<void*>(static_cast<size_t>(value)); }
int number(void* value) { return static_cast<int>(reinterpret_cast<size_t>(value)); }
void require(bool condition, const char* message) { if (!condition) throw std::runtime_error(message); }
bool equal(StasisMotion a, StasisMotion b) { return a.x == b.x && a.y == b.y && a.z == b.z; }
jint push(JNIEnv*, jint) { return 0; }
jobject pop(JNIEnv*, jobject value) { return value; }
jclass find(JNIEnv*, const char* name) {
    return std::strcmp(name, "java/lang/Class") == 0 ? &classClass : &reflectedFieldClass;
}
jmethodID method(JNIEnv*, jclass cls, const char* name, const char* signature) {
    if (std::strcmp(name, "getDeclaredFields") == 0) return id(DeclaredFields);
    if (std::strcmp(name, "getType") == 0) return id(FieldType);
    if (std::strcmp(name, "getModifiers") == 0) return id(Modifiers);
    if (std::strcmp(name, "getDeclaredClasses") == 0) return id(NestedClasses);
    if (std::strcmp(name, "isPrimitive") == 0) return id(Primitive);
    if (std::strcmp(name, "isArray") == 0) return id(Array);
    if (cls == &lookClass && std::strcmp(signature, "(FFZ)V") == 0) return id(Constructor);
    return nullptr;
}
jobject callObject(JNIEnv*, jobject, jmethodID method, ...) {
    switch (number(method)) {
    case DeclaredFields: return &fields;
    case FieldType: return &inputClass;
    case NestedClasses: return &nested;
    default: return nullptr;
    }
}
jint callInt(JNIEnv*, jobject, jmethodID, ...) { return 0; }
jboolean callBool(JNIEnv*, jobject, jmethodID, ...) { return false; }
jsize length(JNIEnv*, jarray) { return 1; }
jobject element(JNIEnv*, jobjectArray array, jsize) { return array == &fields ? &reflectedInput : &lookClass; }
jclass objectClass(JNIEnv*, jobject) { return &playerClass; }
jclass superclass(JNIEnv*, jclass) { return nullptr; }
jfieldID field(JNIEnv*, jclass cls, const char* name, const char*) {
    if (cls != &inputClass) return nullptr;
    if (std::strcmp(name, "moveStrafe") == 0) return id(Strafe);
    if (std::strcmp(name, "moveForward") == 0) return id(Forward);
    if (std::strcmp(name, "jump") == 0) return id(Jump);
    if (std::strcmp(name, "sneak") == 0) return id(Sneak);
    return nullptr;
}
jfieldID fromField(JNIEnv*, jobject) { return id(Input); }
jobject getObject(JNIEnv*, jobject, jfieldID field) { return number(field) == Input ? &input : nullptr; }
jobject global(JNIEnv*, jobject value) { ++globals; return value; }
void releaseGlobal(JNIEnv*, jobject) { --globals; }
void releaseLocal(JNIEnv*, jobject) {}
jboolean same(JNIEnv*, jobject a, jobject b) { return a == b; }
jboolean assignable(JNIEnv*, jclass a, jclass b) { return a == &lookClass && b == &c03Class; }
jboolean instance(JNIEnv*, jobject value, jclass cls) {
    return cls == &c03Class ? (value == &packet || value == &lookPacket) : (cls == &lookClass && value == &lookPacket);
}
jboolean exception(JNIEnv*) { return false; }
void clearException(JNIEnv*) {}
jdouble getDouble(JNIEnv*, jobject object, jfieldID field) {
    auto& v = static_cast<Player*>(object)->motion;
    return number(field) == X ? v.x : number(field) == Y ? v.y : v.z;
}
void setDouble(JNIEnv*, jobject object, jfieldID field, jdouble value) {
    auto& v = static_cast<Player*>(object)->motion;
    if (number(field) == X) v.x = value; else if (number(field) == Y) v.y = value; else v.z = value;
}
jboolean getBool(JNIEnv*, jobject object, jfieldID) { return static_cast<Player*>(object)->grounded; }
jint getInt(JNIEnv*, jobject object, jfieldID) { return static_cast<Player*>(object)->hurt; }
void setFloat(JNIEnv*, jobject, jfieldID field, jfloat value) {
    if (number(field) == Forward || number(field) == MoveForward) forward = value;
    if (number(field) == Strafe || number(field) == MoveStrafe) strafe = value;
}
void setBool(JNIEnv*, jobject, jfieldID field, jboolean value) {
    if (number(field) == Jump) jump = value != 0; else sneak = value != 0;
}
struct Slot { void (*callback)(void*); int priority, reserved; };
struct Descriptor { char header[0x48]; Slot events[27]; };
struct Context { Jvm* java; void* manager; void* profiles; jobject minecraft; jobject world; jobject player;
                 void* keys; void* targets; unsigned char inGame; };
struct Toggle { long long type; Context* context; void* module; unsigned char on; };
struct Event { long long type; Context* context; };
struct Move { long long type; Context* context; float strafe, forward, friction; };
struct Packet { long long type; Jvm* java; jobject packet; long long time; int id, result; };
}

extern "C" void* stasis_descriptor();
extern "C" void stasis_cleanup(JNIEnv*);
extern "C" void queue_module_toggle(void*, void* module, unsigned char on) {
    require(on == 0, "Stasis unexpectedly enabled another module"); ++queued; lastQueued = module;
}
extern "C" jobject get_minecraft(Jvm*) { return &minecraft; }
extern "C" jobject get_local_player(Jvm*, jobject) { return currentPlayer; }
extern "C" void slinky_logf(const char*, ...) {}
extern "C" void W_label(void*, void*, const char*) {}
extern "C" void log_fmt(void*, int, const char*, ...) {}

int main() {
    try {
        JNINativeInterface_ table{};
        table.PushLocalFrame = push; table.PopLocalFrame = pop; table.FindClass = find;
        table.GetMethodID = method; table.CallObjectMethod = callObject; table.CallIntMethod = callInt;
        table.CallBooleanMethod = callBool;
        table.GetArrayLength = length; table.GetObjectArrayElement = element;
        table.GetObjectClass = objectClass; table.GetSuperclass = superclass;
        table.GetFieldID = field; table.FromReflectedField = fromField; table.GetObjectField = getObject;
        table.NewGlobalRef = global; table.DeleteGlobalRef = releaseGlobal; table.DeleteLocalRef = releaseLocal;
        table.IsSameObject = same; table.IsAssignableFrom = assignable; table.IsInstanceOf = instance;
        table.ExceptionCheck = exception; table.ExceptionClear = clearException;
        table.GetDoubleField = getDouble; table.SetDoubleField = setDouble;
        table.GetBooleanField = getBool; table.GetIntField = getInt;
        table.SetFloatField = setFloat; table.SetBooleanField = setBool;
        JNIEnv env = &table;
        JIds ids{};
        ids.Entity_motionX = id(X); ids.Entity_motionY = id(Y); ids.Entity_motionZ = id(Z);
        ids.Entity_onGround = id(Ground); ids.EntityLivingBase_hurtTime = id(Hurt);
        ids.EntityLivingBase_moveForward = id(MoveForward); ids.EntityLivingBase_moveStrafing = id(MoveStrafe);
        ids.C03PacketPlayer = &c03Class;
        Jvm java{&ids, &env};
        Context context{&java, &world, nullptr, &minecraft, &world, &player, nullptr, nullptr, 1};
        auto* descriptor = static_cast<Descriptor*>(stasis_descriptor());
        require(std::strcmp(*reinterpret_cast<const char**>(descriptor), "Stasis") == 0, "Module name changed");
        auto toggle = [&](bool on) { Toggle e{5, &context, descriptor, static_cast<unsigned char>(on)}; descriptor->events[5].callback(&e); };
        auto tick = [&] { Event e{16, &context}; descriptor->events[16].callback(&e); };
        auto verdict = [&](jobject object, int initial = 0) {
            Packet e{3, &java, object, 0, 0, initial}; descriptor->events[3].callback(&e); return e.result;
        };
        const StasisMotion initial{.25, -.4, .75};
        player = {initial, false, 0};
        toggle(true);
        require(globals == 2, "Player and look-packet references were not retained");
        require(verdict(&packet) == 2, "Movement packets were queued instead of dropped");
        require(verdict(&lookPacket) == 0, "Look-only packets were blocked");
        require(verdict(&otherPacket) == 0, "Non-movement packets were blocked");
        player.hurt = 3;
        require(verdict(&packet) == 0, "Hurt-time exception missing");
        player.hurt = 0;
        require(verdict(&packet, 2) == 2, "Previous cancellation was overwritten");
        for (int i = 0; i < 44; ++i) { tick(); require(equal(player.motion, {0, 0, 0}), "Released before tick 45"); }
        require(!jump && !sneak && forward == 0 && strafe == 0, "Movement input not cleared");
        tick();
        require(equal(player.motion, initial) && verdict(&packet) == 0, "Tick 45 did not release saved motion");
        const StasisMotion afterRelease{.2, -.5, .6};
        player.motion = afterRelease;
        tick();
        require(equal(player.motion, {0, 0, 0}) && verdict(&packet) == 2, "Release lasted more than one tick");
        Move move{21, &context, 1, 1, .91f};
        player.motion = {5, 5, 5}; descriptor->events[21].callback(&move);
        require(move.strafe == 0 && move.forward == 0 && equal(player.motion, {0, 0, 0}), "Strafe not frozen");
        player.motion = {3, 3, 3}; Event post{22, &context}; descriptor->events[22].callback(&post);
        require(equal(player.motion, {0, 0, 0}), "Post-movement motion not frozen");
        toggle(false);
        require(equal(player.motion, afterRelease) && globals == 1, "Disable did not restore motion or release player");
        toggle(true); player.grounded = true; tick();
        require(queued == 1 && lastQueued == descriptor && verdict(&packet) == 0, "Landing did not auto-disable");
        tick(); require(queued == 1, "Landing queued duplicate disables");
        player.grounded = false; toggle(true);
        replacement = {{9, 8, 7}, false, 0}; context.player = &replacement; currentPlayer = &replacement;
        Event frame{1, &context}; descriptor->events[1].callback(&frame);
        require(equal(replacement.motion, {9, 8, 7}) && globals == 1, "Player switch leaked saved motion or reference");
        context.player = &player; currentPlayer = &player; player.motion = initial; toggle(true);
        tick(); Event shutdown{26, &context}; descriptor->events[26].callback(&shutdown);
        require(equal(player.motion, initial), "Unload did not restore motion");
        stasis_cleanup(&env);
        require(globals == 0, "JNI global references leaked");
        std::cout << "PASS: Stasis registration, 45/1 cycle, input freeze, packet exceptions, disable/landing/world-change/unload restoration.\n";
        return 0;
    } catch (const std::exception& error) { std::cerr << error.what() << '\n'; return 1; }
}
