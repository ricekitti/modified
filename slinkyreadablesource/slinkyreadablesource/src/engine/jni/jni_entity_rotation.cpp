// JNI: entity rotation and angle accessors.
#include "slinky/engine.h"
#include "slinky/jcall.h"
struct Rot { float yaw, pitch; };
struct Vec3 { double x, y, z; };
FN(Vec3, vec3_make, (double x, double y, double z));
FN(Rot, rot_make, (float yaw, float pitch));
FN(float, limit_rotation_delta, (float lo, float hi, float v));
FN(float, libc_sinf, (float x));   // sinf
FN(float, libc_cosf, (float x));   // cosf
typedef int v4i __attribute__((vector_size(16), aligned(16)));
typedef float v4f __attribute__((vector_size(16), aligned(16)));
extern "C" const float rdata_14f740, rdata_14f744, rdata_14f748, rdata_14f74c, rdata_14f778, rdata_14f77c;
extern "C" const double rdata_14f750, rdata_14f758, rdata_14f770;
extern "C" const v4i rdata_14f760;   // sign mask

extern "C" bool java_variant(Jvm *j) { return j->ids->variant == 0; }

// rotationYaw/rotationPitch packed into a Rot
extern "C" Rot get_entity_rotation(Jvm *j, jobject o) {
    float y = JCALLN(j, GetFloatField, o, ids->Entity_rotationYaw);
    float p = JCALLN(j, GetFloatField, o, ids->Entity_rotationPitch);
    return rot_make(y, p);
}
extern "C" Rot get_entity_prev_rotation(Jvm *j, jobject o) {
    float y = JCALLN(j, GetFloatField, o, ids->Entity_prevRotationYaw);
    float p = JCALLN(j, GetFloatField, o, ids->Entity_prevRotationPitch);
    return rot_make(y, p);
}
// set yaw, pitch clamped to [lo, hi]
extern "C" void set_entity_rotation(Jvm *j, jobject o, Rot r) {
    JCALLN(j, SetFloatField, o, ids->Entity_rotationYaw, r.yaw);
    JIds *ids = j->ids; JNIEnv *env = j->env;
    auto f = (*env)->SetFloatField; jfieldID id = ids->Entity_rotationPitch;

    f(env, o, id, limit_rotation_delta(rdata_14f740, rdata_14f744, r.pitch));
}
extern "C" void set_entity_prev_rotation(Jvm *j, jobject o, Rot r) {
    JCALLN(j, SetFloatField, o, ids->Entity_prevRotationYaw, r.yaw);
    JIds *ids = j->ids; JNIEnv *env = j->env;
    auto f = (*env)->SetFloatField; jfieldID id = ids->Entity_prevRotationPitch;

    f(env, o, id, limit_rotation_delta(rdata_14f748, rdata_14f74c, r.pitch));
}
// look vector from the entity rotation
extern "C" Vec3 java_get_entity_rotation_pitch(Jvm *j, jobject o) {
    float y = JCALLN(j, GetFloatField, o, ids->Entity_rotationYaw);
    float p = JCALLN(j, GetFloatField, o, ids->Entity_rotationPitch);
    Rot r = rot_make(y, p);
    float a = (float)((double)r.yaw * rdata_14f750 / rdata_14f758 + rdata_14f750);
    float ca = libc_cosf(a);
    float sa = libc_sinf(a);
    float b = (float)((double)r.pitch * rdata_14f750 / rdata_14f758);
    float cb = ((v4f)((v4i)(v4f){libc_cosf(b)} ^ rdata_14f760))[0];
    float sb = libc_sinf(b);
    float x = sa * cb;
    Vec3 v = vec3_make(x, sb, cb * ca);

    return v;
}
extern "C" void java_set_entity_motion_x(Jvm *j, jobject o, Vec3 v) {
    JCALLN(j, SetDoubleField, o, ids->Entity_motionX, v.x);
    JCALLN(j, SetDoubleField, o, ids->Entity_motionY, v.y);
    JCALL(j, SetDoubleField, o, ids->Entity_motionZ, v.z);
}
extern "C" void set_motion(Jvm *j, jobject o, float x, float y, float z) {
    JNI(j)->CallVoidMethod(j->env, o, j->ids->Entity_moveFlying, (double)x, (double)y, (double)z);
}
extern "C" jobject java_call_entity_living_base_get_position(Jvm *j, jobject o) {
    return JCALL1(j, CallObjectMethod, o, ids->EntityLivingBase_getPosition, rdata_14f770);
}
static inline __attribute__((always_inline)) bool isnan_bits(float f) {
    return (__builtin_bit_cast(unsigned, f) & 0x7fffffff) >= 0x7f800001;
}
extern "C" float java_call_entity_living_base_get_health(Jvm *j, jobject o) {
    float f = JCALLN(j, CallFloatMethod, o, ids->EntityLivingBase_getHealth);
    return isnan_bits(f) ? rdata_14f778 : f;
}
extern "C" float java_call_entity_living_base_get_max_health(Jvm *j, jobject o) {
    float f = JCALLN(j, CallFloatMethod, o, ids->EntityLivingBase_getMaxHealth);
    return isnan_bits(f) ? rdata_14f77c : f;
}
extern "C" float java_call_entity_player_get_absorption_amount(Jvm *j, jobject o) {
    float f = JCALLN(j, CallFloatMethod, o, ids->EntityPlayer_getAbsorptionAmount);
    if (__builtin_expect(isnan_bits(f), 0)) f = 0.0f;
    return f;
}

