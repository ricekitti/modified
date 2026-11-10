// Actual native module callbacks against a controlled JNI world; no game or auth.
#include <cstring>
#include <iostream>
#include <stdexcept>
#include <cstdlib>
#include <cstdarg>
#include <vector>
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/combat/anti_fireball.cpp"
namespace fixture {
struct Ball {int id;double x,z,previousZ;};
static Ball close={1,0,2,3},far={2,0,5,6},farther={3,0,7,8};
static std::vector<Ball*> loaded={&close,&far,&farther};
static int player,world,otherWorld,mc,caps,settings,input,list,inventory,stack,bow,net,connection,attackPacket,animationPacket,movePacket,digPacket,placePacket;
static int globals=0,queued=0,swings=0,attacks=0,attackPackets=0,animations=0,vertices=0;
static Ball* attacked=nullptr;
static bool flying=false,heldBow=false,sneaking=false;
static bool rotationOverridden=false;
static float forward=1,strafe=0,lastR,lastG,lastB,lastAlpha;
enum Field {PX=1,PY,PZ,PrevX,PrevY,PrevZ,Yaw,Pitch,MinX,MinY,MinZ,MaxX,MaxY,MaxZ,BB,EntityId,Cap,Flying,Input,Forward,Strafe,Sneak,Sensitivity,StackSize};
enum Method {Eye=100,Border,GetById,GetNet,Swing,LocalAttack,AttackCtor,AnimCtor};
enum Class {Fireball=400,Animation,Capabilities,ItemBlock,ItemBow,ItemFireball,Action,C02,C03,C07,C08};
static void* id(int n){return (void*)(size_t)n;}static int n(void* p){return (int)(size_t)p;}
static void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
static bool ball(jobject p){return p==&close||p==&far||p==&farther;}
static jint push(JNIEnv*,jint){return 0;}static jobject pop(JNIEnv*,jobject p){return p;}
static jobject global(JNIEnv*,jobject p){++globals;return p;}static void release(JNIEnv*,jobject){--globals;}
static void local(JNIEnv*,jobject){}static jboolean same(JNIEnv*,jobject a,jobject b){return a==b;}
static jboolean exception(JNIEnv*){return false;}static void clearException(JNIEnv*){}
static jboolean instance(JNIEnv*,jobject p,jclass c){switch(n(c)){
    case Fireball:return ball(p);case ItemBow:return p==&bow;case C02:return p==&attackPacket;case C03:return p==&movePacket;
    case C07:return p==&digPacket;case C08:return p==&placePacket;default:return false;}}
static jobject getObject(JNIEnv*,jobject o,jfieldID f){return n(f)==BB?o:n(f)==Cap?&caps:n(f)==Input?&input:nullptr;}
static jint getInt(JNIEnv*,jobject o,jfieldID f){return n(f)==EntityId?((Ball*)o)->id:n(f)==StackSize?1:0;}
static jboolean getBool(JNIEnv*,jobject,jfieldID f){return n(f)==Flying?flying:n(f)==Sneak?sneaking:false;}
static jfloat getFloat(JNIEnv*,jobject,jfieldID f){return n(f)==Sensitivity?.5f:n(f)==Forward?forward:n(f)==Strafe?strafe:0;}
static jdouble getDouble(JNIEnv*,jobject o,jfieldID f){auto b=ball(o)?(Ball*)o:nullptr;
    switch(n(f)){case PX:case PrevX:return b?b->x:0;case PY:case PrevY:return 0;case PZ:return b?b->z:0;case PrevZ:return b?b->previousZ:0;
        case MinX:return b->x-.5;case MaxX:return b->x+.5;case MinY:return 1;case MaxY:return 2;case MinZ:return b->z-.5;case MaxZ:return b->z+.5;default:return 0;}}
static void setFloat(JNIEnv*,jobject,jfieldID f,jfloat v){if(n(f)==Forward)forward=v;else if(n(f)==Strafe)strafe=v;}
static jfloat callFloat(JNIEnv*,jobject,jmethodID m,...){return n(m)==Eye?1.62f:.1f;}
static jobject callObject(JNIEnv*,jobject,jmethodID m,...){va_list args;va_start(args,m);jobject result=nullptr;
    if(n(m)==GetById){int eid=va_arg(args,int);for(auto b:loaded)if(b->id==eid)result=b;}
    else if(n(m)==GetNet)result=&net;va_end(args);return result;}
static void callVoid(JNIEnv*,jobject,jmethodID m,...){if(n(m)==Swing)++swings;if(n(m)==LocalAttack){++attacks;va_list a;va_start(a,m);attacked=(Ball*)va_arg(a,jobject);va_end(a);}}
static jobject newObject(JNIEnv*,jclass,jmethodID m,...){return n(m)==AttackCtor?&attackPacket:&animationPacket;}
static jobject element(JNIEnv*,jobjectArray,jsize){return &stack;}
static void one(unsigned){}static void zero(){}static void depth(unsigned char){}static void blend(unsigned,unsigned){}
static void color(float r,float g,float b,float a){lastR=r;lastG=g;lastB=b;lastAlpha=a;}
static void vertex(double,double,double){++vertices;}
}
extern "C" void* libc_calloc(unsigned long long n,unsigned long long size){return std::calloc((size_t)n,(size_t)size);}
extern "C" void libc_free(void* p){std::free(p);}
extern "C" void slinky_logf(const char*,...){}
extern "C" bool queue_module_toggle(void*,void*,unsigned char on){fixture::check(!on,"Module must only self-disable");++fixture::queued;return true;}
extern "C" jobject java_get_world(Jvm*,jobject){return &fixture::list;}
extern "C" int java_call_list_size(Jvm*,jobject){return (int)fixture::loaded.size();}
extern "C" jobject java_call_list_get(Jvm*,jobject,int i){return fixture::loaded.at(i);}
extern "C" jobject java_get_entity_player_2(Jvm*,jobject){return fixture::heldBow?&fixture::inventory:nullptr;}
extern "C" int java_get_inventory_player_current_item(Jvm*,jobject){return 0;}
extern "C" jobjectArray java_get_inventory_player_main_inventory(Jvm*,jobject){return &fixture::inventory;}
extern "C" jobject java_call_item_stack_get_item(Jvm*,jobject){return &fixture::bow;}
extern "C" jobject get_net_handler(Jvm*,jobject){return &fixture::settings;}
extern "C" jobject java_get_net_handler_play_client_2(Jvm*,jobject){return &fixture::connection;}
extern "C" void java_call_generic_future_listener(Jvm* j,jobject,jobject packet){
    if(packet==&fixture::attackPacket)++fixture::attackPackets;else ++fixture::animations;
    antifireball::Packet ev={3,j,packet};antifireball::onPacket(&ev);}
