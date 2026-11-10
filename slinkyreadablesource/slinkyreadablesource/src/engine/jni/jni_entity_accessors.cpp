// JNI: entity field accessors.
#include "slinky/engine.h"
#include "slinky/jcall.h"
struct Rot { float yaw, pitch; };
struct Vec3 { double x, y, z; };
FN(Vec3, vec3_make, (double x, double y, double z));
extern "C" void java_set_entity_player_item_in_use_count(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->EntityPlayer_itemInUseCount, a2);
}

extern "C" jobject java_call_entity_player_get_equipment_in_slot(Jvm *j, void *a1, int a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->EntityPlayer_getEquipmentInSlot, a2);
}

extern "C" jobject java_get_entity_player(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->EntityPlayer_Lzs_);
}

extern "C" bool java_get_entity_player_sleeping(Jvm *j, void *a1) {
    return JCALLN(j, GetBooleanField, a1, ids->EntityPlayer_sleeping) != 0;
}

extern "C" jfloat java_get_entity_player_speed_on_ground(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->EntityPlayer_speedOnGround);
}

extern "C" void java_set_entity_player_speed_on_ground(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->EntityPlayer_speedOnGround, a2);
}

extern "C" jint java_get_entity_other_player_mp_other_player_mppos_rotation_increments(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->EntityOtherPlayerMP_otherPlayerMPPosRotationIncrements);
}

extern "C" jdouble java_get_entity_other_player_mp_other_player_mpyaw(Jvm *j, void *a1) {
    return JCALL(j, GetDoubleField, a1, ids->EntityOtherPlayerMP_otherPlayerMPYaw);
}

extern "C" jobject get_block_state_id(Jvm *j, void *a1) {
    return JCALLN(j, CallStaticObjectMethod, ids->Block, ids->Block_getBlockFromItem, a1);
}

extern "C" jint get_block_id(Jvm *j, void *a1) {
    return JCALLN(j, CallStaticIntMethod, ids->Block, ids->Block_getIdFromBlock, a1);
}

extern "C" jobject java_get_tile_entity(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->TileEntity_Laji_);
}

extern "C" jobject java_get_world(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->World_Ljava_util_List_);
}

extern "C" jobject java_get_world_2(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->World_Ljava_util_List__2);
}

extern "C" jobject java_get_world_3(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->World_Ljava_util_List__3);
}

extern "C" jobject java_get_chunk_storage_arrays(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->Chunk_storageArrays);
}

extern "C" jint java_get_chunk_x_position(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Chunk_xPosition);
}

extern "C" jint java_get_chunk_z_position(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Chunk_zPosition);
}

extern "C" jobject java_get_id_b20(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->id_b20);
}

extern "C" jobject java_get_chunk_provider_client(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->ChunkProviderClient_Ljava_util_List_);
}

extern "C" jobject java_get_moving_object_position(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->MovingObjectPosition_Lazw_);
}

extern "C" jobject java_get_moving_object_position_2(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->MovingObjectPosition_Lsa_);
}

extern "C" jobject java_call_list_get(Jvm *j, void *a1, int a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->List_get, a2);
}

extern "C" jobject java_call_map_get(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->Map_get, a2);
}

extern "C" jfloat java_get_game_settings_mouse_sensitivity(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->GameSettings_mouseSensitivity);
}

extern "C" jobject get_block_from_pos(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->_init__getBlock);
}

extern "C" void update_entities(Jvm *j, void *a1) {
    JCALLN(j, CallVoidMethod, a1, ids->World_updateEntities);
}

extern "C" jint java_call_list_size(Jvm *j, void *a1) {
    return JCALLN(j, CallIntMethod, a1, ids->List_size);
}

extern "C" jobject java_jni_is_instance_of_entity_other_player_mp(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->EntityOtherPlayerMP) ? o : nullptr;
}

extern "C" Vec3 java_get_entity_other_player_mp_other_player_mpx(Jvm *j, jobject o) {
    double x = JCALLN(j, GetDoubleField, o, ids->EntityOtherPlayerMP_otherPlayerMPX);
    double y = JCALLN(j, GetDoubleField, o, ids->EntityOtherPlayerMP_otherPlayerMPY);
    double z = JCALLN(j, GetDoubleField, o, ids->EntityOtherPlayerMP_otherPlayerMPZ);
    Vec3 r = vec3_make(x, y, z);

    return r;
}

// ---- hand-written (BlockPos / Vec3i glue, multi-argument calls)
struct I3 { int x, y, z; };
FN(jobject, get_block_from_pos, (Jvm *j, void *a1));