extern "C" jobject java_get_entity(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Entity_Lazt_);
}

extern "C" void java_call_entity_set_sprinting(Jvm *j, void *a1, unsigned char a2) {
    JCALL1(j, CallVoidMethod, a1, ids->Entity_setSprinting, a2);
}

extern "C" jint java_get_entity_entity_id(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Entity_entityId);
}

extern "C" jobject java_get_entity_2(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Entity_Ljava_util_UUID_);
}

extern "C" jint java_get_entity_ticks_existed(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Entity_ticksExisted);
}

extern "C" bool java_get_entity_on_ground(Jvm *j, void *a1) {
    return JCALLN(j, GetBooleanField, a1, ids->Entity_onGround) != 0;
}

extern "C" bool java_get_entity_is_dead(Jvm *j, void *a1) {
    return JCALLN(j, GetBooleanField, a1, ids->Entity_isDead) != 0;
}

extern "C" jfloat java_get_entity_fall_distance(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->Entity_fallDistance);
}

extern "C" jint java_call_static_entity_list_get_entity_id(Jvm *j, void *a1) {
    return JCALLN(j, CallStaticIntMethod, ids->EntityList, ids->EntityList_getEntityID, a1);
}

extern "C" jobject java_get_entity_3(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Entity_Lsa_);
}

extern "C" jint java_get_entity_living_base_last_attacker_time(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->EntityLivingBase_lastAttackerTime);
}

extern "C" jint java_get_entity_living_base_hurt_time(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->EntityLivingBase_hurtTime);
}

extern "C" void java_set_entity_living_base_max_hurt_time(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->EntityLivingBase_maxHurtTime, a2);
}

extern "C" jfloat java_get_entity_living_base_move_forward(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->EntityLivingBase_moveForward);
}

extern "C" jfloat java_get_entity_living_base_move_strafing(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->EntityLivingBase_moveStrafing);
}

extern "C" jobject java_call_id_500(Jvm *j, void *a1, double a2, float a3) {
    return JCALL1(j, CallObjectMethod, a1, ids->id_500, a2, (double)a3);
}

extern "C" jfloat java_get_entity_living_base_land_movement_factor(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->EntityLivingBase_landMovementFactor);
}

extern "C" jint get_hurt_time(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->EntityLivingBase_jumpTicks);
}

extern "C" void java_set_entity_living_base_jump_ticks(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->EntityLivingBase_jumpTicks, a2);
}

extern "C" void java_jni_call_nonvirtual_void_method_entity_living_base_swing_item(Jvm *j, void *a1) {
    JCALLN(j, CallNonvirtualVoidMethod, a1, ids->EntityLivingBase, ids->EntityLivingBase_swingItem);
}

extern "C" jobject java_call_entity_living_base_get_active_potion_effect(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->EntityLivingBase_getActivePotionEffect, a2);
}

extern "C" void java_entity_player_sp_render_arm_pitch(Jvm *j, void *a1, Rot a2) {
    JNI(j)->SetFloatField(j->env, a1, j->ids->EntityPlayerSP_renderArmYaw, a2.yaw);
    JNI(j)->SetFloatField(j->env, a1, j->ids->EntityPlayerSP_renderArmPitch, a2.pitch);
}

extern "C" void java_entity_player_sp_prev_render_arm_pitch(Jvm *j, void *a1, Rot a2) {
    JNI(j)->SetFloatField(j->env, a1, j->ids->EntityPlayerSP_prevRenderArmYaw, a2.yaw);
    JNI(j)->SetFloatField(j->env, a1, j->ids->EntityPlayerSP_prevRenderArmPitch, a2.pitch);
}

extern "C" void java_call_entity_player_sp_add_chat_message(Jvm *j, void *a1, void *a2) {
    JCALLN(j, CallVoidMethod, a1, ids->EntityPlayerSP_addChatMessage, a2);
}

extern "C" void java_set_entity_player_sp_time_in_portal(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->EntityPlayerSP_timeInPortal, a2);
}

extern "C" void java_set_entity_player_sp_prev_time_in_portal(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->EntityPlayerSP_prevTimeInPortal, a2);
}

extern "C" jobject java_get_entity_player_2(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->EntityPlayer_Lyx_);
}

extern "C" bool java_call_entity_player_is_invisible_to_player(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallBooleanMethod, a1, ids->EntityPlayer_isInvisibleToPlayer, a2) != 0;
}

