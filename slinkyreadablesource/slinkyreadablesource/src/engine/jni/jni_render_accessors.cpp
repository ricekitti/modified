// JNI: render-manager accessors (camera fields).
#include "slinky/engine.h"
#include "slinky/jcall.h"
struct Rot { float yaw, pitch; };
struct Vec3 { double x, y, z; };
extern "C" jobject java_new_c_init(Jvm *j, int a1) {
    return JCALLN(j, NewObject, ids->c, ids->c__init_, a1);
}

extern "C" jobject java_get_c01_packet_chat_message(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->C01PacketChatMessage_Ljava_lang_String_);
}

extern "C" void java_set_c01_packet_chat_message(Jvm *j, void *a1, void *a2) {
    JCALL(j, SetObjectField, a1, ids->C01PacketChatMessage_Ljava_lang_String_, a2);
}

extern "C" jobject java_get_c14_packet_tab_complete(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->C14PacketTabComplete_Ljava_lang_String_);
}

extern "C" jobject java_get_static_render_manager(Jvm *j) {
    return JCALL(j, GetStaticObjectField, ids->RenderManager, ids->RenderManager_Lbnn_);
}

extern "C" jfloat java_get_render_manager_player_view_x(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->RenderManager_playerViewX);
}

extern "C" void java_set_render_manager_player_view_x(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->RenderManager_playerViewX, a2);
}

extern "C" jfloat java_get_render_manager_player_view_y(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->RenderManager_playerViewY);
}

extern "C" void java_set_render_manager_player_view_y(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->RenderManager_playerViewY, a2);
}

extern "C" jobject java_get_render_manager(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->RenderManager_Lbbu_);
}

extern "C" jobject java_get_score_player_team(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->ScorePlayerTeam_Ljava_lang_String_);
}

extern "C" void call_get_mouse_over(Jvm *j, void *a1, float a2) {
    JCALL1(j, CallVoidMethod, a1, ids->EntityRenderer_getMouseOver, (double)a2);
}

extern "C" void call_orient_camera(Jvm *j, void *a1, float a2) {
    JCALL1(j, CallVoidMethod, a1, ids->EntityRenderer_orientCamera, (double)a2);
}

extern "C" void set_render_manager_boolean(Jvm *j, void *a1, unsigned char a2) {
    JCALL(j, SetBooleanField, a1, ids->RenderGlobal_displayListEntitiesDirty, a2);
}

extern "C" jboolean java_call_renderer_living_entity(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallBooleanMethod, a1, ids->RendererLivingEntity__, a2);
}

extern "C" jint java_call_font_renderer_get_string_width(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallIntMethod, a1, ids->FontRenderer_getStringWidth, a2);
}

extern "C" void java_set_potion_effect_duration(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->PotionEffect_duration, a2);
}

extern "C" jint java_get_static_display_width(Jvm *j) {
    return JCALL(j, GetStaticIntField, ids->Display, ids->Display_width);
}

extern "C" jint java_get_static_display_height(Jvm *j) {
    return JCALL(j, GetStaticIntField, ids->Display, ids->Display_height);
}

extern "C" jint display_width(Jvm *j) {
    return JCALL(j, GetStaticIntField, ids->Display, ids->Display_framebufferWidth);
}

extern "C" jint display_height(Jvm *j) {
    return JCALL(j, GetStaticIntField, ids->Display, ids->Display_framebufferHeight);
}

extern "C" jfloat java_get_render_item_z_level(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->RenderItem_zLevel);
}

extern "C" void java_set_render_item_z_level(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->RenderItem_zLevel, a2);
}

extern "C" jfloat java_get_player_controller_mp_cur_block_damage_mp(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->PlayerControllerMP_curBlockDamageMP);
}

extern "C" void java_set_player_controller_mp_cur_block_damage_mp(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->PlayerControllerMP_curBlockDamageMP, a2);
}

extern "C" jint java_get_player_controller_mp_block_hit_delay(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->PlayerControllerMP_blockHitDelay);
}

extern "C" void java_set_player_controller_mp_block_hit_delay(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->PlayerControllerMP_blockHitDelay, a2);
}

extern "C" jboolean java_get_player_controller_mp_is_hitting_block(Jvm *j, void *a1) {
    return JCALL(j, GetBooleanField, a1, ids->PlayerControllerMP_isHittingBlock);
}