extern "C" __attribute__((noinline)) jobject java_block_pos_init(Jvm *j, int x, int y, int z) {
    return JNI(j)->NewObject(j->env, (jclass)j->ids->BlockPos, (jmethodID)j->ids->BlockPos__init_, x, y, z);
}
extern "C" Vec3 java_get_block_pos_a(Jvm *j, jobject o) {
    double x = JCALLN(j, GetIntField, o, (jfieldID)ids->BlockPos_a);
    double y = JCALLN(j, GetIntField, o, (jfieldID)ids->BlockPos_c);
    double z = JCALLN(j, GetIntField, o, (jfieldID)ids->BlockPos_d);
    Vec3 r = vec3_make(x, y, z);

    return r;
}
extern "C" __attribute__((noinline)) void java_set_block_pos_a(Jvm *j, jobject o, int x, int y, int z) {
    JCALLN(j, SetIntField, o, (jfieldID)ids->BlockPos_a, x);
    JCALLN(j, SetIntField, o, (jfieldID)ids->BlockPos_c, y);
    JCALL(j, SetIntField, o, (jfieldID)ids->BlockPos_d, z);
}
extern "C" I3 get_block_position(Jvm *j, jobject o) {
    I3 r;
    r.x = JCALLN(j, GetIntField, o, (jfieldID)ids->BlockPos_a);
    r.y = JCALLN(j, GetIntField, o, (jfieldID)ids->BlockPos_c);
    r.z = JCALLN(j, GetIntField, o, (jfieldID)ids->BlockPos_d);
    return r;
}
// block position of an entity/object; the mapping set decides whether it goes through a BlockPos
extern "C" Vec3 java_get_block_pos_a_2(Jvm *j, jobject o) {
    int x;
    long yo, zo;
    if (j->ids->variant != 0) {
        o = JCALLN(j, GetObjectField, o, (jfieldID)ids->id_768);
        x = JCALLN(j, GetIntField, o, (jfieldID)ids->BlockPos_a);
        zo = 0x468; yo = 0x460;
    } else {
        x = JCALLN(j, GetIntField, o, (jfieldID)ids->id_770);
        zo = 0x780; yo = 0x778;
    }
    double dx = x;
    double dy = JCALLN(j, GetIntField, o, *(jfieldID *)((char *)ids + yo));
    double dz = JCALLN(j, GetIntField, o, *(jfieldID *)((char *)ids + zo));
    Vec3 r = vec3_make(dx, dy, dz);

    return r;
}
extern "C" jobject java_call_variant(Jvm *j, jobject o, int x, int y, int z) {
    if (j->ids->variant != 0) {
        jobject p = java_block_pos_init(j, x, y, z);
        jobject r = JCALLN(j, CallObjectMethod, o, (jmethodID)ids->id_328, p);
        return get_block_from_pos(j, r);
    }
    return JNI(j)->CallObjectMethod(j->env, o, (jmethodID)j->ids->id_320, x, y, z);
}
extern "C" jobject java_call_id_328(Jvm *j, jobject o, jobject pos, int x, int y, int z) {
    if (pos) {
        java_set_block_pos_a(j, pos, x, y, z);
        jobject r = JCALLN(j, CallObjectMethod, o, (jmethodID)ids->id_328, pos);
        return get_block_from_pos(j, r);
    }
    return JNI(j)->CallObjectMethod(j->env, o, (jmethodID)j->ids->id_320, x, y, z);
}
extern "C" jboolean set_block_state(Jvm *j, jobject o, int a, int b, int c, void *d, int e, int f) {
    return JNI(j)->CallBooleanMethod(j->env, o, (jmethodID)j->ids->World_setBlock, a, b, c, d, e, f);
}
extern "C" jboolean set_block_at_pos(Jvm *j, jobject o, void *a, void *b, int c) {
    return JNI(j)->CallBooleanMethod(j->env, o, (jmethodID)j->ids->World_setBlockState, a, b, c);
}
extern "C" jobject java_world_ray_trace_blocks(Jvm *j, jobject o, void *a, void *b, bool c, bool d, bool e) {
    int c_ = c, d_ = d, e_ = e;

    return JNI(j)->CallObjectMethod(j->env, o, (jmethodID)j->ids->World_rayTraceBlocks, a, b, c_, d_, e_);
}
extern "C" jobject java_world_get_chunk_from_chunk_coords(Jvm *j, jobject o, int a, int b) {
    return JNI(j)->CallObjectMethod(j->env, o, (jmethodID)j->ids->World_getChunkFromChunkCoords, a, b);
}
extern "C" void update_chunk(Jvm *j, jobject o, int a, int b, int c, int d, int e, int f) {
    JNI(j)->CallVoidMethod(j->env, o, (jmethodID)j->ids->World_markBlockRangeForRenderUpdate, a, b, c, d, e, f);
}
extern "C" jobject java_get_chunk_provider_client_2(Jvm *j, jobject o) {
    jobject x = JCALLN(j, GetObjectField, o, (jfieldID)ids->World_Lapu_);
    return JCALLN(j, IsInstanceOf, x, (jclass)ids->ChunkProviderClient) ? x : nullptr;
}
extern "C" void set_block_meta(Jvm *j, jobject o, int a, int b) {
    JNI(j)->CallVoidMethod(j->env, o, (jmethodID)j->ids->ChunkProviderClient_unloadChunk, a, b);
}
extern "C" jobject java_moving_object_position_init(Jvm *j, void *a, void *b) {
    return JNI(j)->NewObject(j->env, (jclass)j->ids->MovingObjectPosition, (jmethodID)j->ids->MovingObjectPosition__init_, a, b);
}

