// Actual module handlers against a fake JNI inventory and packet connection.
#include <cstring>
#include <iostream>
#include <stdexcept>
#include <cstdarg>
#include <vector>
#include <string>
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/utility/fireball_pearl.cpp"
namespace fixture {
struct Stack {int type,count;};static Stack slots[9];
static int player,world,otherWorld,mc,inventory,controller,net,connection,settings,caps,pos,animation;
static Stack* usedStack;
static int current=4,globals=0,queued=0,releases=0,cleared=0;
static bool busy=false,networkAvailable=true,pending=false;
static float yaw=30,pitch=10,prevYaw=28,prevPitch=9,serverPitch=10;
static bool creative=false;
static std::vector<std::string> actions;
enum Class {Pearl=400,Fireball,Animation,Capabilities,C08,BlockPos};
enum Field {Yaw=1,Pitch,PrevYaw,PrevPitch,Cap,Creative,Size,Sensitivity};
enum Method {GetNet=100,UseCtor,AnimCtor,Sync};
static void* id(int v){return (void*)(size_t)v;}static int n(void* p){return (int)(size_t)p;}
static void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
static jint push(JNIEnv*,jint){return 0;}static jobject pop(JNIEnv*,jobject p){return p;}
static jobject global(JNIEnv*,jobject p){++globals;return p;}static void release(JNIEnv*,jobject){--globals;}
static void local(JNIEnv*,jobject){}static jboolean same(JNIEnv*,jobject a,jobject b){return a==b;}
static jboolean exception(JNIEnv*){return pending;}static void clearException(JNIEnv*){pending=false;}
static jboolean instance(JNIEnv*,jobject o,jclass c){auto s=(Stack*)o;return s&&s->type==n(c);}
static jfloat getFloat(JNIEnv*,jobject,jfieldID f){switch(n(f)){case Yaw:return yaw;case Pitch:return pitch;case PrevYaw:return prevYaw;case PrevPitch:return prevPitch;default:return .5f;}}
static void setFloat(JNIEnv*,jobject,jfieldID f,jfloat v){switch(n(f)){case Yaw:yaw=v;break;case Pitch:pitch=v;break;case PrevYaw:prevYaw=v;break;case PrevPitch:prevPitch=v;break;}}
static jobject getObject(JNIEnv*,jobject,jfieldID){return &caps;}
static jboolean getBool(JNIEnv*,jobject,jfieldID){return creative;}
static jint getInt(JNIEnv*,jobject o,jfieldID){return ((Stack*)o)->count;}
static void setInt(JNIEnv*,jobject o,jfieldID,jint v){((Stack*)o)->count=v;}
static jobject element(JNIEnv*,jobjectArray,jsize slot){return slots[slot].type?&slots[slot]:nullptr;}
static void setElement(JNIEnv*,jobjectArray,jsize slot,jobject value){check(!value,"Empty fireball stack must be removed");slots[slot].type=0;++cleared;}
static jobject callObject(JNIEnv*,jobject,jmethodID method,...){return n(method)==GetNet&&networkAvailable?&net:nullptr;}
static void callVoid(JNIEnv*,jobject,jmethodID method,...){if(n(method)==Sync)actions.push_back("S"+std::to_string(current));}
static jobject newObject(JNIEnv*,jclass cls,jmethodID method,...){va_list a;va_start(a,method);jobject result=nullptr;
    if(n(cls)==BlockPos){int x=va_arg(a,int),y=va_arg(a,int),z=va_arg(a,int);check(x==-1&&y==-1&&z==-1,"Use packet must use sentinel BlockPos");result=&pos;}
    else if(n(method)==UseCtor){auto block=va_arg(a,jobject);int face=va_arg(a,int);auto stack=va_arg(a,jobject);double x=va_arg(a,double),y=va_arg(a,double),z=va_arg(a,double);
        check(block==&pos&&face==255&&x==0&&y==0&&z==0,"Use packet must preserve face and hit-vector arguments");usedStack=(Stack*)stack;result=usedStack;}
    else result=&animation;va_end(a);return result;}
static void resetInventory(bool fire=true,bool pearl=true){for(auto& s:slots)s={};if(pearl)slots[2]={Pearl,3};if(fire)slots[7]={Fireball,1};current=4;
    yaw=30;pitch=10;prevYaw=28;prevPitch=9;serverPitch=10;actions.clear();queued=cleared=0;busy=creative=pending=false;networkAvailable=true;}
}
extern "C" void slinky_logf(const char*,...){}
extern "C" bool queue_module_toggle(void*,void*,unsigned char on){fixture::check(!on,"Only self-disable may be queued");++fixture::queued;return true;}
extern "C" jobject java_get_entity_player_2(Jvm*,jobject){return &fixture::inventory;}
extern "C" int java_get_inventory_player_current_item(Jvm*,jobject){return fixture::current;}
extern "C" void java_set_inventory_player_current_item(Jvm*,jobject,int n){fixture::current=n;}
extern "C" jobjectArray java_get_inventory_player_main_inventory(Jvm*,jobject){return &fixture::inventory;}
extern "C" jobject java_call_item_stack_get_item(Jvm*,jobject stack){return stack;}
extern "C" jobject java_get_minecraft_4(Jvm*,jobject){return &fixture::controller;}
extern "C" jobject get_net_handler(Jvm*,jobject){return &fixture::settings;}
extern "C" jobject java_get_net_handler_play_client_2(Jvm*,jobject){return &fixture::connection;}
extern "C" void java_call_generic_future_listener(Jvm*,jobject,jobject packet){using namespace fixture;
    check(serverPitch<=-89,"Server rotation must be upward before use or animation packets");
    actions.push_back(packet==&animation?"A":((Stack*)packet)->type==Pearl?"P":"F");}