extern "C" void java_set_player_controller_mp_is_hitting_block(Jvm *j, void *a1, unsigned char a2) {
    JCALL(j, SetBooleanField, a1, ids->PlayerControllerMP_isHittingBlock, a2);
}

extern "C" jobject java_call_nbttag_list_get_compound_tag_at(Jvm *j, void *a1, int a2) {
    return JCALLN(j, CallObjectMethod, a1, ids->NBTTagList_getCompoundTagAt, a2);
}

extern "C" jshort java_jni_call_short_method_nbttag_compound_get_short(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallShortMethod, a1, ids->NBTTagCompound_getShort, a2);
}

extern "C" jboolean java_get_entity_arrow_in_ground(Jvm *j, void *a1) {
    return JCALL(j, GetBooleanField, a1, ids->EntityArrow_inGround);
}

extern "C" jobject java_call_static_stat_collector_translate_to_local(Jvm *j, void *a1) {
    return JCALLN(j, CallStaticObjectMethod, ids->StatCollector, ids->StatCollector_translateToLocal, a1);
}

extern "C" jint java_call_enum_ordinal(Jvm *j, void *a1) {
    return JCALLN(j, CallIntMethod, a1, ids->Enum_ordinal);
}

extern "C" void gl_begin_2d(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_enableCull);
}

extern "C" void gl_reset_state_1(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_disableLighting);
}

extern "C" void gl_reset_state_2(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_enableDepth);
}

extern "C" void gl_push_state(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_disableDepth);
}

extern "C" void gl_pop_state(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_enableBlend);
}

extern "C" void gl_bind_texture(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_enableTexture2D);
}

extern "C" void gl_clear_color(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_disableTexture2D);
}

extern "C" void gl_depth_mask(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_enableAlpha);
}

extern "C" void java_call_static_gl_state_manager_disable_alpha(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_disableAlpha);
}

extern "C" void tick_netty(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->Display, ids->Display_update);
}

extern "C" bool world_is_loaded(Jvm *j) {
    return JCALLN(j, CallStaticBooleanMethod, ids->Display, ids->Display_isActive) != 0;
}

extern "C" jobject java_new_render_item(Jvm *j) {
    return JCALLN(j, NewObject, ids->RenderItem, ids->id_ab8);
}

extern "C" void java_call_static_render_helper_enable_standard_item_lighting(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->RenderHelper, ids->RenderHelper_enableStandardItemLighting);
}

extern "C" void java_call_static_render_helper_disable_standard_item_lighting(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->RenderHelper, ids->RenderHelper_disableStandardItemLighting);
}

extern "C" jobject java_call_entity_item_get_entity_item(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->EntityItem_getEntityItem);
}

extern "C" jlong java_jni_call_long_method_uuid_get_least_significant_bits(Jvm *j, void *a1) {
    return JCALLN(j, CallLongMethod, a1, ids->UUID_getLeastSignificantBits);
}

extern "C" jlong java_jni_call_long_method_uuid_get_most_significant_bits(Jvm *j, void *a1) {
    return JCALLN(j, CallLongMethod, a1, ids->UUID_getMostSignificantBits);
}

extern "C" void gui_key_passthrough(Jvm *j, void *a1) {
    JCALLN(j, CallVoidMethod, a1, ids->PlayerControllerMP_updateController);
}

extern "C" jint java_call_nbttag_list_tag_count(Jvm *j, void *a1) {
    return JCALLN(j, CallIntMethod, a1, ids->NBTTagList_tagCount);
}

extern "C" jobject java_jni_is_instance_of_c01_packet_chat_message(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C01PacketChatMessage) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_c14_packet_tab_complete(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->C14PacketTabComplete) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_s2_apacket_particles(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->S2APacketParticles) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_score_player_team(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->ScorePlayerTeam) ? o : nullptr;
}

extern "C" jobject java_jni_is_instance_of_entity_item(Jvm *j, jobject o) {
    return JCALLN(j, IsInstanceOf, o, (jclass)ids->EntityItem) ? o : nullptr;
}

// ---- hand-written
FN(float, libc_roundf, (float v));

