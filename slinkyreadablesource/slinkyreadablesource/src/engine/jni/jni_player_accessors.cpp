// JNI: local-player accessors.
#include "slinky/engine.h"
#include "slinky/jcall.h"
struct Rot { float yaw, pitch; };
struct Vec3 { double x, y, z; };
FN(Vec3, vec3_make, (double x, double y, double z));

extern "C" jint java_get_game_settings_third_person_view(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->GameSettings_thirdPersonView);
}

extern "C" void java_set_game_settings_third_person_view(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->GameSettings_thirdPersonView, a2);
}

extern "C" jint java_get_key_binding_press_time(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->KeyBinding_pressTime);
}

extern "C" void java_set_key_binding_press_time(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->KeyBinding_pressTime, a2);
}

extern "C" jint get_event_key(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->KeyBinding_keyCode);
}

extern "C" bool is_key_down(Jvm *j, void *a1) {
    return JCALLN(j, GetBooleanField, a1, ids->KeyBinding_pressed) != 0;
}

extern "C" void java_set_key_binding_pressed(Jvm *j, void *a1, unsigned char a2) {
    JCALL(j, SetBooleanField, a1, ids->KeyBinding_pressed, a2);
}

extern "C" bool java_call_static_keyboard_is_key_down(Jvm *j, int a1) {
    return JCALLN(j, CallStaticBooleanMethod, ids->Keyboard, ids->Keyboard_isKeyDown, a1) != 0;
}

extern "C" bool java_call_static_mouse_is_button_down(Jvm *j, int a1) {
    return JCALLN(j, CallStaticBooleanMethod, ids->Mouse, ids->Mouse_isButtonDown, a1) != 0;
}

extern "C" jfloat get_partial_ticks(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->Timer_renderPartialTicks);
}

extern "C" jfloat java_get_timer_timer_speed(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->Timer_timerSpeed);
}

extern "C" void java_set_timer_timer_speed(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->Timer_timerSpeed, a2);
}

extern "C" jfloat java_get_timer_elapsed_partial_ticks(Jvm *j, void *a1) {
    return JCALL(j, GetFloatField, a1, ids->Timer_elapsedPartialTicks);
}

extern "C" void java_set_timer_elapsed_partial_ticks(Jvm *j, void *a1, float a2) {
    JCALL(j, SetFloatField, a1, ids->Timer_elapsedPartialTicks, a2);
}

extern "C" jint java_get_inventory_player_current_item(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->InventoryPlayer_currentItem);
}

extern "C" void java_set_inventory_player_current_item(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->InventoryPlayer_currentItem, a2);
}

extern "C" jobject java_get_inventory_player_main_inventory(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->InventoryPlayer_mainInventory);
}

extern "C" jobject java_get_inventory_player(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->InventoryPlayer_Ladd_);
}

extern "C" jobject java_get_gui_container(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->GuiContainer_Laay_);
}

extern "C" bool java_jni_is_instance_of_gui_inventory(Jvm *j, void *a1) {
    return JCALLN(j, IsInstanceOf, a1, ids->GuiInventory) != 0;
}

extern "C" bool java_jni_is_instance_of_gui_chest(Jvm *j, void *a1) {
    return JCALLN(j, IsInstanceOf, a1, ids->GuiChest) != 0;
}

extern "C" jobject java_get_gui_chest(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->GuiChest_Lrb_);
}

extern "C" jint java_call_static_item_get_id_from_item(Jvm *j, void *a1) {
    return JCALLN(j, CallStaticIntMethod, ids->Item, ids->Item_getIdFromItem, a1);
}

extern "C" jfloat java_call_item_stack_get_str_vs_block(Jvm *j, void *a1, void *a2) {
    return JCALLN(j, CallFloatMethod, a1, ids->ItemStack_getStrVsBlock, a2);
}

extern "C" jint java_get_item_stack_stack_size(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->ItemStack_stackSize);
}

extern "C" jint java_get_item_stack_metadata(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->ItemStack_metadata);
}

extern "C" jobject java_get_net_handler_play_client(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->NetHandlerPlayClient_Ljava_util_Map_);
}

extern "C" jobject java_get_net_handler_play_client_2(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->NetHandlerPlayClient_Lej_);
}

extern "C" jobject java_get_network_manager(Jvm *j, void *a1) {
    return JCALL(j, GetObjectField, a1, ids->NetworkManager_Lio_netty_channel_Channel_);
}

extern "C" void send_packet(Jvm *j, void *a1, void *a2) {
    JCALLN(j, CallVoidMethod, a1, ids->id_700, a2);
}