extern "C" bool claim_release_hotbar_module(void*){return !fixture::busy;}
extern "C" void release_owner(void*){++fixture::releases;}
extern "C" void W_bool(void*,void*,void*){}extern "C" void R_bool(void*,const char*,void*){}
int main(){using namespace fixture;using namespace fireballpearl;try{
    JNINativeInterface_ api={};api.PushLocalFrame=push;api.PopLocalFrame=pop;api.NewGlobalRef=global;api.DeleteGlobalRef=release;api.DeleteLocalRef=local;
    api.IsSameObject=same;api.IsInstanceOf=instance;api.ExceptionCheck=exception;api.ExceptionClear=clearException;
    api.GetFloatField=getFloat;api.SetFloatField=setFloat;api.GetObjectField=getObject;api.GetBooleanField=getBool;api.GetIntField=getInt;api.SetIntField=setInt;
    api.GetObjectArrayElement=element;api.SetObjectArrayElement=setElement;api.CallObjectMethod=callObject;api.CallVoidMethod=callVoid;api.NewObject=newObject;
    JNIEnv env=&api;JIds game={};Jvm j={&game,&env};game.Entity_rotationYaw=id(Yaw);game.Entity_rotationPitch=id(Pitch);game.Entity_prevRotationYaw=id(PrevYaw);game.Entity_prevRotationPitch=id(PrevPitch);
    game.ItemStack_stackSize=id(Size);game.GameSettings_mouseSensitivity=id(Sensitivity);game.Minecraft_getNetHandler=id(GetNet);game.BlockPos=id(BlockPos);game.C08PacketPlayerBlockPlacement=id(C08);
    auto* desc=(Descriptor*)fireball_pearl_descriptor();check(std::strcmp(desc->name,"Fireball Pearl")==0&&desc->category==4&&!realRotations.value,"Utility module and silent default must match source");
    ids.pearl=(jclass)global(&env,id(Pearl));ids.fireball=(jclass)global(&env,id(Fireball));ids.animation=(jclass)global(&env,id(Animation));ids.capabilities=(jclass)global(&env,id(Capabilities));
    ids.ready=true;ids.useCtor=id(UseCtor);ids.animationCtor=id(AnimCtor);ids.syncSlot=id(Sync);ids.capabilitiesField=id(Cap);ids.creative=id(Creative);
    Context c={&j,nullptr,nullptr,&mc,&world,&player};Toggle toggle={5,&c,desc,1};Event tick={16,&c};
    auto runTick=[&](){desc->events[16].fn(&tick);desc->events[17].fn(&tick);serverPitch=pitch;desc->events[18].fn(&tick);Rotation r={13};desc->events[13].fn(&r);return r;};
    resetInventory();desc->events[5].fn(&toggle);check(sequence.active&&globals==6&&current==4,"Enable must prepare sequence without switching early");
    auto rotation=runTick();check(current==2&&rotation.on&&rotation.pitch==-90&&yaw==30&&pitch==10&&prevPitch==9,"First tick selects pearl, publishes upward look, and restores camera");
    runTick();check(sequence.step==2&&slots[2].count==3,"Second tick throws pearl without inventing local pearl consumption");
    runTick();check(current==7&&slots[7].count==0&&slots[7].type==0&&cleared==1,"Third tick uses and consumes one fireball");
    runTick();check(current==4&&!published,"Fourth tick restores original hotbar slot and stops rotation");runTick();
    const std::vector<std::string> expected={"S2","P","A","S7","F","A","S4"};check(actions==expected&&queued==1&&!sequence.active&&globals==4&&!claimed,"Five-tick sequence and packet order must match source");
    resetInventory(false);desc->events[5].fn(&toggle);for(int n=0;n<5;++n)runTick();
    check(actions==std::vector<std::string>({"S2","P","A","S4"})&&current==4&&queued==1,"Pearl-only sequence must finish without requiring a fireball");
    resetInventory(true,false);desc->events[5].fn(&toggle);check(queued==1&&!sequence.active&&current==4&&actions.empty()&&globals==4,"Missing pearl must disable without switching");
    resetInventory();desc->events[5].fn(&toggle);runTick();toggle.on=0;desc->events[5].fn(&toggle);check(current==4&&globals==4&&!claimed,"Manual cancellation must restore selected slot and release ownership");toggle.on=1;
    resetInventory();creative=true;realRotations.value=1;desc->events[5].fn(&toggle);for(int n=0;n<5;++n)runTick();
    check(pitch<=-89&&slots[7].count==1&&!cleared&&current==4,"Real rotations remain visible; creative fireball stack must not decrement");realRotations.value=0;
    resetInventory();busy=true;desc->events[5].fn(&toggle);check(queued==1&&current==4&&!claimed,"An occupied hotbar must not be stolen from another module");
    resetInventory();desc->events[5].fn(&toggle);runTick();networkAvailable=false;desc->events[16].fn(&tick);
    check(queued==1&&current==4&&globals==4&&!sequence.active,"Disconnected packet sender must abort and restore slot");
    resetInventory();desc->events[5].fn(&toggle);runTick();c.world=&otherWorld;current=8;Event frame={1,&c};desc->events[1].fn(&frame);
    check(queued==1&&current==8&&globals==4&&!sequence.active,"World change must clean up without editing replacement inventory");c.world=&world;
    fireball_pearl_cleanup(&env);check(globals==0&&!claimed,"Unload must release all references and hotbar claims");
    game.BlockPos=nullptr;desc->events[5].fn(&toggle);check(queued==2&&!sequence.active&&globals==0,"Missing mappings must safely disable");
    std::cout<<"Fireball Pearl sequence, packet ABI/order, rotations, consumption, cancellation, world change, and cleanup passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}
