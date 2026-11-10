// JNI: world and chunk accessors.
#include "slinky/engine.h"
#include "slinky/jcall.h"
struct Rot { float yaw, pitch; };
struct Vec3 { double x, y, z; };
FN(Vec3, vec3_make, (double x, double y, double z));
extern "C" jobject java_get_network_player_info(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->NetworkPlayerInfo_Leu_);
}

extern "C" jobject java_new_chat_component_text_init(Jvm *j, void *a1) {
    return JCALLN(j, NewObject, ids->ChatComponentText, ids->ChatComponentText__init_, a1);
}

extern "C" jobject java_call_iinventory_get_stack_in_slot(Jvm *j, void *a1, int a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->IInventory_getStackInSlot, a2);
}

extern "C" jobject java_get_container_player(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->ContainerPlayer_Laae_);
}

extern "C" jint java_get_slot_slot_number(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Slot_slotNumber);
}

extern "C" jobject java_get_inventory_basic(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->InventoryBasic_Ljava_lang_String_);
}

extern "C" jobject java_call_channel_pipeline_get(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->ChannelPipeline_get, a2);
}

extern "C" jobject java_call_channel_pipeline_remove(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->ChannelPipeline_remove, a2);
}

extern "C" bool java_get_c03_packet_player_moving(Jvm *j, void *a1) {
    return JCALLN(j, GetBooleanField, a1, ids->C03PacketPlayer_moving) != 0;
}

extern "C" bool java_get_c03_packet_player_on_ground(Jvm *j, void *a1) {
    return JCALLN(j, GetBooleanField, a1, ids->C03PacketPlayer_onGround) != 0;
}

extern "C" jdouble java_get_c03_packet_player_b(Jvm *j, void *a1) {
    return JCALL(j, GetDoubleField, a1, ids->C03PacketPlayer_b);
}

extern "C" jobject java_get_c08_packet_player_block_placement(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->C08PacketPlayerBlockPlacement_Ladd_);
}

extern "C" jint java_get_s12_packet_entity_velocity_entity_id(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S12PacketEntityVelocity_entityID);
}

extern "C" jint java_get_s14_packet_entity_entity_id(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S14PacketEntity_entityId);
}

extern "C" jbyte java_jni_get_byte_field_s14_packet_entity_pos_x(Jvm *j, void *a1) {
    return JCALL(j, GetByteField, a1, ids->S14PacketEntity_posX);
}

extern "C" jbyte java_jni_get_byte_field_s14_packet_entity_pos_y(Jvm *j, void *a1) {
    return JCALL(j, GetByteField, a1, ids->S14PacketEntity_posY);
}

extern "C" jbyte java_jni_get_byte_field_s14_packet_entity_pos_z(Jvm *j, void *a1) {
    return JCALL(j, GetByteField, a1, ids->S14PacketEntity_posZ);
}

extern "C" jbyte java_jni_get_byte_field_s14_packet_entity_yaw(Jvm *j, void *a1) {
    return JCALL(j, GetByteField, a1, ids->S14PacketEntity_yaw);
}

extern "C" bool java_get_s14_packet_entity_g(Jvm *j, void *a1) {
    return JCALLN(j, GetBooleanField, a1, ids->S14PacketEntity_g) != 0;
}

extern "C" jint java_get_s18_packet_entity_teleport_entity_id(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S18PacketEntityTeleport_entityId);
}

extern "C" jint java_get_s18_packet_entity_teleport_pos_x(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S18PacketEntityTeleport_posX);
}

extern "C" jint java_get_s18_packet_entity_teleport_pos_y(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S18PacketEntityTeleport_posY);
}

extern "C" jint java_get_s18_packet_entity_teleport_pos_z(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S18PacketEntityTeleport_posZ);
}

extern "C" jbyte java_jni_get_byte_field_s18_packet_entity_teleport_yaw(Jvm *j, void *a1) {
    return JCALL(j, GetByteField, a1, ids->S18PacketEntityTeleport_yaw);
}

extern "C" jint java_get_s19_packet_entity_status_entity_id(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S19PacketEntityStatus_entityId);
}

extern "C" jfloat java_get_s27_packet_explosion_f(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->S27PacketExplosion_f);
}

extern "C" jfloat java_get_s27_packet_explosion_h(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->S27PacketExplosion_h);
}

