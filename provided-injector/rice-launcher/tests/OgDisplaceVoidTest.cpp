// Links the actual hash-locked OG Slinky Displace object, with game boundaries mocked.
#include <cmath>
#include <cstring>
#include <iostream>
#include <stdexcept>
#include <string>
#include <vector>
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/combat/og_displace_void.cpp"
static bool timer,weapon=true,sprinting=true,found=true;
static unsigned itemFlags=2,priority;
static int scans,rotations,clicks,retains;
static float answer=-90;
static RotationOverrideAngle camera{20,3},pending{},emitted{};
static std::vector<std::string> keys;
extern "C" {
alignas(8) char data_2f5d60[0x210]={};
alignas(8) char data_2f6088[64]={};
unsigned data_2f60b8=0,data_304af4=9,data_304af8=5;
int data_304b08=0;
unsigned char data_304af0=0,data_304af1=0,data_304af2=0,data_304af3=0;
unsigned char data_2f5ff8=0,data_2f6018=0,data_2f6038=0,data_2f6058=0,data_2f6100=0;
alignas(4) unsigned char data_2f60e0[4]={};
float data_2f5fd8=90,data_2f5fa0=5;
RotationOverrideAngle data_304b00{};
extern const float rdata_156680=.5f,rdata_156684=10,rdata_156688=20,rdata_1566b4=.5f;
alignas(16) extern const unsigned rdata_156690[4]={0x80000000u,0,0,0};
void W_enumw(void*,void*,void*){}void W_slider(void*,void*,void*){}void W_bool(void*,void*,void*){}
void R_enum(void*,const char* key,void*){keys.emplace_back(key);}void R_num(void*,const char* key,void*){keys.emplace_back(key);}void R_bool(void*,const char* key,void*){keys.emplace_back(key);}
jobject java_get_minecraft_5(Jvm*,jobject){return (void*)4;}
int java_get_moving_object_position_3(Jvm*,jobject){return 2;}
jobject java_get_moving_object_position_2(Jvm*,jobject){return (void*)5;}
void* java_jni_is_instance_of_entity_other_player_mp(void*,void* entity){return entity;}
unsigned char entity_flags_has(void*,void*,int){return 0;}
unsigned char pending_action_flag(){return timer;}
unsigned char weapons_part(void*,void*,unsigned* flags){*flags=itemFlags;return weapon;}
unsigned char java_call_entity_is_sprinting(void*,void*){return sprinting;}
RotationOverrideAngle get_entity_rotation(void*,void*){return camera;}
unsigned char yaw_diff_check(void*,RotationOverrideAngle,void*){return 1;}
unsigned char increment_wrap(int value){if((unsigned)value<priority)return 0;priority=value;return 1;}
void apply_player_rotation(void*,unsigned char,int,int,RotationOverrideAngle){++retains;}
unsigned char random_chance(float){return 1;}
static float bounds[2];
void* rot_make_floats(float low,float high){bounds[0]=low;bounds[1]=high;return bounds;}
float random_rotation(void*){return (bounds[0]+bounds[1])/2;}
void apply_silent_rotation(RotationOverrideAngle value){emitted=value;pending.yaw+=value.yaw;pending.pitch+=value.pitch;}
void set_rotation_active(void*,void*,int){camera.yaw+=pending.yaw;camera.pitch+=pending.pitch;pending={};++rotations;}
float java_get_entity_living_base_move_strafing(void*,void*){return 0;}
RotationOverrideAngle get_render_state(){return pending;}
RotationOverrideAngle rot_add(RotationOverrideAngle a,RotationOverrideAngle b){return {a.yaw+b.yaw,a.pitch+b.pitch};}
RotationOverrideAngle rot_sub(RotationOverrideAngle a,RotationOverrideAngle b){return {a.yaw-b.yaw,a.pitch-b.pitch};}
void* get_net_handler(void*,void*){return (void*)6;}
void java_set_minecraft_left_click_counter(void*,void*,int){}
void* java_get_game_settings_key_bindings_8(void*,void*){return (void*)7;}
void java_get_key_binding_press_time_2(void*,void*){++clicks;}
bool source_displace_void_yaw(JNIEnv*,jobject,float,float angle,float radius,bool,float* result){++scans;if(angle!=90||radius!=6)throw std::runtime_error("Original angle or Void radius changed");*result=answer;return found;}
void knockback_displace_part_3(void*);void knockback_displace_part_4(void*);void knockback_displace_part_5(void*);void knockback_displace_part(void*);
}
static jint push(JNIEnv*,jint){return 0;}static jobject pop(JNIEnv*,jobject value){return value;}static jboolean exception(JNIEnv*){return 0;}
static void originalSettings(void*,void*){}static void originalVisit(void*){keys.emplace_back("cooldown");}
static void check(bool ok,const char* text){if(!ok)throw std::runtime_error(text);}
static bool near(float a,float b){return std::abs(a-b)<.001f;}
static void reset(){data_304af0=data_304af1=data_304af2=data_304af3=0;data_304af4=9;data_304af8=5;data_2f5fa0=5;data_2f60b8=4;data_304b08=-1;data_2f6018=data_2f6038=data_2f5ff8=data_2f6058=0;data_2f60e0[0]=0;camera={20,3};pending={};priority=0;scans=rotations=clicks=retains=0;timer=false;weapon=sprinting=found=true;itemFlags=2;}
int main(){try{
    using namespace ogvoid;
    auto descriptor=(Descriptor*)data_2f5d60;descriptor->events[14]={knockback_displace_part_3,1000,17};descriptor->events[15]={knockback_displace_part_4,0,0};descriptor->events[18]={knockback_displace_part_5,1000,0};descriptor->settings=::originalSettings;descriptor->visit=::originalVisit;
    const char* originalDirections[]={"Left","Right","Random","Strafe","Strafe (inverted)"};
    auto directionSetting=(Choice*)data_2f6088;directionSetting->label="Direction";directionSetting->names=originalDirections;directionSetting->count=5;
    og_displace_void_install();og_displace_void_install();
    check(directionSetting->count==6&&std::strcmp(directionSetting->names[5],"Void")==0&&directionSetting->names[0]==originalDirections[0],"Void must extend the original Direction list without replacing existing choices");
    check(descriptor->events[14].priority==1000&&descriptor->events[14].reserved==17&&descriptor->events[18].fn==knockback_displace_part_5,"OG callback priority or restore handler changed");
    JNINativeInterface_ table{};table.PushLocalFrame=push;table.PopLocalFrame=pop;table.ExceptionCheck=exception;JNIEnv env=&table;Jvm java={nullptr,&env};
    Context context={&java,nullptr,nullptr,(void*)1,(void*)2,(void*)3,nullptr,nullptr,1};Attack event={14,&context,1,0};
    reset();descriptor->events[14].fn(&event);
    check(scans==0&&data_304af1&&event.handled&&near(camera.yaw,105)&&near(camera.pitch,18),"Default mode must use real OG angle, jitter and activation");
    reset();event.handled=0;data_2f60b8=5;pending={10,2};data_2f60e0[0]=1;descriptor->events[14].fn(&event);
    check(scans==1&&data_304af1&&event.handled&&near(camera.yaw,-90)&&near(camera.pitch,3),"Void must use scanner yaw while OG activates and cancels prior pending aim");
    check(data_2f60b8==5&&data_304b08==-1&&data_2f5fa0==5&&data_304af8==0&&data_304af0==1,"Void changed user settings, OG cooldown reset, or hidden packet state");
    struct Packet{char pad[0x24];int result;} packet{};knockback_displace_part(&packet);check(packet.result==1,"Void must preserve OG hidden-packet handler");
    struct Tick{long long type;Context* ctx;} tick{18,&context};knockback_displace_part_5(&tick);
    check(near(camera.yaw,20)&&near(camera.pitch,3)&&clicks==1&&!data_304af1&&data_304af2&&data_304af8==1,"Real OG late tick must restore camera, replay attack and advance counters");
    reset();event.handled=0;data_2f60b8=5;data_304af8=4;descriptor->events[14].fn(&event);check(!event.handled&&!rotations&&scans==0,"Original cooldown threshold must block before readiness");
    data_304af8=5;descriptor->events[14].fn(&event);check(event.handled&&rotations==1,"Original cooldown threshold must activate at readiness");
    reset();event.handled=0;data_2f60b8=5;timer=true;descriptor->events[14].fn(&event);check(!event.handled&&!rotations,"OG Timer Range condition bypassed");
    reset();event.handled=0;data_2f60b8=5;weapon=false;data_2f6018=1;descriptor->events[14].fn(&event);check(!event.handled&&!rotations,"OG weapon condition bypassed");
    reset();event.handled=0;data_2f60b8=5;found=false;descriptor->events[14].fn(&event);check(!event.handled&&!rotations&&data_304af3&&data_2f60b8==5&&data_304b08==-1,"No Void path must pass through OG bookkeeping without displacement or settings changes");
    descriptor->visit(nullptr);check(keys.size()==3&&keys[0]=="cooldown"&&keys[1]=="slinky_displace_void_radius","Original settings persistence must be retained");
    std::cout<<"Actual OG Slinky attack, cooldown, conditions, hidden packets and restoration with optional Raven yaw passed\n";return 0;
}catch(const std::exception& error){std::cerr<<error.what()<<'\n';return 1;}}
