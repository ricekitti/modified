#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#define SOURCE_FEATURES_TEST
#include <windows.h>
#include <cstdlib>
#include <cstdio>
#include <cstdarg>
#include <string>
#include <fstream>
#include <sstream>
#include <stdexcept>
#include <cmath>
#include <vector>
#include <cstring>
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/combat/source_features.cpp"
static std::vector<std::string> renderedSettings;
static unsigned priority;
static RotationOverrideAngle pending{},retained{};
static bool retainActive;
static int toggles;
static bool timerFrozen;
static void originalDisplaceEvent(void*){}
static void originalDisplaceSettings(void*,void*){}
static void originalDisplaceVisit(void*){}
extern "C" {
void hud_editor_scaffold(float* values){values[0]=values[1]=0;}
alignas(sourcefeatures::Descriptor) char data_2f5d60[0x210]={};
char data_2f5580[0x210]={},data_2f2c90[0x210]={};unsigned char data_304a1c=0;
float data_2f2ed0=.2f,data_2f2f08=.2f,data_2f2f40=.2f;
int rescueNotifications=0;void anti_fireball_record_rescue_launch(JNIEnv*){++rescueNotifications;}
alignas(sourcefeatures::Descriptor) char data_2f1d90[0x210]={};float data_2f2028=1.5f;char data_2eb740[0x210]={};
char data_2f9e30[0x500]={};
alignas(8) char data_2f6088[64]={};
unsigned data_2f60b8=0,data_304af4=0,data_304af8=0;int data_304b08=0;
unsigned char data_304af1=0,data_304af2=0;float data_2f5fd8=90,data_2f5fa0=50;
jobject java_get_minecraft_5(Jvm*,jobject){return nullptr;}
int java_get_moving_object_position_3(Jvm*,jobject){return 2;}
jobject java_get_moving_object_position_2(Jvm*,jobject){return nullptr;}
RotationOverrideAngle get_entity_rotation(void* raw,void* player){auto j=(Jvm*)raw;auto env=j->env;return {(*env)->GetFloatField(env,player,j->ids->Entity_rotationYaw),(*env)->GetFloatField(env,player,j->ids->Entity_rotationPitch)};}

unsigned char data_2f7259=0;JIds* data_2f6ef0=nullptr;
void* data_2f6f08=(void*)1;
void* libc_calloc(unsigned long long a,unsigned long long b){return std::calloc((size_t)a,(size_t)b);}
void libc_free(void* p){std::free(p);}
void slinky_logf(const char* format,...){va_list args;va_start(args,format);std::vprintf(format,args);std::printf("\n");va_end(args);}
void queue_module_toggle(void*,void*,unsigned char){++toggles;}
unsigned entity_flags_get(void*,void*){return 0;}
bool increment_wrap(unsigned p){if(p<priority)return false;priority=p;return true;}
bool pending_action_flag(){return timerFrozen;}
void apply_player_rotation(void*,unsigned char on,unsigned char,unsigned char,RotationOverrideAngle r){retainActive=on!=0;retained=r;}
void apply_silent_rotation(RotationOverrideAngle r){pending.yaw+=r.yaw;pending.pitch+=r.pitch;}
RotationOverrideAngle get_render_state(){return pending;}
void set_rotation_active(void* raw,void* player,bool){auto j=(Jvm*)raw;auto e=j->env;float yaw=(*e)->GetFloatField(e,player,j->ids->Entity_rotationYaw);float pitch=(*e)->GetFloatField(e,player,j->ids->Entity_rotationPitch);(*e)->SetFloatField(e,player,j->ids->Entity_rotationYaw,yaw+pending.yaw);(*e)->SetFloatField(e,player,j->ids->Entity_rotationPitch,pitch+pending.pitch);pending={};}
void W_label(void*,void*,const char* label){renderedSettings.push_back(label);}
void W_bool(void*,void*,void* raw){renderedSettings.push_back(((sourcefeatures::Bool*)raw)->label);}
void W_slider(void*,void*,void* raw){renderedSettings.push_back(((sourcefeatures::Number*)raw)->label);}
void W_enumw(void*,void*,void* raw){renderedSettings.push_back(((sourcefeatures::Choice*)raw)->label);}
void R_bool(void*,const char*,void*){}void R_num(void*,const char*,void*){}void R_enum(void*,const char*,void*){}
void log_fmt(void*,int,const char*,...){}
void W_color(void*,void*,void*){}void R_key(void*,const char*,void*){}
jobject get_minecraft(Jvm* j){return (*j->env)->GetStaticObjectField(j->env,j->ids->Minecraft,j->ids->Minecraft_Lbao_);}
jobject get_world(Jvm* j,void* mc){return (*j->env)->GetObjectField(j->env,mc,j->ids->Minecraft_Lbjf_);}
jobject get_local_player(Jvm* j,void* mc){return (*j->env)->GetObjectField(j->env,mc,j->ids->id_188);}
}
static void check(bool condition,const char* message){if(!condition)throw std::runtime_error(message);}
int main(int argc,char** argv){try{
    check(argc==2||argc==3,"Expected classpath file and optional naming configuration");SetEnvironmentVariableW(L"JAVA_TOOL_OPTIONS",nullptr);SetEnvironmentVariableW(L"_JAVA_OPTIONS",nullptr);
    std::vector<std::string> names={"1","theMinecraft","Lnet/minecraft/client/Minecraft;","thePlayer","Lnet/minecraft/client/entity/EntityPlayerSP;","theWorld","Lnet/minecraft/client/multiplayer/WorldClient;","net/minecraft/entity/Entity","rotationYaw","rotationPitch","posX","posZ","onGround","minY","net/minecraft/util/AxisAlignedBB","net/minecraft/entity/player/InventoryPlayer","currentItem"};
    if(argc==3){std::ifstream mappings(argv[2]);for(auto& name:names)check((bool)std::getline(mappings,name),"Incomplete naming configuration");}
    std::ifstream input(argv[1]);std::ostringstream contents;contents<<input.rdbuf();std::string option="-Djava.class.path="+contents.str();
    SetDllDirectoryW(L"C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.12.101-hotspot\\bin");
    auto library=LoadLibraryW(L"C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.12.101-hotspot\\bin\\server\\jvm.dll");check(library!=nullptr,"JVM library unavailable");
    struct Option{char* text;void* extra;};struct Arguments{int version,count;Option* options;unsigned char ignore;};
    Option options[]={{&option[0],nullptr}};Arguments arguments={0x00010008,1,options,0};void* vm=nullptr;JNIEnv* env=nullptr;
    auto create=(int(*)(void**,void**,void*))GetProcAddress(library,"JNI_CreateJavaVM");check(create&&create(&vm,(void**)&env,&arguments)==0,"Could not start test JVM");
    auto fixture=(*env)->FindClass(env,"FeatureFixture");if(!fixture&&(*env)->ExceptionCheck(env))(*env)->ExceptionDescribe(env);check(fixture!=nullptr,"Missing Java fixture");auto factory=(*env)->GetStaticMethodID(env,fixture,"createObject","()Ljava/lang/Object;");
    jobject mc=(*env)->CallStaticObjectMethod(env,fixture,factory);if((*env)->ExceptionCheck(env))(*env)->ExceptionDescribe(env);check(mc!=nullptr&&!(*env)->ExceptionCheck(env),"Minecraft fixture failed");
    JIds ids={};ids.names=std::stoi(names[0]);ids.Minecraft=(*env)->GetObjectClass(env,mc);ids.Minecraft_Lbao_=(*env)->GetStaticFieldID(env,ids.Minecraft,names[1].c_str(),names[2].c_str());
    auto entity=(*env)->FindClass(env,names[7].c_str());ids.Entity_rotationYaw=(*env)->GetFieldID(env,entity,names[8].c_str(),"F");ids.Entity_rotationPitch=(*env)->GetFieldID(env,entity,names[9].c_str(),"F");
    auto original=(sourcefeatures::Descriptor*)data_2f5d60;
    original->name="Knockback Displace";original->description="Original Slinky";
    for(unsigned t:{3u,13u,14u,15u,18u})original->events[t]={originalDisplaceEvent,(int)t,0};
    original->settings=originalDisplaceSettings;original->visit=originalDisplaceVisit;original->status=originalDisplaceVisit;
    unsigned char originalBytes[sizeof(sourcefeatures::Descriptor)];std::memcpy(originalBytes,original,sizeof originalBytes);
    Jvm java={&ids,env};check(source_features_initialize(&java),"Java module initialization failed");displace_install();
    auto pearlFixture=(*env)->FindClass(env,"PearlFixture");
    auto pearlRun=pearlFixture?(*env)->GetStaticMethodID(env,pearlFixture,"run","(Ljava/lang/Class;)V"):nullptr;
    check(pearlRun!=nullptr,"Auto Pearl fixture missing");
    (*env)->CallStaticVoidMethod(env,pearlFixture,pearlRun,sourcefeatures::bridge);
    check(!sourcefeatures::error(env,"Auto Pearl fixture"),"Auto Pearl behavior fixture failed");
    auto comboRun=(*env)->GetStaticMethodID(env,pearlFixture,"combo","(Ljava/lang/Class;)V");
    (*env)->CallStaticVoidMethod(env,pearlFixture,comboRun,sourcefeatures::bridge);
    check(!sourcefeatures::error(env,"Fireball Pearl Clutch fixture"),"Combo behavior fixture failed");
    check(rescueNotifications==1,"Successful combo must notify native AntiFireball exactly once");
    auto eggRun=(*env)->GetStaticMethodID(env,pearlFixture,"egg","(Ljava/lang/Class;)V");
    (*env)->CallStaticVoidMethod(env,pearlFixture,eggRun,sourcefeatures::bridge);
    check(!sourcefeatures::error(env,"Bridge Egg fixture"),"Bridge Egg behavior fixture failed");
    auto quickFixture=(*env)->FindClass(env,"QuickBuyFixture");
    auto quickRun=quickFixture?(*env)->GetStaticMethodID(env,quickFixture,"run","(Ljava/lang/Class;)V"):nullptr;
    check(quickRun!=nullptr,"Independent Quick Buy fixture missing");
    (*env)->CallStaticVoidMethod(env,quickFixture,quickRun,sourcefeatures::bridge);
    check(!sourcefeatures::error(env,"Quick Buy helpers"),"Independent Quick Buy behavior failed");
    check(sourcefeatures::counts[6]==14&&sourcefeatures::counts[7]==12&&sourcefeatures::counts[8]==8,"Rescue native settings counts differ from source");
    check(sourcefeatures::descriptors[6]->category==4&&sourcefeatures::descriptors[7]->category==4&&sourcefeatures::descriptors[8]->category==4,"Rescue modules belong in Utility");
    auto rescueSetting=[](unsigned index,const char* key)->sourcefeatures::Setting&{for(unsigned n=0;n<sourcefeatures::counts[index];++n)if(sourcefeatures::equal(sourcefeatures::settings[index][n].key,key))return sourcefeatures::settings[index][n];throw std::runtime_error(key);};
    const char* screenshotKeys[]={"range","avoid_enemies","edge_margin","pre_pearl_timer","after_pearl_timer"};
    const char* screenshotValues[]={"40 blocks","10 blocks","2 blocks","0.02x","0.1x"};
    for(unsigned row=0;row<5;++row){auto& setting=rescueSetting(6,screenshotKeys[row]);char text[64];std::snprintf(text,sizeof(text),setting.number.format,(double)setting.number.value);check(!std::strcmp(text,screenshotValues[row]),"Auto Pearl display must match supplied Valen screenshot");}
    check(sourcefeatures::equal(rescueSetting(6,"pre_pearl_timer").persistence,"source_auto_pearl_pre_pearl_timer_valen"),"Old pre-pearl timer profiles must not override screenshot default");
    check(rescueSetting(6,"pre_pearl_timer").number.min==0&&rescueSetting(6,"pre_pearl_timer").number.max==1&&std::abs(rescueSetting(6,"pre_pearl_timer").number.step-.01f)<1e-7,"Valen timer bounds/step must be retained");
    check(!sourcefeatures::visible(6,"in_void_for")&&!sourcefeatures::visible(6,"time"),"Void duration controls must be hidden until Above void is on");
    rescueSetting(6,"above_void").boolean.value=1;check(sourcefeatures::visible(6,"in_void_for")&&!sourcefeatures::visible(6,"time"),"Above void reveals the timed-void switch");
    rescueSetting(6,"in_void_for").boolean.value=1;check(sourcefeatures::visible(6,"time"),"Timed void reveals its duration");
    rescueSetting(6,"predict_safe_landing").boolean.value=1;check(!sourcefeatures::visible(6,"in_void_for")&&!sourcefeatures::visible(6,"time"),"Prediction must hide timed-void controls");
    rescueSetting(6,"above_void").boolean.value=rescueSetting(6,"in_void_for").boolean.value=rescueSetting(6,"predict_safe_landing").boolean.value=0;
    check(sourcefeatures::descriptors[6]->events[13].fn==sourcefeatures::dispatch<6>&&sourcefeatures::descriptors[7]->events[13].fn==sourcefeatures::dispatch<7>&&sourcefeatures::descriptors[8]->events[13].fn==sourcefeatures::dispatch<8>,"Rescue native rotation slots are not distinct");
    check(sourcefeatures::equal(sourcefeatures::settings[6][0].persistence,"source_auto_pearl_range")&&sourcefeatures::equal(sourcefeatures::settings[7][0].persistence,"source_fireball_pearl_clutch_range")&&sourcefeatures::equal(sourcefeatures::settings[8][0].persistence,"source_bridge_egg_lowest_y"),"Rescue persistence namespaces collide");
    sourcefeatures::data_2f5580[0x14]=1;sourcefeatures::data_304a1c=1;check(sourcefeatures::rescueHost(env,nullptr,0,0)==1,"Clutch planned-target host query missing");sourcefeatures::data_2f5580[0x14]=0;check(sourcefeatures::rescueHost(env,nullptr,0,0)==0,"Disabled Clutch must not block rescue");sourcefeatures::data_304a1c=0;
    check(original->events[14].fn==sourcefeatures::dispatch<0>&&original->events[14].priority==1000,"Supplied Displace must own manual interaction");
    check(original->events[13].priority==-1000&&original->events[18].priority==1000,"Displace event priorities changed");
    check(sourcefeatures::counts[0]==12&&sourcefeatures::counts[1]==18,"Supplied Displace and Bow settings missing");
    check(sourcefeatures::counts[4]==12,"Supplied Scaffold must expose exactly 12 source controls");
    auto plates=(sourcefeatures::Descriptor*)bedplates_descriptor();
    check(plates->category==2&&!plates->enabled&&plates->events[7].fn&&plates->events[18].fn&&plates->events[26].fn,"Bedplates registration missing");
    renderedSettings.clear();plates->settings(nullptr,nullptr);
    check(renderedSettings==std::vector<std::string>({"Align Top Center","Auto Scale","Show Block Count","Scale","Render Distance","Y Offset","Mode","Customize Key"}),"Bedplates static controls must match supplied defaults");
    bedplatesnative::mode.value=2;renderedSettings.clear();plates->settings(nullptr,nullptr);
    check(renderedSettings.size()==9&&renderedSettings[7]=="Keybind"&&renderedSettings.back()=="Customize Key","Bedplates Hold/Toggle must expose display and customize keybinds");
    bedplatesnative::mode.value=0;
    sourcefeatures::Context plateContext={};plateContext.java=&java;plateContext.mc=mc;
    sourcefeatures::Toggle plateToggle={5,&plateContext,plates,1};plates->events[5].fn(&plateToggle);
    check(!(*env)->ExceptionCheck(env),"Bedplates native-to-Java toggle signature mismatch");
    auto mine=(sourcefeatures::Descriptor*)data_2f1d90;
    static int mineCalls;static float observedMineSpeed;
    mine->events[18].fn=[](void*){++mineCalls;observedMineSpeed=data_2f2028;};
    mine->settings=[](void*,void*){renderedSettings.insert(renderedSettings.end(),{"Speed","Break delay","Decrement block hit delay"});};
    auto anticheat=(sourcefeatures::Descriptor*)rice_anticheat_descriptor();
    check(anticheat->category==4&&!anticheat->enabled&&anticheat->events[2].fn&&anticheat->events[18].fn,"Anticheat must be a disabled Utility module with packet/tick handlers");
    renderedSettings.clear();mine->settings(nullptr,nullptr);
    check(renderedSettings==std::vector<std::string>({"Speed","Break delay","Decrement block hit delay","Ignore fatigue"}),"Fast Mine must keep existing controls and default fatigue to Off");
    riceutility::fatigue.value=2;renderedSettings.clear();mine->settings(nullptr,nullptr);
    check(renderedSettings.back()=="Dig speed"&&riceutility::digSpeed.value==75,"Increase must expose supplied fatigue dig speed");
    sourcefeatures::Context riceContext={};riceContext.java=&java;riceContext.mc=mc;
    sourcefeatures::Event mineLate={18,&riceContext};mine->enabled=1;
    auto fatigueFixture=(*env)->GetStaticMethodID(env,fixture,"nativeFatigue","(I)V");
    (*env)->CallStaticVoidMethod(env,fixture,fatigueFixture,(jint)1);
    riceutility::fatigue.value=0;mine->events[18].fn(&mineLate);
    check(mineCalls==1&&observedMineSpeed==1.5f&&data_2f2028==1.5f,"Fatigue Off must delegate the original speed unchanged");
    riceutility::fatigue.value=2;mine->events[18].fn(&mineLate);
    check(mineCalls==2&&std::abs(observedMineSpeed-(.75f/.09f))<.001&&data_2f2028==1.5f,"Increase must override only the native callback scope and restore saved Speed");
    (*env)->CallStaticVoidMethod(env,fixture,fatigueFixture,(jint)-1);mine->events[18].fn(&mineLate);
    check(mineCalls==3&&observedMineSpeed==1.5f,"No fatigue must leave ordinary Fast Mine unchanged");
    riceutility::fatigue.value=1;
    auto fatiguePacketFixture=(*env)->GetStaticMethodID(env,fixture,"nativeFatiguePacket","()Ljava/lang/Object;");
    auto fatiguePacket=(*env)->CallStaticObjectMethod(env,fixture,fatiguePacketFixture);sourcefeatures::Packet heldPacket={2,&java,fatiguePacket,0,0,0};mine->events[2].fn(&heldPacket);
    check(heldPacket.result==2,"Hold mode must consume only the local fatigue effect through native packet verdict");
    riceutility::fatigue.value=0;sourcefeatures::Toggle mineToggle={5,&riceContext,mine,0};mine->enabled=0;mine->events[5].fn(&mineToggle);
    (*env)->CallStaticVoidMethod(env,fixture,fatigueFixture,(jint)-1);
    check(!sourcefeatures::error(env,"Rice utility integration"),"Anticheat/Fatigue native bridge raised an exception");
    auto player=(*env)->GetObjectField(env,mc,(*env)->GetFieldID(env,ids.Minecraft,names[3].c_str(),names[4].c_str()));
    auto world=(*env)->GetObjectField(env,mc,(*env)->GetFieldID(env,ids.Minecraft,names[5].c_str(),names[6].c_str()));
    ids.Minecraft_Lbjf_=(*env)->GetFieldID(env,ids.Minecraft,names[5].c_str(),names[6].c_str());ids.id_188=(*env)->GetFieldID(env,ids.Minecraft,names[3].c_str(),names[4].c_str());
    auto inventoryType=(*env)->FindClass(env,names[15].c_str());ids.InventoryPlayer_currentItem=(*env)->GetFieldID(env,inventoryType,names[16].c_str(),"I");
    auto playerClassName=names[4].substr(1,names[4].size()-2);
    ids.EntityPlayerSP=(*env)->FindClass(env,playerClassName.c_str());
    ids.Entity_posX=(*env)->GetFieldID(env,entity,names[10].c_str(),"D");
    ids.Entity_posZ=(*env)->GetFieldID(env,entity,names[11].c_str(),"D");
    ids.Entity_onGround=(*env)->GetFieldID(env,entity,names[12].c_str(),"Z");
    auto boxType=(*env)->FindClass(env,names[14].c_str());ids.AxisAlignedBB_minY=(*env)->GetFieldID(env,boxType,names[13].c_str(),"D");
    auto getBox=(*env)->GetStaticMethodID(env,fixture,"scaffoldBox","()Ljava/lang/Object;");auto box=(*env)->CallStaticObjectMethod(env,fixture,getBox);
    double actualX=(*env)->GetDoubleField(env,player,ids.Entity_posX),actualZ=(*env)->GetDoubleField(env,player,ids.Entity_posZ),actualY=(*env)->GetDoubleField(env,box,ids.AxisAlignedBB_minY);
    float cameraYaw=(*env)->GetFloatField(env,player,ids.Entity_rotationYaw),cameraPitch=(*env)->GetFloatField(env,player,ids.Entity_rotationPitch);
    {
        ScaffoldPacketRotationScope rotation{env,player,ids.Entity_rotationYaw,ids.Entity_rotationPitch};rotation.apply(123,86);
        check((*env)->GetFloatField(env,player,ids.Entity_rotationYaw)==123&&(*env)->GetFloatField(env,player,ids.Entity_rotationPitch)==86,"Packet rotation scope did not expose helper angles");
        check((*env)->GetDoubleField(env,player,ids.Entity_posX)==actualX,"Packet rotation must not modify coordinates");
    }
    check((*env)->GetFloatField(env,player,ids.Entity_rotationYaw)==cameraYaw&&(*env)->GetFloatField(env,player,ids.Entity_rotationPitch)==cameraPitch,"Packet rotation scope did not restore camera angles");
    jthrowable rotationError=nullptr;
    {
        ScaffoldPacketRotationScope rotation{env,player,ids.Entity_rotationYaw,ids.Entity_rotationPitch};rotation.apply(123,86);
        (*env)->SetFloatField(env,player,ids.Entity_rotationYaw,77);
        auto errorClass=(*env)->FindClass(env,"java/lang/IllegalStateException");(*env)->ThrowNew(env,errorClass,"original walking failure");(*env)->DeleteLocalRef(env,errorClass);
        rotationError=(*env)->ExceptionOccurred(env);
    }
    auto scopeError=(*env)->ExceptionOccurred(env);(*env)->ExceptionClear(env);
    check(scopeError&&(*env)->IsSameObject(env,rotationError,scopeError),"Packet rotation cleanup swallowed the original walking exception");
    check((*env)->GetFloatField(env,player,ids.Entity_rotationYaw)==77&&(*env)->GetFloatField(env,player,ids.Entity_rotationPitch)==cameraPitch,"Packet rotation cleanup overwrote a newer angle writer");
    (*env)->DeleteLocalRef(env,rotationError);(*env)->DeleteLocalRef(env,scopeError);(*env)->SetFloatField(env,player,ids.Entity_rotationYaw,cameraYaw);
    sourcefeatures::Context context={&java,(void*)1,nullptr,mc,world,player};
    auto prepareAboveVoid=(*env)->GetStaticMethodID(env,pearlFixture,"prepareAboveVoid","(Ljava/lang/Class;)V");
    auto checkAboveVoid=(*env)->GetStaticMethodID(env,pearlFixture,"checkAboveVoid","(Ljava/lang/Class;)V");
    (*env)->CallStaticVoidMethod(env,pearlFixture,prepareAboveVoid,sourcefeatures::bridge);
    check(!sourcefeatures::error(env,"Above void setup"),"Above void fixture setup failed");
    priority=0;hook_walking_player(env,nullptr,player);
    check(!sourcefeatures::error(env,"Above void walking"),"Above void walking hook failed");
    (*env)->CallStaticVoidMethod(env,pearlFixture,checkAboveVoid,sourcefeatures::bridge);
    check(!sourcefeatures::error(env,"Above void launch"),"Above void walking launch failed");
    auto mayu=sourcefeatures::descriptors[4];
    check(mayu->category==3&&mayu->settings&&mayu->visit,"Mayu Scaffold registration missing");
    check(!mayu->events[13].fn,"Mayu must not arm the host persistent rotation/key remapper");
    check(sourcefeatures::settings[4][0].choice.count==3&&sourcefeatures::settings[4][0].choice.value==1,"Mayu rotation modes/default differ from source");
    check(sourcefeatures::equal(sourcefeatures::settings[4][0].persistence,"supplied_scaffold_mode"),"Mayu must not load old Rise configuration values");
    sourcefeatures::Toggle toggle={5,&context,nullptr,1};sourcefeatures::Event late={18,&context};
    auto prepareDisplace=(*env)->GetStaticMethodID(env,fixture,"prepareDisplace","(Ljava/lang/Class;II)V");
    auto displace=sourcefeatures::descriptors[0];displace->enabled=1;toggle.module=displace;displace->events[5].fn(&toggle);
    auto setting=[&](const char* key)->sourcefeatures::Setting&{for(unsigned n=0;n<sourcefeatures::counts[0];++n)if(sourcefeatures::equal(sourcefeatures::settings[0][n].key,key))return sourcefeatures::settings[0][n];throw std::runtime_error(key);};
    check(setting("direction").choice.count==6&&setting("direction").choice.value==5&&setting("cooldown").number.value==5,"Supplied Displace defaults/options changed");
    check(setting("third_person_rotations").boolean.value&&setting("void_scan_radius").number.value==6,"Supplied constructor settings missing");
    renderedSettings.clear();displace->settings(nullptr,nullptr);
    check(renderedSettings==std::vector<std::string>{"Main","Direction","Cooldown","Angle","Void scan radius","Void ignore one block wall","Hide rotation from players","Condition","Not taking knockback","Holding weapon","Knockback enchantment","Target facing away"},"Displace controls must retain the screenshot layout from the supplied source");
    char display[64];auto& cooldown=setting("cooldown").number;std::snprintf(display,sizeof(display),cooldown.format,(double)(cooldown.value*cooldown.scale));
    check(std::string(display)=="250 ms"&&cooldown.min==0&&cooldown.max==40,"Cooldown units/default/range mismatch");
    auto& angle=setting("angle").number;std::snprintf(display,sizeof(display),angle.format,(double)(angle.value*angle.scale));
    check(std::string(display)=="90\xC2\xB0"&&angle.min==45&&angle.max==180,"Angle units/default/range mismatch");
    check(setting("void_scan_radius").number.value==6&&!setting("void_ignore_one_block_wall").boolean.value&&!setting("hide_rotation").boolean.value,"Screenshot Void defaults mismatch");
    for(const char* key:{"require_no_kb","require_weapon","require_kb_enchant","require_facing_away"})check(!setting(key).boolean.value,"Screenshot condition must start disabled");
    setting("direction").choice.value=0;
    setting("direction").choice.value=5;
    setting("hide_rotation").boolean.value=1;
    struct Interaction{long long type;sourcefeatures::Context* ctx;unsigned char active,cancelled;};
    for(unsigned direction=0;direction<5;++direction){
        setting("direction").choice.value=direction;
        (*env)->CallStaticVoidMethod(env,fixture,prepareDisplace,sourcefeatures::bridge,(jint)9,(jint)5);
        priority=0;pending={};Interaction attack={14,&context,1,0};displace->events[14].fn(&attack);
        float yaw=(*env)->GetFloatField(env,player,ids.Entity_rotationYaw);
        check(attack.cancelled&&std::abs(std::abs(yaw)-90)<5.01,"Manual Displace failed direction/jitter activation");
        auto model=source_scaffold_visual_begin(env,player);check(model!=nullptr,"Third person Displace model pose missing");source_scaffold_visual_end(env,player,model);
        sourcefeatures::Packet packet={3,&java,nullptr,0,0,0};displace->events[3].fn(&packet);check(packet.result==1,"Hidden rotation must use dispatch mode 1");
        sourcefeatures::Rotation movement={13,1,1,0,0,0,0};displace->events[13].fn(&movement);check(!movement.moveFix,"Active Displace must disable movement remapping");
        displace->events[18].fn(&late);check(std::abs((*env)->GetFloatField(env,player,ids.Entity_rotationYaw))<.01,"Displace failed late tick yaw restore");
        auto replay=(*env)->GetStaticMethodID(env,fixture,"checkDisplaceReplay","()V");(*env)->CallStaticVoidMethod(env,fixture,replay);
        packet.result=0;displace->events[3].fn(&packet);check(packet.result==1,"Restore frame must retain packet suppression");
        displace->events[18].fn(&late);packet.result=0;displace->events[3].fn(&packet);check(packet.result==0,"Packet suppression persisted beyond restore frame");
    }
    for(int gate=0;gate<3;++gate){
        (*env)->CallStaticVoidMethod(env,fixture,prepareDisplace,sourcefeatures::bridge,(jint)(gate==0?8:9),(jint)(gate==1?4:5));
        timerFrozen=gate==2;priority=0;pending={};Interaction attack={14,&context,1,0};displace->events[14].fn(&attack);
        check(!attack.cancelled,"Displace ignored input cooldown, activation cooldown, or Timer Range guard");
    }
    timerFrozen=false;
    (*env)->CallStaticVoidMethod(env,fixture,prepareDisplace,sourcefeatures::bridge,(jint)9,(jint)5);
    setting("direction").choice.value=0;priority=0;pending={};Interaction attack={14,&context,1,0};displace->events[14].fn(&attack);check(attack.cancelled,"Cleanup fixture did not activate");
    data_2eb740[0x14]=1;displace->events[18].fn(&late);data_2eb740[0x14]=0;
    check(std::abs((*env)->GetFloatField(env,player,ids.Entity_rotationYaw))<.01,"Menu cleanup did not restore temporary yaw");
    sourcefeatures::Packet cleared={3,&java,nullptr,0,0,0};displace->events[3].fn(&cleared);check(cleared.result==0,"Menu cleanup retained pending packet state");
    toggle.on=0;displace->events[5].fn(&toggle);displace->enabled=0;
    (*env)->SetFloatField(env,player,ids.Entity_rotationYaw,0);(*env)->SetFloatField(env,player,ids.Entity_rotationPitch,0);pending={};priority=0;
    check(!sourcefeatures::error(env,"manual Displace checks"),"Manual Displace fixture failed");
    std::puts("Supplied manual Displace: directions, jitter, cooldown gates, Timer Range, rotation restore, attack replay, packet mode and GUI cleanup passed");
    auto scan=(*env)->GetStaticMethodID(env,fixture,"scanVoid","(Ljava/lang/Class;IF)F");
    float scanRear=(*env)->CallStaticFloatMethod(env,fixture,scan,sourcefeatures::bridge,1,0.0);
    check(std::isfinite(scanRear)&&std::abs(scanRear)>90,"Pinned Raven scanner must find the rear void");
    float scanLeft=(*env)->CallStaticFloatMethod(env,fixture,scan,sourcefeatures::bridge,2,0.0);
    float scanRight=(*env)->CallStaticFloatMethod(env,fixture,scan,sourcefeatures::bridge,3,0.0);
    check(std::isfinite(scanLeft)&&scanLeft<0&&std::isfinite(scanRight)&&scanRight>0,"Pinned scanner must find left/right void paths");
    auto enemyMethod=(*env)->GetStaticMethodID(env,fixture,"voidTarget","()Ljava/lang/Object;");
    auto enemy=(*env)->CallStaticObjectMethod(env,fixture,enemyMethod);float serviceYaw=0;
    // Restore the fixture layout used by the existing native service equality check.
    (*env)->CallStaticFloatMethod(env,fixture,scan,sourcefeatures::bridge,3,0.0);
    check(source_displace_void_yaw(env,enemy,0,90,6,false,&serviceYaw)&&std::abs(serviceYaw-scanRight)<.01,"Native Void service must use Raven's exact scanner result");
    displace->enabled=1;toggle.module=displace;toggle.on=1;displace->events[5].fn(&toggle);setting("direction").choice.value=5;
    (*env)->CallStaticVoidMethod(env,fixture,prepareDisplace,sourcefeatures::bridge,(jint)9,(jint)5);
    float expectedVoid=(*env)->CallStaticFloatMethod(env,fixture,scan,sourcefeatures::bridge,3,0.0);
    priority=0;pending={};Interaction voidAttack={14,&context,1,0};displace->events[14].fn(&voidAttack);
    check(voidAttack.cancelled&&std::abs((*env)->GetFloatField(env,player,ids.Entity_rotationYaw)-expectedVoid)<.01,"Supplied Void activation must apply exact scanner yaw without jitter");
    toggle.on=0;displace->events[5].fn(&toggle);displace->enabled=0;
    check(std::abs((*env)->GetFloatField(env,player,ids.Entity_rotationYaw))<.01,"Disable cleanup did not restore temporary Void yaw");
    displace->enabled=1;toggle.on=1;displace->events[5].fn(&toggle);
    (*env)->CallStaticVoidMethod(env,fixture,prepareDisplace,sourcefeatures::bridge,(jint)9,(jint)5);
    (*env)->CallStaticFloatMethod(env,fixture,scan,sourcefeatures::bridge,0,0.0);
    priority=0;pending={};voidAttack={14,&context,1,0};displace->events[14].fn(&voidAttack);check(!voidAttack.cancelled,"Void without a path must not activate");
    toggle.on=0;displace->events[5].fn(&toggle);displace->enabled=0;
    (*env)->SetFloatField(env,player,ids.Entity_rotationPitch,0);pending={};priority=0;
    (*env)->SetFloatField(env,player,ids.Entity_rotationYaw,0);(*env)->SetFloatField(env,player,ids.Entity_rotationPitch,0);pending={};priority=0;
    (*env)->DeleteLocalRef(env,enemy);
    (*env)->CallStaticFloatMethod(env,fixture,scan,sourcefeatures::bridge,0,0.0);
    auto bow=(sourcefeatures::Descriptor*)bow_aimbot_descriptor();toggle.module=bow;toggle.on=1;bow->events[5].fn(&toggle);
    sourcefeatures::Event tick={16,&context},update={17,&context};bow->events[16].fn(&tick);bow->events[17].fn(&update);check(!toggles,"Idle Bow Aimbot event unexpectedly failed");
    for(int idle=0;idle<12;++idle){
        Jvm temporary=java;sourcefeatures::Context idleContext=context;idleContext.java=&temporary;
        sourcefeatures::Event idleUpdate={17,&idleContext};priority=0;bow->events[17].fn(&idleUpdate);temporary={};
        sourcefeatures::Rotation idleRotation={13,0,0,0,0,0,0};bow->events[13].fn(&idleRotation);
        check(!idleRotation.on&&!sourcefeatures::error(env,"idle Bow enable"),"Enabled idle Bow must survive rotation with expired prior hook context");
    }
    auto prepare=(*env)->GetStaticMethodID(env,fixture,"prepareBow","()V");(*env)->CallStaticVoidMethod(env,fixture,prepare);
    for(int i=0;i<8;++i){priority=0;bow->events[16].fn(&tick);bow->events[17].fn(&update);}
    check(!toggles&&!(*env)->ExceptionCheck(env),"Bow tracking/solver event raised a Java failure");
    // Regression: a later rotation event must not dereference a hook's expired Jvm.
    Jvm expiredJava=java;sourcefeatures::Context transientContext=context;transientContext.java=&expiredJava;
    sourcefeatures::Event transientUpdate={17,&transientContext};priority=0;bow->events[17].fn(&transientUpdate);
    expiredJava={};
    check(!sourcefeatures::current.java,"Dispatch retained a hook-local JNI context after return");
    sourcefeatures::Rotation rotation={13,0,0,0,0,0,0};bow->events[13].fn(&rotation);
    if(!rotation.on){auto diagnose=(*env)->GetStaticMethodID(env,fixture,"diagnose","(Ljava/lang/Class;)Ljava/lang/String;");auto message=(jstring)(*env)->CallStaticObjectMethod(env,fixture,diagnose,sourcefeatures::bridge);if(message){auto text=(*env)->GetStringUTFChars(env,message,nullptr);std::puts(text);(*env)->ReleaseStringUTFChars(env,message,text);}}
    check(rotation.on&&(rotation.reserved&kApplyRotationOverride),"Bow solver must publish its original aiming result to the native rotation event");
    data_2eb740[0x14]=1;bow->events[17].fn(&update);rotation={13,0,0,0,0,0,0};bow->events[13].fn(&rotation);
    check(!rotation.on,"Native GUI overlay must pause Bow actions and clear pending aim");data_2eb740[0x14]=0;
    toggle.on=0;bow->events[5].fn(&toggle);
    check(sourcefeatures::counts[2]==6&&((sourcefeatures::Descriptor*)fog_descriptor())->category==2,"Fog must expose six original controls under Visual");
    auto fogCheck=(*env)->GetStaticMethodID(env,fixture,"checkFog","(Ljava/lang/Class;)V");(*env)->CallStaticVoidMethod(env,fixture,fogCheck,sourcefeatures::bridge);
    bool fogFailed=(*env)->ExceptionCheck(env)!=0;if(fogFailed)(*env)->ExceptionDescribe(env);check(!fogFailed,"Fog settings, falloff, fluids and disable restoration failed");
    auto fog=(sourcefeatures::Descriptor*)fog_descriptor();fog->enabled=1;toggle.module=fog;toggle.on=1;fog->events[5].fn(&toggle);
    for(unsigned n=0;n<sourcefeatures::counts[2];++n){auto& setting=sourcefeatures::settings[2][n];if(sourcefeatures::equal(setting.key,"fog_color"))setting.color.rgb[0]=.25f;if(sourcefeatures::equal(setting.key,"color_strength"))setting.number.value=50;}
    source_fog_render(env);auto verifyFog=(*env)->GetStaticMethodID(env,fixture,"checkNativeFog","()V");(*env)->CallStaticVoidMethod(env,fixture,verifyFog);
    fogFailed=(*env)->ExceptionCheck(env)!=0;if(fogFailed)(*env)->ExceptionDescribe(env);check(!fogFailed,"Native Fog settings bridge failed");toggle.on=0;fog->events[5].fn(&toggle);fog->enabled=0;
    auto shaders=(sourcefeatures::Descriptor*)shaders_descriptor();check(shaders->category==2&&sourcefeatures::counts[3]==1&&sourcefeatures::settings[3][0].choice.count==20,"Shaders must expose all 20 presets under Visual");
    auto prepareShaders=(*env)->GetStaticMethodID(env,fixture,"prepareShaders","(Ljava/lang/Class;)V");(*env)->CallStaticVoidMethod(env,fixture,prepareShaders,sourcefeatures::bridge);
    bool shaderFailed=(*env)->ExceptionCheck(env)!=0;if(shaderFailed)(*env)->ExceptionDescribe(env);check(!shaderFailed,"Shader reflection/renderer setup failed");
    auto verifyShader=(*env)->GetStaticMethodID(env,fixture,"checkShader","(II)V");shaders->enabled=1;toggle.module=shaders;toggle.on=1;shaders->events[5].fn(&toggle);
    for(int choice=0;choice<20;++choice){sourcefeatures::settings[3][0].choice.value=(unsigned)choice;for(int repeat=0;repeat<3;++repeat)shaders->events[18].fn(&late);(*env)->CallStaticVoidMethod(env,fixture,verifyShader,choice,choice+1);
        shaderFailed=(*env)->ExceptionCheck(env)!=0;if(shaderFailed)(*env)->ExceptionDescribe(env);check(!shaderFailed,"Shader preset switch/asset/stable tick check failed");}
    auto loseShader=(*env)->GetStaticMethodID(env,fixture,"loseShader","()V");(*env)->CallStaticVoidMethod(env,fixture,loseShader);shaders->events[18].fn(&late);(*env)->CallStaticVoidMethod(env,fixture,verifyShader,19,21);check(!(*env)->ExceptionCheck(env),"Shader must recover if vanilla releases its pipeline");
    toggle.on=0;shaders->events[5].fn(&toggle);shaders->enabled=0;auto stopped=(*env)->GetStaticMethodID(env,fixture,"checkShaderStopped","()V");(*env)->CallStaticVoidMethod(env,fixture,stopped);check(!(*env)->ExceptionCheck(env),"Disabling Shaders must stop the effect");
    auto support=(*env)->GetStaticMethodID(env,fixture,"shaderSupport","(Z)V");(*env)->CallStaticVoidMethod(env,fixture,support,(jboolean)0);int previousToggles=toggles;toggle.on=1;shaders->events[5].fn(&toggle);check(toggles==previousToggles+1,"Unsupported shaders must queue a native disable");toggle.on=0;shaders->events[5].fn(&toggle);(*env)->CallStaticVoidMethod(env,fixture,support,(jboolean)1);
    auto mayuFixture=(*env)->FindClass(env,"SuppliedScaffoldTest");
    auto mayuPrepare=(*env)->GetStaticMethodID(env,mayuFixture,"nativePrepare","()V");
    (*env)->CallStaticVoidMethod(env,mayuFixture,mayuPrepare);
    check(!sourcefeatures::error(env,"Mayu fixture setup"),"Mayu fixture setup failed");
    auto positionMethod=(*env)->GetStaticMethodID(env,mayuFixture,"nativePosition","()Ljava/lang/Object;");
    auto position=(*env)->CallStaticObjectMethod(env,mayuFixture,positionMethod);
    auto mayuWorld=get_world(&java,mc);
    auto slotMethod=(*env)->GetStaticMethodID(env,mayuFixture,"nativeSlot","()I");
    auto placementsMethod=(*env)->GetStaticMethodID(env,mayuFixture,"nativePlacements","()I");
    auto inventoryMethod=(*env)->GetStaticMethodID(env,mayuFixture,"nativeInventory","()Ljava/lang/Object;");
    auto inventory=(*env)->CallStaticObjectMethod(env,mayuFixture,inventoryMethod);
    mayu->enabled=1;toggle.module=mayu;toggle.on=1;mayu->events[5].fn(&toggle);
    auto seedMethod=(*env)->GetStaticMethodID(env,mayuFixture,"nativeSeed","(Ljava/lang/Class;)V");
    (*env)->CallStaticVoidMethod(env,mayuFixture,seedMethod,sourcefeatures::bridge);
    for(int tick=0;tick<8;++tick){
        priority=0;
        check(hook_mayu_loaded(env,nullptr,mayuWorld,position),"Loaded-world wrapper changed vanilla result");
        if(tick<2)check((*env)->CallStaticIntMethod(env,mayuFixture,placementsMethod)==0,"Placement must wait for supplied source warmup");
        hook_walking_player(env,nullptr,player);
    }
    check(!sourcefeatures::error(env,"Mayu player update"),"Mayu native player update failed");
    auto movementCheck=(*env)->GetStaticMethodID(env,mayuFixture,"checkNativeMovement","(Ljava/lang/Class;)V");
    (*env)->CallStaticVoidMethod(env,mayuFixture,movementCheck,sourcefeatures::bridge);
    check(!sourcefeatures::error(env,"Mayu movement direction"),"Mayu directional movement regression failed");
    check((*env)->CallStaticIntMethod(env,mayuFixture,slotMethod)==2,"Native adapter did not select the supplied largest block stack");
    check((*env)->CallStaticIntMethod(env,mayuFixture,placementsMethod)>=1,"Native Mayu update did not place a block through the real controller boundary");
    auto viewMethod=(*env)->GetStaticMethodID(env,mayuFixture,"nativeSetView","(FF)V");
    auto forceLookMethod=(*env)->GetStaticMethodID(env,mayuFixture,"nativeForceLook","()V");
    auto packetMatches=(*env)->GetStaticMethodID(env,mayuFixture,"nativePacketMatches","(FF)V");
    auto visualMatches=(*env)->GetStaticMethodID(env,mayuFixture,"nativeVisualMatches","(FF)V");
    const float cameraViews[][2]={{-135,-35},{0,30},{75,-10},{179,20}};
    for(const auto& view:cameraViews){
        priority=0; // The real engine resets rotation priority at the start of each game tick.
        (*env)->CallStaticVoidMethod(env,mayuFixture,viewMethod,(double)view[0],(double)view[1]);
        hook_mayu_loaded(env,nullptr,mayuWorld,position);
        check((*env)->GetFloatField(env,player,ids.Entity_rotationYaw)==view[0]&&(*env)->GetFloatField(env,player,ids.Entity_rotationPitch)==view[1],"Scaffold update must preserve freely chosen camera direction");
        auto target=(jfloatArray)(*env)->CallStaticObjectMethod(env,sourcefeatures::bridge,sourcefeatures::motionRotationMethod);
        check(target&&(*env)->GetArrayLength(env,target)==2,"Mayu packet rotation must be available");
        float angles[2];(*env)->GetFloatArrayRegion(env,target,0,2,angles);(*env)->DeleteLocalRef(env,target);
        (*env)->CallStaticVoidMethod(env,mayuFixture,forceLookMethod);
        hook_walking_player(env,nullptr,player);
        check(!sourcefeatures::error(env,"Mayu original walking method"),"Vanilla walking method raised an exception");
        (*env)->CallStaticVoidMethod(env,mayuFixture,packetMatches,(double)angles[0],(double)angles[1]);
        check(!sourcefeatures::error(env,"Mayu packet/camera separation"),"Mayu walking packet did not carry placement angles");
        check((*env)->GetFloatField(env,player,ids.Entity_rotationYaw)==view[0]&&(*env)->GetFloatField(env,player,ids.Entity_rotationPitch)==view[1],"Walking packet rotation must restore camera yaw and pitch");
        auto visual=source_scaffold_visual_begin(env,player);
        check(visual!=nullptr,"Third-person placement pose missing");
        (*env)->CallStaticVoidMethod(env,mayuFixture,visualMatches,(double)angles[0],(double)angles[1]);
        source_scaffold_visual_end(env,player,visual);
        check(!sourcefeatures::error(env,"Mayu third-person pose"),"Third-person pose does not follow placement");
        check((*env)->GetFloatField(env,player,ids.Entity_rotationYaw)==view[0]&&(*env)->GetFloatField(env,player,ids.Entity_rotationPitch)==view[1],"Third-person rendering must restore the freely chosen camera");
    }
    hook_mayu_scroll(env,nullptr,inventory,1);hook_mayu_select(env,nullptr,inventory,4);
    check((*env)->CallStaticIntMethod(env,mayuFixture,slotMethod)==2,"User hotbar requests must leave the placement slot active");
    priority=0;hook_mayu_loaded(env,nullptr,mayuWorld,position);
    float originalYaw=(*env)->GetFloatField(env,player,ids.Entity_rotationYaw);
    check(source_mayu_move(env,player,0,1,.1f),"Mayu movement yaw wrapper did not run");
    check((*env)->GetFloatField(env,player,ids.Entity_rotationYaw)==originalYaw,"Movement yaw must restore the camera");
    data_2eb740[0x14]=1;hook_mayu_loaded(env,nullptr,mayuWorld,position);
    check((*env)->CallStaticIntMethod(env,mayuFixture,slotMethod)==4,"Opening the native menu must release Mayu's slot to the user's requested slot");
    data_2eb740[0x14]=0;hook_mayu_loaded(env,nullptr,mayuWorld,position);
    toggle.on=0;mayu->events[5].fn(&toggle);mayu->enabled=0;
    check((*env)->CallStaticIntMethod(env,mayuFixture,slotMethod)==4,"Disabling Mayu must restore the user slot");
    for(int returnTick=0;returnTick<3;++returnTick){
        priority=0;hook_mayu_loaded(env,nullptr,mayuWorld,position);
        auto returning=(jfloatArray)(*env)->CallStaticObjectMethod(env,sourcefeatures::bridge,sourcefeatures::motionRotationMethod);
        check((returning!=nullptr)==(returnTick<2),"Disabled Scaffold must finish the supplied two-look return and release rotation");
        if(returning)(*env)->DeleteLocalRef(env,returning);
        hook_walking_player(env,nullptr,player);
    }
    check(!sourcefeatures::error(env,"Mayu integration completion"),"Mayu native integration left a Java exception");
    std::puts("Supplied Scaffold: native registration, settings, real placement, movement yaw, input routing, menu/disable cleanup passed");
    auto expirePearl=(*env)->GetStaticMethodID(env,pearlFixture,"expireFromNative","(Ljava/lang/Class;)V");
    (*env)->CallStaticVoidMethod(env,pearlFixture,expirePearl,sourcefeatures::bridge);
    check(!sourcefeatures::error(env,"Native rescue expiry setup"),"Native rescue expiry setup failed");
    int priorRescueToggles=toggles;sourcefeatures::Event rescueFrame={1,&context};sourcefeatures::descriptors[6]->events[1].fn(&rescueFrame);
    check(toggles==priorRescueToggles+1,"Rescue self-disable must queue a real native toggle");
    displace_cleanup(env);
    check(!sourcefeatures::bridge&&!sourcefeatures::ready,"Unload must release the Java bridge");
    check(source_features_initialize(&java),"Re-injection must reuse the bootstrap loader class with fresh module state");displace_cleanup(env);
    std::puts("Original supplied Java modules: supplied manual Displace replacement, complete settings, bow prediction, native event/rotation handoff, and cleanup passed");
    return 0;
}catch(const std::exception& e){std::fprintf(stderr,"%s\n",e.what());return 1;}}