extern "C" float java_call_entity_renderer_get_fovmodifier(Jvm *j, jobject o, float partial) {
    return JCALL1(j, CallFloatMethod, o, (jmethodID)ids->EntityRenderer_getFOVModifier, (double)partial, 1);
}
extern "C" void set_active_render_info(Jvm *j, void *a, bool b) {
    JCALL2(j, CallStaticVoidMethod, (jclass)ids->ActiveRenderInfo, (jmethodID)ids->ActiveRenderInfo_updateRenderInfo, a, (int)b);
}
struct D2 { double a, b; };
extern "C" void java_jni_call_nonvirtual_void_method_renderer_living_entity_2(Jvm *j, jobject o, void *c, double x, D2 d, float f0, float f1) {
    JCALL2(j, CallNonvirtualVoidMethod, o, (jclass)ids->RendererLivingEntity, (jmethodID)ids->RendererLivingEntity___2, c, x, d, (double)f0, (double)f1);
}
// FontRenderer.drawString: float coordinates in the newer mappings, rounded ints in the old ones
extern "C" jint java_variant_2(Jvm *j, jobject font, jobject str, float x, float y, unsigned color, unsigned char shadow) {
    if (j->ids->variant != 0)
        return JNI(j)->CallIntMethod(j->env, font, (jmethodID)j->ids->id_9a8, str, (double)x, (double)y, color, (int)shadow);
    int ix = (int)libc_roundf(x);
    int iy = (int)libc_roundf(y);
    return JNI(j)->CallIntMethod(j->env, font, (jmethodID)j->ids->id_9a8, str, ix, iy, color, (int)shadow);
}
extern "C" void gl_blend_mode(Jvm *j, int a, int b) {
    JCALL2(j, CallStaticVoidMethod, (jclass)ids->GlStateManager, (jmethodID)ids->GlStateManager_blendFunc, a, b);
}
extern "C" void gl_set_color(Jvm *j, float a, float b, float c, float d) {
    JCALL2(j, CallStaticVoidMethod, (jclass)ids->GlStateManager, (jmethodID)ids->GlStateManager_color, (double)a, (double)b, (double)c, (double)d);
}
// element k (< 256) of a static object array
extern "C" jobject java_potion_potion_types(Jvm *j, unsigned k) {
    if (k > 0xff) return nullptr;
    JIds *ids = j->ids;
    JNIEnv *env = j->env;
    jobject a = (*env)->GetStaticObjectField(env, (jclass)ids->Potion, (jfieldID)ids->Potion_potionTypes);
    return (*env)->GetObjectArrayElement(env, (jobjectArray)a, k);
}
extern "C" void java_render_item_init(Jvm *j, jobject o, void *a, void *b, void *c, int d, int e) {
    if (j->ids->variant != 0)
        JNI(j)->CallVoidMethod(j->env, o, (jmethodID)j->ids->RenderItem__init_, c, d, e);
    else
        JNI(j)->CallVoidMethod(j->env, o, (jmethodID)j->ids->RenderItem__init_, a, b, c, d, e);
}
extern "C" void java_render_item_render_item_overlay_into_gui(Jvm *j, jobject o, void *a, void *b, void *c, int d, int e, void *f) {
    if (j->ids->variant != 0)
        JNI(j)->CallVoidMethod(j->env, o, (jmethodID)j->ids->RenderItem_renderItemOverlayIntoGUI, a, c, d, e, f);
    else
        JNI(j)->CallVoidMethod(j->env, o, (jmethodID)j->ids->RenderItem_renderItemOverlayIntoGUI, a, b, c, d, e, f);
}

// ---- JNI bridge functions found by the gap scan (no caller outside the virtualised init) ----
extern "C" void java_call_framebuffer_bind_framebuffer(Jvm *j, void *a1, unsigned char a2) {
    JCALL1(j, CallVoidMethod, a1, ids->Framebuffer_bindFramebuffer, a2);
}

extern "C" void java_call_static_gl_state_manager_disable_cull(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_disableCull);
}

extern "C" void java_call_static_gl_state_manager_enable_lighting(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_enableLighting);
}

extern "C" void java_call_static_gl_state_manager_disable_blend(Jvm *j) {
    JCALLN(j, CallStaticVoidMethod, ids->GlStateManager, ids->GlStateManager_disableBlend);
}

extern "C" void java_call_entity_item_search_for_other_items_nearby(Jvm *j, void *a1) {
    JCALLN(j, CallVoidMethod, a1, ids->EntityItem_searchForOtherItemsNearby);
}

// EntityRenderer.renderHand(partialTicks, pass)
extern "C" void java_call_entity_renderer_render_hand(Jvm *j, jobject renderer, float partial_ticks, int pass) {
    JCALL1(j, CallVoidMethod, renderer, ids->EntityRenderer_renderHand, (double)partial_ticks, pass);
}