extern "C" jobject java_get_static_init_2(Jvm *j) {
    return JCALL(j, GetStaticObjectField, ids->_init__2, ids->id_c80);
}

extern "C" jobject java_new_c16_packet_client_status(Jvm *j, void *a1) {
    return JCALLN(j, NewObject, ids->C16PacketClientStatus, ids->id_c70, a1);
}

extern "C" jobject java_call_network_player_info_get_player_team(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->NetworkPlayerInfo_getPlayerTeam);
}

extern "C" jint java_call_object_hash_code(Jvm *j, void *a1) {
    return JCALLN(j, CallIntMethod, a1, ids->Object_hashCode);
}

extern "C" jobject java_call_ichat_component_get_formatted_text(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->IChatComponent_getFormattedText);
}

extern "C" jobject java_call_slot_get_stack(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->Slot_getStack);
}

extern "C" jobject java_call_channel_pipeline(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->Channel_pipeline);
}

extern "C" jobject java_jni_is_instance_of_inventory_basic(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->InventoryBasic) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c03_packet_player(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C03PacketPlayer) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c07_packet_player_digging(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C07PacketPlayerDigging) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c08_packet_player_block_placement(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C08PacketPlayerBlockPlacement) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c02_packet_use_entity(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C02PacketUseEntity) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c0_bpacket_entity_action(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C0BPacketEntityAction) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s12_packet_entity_velocity(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S12PacketEntityVelocity) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s14_packet_entity(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S14PacketEntity) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s18_packet_entity_teleport(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S18PacketEntityTeleport) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s19_packet_entity_status(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S19PacketEntityStatus) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s29_packet_sound_effect(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S29PacketSoundEffect) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s06_packet_update_health(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S06PacketUpdateHealth) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s08_packet_player_pos_look(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S08PacketPlayerPosLook) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s27_packet_explosion(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S27PacketExplosion) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s00_packet_keep_alive(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S00PacketKeepAlive) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c00_packet_keep_alive(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C00PacketKeepAlive) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c0_epacket_click_window(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C0EPacketClickWindow) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c16_packet_client_status(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C16PacketClientStatus) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->c) ? o : nullptr;
}

extern "C" Vec3 java_get_c03_packet_player_a(Jvm *j, jobject o) {
    double x = JCALLN(j, GetDoubleField, o, ids->C03PacketPlayer_a);
    double y = JCALLN(j, GetDoubleField, o, ids->C03PacketPlayer_b);
    double z = JCALLN(j, GetDoubleField, o, ids->C03PacketPlayer_c);
    Vec3 r = vec3_make(x, y, z);

    return r;
}

// ---- hand-written
FN(jint, java_call_enum_ordinal, (Jvm *j, void *a1));     // enum ordinal

// enum field: object (ordinal via java_call_enum_ordinal) in the newer mappings, plain int in the old ones
extern "C" jint java_c07_packet_player_digging_status(Jvm *j, jobject o) {
    JIds *ids = j->ids; JNIEnv *env = j->env;
    auto vt = *env; jfieldID id = (jfieldID)ids->C07PacketPlayerDigging_status;
    if (ids->variant != 0)
        return java_call_enum_ordinal(j, vt->GetObjectField(env, o, id));
    return vt->GetIntField(env, o, id);
}
extern "C" jint java_c0_bpacket_entity_action(Jvm *j, jobject o) {
    JIds *ids = j->ids; JNIEnv *env = j->env;
    auto vt = *env; jfieldID id = (jfieldID)ids->C0BPacketEntityAction_Lis_a_;
    if (ids->variant != 0)
        return java_call_enum_ordinal(j, vt->GetObjectField(env, o, id));
    return vt->GetIntField(env, o, id);
}
extern "C" jobject java_call_channel_pipeline_add_before(Jvm *j, jobject o, const char *a, const char *b, void *c) {
    JNIEnv *env = j->env;
    jstring sa = (*env)->NewStringUTF(env, a);
    jstring sb = (*env)->NewStringUTF(env, b);
    return JCALL1(j, CallObjectMethod, o, (jmethodID)ids->ChannelPipeline_addBefore, sa, sb, c);
}
extern "C" jint java_get_c16_packet_client_status(Jvm *j, jobject o) {
    jobject e = JCALLN(j, GetObjectField, o, (jfieldID)ids->C16PacketClientStatus_Lit_);
    return java_call_enum_ordinal(j, e);
}