extern "C" jint java_get_entity_player_item_in_use_count(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->EntityPlayer_itemInUseCount);
}

extern "C" bool java_call_entity_is_sprinting(Jvm *j, void *a1) {
    return JCALLN(j, CallBooleanMethod, a1, ids->Entity_isSprinting) != 0;
}

extern "C" bool is_sneaking(Jvm *j, void *a1) {
    return JCALLN(j, CallBooleanMethod, a1, ids->Entity_isSneaking) != 0;
}

extern "C" jobject java_call_entity_get_formatted_command_sender_name(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->Entity_getFormattedCommandSenderName);
}

extern "C" jobject java_call_entity_get_command_sender_name(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->Entity_getCommandSenderName);
}

extern "C" jboolean java_call_entity_can_be_collided_with(Jvm *j, void *a1) {
    return JCALLN(j, CallBooleanMethod, a1, ids->Entity_canBeCollidedWith);
}

extern "C" jobject java_call_entity_living_base_get_team(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->EntityLivingBase_getTeam);
}

extern "C" bool java_call_entity_player_is_using_item(Jvm *j, void *a1) {
    return JCALLN(j, CallBooleanMethod, a1, ids->EntityPlayer_isUsingItem) != 0;
}

extern "C" Vec3 java_get_entity_pos_x(Jvm *j, jobject o) {
    double x = JCALLN(j, GetDoubleField, o, ids->Entity_posX);
    double y = JCALLN(j, GetDoubleField, o, ids->Entity_posY);
    double z = JCALLN(j, GetDoubleField, o, ids->Entity_posZ);
    Vec3 r = vec3_make(x, y, z);

    return r;
}

extern "C" Vec3 java_get_entity_prev_pos_x(Jvm *j, jobject o) {
    double x = JCALLN(j, GetDoubleField, o, ids->Entity_prevPosX);
    double y = JCALLN(j, GetDoubleField, o, ids->Entity_prevPosY);
    double z = JCALLN(j, GetDoubleField, o, ids->Entity_prevPosZ);
    Vec3 r = vec3_make(x, y, z);

    return r;
}

extern "C" Vec3 java_get_entity_motion_x(Jvm *j, jobject o) {
    double x = JCALLN(j, GetDoubleField, o, ids->Entity_motionX);
    double y = JCALLN(j, GetDoubleField, o, ids->Entity_motionY);
    double z = JCALLN(j, GetDoubleField, o, ids->Entity_motionZ);
    Vec3 r = vec3_make(x, y, z);

    return r;
}

extern "C" jobject java_jni_is_instance_of_entity_living_base(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->EntityLivingBase) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_entity_player(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->EntityPlayer) ? o : nullptr;
}

// ---- JNI bridge functions found by the gap scan (no caller outside the virtualised init) ----
extern "C" void java_call_entity_set_angles(Jvm *j, void *a1, float a2, float a3) {
    JCALL1(j, CallVoidMethod, a1, ids->Entity_setAngles, ({ double y = a2;  y; }), (double)a3);
}

extern "C" jdouble java_get_entity_motion_x_2(Jvm *j, void *a1) {
    return JCALL(j, GetDoubleField, a1, ids->Entity_motionX);
}

extern "C" void java_call_entity_living_base_jump(Jvm *j, void *a1) {
    JCALLN(j, CallVoidMethod, a1, ids->EntityLivingBase_jump);
}

// ---- gap-scan functions: render-arm rotation of the local player, casts ----
extern "C" Rot java_get_entity_player_sp_render_arm_pitch(Jvm *j, jobject o) {   // renderArmYaw / renderArmPitch
    float y = JCALLN(j, GetFloatField, o, ids->EntityPlayerSP_renderArmYaw);
    float p = JCALLN(j, GetFloatField, o, ids->EntityPlayerSP_renderArmPitch);
    return Rot{y, p};
}
extern "C" Rot java_get_entity_player_sp_prev_render_arm_pitch(Jvm *j, jobject o) {   // prevRenderArmYaw / prevRenderArmPitch
    float y = JCALLN(j, GetFloatField, o, ids->EntityPlayerSP_prevRenderArmYaw);
    float p = JCALLN(j, GetFloatField, o, ids->EntityPlayerSP_prevRenderArmPitch);
    return Rot{y, p};
}
extern "C" jobject java_jni_is_instance_of_entity_player_sp(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->EntityPlayerSP) ? o : nullptr;
}

// ---- functions the first gap scan missed (padding / tail-jump neighbours) ----
extern "C" jdouble java_get_entity_motion_y(Jvm *j, void *a1) {
    return JCALL(j, GetDoubleField, a1, ids->Entity_motionY);
}

extern "C" jdouble java_get_entity_motion_z(Jvm *j, void *a1) {
    return JCALL(j, GetDoubleField, a1, ids->Entity_motionZ);
}