extern "C" bool keyboard_has_next(Jvm *j) {
    return JCALLN(j, CallStaticBooleanMethod, ids->Keyboard, ids->Keyboard_next) != 0;
}

extern "C" bool keyboard_can_repeat(Jvm *j) {
    return JCALLN(j, CallStaticBooleanMethod, ids->Keyboard, ids->Keyboard_getEventKeyState) != 0;
}

extern "C" jint keyboard_next_key(Jvm *j) {
    return JCALLN(j, CallStaticIntMethod, ids->Keyboard, ids->Keyboard_getEventKey);
}

extern "C" bool java_call_static_mouse_is_grabbed(Jvm *j) {
    return JCALLN(j, CallStaticBooleanMethod, ids->Mouse, ids->Mouse_isGrabbed) != 0;
}

extern "C" bool keyboard_has_events(Jvm *j) {
    return JCALLN(j, CallStaticBooleanMethod, ids->Mouse, ids->Mouse_next) != 0;
}

extern "C" jobject java_call_item_stack_get_item(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->ItemStack_getItem);
}

extern "C" jobject java_call_item_stack_get_enchantment_tag_list(Jvm *j, void *a1) {
    return JCALLN(j, CallObjectMethod, a1, ids->ItemStack_getEnchantmentTagList);
}

extern "C" jint java_call_item_stack_get_max_stack_size(Jvm *j, void *a1) {
    return JCALLN(j, CallIntMethod, a1, ids->ItemStack_getMaxStackSize);
}

extern "C" bool java_call_item_stack_has_display_name(Jvm *j, void *a1) {
    return JCALLN(j, CallBooleanMethod, a1, ids->ItemStack_hasDisplayName) != 0;
}

extern "C" Vec3 java_get_vec3_x_coord(Jvm *j, jobject o) {
    double x = JCALLN(j, GetDoubleField, o, ids->Vec3_xCoord);
    double y = JCALLN(j, GetDoubleField, o, ids->Vec3_yCoord);
    double z = JCALLN(j, GetDoubleField, o, ids->Vec3_zCoord);
    Vec3 r = vec3_make(x, y, z);

    return r;
}

extern "C" jobject java_get_game_settings_key_bindings_3(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, 7);
}

extern "C" jobject get_held_item(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, 6);
}

extern "C" jobject java_get_game_settings_key_bindings_4(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, 2);
}

extern "C" jobject java_get_game_settings_key_bindings_5(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, 4);
}

extern "C" jobject java_get_game_settings_key_bindings_6(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, 3);
}

extern "C" jobject java_get_game_settings_key_bindings_7(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, 5);
}

extern "C" jobject java_get_game_settings_key_bindings_8(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, 0);
}

// ---- hand-written
struct Box { Vec3 lo, hi; };
FN(Box, aabb_make, (const Vec3 *lo, const Vec3 *hi));
FN(bool, java_call_static_keyboard_is_key_down, (Jvm *j, int key));     // Keyboard.isKeyDown
FN(bool, java_call_static_mouse_is_button_down, (Jvm *j, int button));  // Mouse.isButtonDown