// ---- JNI bridge functions found by the gap scan (no caller outside the virtualised init) ----
extern "C" jobject java_call_static_ichat_component_serializer_json_to_component(Jvm *j, void *a1) {
    return JCALLN(j, CallStaticObjectMethod, ids->IChatComponent_Serializer, ids->IChatComponent_Serializer_jsonToComponent, a1);
}

extern "C" jdouble java_get_c03_packet_player_a_2(Jvm *j, void *a1) {
    return JCALL(j, GetDoubleField, a1, ids->C03PacketPlayer_a);
}

extern "C" jdouble java_get_c03_packet_player_c(Jvm *j, void *a1) {
    return JCALL(j, GetDoubleField, a1, ids->C03PacketPlayer_c);
}

extern "C" jint java_get_s12_packet_entity_velocity_motion_x(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S12PacketEntityVelocity_motionX);
}

extern "C" jbyte java_jni_get_byte_field_s14_packet_entity_pitch(Jvm *j, void *a1) {
    return JCALL(j, GetByteField, a1, ids->S14PacketEntity_pitch);
}

extern "C" jbyte java_jni_get_byte_field_s18_packet_entity_teleport_pitch(Jvm *j, void *a1) {
    return JCALL(j, GetByteField, a1, ids->S18PacketEntityTeleport_pitch);
}

extern "C" void java_set_s27_packet_explosion_f(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->S27PacketExplosion_f, a2);
}

extern "C" void java_set_s27_packet_explosion_h(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->S27PacketExplosion_h, a2);
}

extern "C" jobject java_jni_is_instance_of_s3_fpacket_custom_payload(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S3FPacketCustomPayload) ? o : nullptr;
}

// ---- functions the first gap scan missed (padding / tail-jump neighbours) ----
extern "C" void java_set_s12_packet_entity_velocity_motion_x(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->S12PacketEntityVelocity_motionX, a2);
}

extern "C" jint java_get_s12_packet_entity_velocity_motion_y(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S12PacketEntityVelocity_motionY);
}

extern "C" void java_set_s12_packet_entity_velocity_motion_y(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->S12PacketEntityVelocity_motionY, a2);
}

extern "C" jint java_get_s12_packet_entity_velocity_motion_z(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->S12PacketEntityVelocity_motionZ);
}

extern "C" void java_set_s12_packet_entity_velocity_motion_z(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->S12PacketEntityVelocity_motionZ, a2);
}

extern "C" jfloat java_get_s27_packet_explosion_g(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->S27PacketExplosion_g);
}

extern "C" void java_set_s27_packet_explosion_g(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->S27PacketExplosion_g, a2);
}

extern "C" jobject java_get_s3_fpacket_custom_payload(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->S3FPacketCustomPayload_Ljava_lang_String_);
}

extern "C" jint java_call_packet_buffer_readable_bytes(Jvm *j, void *a1) {
    return JCALLN(j, CallIntMethod, a1, ids->PacketBuffer_readableBytes);
}

extern "C" jint java_call_packet_buffer_reader_index(Jvm *j, void *a1) {
    return JCALLN(j, CallIntMethod, a1, ids->PacketBuffer_readerIndex);
}

// S3FPacketCustomPayload payload as a byte[]: 1.7 keeps a byte[] field; 1.8 keeps a PacketBuffer whose readable
// bytes are copied into a new array (getBytes(readerIndex, array))
extern "C" jobject java_get_s3_fpacket_custom_payload_data(Jvm *j, void *pkt) {
    jobject data = JCALLN(j, GetObjectField, pkt, ids->S3FPacketCustomPayload_data);
    if (j->ids->variant == 0) return data;
    jint len = JCALLN(j, CallIntMethod, data, ids->PacketBuffer_readableBytes);
    jint idx = JCALLN(j, CallIntMethod, data, ids->PacketBuffer_readerIndex);
    jobject arr = JCALLN(j, NewByteArray, len);
    if (!arr) return nullptr;
    JCALLN(j, CallObjectMethod, data, ids->PacketBuffer_getBytes, idx, arr);
    return arr;
}

extern "C" jobject java_call_packet_buffer_get_bytes(Jvm *j, void *buf, int idx, jobject arr) {
    return JCALL1(j, CallObjectMethod, buf, ids->PacketBuffer_getBytes, idx, arr);
}