extern "C" void* slinky_gl_get_proc(const char*){return nullptr;}
extern "C" FireRotation get_render_rotation(){return {antifireball::movementYaw+(fixture::rotationOverridden?90.f:0.f),0};}
extern "C" unsigned char is_silent_rotation_active(){return 1;}
extern "C" {float data_2ed240=.2f,data_2ed244=.4f,data_2ed248=.8f;}
extern "C" void W_slider(void*,void*,void*){}extern "C" void W_bool(void*,void*,void*){}extern "C" void W_enumw(void*,void*,void*){}
extern "C" void R_num(void*,const char*,void*){}extern "C" void R_bool(void*,const char*,void*){}extern "C" void R_enum(void*,const char*,void*){}
int main(){using namespace fixture;using namespace antifireball;try{
    JNINativeInterface_ api={};api.PushLocalFrame=push;api.PopLocalFrame=pop;api.NewGlobalRef=global;api.DeleteGlobalRef=release;api.DeleteLocalRef=local;
    api.IsSameObject=same;api.IsInstanceOf=instance;api.ExceptionCheck=exception;api.ExceptionClear=clearException;api.GetObjectField=getObject;api.GetIntField=getInt;
    api.GetBooleanField=getBool;api.GetFloatField=getFloat;api.GetDoubleField=getDouble;api.SetFloatField=setFloat;api.CallFloatMethod=callFloat;
    api.CallObjectMethod=callObject;api.CallVoidMethod=callVoid;api.NewObject=newObject;api.GetObjectArrayElement=element;
    JNIEnv env=&api;JIds game={};Jvm j={&game,&env};
    game.Entity_posX=id(PX);game.Entity_posY=id(PY);game.Entity_posZ=id(PZ);game.Entity_prevPosX=id(PrevX);game.Entity_prevPosY=id(PrevY);game.Entity_prevPosZ=id(PrevZ);
    game.Entity_rotationYaw=id(Yaw);game.Entity_rotationPitch=id(Pitch);game.Entity_entityId=id(EntityId);game.Entity_Lazt_=id(BB);
    game.AxisAlignedBB_minX=id(MinX);game.AxisAlignedBB_minY=id(MinY);game.AxisAlignedBB_minZ=id(MinZ);game.AxisAlignedBB_maxX=id(MaxX);game.AxisAlignedBB_maxY=id(MaxY);game.AxisAlignedBB_maxZ=id(MaxZ);
    game.EntityLivingBase_moveForward=id(Forward);game.EntityLivingBase_moveStrafing=id(Strafe);game.GameSettings_mouseSensitivity=id(Sensitivity);game.ItemStack_stackSize=id(StackSize);
    game.World_getEntityByID=id(GetById);game.Minecraft_getNetHandler=id(GetNet);game.EntityLivingBase_swingItem=id(Swing);
    game.C02PacketUseEntity=id(C02);game.C03PacketPlayer=id(C03);game.C07PacketPlayerDigging=id(C07);game.C08PacketPlayerBlockPlacement=id(C08);
    auto* desc=(Descriptor*)anti_fireball_descriptor();check(std::strcmp(desc->name,"AntiFireball")==0&&desc->category==0,"Must be registered as Combat/AntiFireball");
    check(range.value==5&&range.min==3&&range.max==8&&fov.value==360&&rotations.value&&swing.value&&moveFix.value==1&&showTarget.value==0,"All settings must match Mayu defaults");
    ids.fireball=(jclass)global(&env,id(Fireball));ids.animation=(jclass)global(&env,id(Animation));ids.capabilities=(jclass)global(&env,id(Capabilities));
    ids.itemBlock=(jclass)global(&env,id(ItemBlock));ids.itemBow=(jclass)global(&env,id(ItemBow));ids.itemFireball=(jclass)global(&env,id(ItemFireball));ids.attackAction=global(&env,id(Action));
    ids.ready=true;ids.eyeHeight=id(Eye);ids.borderSize=id(Border);ids.capabilitiesField=id(Cap);ids.allowFlying=id(Flying);ids.input=id(Input);ids.forward=id(Forward);ids.strafe=id(Strafe);ids.sneak=id(Sneak);
    ids.packetCtor=id(AttackCtor);ids.animationCtor=id(AnimCtor);ids.attackLocal=id(LocalAttack);
    Context c={&j,nullptr,nullptr,&mc,&world,&player};Toggle toggle={5,&c,desc,1};desc->events[5].fn(&toggle);check(globals==10&&!queued,"Enable must bind world/player/client");
    Event tick={16,&c};desc->events[16].fn(&tick);check(target&&target->entity==&far&&globals==13,"Must ignore close-spawned fireball and choose nearest approaching candidate");
    Rotation r={13};desc->events[13].fn(&r);check(r.on&&swings==1&&attackPackets==1&&attacks==1&&attacked==&far,"Rotation callback must swing, send attack, and attack locally");
    desc->events[13].fn(&r);check(swings==1&&attacks==1,"UseEntity tracker must suppress a duplicate attack");
    Packet packet={3,&j,&digPacket};desc->events[3].fn(&packet);check(actions()==3,"Digging must join action mask");packet.packet=&placePacket;desc->events[3].fn(&packet);check(actions()==7,"Placement must join action mask");
    packet.packet=&movePacket;desc->events[3].fn(&packet);check(!actions(),"Movement packet must reset tracking");
    swing.value=0;heldBow=true;r={13};desc->events[13].fn(&r);check(!r.on&&animations==1&&attacks==2,"Bow must suppress rotations while packet-only swing and attack remain enabled");
    heldBow=false;rotations.value=0;packet.packet=&movePacket;desc->events[3].fn(&packet);r={13};desc->events[13].fn(&r);check(!r.on&&attacks==3,"Rotation toggle must not disable deflection");rotations.value=1;
    loaded={&close,&farther};close.z=6;desc->events[16].fn(&tick);check(target&&target->entity==&farther,"Close-spawn exclusion must persist after that fireball moves away");
    packet.packet=&movePacket;desc->events[3].fn(&packet);int before=attacks;desc->events[13].fn(&r);check(attacks==before&&animations==3,"Candidate outside attack range may swing but must not attack");
    flying=true;desc->events[16].fn(&tick);check(!target,"AllowFlying must suppress target selection");flying=false;desc->events[16].fn(&tick);check(target,"Selection must resume without losing tracking");
    ownsRotation=true;movementYaw=-90;viewYaw=0;forward=1;strafe=0;Move move={21,&c,0,1,.91f};desc->events[21].fn(&move);check(move.forward==0&&move.strafe==-1&&strafe==-1,"Silent mode must correct both input and movement event");
    rotationOverridden=true;forward=1;strafe=0;move={21,&c,0,1,.91f};desc->events[21].fn(&move);check(move.forward==1&&move.strafe==0,"Higher-priority rotation must suppress AntiFireball correction");rotationOverridden=false;
    moveFix.value=2;forward=1;strafe=0;move={21,&c,0,1,.91f};desc->events[21].fn(&move);check(move.forward==1&&move.strafe==0,"Strict mode must leave directional input unchanged");
    gl={one,zero,one,one,depth,blend,color,one,zero,vertex};showTarget.value=1;Render render={6,{},&c,1};desc->events[6].fn(&render);
    check(vertices==24&&lastG==1&&lastR<1&&lastAlpha==63.f/255,"Default marker must render a green translucent six-face box for approaching target");
    showTarget.value=2;desc->events[6].fn(&render);check(lastR==.2f&&lastG==.4f&&lastB==.8f,"HUD marker must use native HUD color");
    loaded.clear();before=attacks;desc->events[13].fn(&r);check(attacks==before,"Unloaded target must never be attacked");desc->events[16].fn(&tick);check(!tracked&&globals==10,"Unloaded entities must release references");
    c.world=&otherWorld;Event frame={1,&c};desc->events[1].fn(&frame);check(globals==7&&!queued,"World switch must release state while keeping module enabled");
    loaded={&far};desc->events[16].fn(&tick);check(target&&globals==11,"New world must bind and resume tracking");
    toggle.on=0;desc->events[5].fn(&toggle);check(globals==7&&!target&&!actions(),"Disable must clear tracking and action mask");
    anti_fireball_cleanup(&env);check(globals==0,"Unload must release all mapping and entity references");
    toggle.on=1;desc->events[5].fn(&toggle);check(queued==1&&globals==0,"Missing mappings must safely disable");
    std::cout<<"AntiFireball native settings, tracking, packets, rotations, swing, movement, rendering, and cleanup passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}