extern "C" jobject java_get_game_settings_key_bindings_9(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, (jfieldID)ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, (ids->variant == 0) * 9 + 8);
}
extern "C" jobject get_game_screen(Jvm *j, jobject o) {
    jobject a = JCALLN(j, GetObjectField, o, (jfieldID)ids->GameSettings_keyBindings);
    return JCALL(j, GetObjectArrayElement, (jobjectArray)a, ids->variant == 0 ? 9 : 10);
}
extern "C" void java_get_key_binding_press_time_2(Jvm *j, jobject o) {
    jint v = JCALLN(j, GetIntField, o, (jfieldID)ids->KeyBinding_pressTime);
    JCALL(j, SetIntField, o, (jfieldID)ids->KeyBinding_pressTime, v + 1);
}
// key binding pressed: negative codes are mouse buttons (code + 100)
extern "C" bool java_get_key_binding_key_code(Jvm *j, jobject o) {
    jint v = JCALLN(j, GetIntField, o, (jfieldID)ids->KeyBinding_keyCode);
    if (v < 0) return java_call_static_mouse_is_button_down(j, v + 100);
    if ((unsigned)v <= 0x100) return java_call_static_keyboard_is_key_down(j, v);
    return false;
}
extern "C" jobject java_new_vec3_init(Jvm *j, Vec3 v) {
    return JCALL2(j, NewObject, (jclass)ids->Vec3, (jmethodID)ids->Vec3__init_, v.x, v.y, v.z);
}
extern "C" jobject java_new_axis_aligned_bb_init(Jvm *j, Box b) {
    return JCALL2(j, NewObject, (jclass)ids->AxisAlignedBB, (jmethodID)ids->AxisAlignedBB__init_,
                             b.lo.x, b.lo.y, b.lo.z, b.hi.x, b.hi.y, b.hi.z);
}
extern "C" Box java_get_axis_aligned_bb_min_x(Jvm *j, jobject o) {
    double x = JCALLN(j, GetDoubleField, o, (jfieldID)ids->AxisAlignedBB_minX);
    double y = JCALLN(j, GetDoubleField, o, (jfieldID)ids->AxisAlignedBB_minY);
    double z = JCALLN(j, GetDoubleField, o, (jfieldID)ids->AxisAlignedBB_minZ);
    Vec3 lo = vec3_make(x, y, z);
    x = JCALLN(j, GetDoubleField, o, (jfieldID)ids->AxisAlignedBB_maxX);
    y = JCALLN(j, GetDoubleField, o, (jfieldID)ids->AxisAlignedBB_maxY);
    z = JCALLN(j, GetDoubleField, o, (jfieldID)ids->AxisAlignedBB_maxZ);
    Vec3 hi = vec3_make(x, y, z);
    Box r = aabb_make(&lo, &hi);

    return r;
}
extern "C" void java_call_static_mouse_set_grabbed(Jvm *j, bool grab) {
    JCALL2(j, CallStaticVoidMethod, (jclass)ids->Mouse, (jmethodID)ids->Mouse_setGrabbed, grab);
}
extern "C" void java_call_static_mouse_set_cursor_position(Jvm *j, int x, int y) {
    JCALL2(j, CallStaticVoidMethod, (jclass)ids->Mouse, (jmethodID)ids->Mouse_setCursorPosition, x, y);
}
extern "C" void keyboard_send_key(Jvm *j, jobject o, unsigned short c, int k) {
    JCALL1(j, CallVoidMethod, o, (jmethodID)ids->GuiScreen_keyTyped, (int)c, k);
}
extern "C" void java_gui_container_handle_mouse_click(Jvm *j, jobject o, int a, int b, int c) {
    JNI(j)->CallVoidMethod(j->env, o, (jmethodID)j->ids->GuiContainer_handleMouseClick, 0, a, b, c);
}
extern "C" bool java_call_static_item_stack_are_item_stack_tags_equal(Jvm *j, void *a, void *b) {
    return JCALL2(j, CallStaticBooleanMethod, (jclass)ids->ItemStack, (jmethodID)ids->ItemStack_areItemStackTagsEqual, a, b) != 0;
}
extern "C" void send_packet_with_id(Jvm *j, jobject o, void *a, void *b) {
    JNI(j)->CallVoidMethod(j->env, o, (jmethodID)j->ids->id_6f8, a, b);
}
// newer mappings take an extra (empty) array argument
extern "C" void java_call_generic_future_listener(Jvm *j, jobject o, void *a) {
    if (j->ids->id_6f8) {
        jobjectArray arr = JNI(j)->NewObjectArray(j->env, 0, (jclass)j->ids->GenericFutureListener, nullptr);
        JCALL1(j, CallVoidMethod, o, (jmethodID)ids->id_6f8, a, arr);
    } else {
        JCALL1(j, CallVoidMethod, o, (jmethodID)ids->id_700, a);
    }
}

// ---- JNI bridge functions found by the gap scan (no caller outside the virtualised init) ----
extern "C" jint java_get_timer_elapsed_ticks(Jvm *j, void *a1) {
    return JCALL(j, GetIntField, a1, ids->Timer_elapsedTicks);
}

extern "C" void java_jni_set_long_field_gui_container_last_click_time(Jvm *j, void *a1, long long a2) {
    JCALL(j, SetLongField, a1, ids->GuiContainer_lastClickTime, (jlong)a2);
}

extern "C" jobject java_call_static_item_get_item_by_id(Jvm *j, int a1) {
    return JCALLN(j, CallStaticObjectMethod, ids->Item, ids->Item_getItemById, a1);
}

extern "C" void java_set_item_stack_stack_size(Jvm *j, void *a1, int a2) {
    JCALL(j, SetIntField, a1, ids->ItemStack_stackSize, a2);
}

// ---- functions the first gap scan missed (padding / tail-jump neighbours) ----
extern "C" jobject java_new_item_stack_init(Jvm *j, void *a1) {
    return JCALLN(j, NewObject, ids->ItemStack, ids->ItemStack__init_, a1);
}