FN(jint, java_call_enum_ordinal, (Jvm *j, void *a1));     // enum ordinal
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));        // a + b
FN(Vec3, java_get_block_pos_a, (Jvm *j, jobject o));
FN(I3, get_block_position, (Jvm *j, jobject o));
extern "C" const Vec3 rdata_14f780[];    // facing -> unit offset

extern "C" jobject java_axis_aligned_bb_calculate_intercept(Jvm *j, jobject o, void *a, void *b) {
    return JNI(j)->CallObjectMethod(j->env, o, (jmethodID)j->ids->AxisAlignedBB_calculateIntercept, a, b);
}
extern "C" jint java_get_moving_object_position_3(Jvm *j, jobject o) {
    jobject x = JCALLN(j, GetObjectField, o, (jfieldID)ids->MovingObjectPosition__);
    return java_call_enum_ordinal(j, x);
}
// side hit of a MovingObjectPosition (EnumFacing ordinal)
extern "C" jint java_get_variant(Jvm *j, jobject o) {
    if (j->ids->variant != 0) {
        jobject x = JCALLN(j, GetObjectField, o, (jfieldID)ids->id_3f0);
        return java_call_enum_ordinal(j, x);
    }
    return JCALL(j, GetIntField, o, (jfieldID)ids->id_3e8);
}
// block position of a MovingObjectPosition
extern "C" Vec3 java_get_moving_object_position_block_x(Jvm *j, jobject o) {
    if (j->ids->variant != 0) {
        jobject p = JCALLN(j, GetObjectField, o, (jfieldID)ids->MovingObjectPosition_Lcj_);
        return java_get_block_pos_a(j, p);
    }
    double x = JCALLN(j, GetIntField, o, (jfieldID)ids->MovingObjectPosition_blockX);
    double y = JCALLN(j, GetIntField, o, (jfieldID)ids->MovingObjectPosition_blockY);
    double z = JCALLN(j, GetIntField, o, (jfieldID)ids->MovingObjectPosition_blockZ);
    return vec3_make(x, y, z);
}
extern "C" I3 java_get_moving_object_position_block_x_2(Jvm *j, jobject o) {
    if (j->ids->variant != 0) {
        jobject p = JCALLN(j, GetObjectField, o, (jfieldID)ids->MovingObjectPosition_Lcj_);
        return get_block_position(j, p);
    }
    I3 r;
    r.x = JCALLN(j, GetIntField, o, (jfieldID)ids->MovingObjectPosition_blockX);
    r.y = JCALLN(j, GetIntField, o, (jfieldID)ids->MovingObjectPosition_blockY);
    r.z = JCALLN(j, GetIntField, o, (jfieldID)ids->MovingObjectPosition_blockZ);
    return r;
}
// block position + facing offset (the block the hit side points to)
extern "C" Vec3 java_get_variant_2(Jvm *j, jobject o) {
    Vec3 p = java_get_moving_object_position_block_x(j, o);
    unsigned side;
    if (j->ids->variant != 0) {
        jobject x = JCALLN(j, GetObjectField, o, (jfieldID)ids->id_3f0);
        side = java_call_enum_ordinal(j, x);
    } else {
        side = JCALLN(j, GetIntField, o, (jfieldID)ids->id_3e8);
    }
    return vec3_add(p, rdata_14f780[side]);
}
extern "C" jobject java_get_game_settings_key_bindings(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, (jfieldID)ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, 1);
}
extern "C" jobject java_get_game_settings_key_bindings_2(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, (jfieldID)ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, ids->variant == 0 ? 8 : 9);
}

// ---- JNI bridge functions found by the gap scan (no caller outside the virtualised init) ----
extern "C" jobject java_call_world_get_entity_by_id(Jvm *j, void *a1, int a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->World_getEntityByID, a2);
}

extern "C" jboolean java_call_list_add(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallBooleanMethod, a1, ids->List_add, a2);
}

// ---- functions the first gap scan missed (padding / tail-jump neighbours) ----
extern "C" jboolean java_call_list_remove(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallBooleanMethod, a1, ids->List_remove, a2);
}

extern "C" bool java_call_map_contains_key(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallBooleanMethod, a1, ids->Map_containsKey, a2) != 0;
}
