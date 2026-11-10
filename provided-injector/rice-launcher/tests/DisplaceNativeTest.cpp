// Exercise the actual native handlers with a controlled JNI world. No game or authentication.
#include <cstring>
#include <iostream>
#include <stdexcept>
#include <cstdarg>
#include <cmath>
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/combat/displace.cpp"
namespace fixture {
static int world,otherWorld,mc,player,target,list,settings,input,box,blockPos,collisions,packet,otherPacket,state,block;
static int globals=0,queued=0,clicks=0,vertices=0,queries=0;
static int legacyPackets=0,legacyRotations=0,legacySettings=0,legacyVisits=0,legacyResets=0;
static int hit,closer,closerBox;
static bool physicalPressed=false,twoTargets=false,pointAtTarget=false;
static float viewYaw=0,viewPitch=0;
static RotationOverrideAngle previousView{},packetView{},silentView{};
static bool silentActive=false;
static int worldUpdates=0;
static RotationOverrideAngle pendingRotation{};
static void legacyPacket(void*){++legacyPackets;}
static void legacyRotation(void*){++legacyRotations;}
static void legacySetting(void*,void*){++legacySettings;}
static void legacyVisit(void*){++legacyVisits;}
static bool pressed=true,pending=false,brokenWorld=false,allBlocked=false;
static double now=10;
static float inputForward=0,inputStrafe=0;
enum Field {PX=1,PY,PZ,BMinX,BMinY,BMinZ,BMaxX,BMaxY,BMaxZ,Dead,Death,EntityId,BB,MoveInput,Forward,Strafe,Yaw,Pitch};
enum Method {GetId=101,Eye,Loaded,AddBoxes,Size,Clear,Constructor,State,GetBlock};
static void* id(int n){return reinterpret_cast<void*>((size_t)n);}
static int n(void* p){return (int)(size_t)p;}
static void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
static jint push(JNIEnv*,jint){return 0;}
static jobject pop(JNIEnv*,jobject p){return p;}
static jobject global(JNIEnv*,jobject p){++globals;return p;}
static void release(JNIEnv*,jobject){--globals;}
static void local(JNIEnv*,jobject){}
static jboolean same(JNIEnv*,jobject a,jobject b){return a==b;}
static jboolean instance(JNIEnv*,jobject a,jclass cls){return cls==id(400)?(a==&target||a==&closer):cls==id(401)&&a==&packet;}
static jboolean exception(JNIEnv*){return pending;}
static void clearException(JNIEnv*){pending=false;}
static jclass unavailableClass(JNIEnv*,const char*){pending=true;return nullptr;}
static jboolean getBoolean(JNIEnv*,jobject,jfieldID){return false;}
static jint getInt(JNIEnv*,jobject,jfieldID f){return n(f)==EntityId?42:0;}
static jfloat getFloat(JNIEnv*,jobject,jfieldID f){return n(f)==Yaw?viewYaw:n(f)==Pitch?viewPitch:0;}
static jdouble getDouble(JNIEnv*,jobject o,jfieldID f){
    if(o==&closerBox){switch(n(f)){case BMinX:return -.3;case BMinZ:return -.9;case BMinY:return 64;case BMaxX:return .3;case BMaxZ:return -.3;case BMaxY:return 65.8;}}
    if(o==&box){switch(n(f)){case BMinX:case BMinZ:return -.3;case BMinY:return 64;case BMaxX:case BMaxZ:return .3;case BMaxY:return 65.8;}}
    switch(n(f)){case PX:return 0;case PY:return 64;case PZ:return o==&player?-2:0;default:return 0;}
}
static jobject getObject(JNIEnv*,jobject o,jfieldID f){return n(f)==BB?(o==&closer?&closerBox:&box):n(f)==MoveInput?&input:nullptr;}
static void setFloat(JNIEnv*,jobject,jfieldID f,jfloat value){if(n(f)==Forward)inputForward=value;else if(n(f)==Strafe)inputStrafe=value;}
static void setInt(JNIEnv*,jobject,jfieldID,jint){}
static jobject callObject(JNIEnv*,jobject,jmethodID m,...){if(n(m)==GetId)return &target;if(brokenWorld){pending=true;return nullptr;}return n(m)==State?&state:n(m)==GetBlock?&block:nullptr;}
static jboolean callBool(JNIEnv*,jobject,jmethodID,...){++queries;return true;}
static jfloat callFloat(JNIEnv*,jobject,jmethodID m,...){return n(m)==Eye?1.62f:20;}
static jint callInt(JNIEnv*,jobject,jmethodID m,...){return n(m)==Size&&allBlocked?1:0;}
static jint callStaticInt(JNIEnv*,jclass,jmethodID,...){return 0;}
static void callVoid(JNIEnv*,jobject,jmethodID,...){}
static jobject newObject(JNIEnv*,jclass c,jmethodID,...){return c==id(402)?&blockPos:c==id(403)?&box:&collisions;}
static void vertex(double,double,double){++vertices;}
static void one(unsigned){} static void zero(){} static void depth(unsigned char){}
static void blend(unsigned,unsigned){} static void line(float){} static void color(float,float,float,float){}
}
extern "C" {
alignas(Descriptor) char data_2f5d60[sizeof(Descriptor)]={};
unsigned char data_2e8cc0[0x210]={};
jobject data_2f7be0=nullptr;
RotationOverrideAngle get_render_state(){return fixture::pendingRotation;}
RotationOverrideAngle get_entity_rotation(void*,void*){return {fixture::viewYaw,fixture::viewPitch};}
RotationOverrideAngle get_entity_prev_rotation(void*,void*){return fixture::previousView;}
RotationOverrideAngle get_silent_rotation(){return fixture::silentView;}
unsigned char is_silent_rotation_active(){return fixture::silentActive;}
void set_entity_rotation(void*,void*,RotationOverrideAngle r){fixture::viewYaw=r.yaw;fixture::viewPitch=r.pitch;}
void set_entity_prev_rotation(void*,void*,RotationOverrideAngle r){fixture::previousView=r;}
void update_entities(void*,void*){++fixture::worldUpdates;fixture::packetView={fixture::viewYaw,fixture::viewPitch};}
void apply_silent_rotation(RotationOverrideAngle r){fixture::pendingRotation.yaw+=r.yaw;fixture::pendingRotation.pitch+=r.pitch;}
float wrap_angle(float v){v=std::fmod(v+180,360);if(v<0)v+=360;return v-180;}
void knockback_displace_reset(){++fixture::legacyResets;}
bool java_get_key_binding_key_code(Jvm*,jobject k){return fixture::physicalPressed&&(k==fixture::id(500)||k==fixture::id(504));}
jobject java_get_minecraft_5(Jvm*,jobject){return fixture::pointAtTarget?&fixture::hit:nullptr;}
int java_get_moving_object_position_3(Jvm*,jobject){return 2;}
jobject java_get_moving_object_position_2(Jvm*,jobject){return &fixture::target;}
}
extern "C" double time_now(){return fixture::now;}
extern "C" void slinky_logf(const char*,...){}
extern "C" void queue_module_toggle(void*,void*,unsigned char on){fixture::check(!on,"Only self-disable is allowed");++fixture::queued;}
extern "C" unsigned entity_flags_get(Jvm*,jobject){return 0;}
extern "C" jobject java_get_world(Jvm*,jobject){return &fixture::list;}
extern "C" int java_call_list_size(Jvm*,jobject){return fixture::twoTargets?2:1;}
extern "C" jobject java_call_list_get(Jvm*,jobject,int index){return fixture::twoTargets&&index==0?&fixture::closer:&fixture::target;}
extern "C" jobject get_net_handler(Jvm*,jobject){return &fixture::settings;}
extern "C" jobject java_get_game_settings_key_bindings_4(Jvm*,jobject){return fixture::id(500);}
extern "C" jobject java_get_game_settings_key_bindings_5(Jvm*,jobject){return fixture::id(501);}
extern "C" jobject java_get_game_settings_key_bindings_6(Jvm*,jobject){return fixture::id(502);}
extern "C" jobject java_get_game_settings_key_bindings_7(Jvm*,jobject){return fixture::id(503);}
extern "C" jobject java_get_game_settings_key_bindings_8(Jvm*,jobject){return fixture::id(504);}
extern "C" bool is_key_down(Jvm*,jobject k){return fixture::pressed&&(k==fixture::id(500)||k==fixture::id(504));}
extern "C" void java_get_key_binding_press_time_2(Jvm*,jobject){++fixture::clicks;}
extern "C" jobject java_get_entity_player_2(Jvm*,jobject){return nullptr;}
extern "C" int java_get_inventory_player_current_item(Jvm*,jobject){return 0;}
extern "C" jobjectArray java_get_inventory_player_main_inventory(Jvm*,jobject){return nullptr;}
extern "C" jobject java_call_item_stack_get_item(Jvm*,jobject){return nullptr;}
extern "C" int java_call_static_item_get_id_from_item(Jvm*,jobject){return 0;}
extern "C" unsigned enchantment_flags_item_stack(Jvm*,jobject){return 0;}
extern "C" void* slinky_gl_get_proc(const char*){return nullptr;}
extern "C" void libc_free(void*){}
extern "C" int id_vector_count(IdVec*){return 0;}
extern "C" void* id_vector_get(IdVec*,void*){return nullptr;}
extern "C" bool id_vector_insert(IdVec*,void*){return true;}
extern "C" void* id_vector_remove(IdVec*,void*){return nullptr;}
extern "C" void id_vector_clear(IdVec*){}
extern "C" bool id_vector_contains(IdVec*,int){return false;}
extern "C" void key_name_append(char*,char){}
extern "C" void default_name_list(void**,unsigned long long*){}
extern "C" void W_bool(void*,void*,void*){}
extern "C" void W_slider(void*,void*,void*){}
extern "C" void W_enumw(void*,void*,void*){}
extern "C" void W_items(void*,void*,void*){}
extern "C" void R_bool(void*,const char*,void*){}
extern "C" void R_num(void*,const char*,void*){}
extern "C" void R_enum(void*,const char*,void*){}
extern "C" void R_kind2(void*,const char*,void*){}
extern "C" void log_fmt(void*,int,const char*,...){}
int main(){using namespace fixture;try{
    JNINativeInterface_ api={};api.PushLocalFrame=push;api.PopLocalFrame=pop;api.NewGlobalRef=global;api.DeleteGlobalRef=release;
    api.DeleteLocalRef=local;api.IsSameObject=same;api.IsInstanceOf=instance;api.ExceptionCheck=exception;api.ExceptionClear=clearException;
    api.GetBooleanField=getBoolean;api.GetIntField=getInt;api.GetFloatField=getFloat;api.GetDoubleField=getDouble;api.GetObjectField=getObject;
    api.SetFloatField=setFloat;api.SetIntField=setInt;api.CallObjectMethod=callObject;api.CallBooleanMethod=callBool;api.CallFloatMethod=callFloat;
    api.CallIntMethod=callInt;api.CallVoidMethod=callVoid;api.NewObject=newObject;
    api.CallStaticIntMethod=callStaticInt;api.FindClass=unavailableClass;
    JNIEnv env=&api;JIds ids={};Jvm j={&ids,&env};
    ids.Entity_posX=id(PX);ids.Entity_posY=id(PY);ids.Entity_posZ=id(PZ);ids.Entity_prevPosX=id(PX);ids.Entity_prevPosY=id(PY);ids.Entity_prevPosZ=id(PZ);
    ids.Entity_entityId=id(EntityId);ids.Entity_isDead=id(Dead);ids.Entity_Lazt_=id(BB);
    ids.AxisAlignedBB_minX=id(BMinX);ids.AxisAlignedBB_minY=id(BMinY);ids.AxisAlignedBB_minZ=id(BMinZ);
    ids.AxisAlignedBB_maxX=id(BMaxX);ids.AxisAlignedBB_maxY=id(BMaxY);ids.AxisAlignedBB_maxZ=id(BMaxZ);
    ids.EntityLivingBase_moveForward=id(Forward);ids.EntityLivingBase_moveStrafing=id(Strafe);
    ids.World_getEntityByID=id(GetId);ids.EntityPlayer=id(400);ids.C03PacketPlayer=id(401);ids.BlockPos=id(402);ids.AxisAlignedBB=id(403);
    ids.Entity_rotationYaw=id(Yaw);ids.Entity_rotationPitch=id(Pitch);
    auto* legacy=reinterpret_cast<Descriptor*>(data_2f5d60);
    legacy->name="Knockback Displace";legacy->category=0;
    legacy->events[3]={legacyPacket,0};legacy->events[13]={legacyRotation,-1000};
    legacy->settings=legacySetting;legacy->visit=legacyVisit;
    Context c={&j,nullptr,nullptr,&mc,&world,&player};auto* desc=(Descriptor*)displace_descriptor();
    check(desc==legacy&&std::strcmp(desc->name,"Knockback Displace")==0&&desc->category==0&&mode.value==2&&mode.count==3,"Modes must be installed into original module, defaulting to Dynamic");
    // Known mappings supplied by this controlled world; resolution failure tested separately below.
    mapped=true;movementInput=id(MoveInput);forwardInput=id(Forward);strafeInput=id(Strafe);eyeHeight=id(Eye);deathTime=id(Death);
    loaded=id(Loaded);addBoxes=id(AddBoxes);listInit=id(Constructor);listSize=id(Size);listClear=id(Clear);
    Toggle toggle={5,&c,desc,1};desc->events[5].fn(&toggle);check(globals==1&&!queued,"Enable must retain only the world");
    mode.value=1;blink.value=1;Event update={17,&c};desc->events[17].fn(&update);
    check(cycle.active&&cycle.phase&&hasArrow&&globals==2,"Static attack must select a target and retain it");
    Rotation rotation={13};desc->events[13].fn(&rotation);check(rotation.on&&rotation.yaw==-90,"Rotation handler must publish selected yaw");
    // Simulate the real final rotation stage with a live manual view and an
    // Aim Assist delta already queued. Use the actual shared engine helper.
    viewYaw=25;viewPitch=10;pendingRotation={12,-3};
    Rotation assisted={13,1,1,0,0,25,10};desc->events[13].fn(&assisted);
    check(assisted.yaw==-53&&assisted.pitch==7&&(assisted.reserved&kApplyRotationOverride),"Static rotation must include current Aim Assist view and request final arbitration");
    queue_rotation_override(&j,&player,{assisted.yaw,assisted.pitch});
    check(viewYaw+pendingRotation.yaw==-53&&viewPitch+pendingRotation.pitch==7&&viewYaw==25,"Final override must account for queued aim without changing camera view");
    viewYaw=42;viewPitch=20;pendingRotation={};Rotation manual={13,0,0,0,0,42,20};desc->events[13].fn(&manual);
    queue_rotation_override(&j,&player,{manual.yaw,manual.pitch});
    check(viewYaw+pendingRotation.yaw==-48&&viewPitch+pendingRotation.pitch==20,"Manual turning between update and rotation must remain live");
    // Unlike final arbitration, vanilla sends its look packet inside the world
    // update. Assert the current phase is present there and restored afterward.
    desc->enabled=1;viewYaw=25;viewPitch=10;previousView={20,8};pendingRotation={12,-3};
    displace_update_entities(&j,&world,&c);
    check(packetView.yaw==-53&&packetView.pitch==7&&worldUpdates==1,
        "Vanilla player update must see this tick's Displace rotation before sending movement/look packets");
    check(viewYaw==25&&viewPitch==10&&previousView.yaw==20&&previousView.pitch==8&&pendingRotation.yaw==12,
        "World update must restore camera/previous rotation and preserve queued Aim Assist input");
    viewYaw=viewPitch=0;pendingRotation={};
    Move movement={21,&c,0,0,.91f};desc->events[21].fn(&movement);check(movement.forward==1&&inputForward==1,"Movement input and physics must both advance");
    Packet p={3,&j,&otherPacket};desc->events[3].fn(&p);check(p.result==0,"Blink must start at a movement packet");
    p.packet=&packet;desc->events[3].fn(&p);check(p.result==1,"Movement packet must start the short blink");
    p.packet=&otherPacket;p.result=0;desc->events[3].fn(&p);check(p.result==1,"Other packets must join the same blink");
    p.result=2;desc->events[3].fn(&p);check(p.result==2,"Displace must preserve another module's packet drop");
    p.result=3;desc->events[3].fn(&p);check(p.result==3,"Displace must preserve explicit packet pass");
    Event preTick={16,&c};desc->events[16].fn(&preTick);p.result=0;desc->events[3].fn(&p);check(p.result==0,"Next game tick must release blink");
    desc->events[17].fn(&update);check(!cycle.phase&&clicks==1,"Alternating tick must restore one attack click");
    silentActive=true;silentView={35,15};viewYaw=-55;viewPitch=15;pendingRotation={4,-2};
    displace_update_entities(&j,&world,&c);
    check(packetView.yaw==39&&packetView.pitch==13&&viewYaw==-55,
        "Release-phase packet must use live manual/Aim Assist view instead of last tick's displaced yaw");
    silentActive=false;viewYaw=viewPitch=0;pendingRotation={};
    movement.forward=movement.strafe=0;desc->events[21].fn(&movement);check(movement.strafe==-1&&inputStrafe==-1,"Following input must compensate left");
    gl={one,zero,one,one,depth,blend,line,one,zero,color,vertex};Render render={6,{},&c,1};desc->events[6].fn(&render);
    check(vertices==18,"Direction arrow must emit twelve triangle and six outline vertices");
    pressed=false;now+=.05;desc->events[17].fn(&update);check(!cycle.active&&fadeStart>0&&!std::strcmp(activationState,"Hold attack"),"Releasing attack must start fade and identify missing attack input");
    pendingRotation={6,2};Rotation inactive={13,1,1,0,0,10,5};desc->events[13].fn(&inactive);
    check(inactive.yaw==10&&inactive.pitch==5&&!inactive.reserved&&pendingRotation.yaw==6,
        "Inactive Displace must leave Aim Assist/manual rotation untouched");pendingRotation={};
    vertices=0;now+=.1;desc->events[6].fn(&render);check(vertices==18,"Arrow must remain during its 250ms fade");
    now+=.2;desc->events[6].fn(&render);check(!arrowTarget&&globals==1,"Expired arrow must release target reference");
    // Physical input remains usable when another module rewrites logical keys.
    physicalPressed=true;pressed=false;twoTargets=true;pointAtTarget=true;
    desc->events[17].fn(&update);
    check(cycle.active&&arrowTarget==&target,"Manual crosshair target must win over a closer unrelated player with logical keys released");
    pointAtTarget=false;data_2e8cc0[0x14]=1;data_2f7be0=&target;
    desc->events[17].fn(&update);
    check(cycle.active&&arrowTarget==&target,"Aim Assist target must win over unrelated nearest player");
    data_2e8cc0[0x14]=0;data_2f7be0=nullptr;physicalPressed=false;twoTargets=false;
    pressed=true;mode.value=2;ids.id_328=id(State);ids._init__getBlock=id(GetBlock);
    desc->events[17].fn(&update);check(cycle.active&&cycle.phase&&queries>0,"Dynamic must scan the mapped native world and select a path");
    viewYaw=55;viewPitch=20;pendingRotation={7,3};Rotation dynamicAim={13,1,1,0,0,55,20};
    desc->events[13].fn(&dynamicAim);queue_rotation_override(&j,&player,{dynamicAim.yaw,dynamicAim.pitch});
    check(std::abs(wrap_angle(viewYaw+pendingRotation.yaw-dynamicAim.yaw))<.001&&dynamicAim.pitch==23,
        "Dynamic void direction must remain correct with queued Aim Assist and manual camera yaw");
    // A later handler may win the same event. The engine applies only the final
    // event target, so the earlier Displace request cannot corrupt that winner.
    pendingRotation={7,3};dynamicAim.yaw=110;dynamicAim.pitch=-70;
    queue_rotation_override(&j,&player,{dynamicAim.yaw,dynamicAim.pitch});
    check(viewYaw+pendingRotation.yaw==110&&viewPitch+pendingRotation.pitch==-70,
        "Final rotation arbitration must preserve a later handler's target");
    viewYaw=viewPitch=0;pendingRotation={};
    allBlocked=true;desc->events[17].fn(&update);check(!cycle.active&&!queued&&!std::strcmp(activationState,"No void path"),"Blocked Dynamic paths must report no void path without an error");
    allBlocked=false;brokenWorld=true;desc->events[17].fn(&update);check(!cycle.active,"World-query failure must stop the module");
    check(queued==1&&globals==0,"Failed Dynamic query must self-disable and release references");
    mapped=false;movementInput=nullptr;queued=0;ids.id_328=nullptr;desc->events[5].fn(&toggle);check(queued==1&&globals==0,"Missing 1.8 mappings must safely disable");
    // Static must not require Dynamic world queries or optional reflection.
    // Both were required by the old enable path, hiding the module's core behavior.
    mode.value=1;brokenWorld=false;movementInput=nullptr;mapped=false;
    desc->events[5].fn(&toggle);
    check(globals==1&&queued==1&&!pending&&!movementInput,
        "Static must enable with unavailable world-scan mappings and MovementInput reflection");
    desc->events[17].fn(&update);
    check(cycle.active&&cycle.phase&&!std::strcmp(activationState,"Active"),
        "Static targeting must run without resolving Dynamic scan methods");
    Move fallbackMove={21,&c,0,0,.91f};desc->events[21].fn(&fallbackMove);
    check(fallbackMove.forward==1,"Unavailable optional input fields must retain physics-event movement compensation");
    mode.value=2;
    mapped=true;movementInput=id(MoveInput);desc->events[5].fn(&toggle);c.world=&otherWorld;Event frame={1,&c};desc->events[1].fn(&frame);
    check(globals==0&&queued==2,"World switch must release state and disable");
    // Switching back to Slinky must restore original callbacks and cleanly
    // release imported references/packet state without a second module.
    c.world=&world;desc->events[5].fn(&toggle);
    mode.value=0;desc->events[1].fn(&frame);
    viewYaw=17;viewPitch=6;pendingRotation={9,3};displace_update_entities(&j,&world,&c);
    check(packetView.yaw==17&&packetView.pitch==6,"Original Slinky mode must bypass the imported world-update rotation entirely");
    viewYaw=viewPitch=0;pendingRotation={};
    check(globals==0&&!loadPacket(),"Switching to Slinky must release imported state");
    int previousPackets=legacyPackets;desc->events[3].fn(&p);
    int previousRotations=legacyRotations;desc->events[13].fn(&rotation);
    desc->settings(nullptr,nullptr);desc->visit(nullptr);
    check(legacyPackets==previousPackets+1&&legacyRotations==previousRotations+1&&legacySettings==1&&legacyVisits==1,"Slinky mode must retain original events, settings, and persistence");
    mode.value=1;desc->events[1].fn(&frame);check(globals==1&&runningMode()==1,"Switching back to Static must initialize imported state once");
    displace_cleanup(&env);check(globals==0&&!loadPacket(),"Unload must leave no references or held packets");
    std::cout<<"Integrated Displace modes, manual/Aim Assist arbitration, physical input, targeting, blink, and cleanup passed\n";return 0;
}catch(const std::exception& error){std::cerr<<error.what()<<'\n';return 1;}}
