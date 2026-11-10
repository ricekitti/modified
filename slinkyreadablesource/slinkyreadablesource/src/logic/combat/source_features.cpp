extern "C" void hud_editor_scaffold(float*);
// Executes the supplied Java module classes; only framework/event adaptation lives here.
#include "slinky/engine.h"
#include "slinky/jids.h"
#include "slinky/rotation_override.h"
#include <stddef.h>
extern "C" {
extern const unsigned char feature_loader[],feature_named[],feature_srg[],feature_official[];
extern const unsigned feature_loader_size,feature_named_size,feature_srg_size,feature_official_size;
extern char data_2f5d60[],data_2eb740[];
extern char data_2f9e30[];
extern unsigned char data_2f7259;
extern void* data_2f6f08;
void* libc_calloc(unsigned long long,unsigned long long);
void libc_free(void*);
void slinky_logf(const char*,...);
void queue_module_toggle(void*,void*,unsigned char);
unsigned entity_flags_get(void*,void*);
bool increment_wrap(unsigned);
bool pending_action_flag();
void apply_player_rotation(void*,unsigned char,unsigned char,unsigned char,RotationOverrideAngle);
void apply_silent_rotation(RotationOverrideAngle);
RotationOverrideAngle get_render_state();
void set_rotation_active(void*,void*,bool);
void W_label(void*,void*,const char*);
void W_bool(void*,void*,void*);void W_slider(void*,void*,void*);void W_enumw(void*,void*,void*);
void R_bool(void*,const char*,void*);void R_num(void*,const char*,void*);void R_enum(void*,const char*,void*);
void log_fmt(void*,int,const char*,...);
void W_color(void*,void*,void*);void R_key(void*,const char*,void*);
jobject get_minecraft(Jvm*);jobject get_world(Jvm*,void*);jobject get_local_player(Jvm*,void*);
}
namespace sourcefeatures {
struct Slot{void(*fn)(void*);int priority,reserved;};
struct Descriptor{const char* name;const char* description;unsigned category;unsigned char enabled;char pad15[3];unsigned flags;int key;unsigned char hold;char pad21[7];char animation[16];float height;char pad3c[4];unsigned char expanded;char pad41[7];Slot events[27];void(*settings)(void*,void*);void(*visit)(void*);void(*status)(void*);};
struct Context{Jvm* java;void* manager;void* profiles;jobject mc,world,player;void* keys;void* targets;unsigned char inGame;};
struct Event{long long type;Context* ctx;};
struct Toggle{long long type;Context* ctx;void* module;unsigned char on;};
struct Rotation{long long type;unsigned char on,moveFix,useTarget,reserved;float yaw,pitch;};
struct Packet{long long type;Jvm* java;jobject packet;long long age;int id,result;};
// moveFlying (events 21/22, java_natives_world.cpp VelEvent): strafe=x, forward=y, friction=z, jump=flag
struct Vel{long long type;void* ctx;float x,y,z;unsigned char flag;};
struct WorldRender{long long type;double camera[3];Context* ctx;float partial;unsigned char flag;};
struct Bool{void* unused[2];const char* label;unsigned char value,saved;char pad[6];};
struct Number{void* unused[2];const char* label;const char* format;float step,scale,min,max,value,saved;};
struct Choice{void* unused[3];const char* label;unsigned count,pad;const char** names;unsigned value,saved;unsigned char flags;char rest[7];};
struct Color{const char* label;float rgb[3],saved[3];};
struct Setting{int kind;char* key;char* persistence;Bool boolean;Number number;Choice choice;Color color;};
static_assert(sizeof(Descriptor)==0x210&&sizeof(Choice)==64&&offsetof(Packet,result)==0x24,"Feature ABI");
static Descriptor bow,fog,shaders,scaffold,fireballBoost,autoPearl,fireballPearlClutch,bridgeEgg;static Descriptor* descriptors[9]={reinterpret_cast<Descriptor*>(data_2f5d60),&bow,&fog,&shaders,&scaffold,&fireballBoost,&autoPearl,&fireballPearlClutch,&bridgeEgg};
static Setting settings[9][64];static unsigned counts[9];static char statuses[9][128];
static jclass bridge;static jmethodID invoke,configure,getStatus,fogSetup,inputMethod,visualMethod,hudMethod,moveEntityMethod,suspendMethod,jumpMethod,motionRotationMethod;static bool installed,ready;

// JNI environments belong to a thread; hook-local Jvm/context pointers must never
// be cached for the later rotation event. Ask the VM for this thread's environment.
struct VmInvoke {void* reserved[6];jint (*getEnv)(JavaVM*,void**,jint);};
static JavaVM* featureVm;
static JNIEnv* threadEnvironment(){
    if(!featureVm)return nullptr;
    auto table=*reinterpret_cast<const VmInvoke* const*>(featureVm);void* env=nullptr;
    return table->getEnv(featureVm,&env,0x00010008)==0?(JNIEnv*)env:nullptr;
}
static Context current;static JIds* ids;
static unsigned currentFeature;
// Native rotation callbacks see only the current call's live local references.
// Nested events restore the outer scope before its Java/native callback resumes.
struct ContextScope {
    Context previous;unsigned feature;bool enabled;
    ContextScope(Context* c,unsigned index):previous(current),feature(currentFeature),enabled(c!=nullptr){
        if(enabled){current=*c;currentFeature=index;}
    }
    ~ContextScope(){if(enabled){current=previous;currentFeature=feature;}}
};
// Identity markers survive the client-to-Netty handoff. Their own lock is independent
// of the module dispatch lock, so a synchronous silent write cannot recursively dispatch.
struct SilentPacket{jobject packet;SilentPacket* next;};
static SilentPacket* silentPackets;static unsigned long silentLock;
struct SilentGuard{
#ifdef SOURCE_FEATURES_TEST
    SilentGuard(){while(InterlockedExchange((volatile LONG*)&silentLock,1))YieldProcessor();}
    ~SilentGuard(){InterlockedExchange((volatile LONG*)&silentLock,0);}
#else
    SilentGuard(){while(__atomic_exchange_n(&silentLock,1,__ATOMIC_ACQUIRE))__builtin_ia32_pause();}
    ~SilentGuard(){__atomic_store_n(&silentLock,0,__ATOMIC_RELEASE);}
#endif
};
static bool consume_silent(JNIEnv* env,jobject packet){
    SilentGuard guard;
    for(auto link=&silentPackets;*link;link=&(*link)->next){
        auto entry=*link;
        if((*env)->IsSameObject(env,packet,entry->packet)){
            *link=entry->next;(*env)->DeleteGlobalRef(env,entry->packet);libc_free(entry);return true;
        }
    }
    return false;
}
static void clear_silent(JNIEnv* env){
    SilentGuard guard;
    while(silentPackets){auto entry=silentPackets;silentPackets=entry->next;(*env)->DeleteGlobalRef(env,entry->packet);libc_free(entry);}
}
static jboolean packetSilent(JNIEnv* env,jclass,jobject packet,jboolean mark){
    if(!packet)return 0;
    if(!mark)return consume_silent(env,packet);
    if(!ready)return 0;
    auto entry=(SilentPacket*)libc_calloc(1,sizeof(SilentPacket));
    if(!entry)return 0;
    entry->packet=(*env)->NewGlobalRef(env,packet);
    if(!entry->packet){libc_free(entry);return 0;}
    SilentGuard guard;if(!ready){(*env)->DeleteGlobalRef(env,entry->packet);libc_free(entry);return 0;}entry->next=silentPackets;silentPackets=entry;return 1;
}
static bool equal(const char* a,const char* b){while(*a&&*a==*b){++a;++b;}return *a==*b;}
static void copy(char* target,const char* text,unsigned capacity){unsigned n=0;while(text[n]&&n+1<capacity){target[n]=text[n];++n;}target[n]=0;}
static char* string(JNIEnv* e,jstring value){if(!value)return nullptr;auto text=(*e)->GetStringUTFChars(e,value,nullptr);if(!text)return nullptr;unsigned size=0;while(text[size])++size;auto result=(char*)libc_calloc(size+1,1);if(result)copy(result,text,size+1);(*e)->ReleaseStringUTFChars(e,value,text);return result;}
static bool error(JNIEnv* e,const char* phase){if(!(*e)->ExceptionCheck(e))return false;auto exception=(*e)->ExceptionOccurred(e);
#ifdef SOURCE_FEATURES_TEST
    (*e)->ExceptionDescribe(e);
#endif
    (*e)->ExceptionClear(e);
    auto type=(*e)->GetObjectClass(e,exception);auto method=type?(*e)->GetMethodID(e,type,"toString","()Ljava/lang/String;"):nullptr;
    auto message=method?(jstring)(*e)->CallObjectMethod(e,exception,method):nullptr;
    auto text=message?(*e)->GetStringUTFChars(e,message,nullptr):nullptr;
    slinky_logf("Source features: Java failure during %s: %.250s",phase,text?text:"unknown");
    if(text)(*e)->ReleaseStringUTFChars(e,message,text);if((*e)->ExceptionCheck(e))(*e)->ExceptionClear(e);return true;}
static bool ravenVoidMode(){for(unsigned n=0;n<counts[0];++n){auto& s=settings[0][n];
    if(equal(s.key,"displace_mode")&&s.choice.value==1)return true;
    if(equal(s.key,"direction")&&s.choice.value==5)return true;
}return false;}
static jdouble rotation(JNIEnv* env,jclass,jint operation,jdouble a,jdouble b){
    if(!current.player||!current.java)return 0;Jvm java={ids,env};
    switch(operation){case 0:return increment_wrap((unsigned)a);case 1:apply_player_rotation(&current,1,0,0,{(float)a,(float)b});break;
    case 2:
        apply_silent_rotation({(float)a,(float)b});break;case 3:set_rotation_active(&java,current.player,a!=0);break;
    case 4:return get_render_state().yaw;case 5:return get_render_state().pitch;}
    return 0;
}
static jint flags(JNIEnv* env,jclass,jobject entity){Jvm java={ids,env};return entity?(jint)entity_flags_get(&java,entity):0;}
static jboolean guiOpen(JNIEnv*,jclass){return (unsigned char)data_2eb740[0x14]!=0;}
static bool menu_pause(JNIEnv* env){
    if(!data_2eb740[0x14])return false;

    if(ready&&bridge&&suspendMethod){(*env)->CallStaticVoidMethod(env,bridge,suspendMethod);error(env,"Scaffold menu pause");}
    return true;
}
static jboolean timerBlocked(JNIEnv*,jclass){return pending_action_flag();}
extern "C" char data_2f5580[],data_2f2c90[];
extern "C" unsigned char data_304a1c;
extern "C" float data_2f2ed0,data_2f2f08,data_2f2f40;
extern "C" void anti_fireball_record_rescue_launch(JNIEnv*);
static jdouble rescueHost(JNIEnv* env,jclass,jint operation,jdouble value){
    if(operation==0)return (unsigned char)data_2f5580[0x14]&&data_304a1c;
    if(operation==3){anti_fireball_record_rescue_launch(env);return 1;}
    if(operation==4){
        if(!(unsigned char)data_2f2c90[0x14])return .2;
        unsigned id=(unsigned)value;
        return id==261?data_2f2ed0:(id==267||id==268||id==272||id==276||id==283)?data_2f2f08:data_2f2f40;
    }
    return 0;
}
static double value(const Setting& s){return s.kind==0?s.boolean.value:s.kind==1?s.number.value:s.choice.value;}
static void sync(JNIEnv* e,unsigned index){jdouble values[192];unsigned count=0;for(unsigned n=0;n<counts[index];++n){auto& s=settings[index][n];if(s.kind==3){for(float lane:s.color.rgb)values[count++]=lane;}else values[count++]=value(s);}
    auto array=(*e)->NewDoubleArray(e,count);if(!array)return;(*e)->SetDoubleArrayRegion(e,array,0,count,values);(*e)->CallStaticVoidMethod(e,bridge,configure,(jint)index,array);(*e)->DeleteLocalRef(e,array);}
static bool setting_on(unsigned index,const char* key){
    for(unsigned n=0;n<counts[index];++n)if(equal(settings[index][n].key,key))return value(settings[index][n])!=0;
    return false;
}
static bool row_is(const char* key,const char* base){
    while(*base&&*key==*base){++base;++key;}
    return !*base&&(!*key||equal(key,"_lo")||equal(key,"_hi"));
}
static bool visible(unsigned index,const char* key){
    if(index==6||index==7){
        bool timed=setting_on(index,"above_void")&&!setting_on(index,"predict_safe_landing");
        if(equal(key,"in_void_for"))return timed;
        if(equal(key,"time"))return timed&&setting_on(index,"in_void_for");
    }
    if(index==8){
        if(equal(key,"time"))return setting_on(index,"in_void_for");
        if(equal(key,"pre_timer_speed")||equal(key,"after_timer_speed"))return !setting_on(index,"use_stasis_instead");
    }
    if(index==4&&equal(key,"keep_y_on_right_click"))return setting_on(4,"keep_y");
    if(index==0&&(equal(key,"void_scan_radius")||equal(key,"void_ignore_one_block_wall")))return ravenVoidMode();
    if(index==0&&equal(key,"third_person_rotations"))return false;
    if(index==0&&equal(key,"use_last_strafe")){for(unsigned n=0;n<counts[0];++n)if(equal(settings[0][n].key,"direction"))return settings[0][n].choice.value==3||settings[0][n].choice.value==4;return false;}
    if(index==1&&(equal(key,"minimum_rod_range")||equal(key,"maximum_rod_range"))){bool rod=false,automatic=false;for(unsigned n=0;n<counts[1];++n){auto& s=settings[1][n];if(equal(s.key,"rod"))rod=s.boolean.value!=0;if(equal(s.key,"rod_mode"))automatic=s.choice.value==1;}return rod&&automatic;}
    return true;
}
static void draw_setting(void* a,void* b,Setting& s){if(s.kind==0)W_bool(b,a,&s.boolean);else if(s.kind==1)W_slider(b,a,&s.number);else if(s.kind==2)W_enumw(b,a,&s.choice);else W_color(b,a,&s.color);}
template<unsigned I>static void show(void* a,void* b){
    if(I==0){
        W_label(a,b,"Main");
        const char* keys[]={"direction","use_last_strafe","cooldown","angle","void_scan_radius","void_ignore_one_block_wall","hide_rotation","require_no_kb","require_weapon","require_kb_enchant","require_facing_away"};
        for(auto key:keys){
            if(equal(key,"require_no_kb"))W_label(a,b,"Condition");
            for(unsigned n=0;n<counts[0];++n){auto& s=settings[0][n];if(equal(s.key,key)&&visible(0,key))draw_setting(a,b,s);}
        }
        return;
    }
    for(unsigned n=0;n<counts[I];++n){auto& s=settings[I][n];if(visible(I,s.key))draw_setting(a,b,s);}
}
template<unsigned I>static void visit(void* out){for(unsigned n=0;n<counts[I];++n){auto& s=settings[I][n];if(s.kind==0)R_bool(out,s.persistence,&s.boolean);else if(s.kind==1)R_num(out,s.persistence,&s.number);else if(s.kind==2)R_enum(out,s.persistence,&s.choice);else R_key(out,s.persistence,&s.color);}}
template<unsigned I>static void status(void* out){if(statuses[I][0])log_fmt(out,0x20,"%s",statuses[I]);}
template<unsigned I>static void dispatch(void* raw){
    if(!raw)return;unsigned type=(unsigned)*(long long*)raw;Jvm rotationJava={ids,nullptr};Context rotationContext={};Jvm* java=nullptr;Context* context=nullptr;jobject packet=nullptr;
    if(type==5){auto e=(Toggle*)raw;if(e->module!=descriptors[I])return;context=e->ctx;}
    else if(type==2||type==3){auto e=(Packet*)raw;java=e->java;packet=e->packet;}
    else if(type==13){rotationJava.env=threadEnvironment();if(!rotationJava.env)return;java=&rotationJava;}
    else if(type==6){context=((WorldRender*)raw)->ctx;}
    else{context=((Event*)raw)->ctx;}
    if(context)java=context->java;
    if(!java)return;auto e=java->env;
    if(!ready){if(type==5&&((Toggle*)raw)->on&&context)queue_module_toggle(context->manager,descriptors[I],0);return;}
    if(I==4&&type!=2&&type!=3&&type!=5&&type!=26&&menu_pause(e))return;
    if((*e)->PushLocalFrame(e,32)!=0)return;
    if(type==13){
        auto mc=get_minecraft(java);auto world=mc?get_world(java,mc):nullptr;auto player=mc?get_local_player(java,mc):nullptr;
        rotationContext=Context{java,data_2f6f08,nullptr,mc,world,player,nullptr,nullptr,(unsigned char)(world&&player)};
        context=&rotationContext;
    }
    ContextScope scope(context,I);
    if(type!=2&&type!=3)sync(e,I);
    float v[13]={};bool active=true;
    if(type==5)active=((Toggle*)raw)->on!=0;
    else if(type==2||type==3)v[1]=(float)((Packet*)raw)->result;
    else if(type==13){auto r=(Rotation*)raw;v[2]=r->yaw;v[3]=r->pitch;v[4]=(float)r->moveFix;active=r->on!=0;}
    else if(type==14||type==15){auto bytes=(unsigned char*)raw;active=bytes[0x10]!=0;v[0]=(float)bytes[0x11];if(context&&context->targets)v[5]=*((unsigned char*)context->targets+0x18)?1.f:0.f;}
    else if(type==21||type==22){auto ve=(Vel*)raw;v[6]=ve->x;v[7]=ve->y;v[8]=ve->z;v[5]=ve->flag?1.f:0.f;}
    else if(type==6)v[8]=((WorldRender*)raw)->partial;
    auto values=(*e)->NewFloatArray(e,13);
    if(values){(*e)->SetFloatArrayRegion(e,values,0,13,v);(*e)->CallStaticObjectMethod(e,bridge,invoke,(jint)I,(jint)type,(jboolean)active,packet,values);
        if(!error(e,"event")){(*e)->GetFloatArrayRegion(e,values,0,13,v);
            if(v[12]<0&&context)queue_module_toggle(context->manager,descriptors[I],0);
            if(type==2||type==3){auto p=(Packet*)raw;if(p->result!=2&&p->result!=3&&v[1]!=0)p->result=(int)v[1];}
            else if(type==13){auto r=(Rotation*)raw;r->moveFix=(unsigned char)(v[4]!=0);if(v[12]>0){r->on=1;r->yaw=v[2];r->pitch=v[3];r->reserved|=kApplyRotationOverride;}}
            else if(type==14||type==15){if(v[0]!=0)((unsigned char*)raw)[0x11]=1;}
            else if(type==21||type==22){auto ve=(Vel*)raw;ve->x=v[6];ve->y=v[7];ve->z=v[8];}
            if(type==17||type==18){auto text=(jstring)(*e)->CallStaticObjectMethod(e,bridge,getStatus,(jint)I);if(text){auto data=(*e)->GetStringUTFChars(e,text,nullptr);if(data){copy(statuses[I],data,128);(*e)->ReleaseStringUTFChars(e,text,data);}}else statuses[I][0]=0;}
        }else if(context)queue_module_toggle(context->manager,descriptors[I],0);
    }
    error(e,"completion");(*e)->PopLocalFrame(e,nullptr);
}
static bool metadata(JNIEnv* e,unsigned index){
    auto labelsMethod=(*e)->GetStaticMethodID(e,bridge,"labels","(I)[[Ljava/lang/String;");
    auto specMethod=(*e)->GetStaticMethodID(e,bridge,"specification","(II)[D");if(!labelsMethod||!specMethod)return false;
    auto rows=(jobjectArray)(*e)->CallStaticObjectMethod(e,bridge,labelsMethod,(jint)index);if(!rows)return false;
    unsigned count=(unsigned)(*e)->GetArrayLength(e,rows);if(count>64)return false;counts[index]=count;
    for(unsigned n=0;n<count;++n){auto row=(jobjectArray)(*e)->GetObjectArrayElement(e,rows,n);auto numbers=(jdoubleArray)(*e)->CallStaticObjectMethod(e,bridge,specMethod,(jint)index,(jint)n);if(!row||!numbers)return false;
        double spec[5];(*e)->GetDoubleArrayRegion(e,numbers,0,5,spec);auto& s=settings[index][n];s.kind=(int)spec[0];
        s.key=string(e,(jstring)(*e)->GetObjectArrayElement(e,row,0));auto label=string(e,(jstring)(*e)->GetObjectArrayElement(e,row,1));if(!s.key||!label)return false;
        s.persistence=(char*)libc_calloc(256,1);if(!s.persistence)return false;copy(s.persistence,index==8?"source_bridge_egg_":index==7?"source_fireball_pearl_clutch_":index==6?"source_auto_pearl_":index==5?"source_fireball_boost_":index==4?"supplied_scaffold_":index==3?"source_shaders_":index==2?"source_fog_":index==1?"source_bow_":"source_displace_",256);unsigned end=0;while(s.persistence[end])++end;copy(s.persistence+end,s.key,256-end);
        if(s.kind==0){s.boolean.label=label;s.boolean.value=s.boolean.saved=(unsigned char)(spec[4]!=0);}
        else if(s.kind==1){s.number.label=label;s.number.format="%.2f";s.number.step=(float)spec[3];s.number.scale=1;s.number.min=(float)spec[1];s.number.max=(float)spec[2];s.number.value=s.number.saved=(float)spec[4];}
        else if(s.kind==2){s.choice.label=label;s.choice.value=s.choice.saved=(unsigned)spec[4];s.choice.count=(unsigned)(*e)->GetArrayLength(e,row)-2;s.choice.names=(const char**)libc_calloc(s.choice.count,8);if(!s.choice.names)return false;
            for(unsigned option=0;option<s.choice.count;++option)s.choice.names[option]=string(e,(jstring)(*e)->GetObjectArrayElement(e,row,option+2));}
        else if(s.kind==3){s.color.label=label;for(unsigned lane=0;lane<3;++lane)s.color.rgb[lane]=s.color.saved[lane]=(float)spec[lane+1];}
        else return false;
        if(index==6&&equal(s.key,"pre_pearl_timer")){
            unsigned length=0;while(s.persistence[length])++length;copy(s.persistence+length,"_valen",256-length);
        }
        if(s.kind==1){
            if(index==5&&(equal(s.key,"duration")||equal(s.key,"cooldown")))s.number.format="%.0f ms";
            if(index==6||index==7){
                if(equal(s.key,"range")||equal(s.key,"avoid_enemies")||equal(s.key,"edge_margin"))s.number.format="%.0f blocks";
                else if(equal(s.key,"pre_pearl_timer")||equal(s.key,"after_pearl_timer"))s.number.format="%.2gx";
                else if(equal(s.key,"time"))s.number.format="%.1fs";
            }
            if(index==8){
                if(equal(s.key,"lowest_y"))s.number.format="%.0f";
                else if(equal(s.key,"pre_timer_speed")||equal(s.key,"after_timer_speed"))s.number.format="%.2gx";
                else if(equal(s.key,"time"))s.number.format="%.1fs";
            }
        }
        if(index==0){
            if(equal(s.key,"cooldown")){s.number.format="%.0f ms";s.number.scale=50;}
            else if(equal(s.key,"angle"))s.number.format="%.0f\xC2\xB0";
            else if(equal(s.key,"void_scan_radius"))s.number.format="%.1f blocks";
        }
        (*e)->DeleteLocalRef(e,row);(*e)->DeleteLocalRef(e,numbers);
    }
    return !error(e,"settings metadata");
}
}
extern "C" unsigned char source_consume_silent_packet(JNIEnv* env,jobject packet){return sourcefeatures::consume_silent(env,packet);}
extern "C" void source_scaffold_render_hud(JNIEnv* env,double now){
    using namespace sourcefeatures;
    if(!ready||!bridge||!hudMethod||(*env)->ExceptionCheck(env))return;
    auto layoutMethod=(*env)->GetStaticMethodID(env,bridge,"scaffoldHudLayout","()[F");
    auto positionMethod=(*env)->GetStaticMethodID(env,bridge,"scaffoldHudPosition","(FF)V");
    if(layoutMethod&&positionMethod){
        auto layout=(jfloatArray)(*env)->CallStaticObjectMethod(env,bridge,layoutMethod);
        if(layout&&(*env)->GetArrayLength(env,layout)==4){
            float values[4];(*env)->GetFloatArrayRegion(env,layout,0,4,values);hud_editor_scaffold(values);
            (*env)->CallStaticVoidMethod(env,bridge,positionMethod,(jdouble)values[0],(jdouble)values[1]);
        }
        if(layout)(*env)->DeleteLocalRef(env,layout);
    }
    if(error(env,"Scaffold HUD layout")||menu_pause(env))return;
    if((*env)->PushLocalFrame(env,64)!=0)return;
    sync(env,4);
    if(!error(env,"Scaffold HUD settings")){
        auto color=(float*)(data_2f9e30+0x360);unsigned rgb=0;
        for(unsigned n=0;n<3;++n){float channel=color[n];if(!(channel>=0))channel=0;if(channel>1)channel=1;rgb=(rgb<<8)|(unsigned)(channel*255);}
        (*env)->CallStaticVoidMethod(env,bridge,hudMethod,(jint)rgb,(jdouble)now);
        error(env,"Scaffold HUD");
    }
    (*env)->PopLocalFrame(env,nullptr);
}
extern "C" jobjectArray source_scaffold_visual_begin(JNIEnv* env,jobject entity){
    using namespace sourcefeatures;
    if(!ready||!bridge||!visualMethod||(*env)->ExceptionCheck(env))return nullptr;
    if(menu_pause(env))return nullptr;
    auto original=(jclass)(*env)->NewLocalRef(env,bridge);
    auto apply=original?(*env)->GetStaticMethodID(env,original,"scaffoldVisual","(Ljava/lang/Object;[F)[F"):nullptr;
    if(!apply){if(original)(*env)->DeleteLocalRef(env,original);error(env,"Scaffold visual bridge");return nullptr;}
    auto object=(*env)->FindClass(env,"java/lang/Object");
    auto scope=object?(*env)->NewObjectArray(env,2,object,nullptr):nullptr;
    if(object)(*env)->DeleteLocalRef(env,object);
    if(!scope){(*env)->DeleteLocalRef(env,original);error(env,"Scaffold visual snapshot");return nullptr;}
    // Root the original bridge class before applying the pose. Cleanup must still
    // use that class if unload or reinjection replaces the global bridge reference.
    (*env)->SetObjectArrayElement(env,scope,1,original);
    auto saved=(jfloatArray)(*env)->CallStaticObjectMethod(env,original,apply,entity,nullptr);
    (*env)->DeleteLocalRef(env,original);
    if(error(env,"Scaffold visual pose")||!saved){if(saved)(*env)->DeleteLocalRef(env,saved);(*env)->DeleteLocalRef(env,scope);return nullptr;}
    (*env)->SetObjectArrayElement(env,scope,0,saved);(*env)->DeleteLocalRef(env,saved);
    return scope;
}
extern "C" void source_scaffold_visual_end(JNIEnv* env,jobject entity,jobjectArray scope){
    if(!scope)return;
    // Restore even if the original renderer threw, then rethrow its original exception.
    auto pending=(*env)->ExceptionOccurred(env);if(pending)(*env)->ExceptionClear(env);
    auto saved=(*env)->GetObjectArrayElement(env,scope,0);
    auto original=(jclass)(*env)->GetObjectArrayElement(env,scope,1);
    auto restore=original?(*env)->GetStaticMethodID(env,original,"scaffoldVisual","(Ljava/lang/Object;[F)[F"):nullptr;
    if(restore)(*env)->CallStaticObjectMethod(env,original,restore,entity,saved);
    sourcefeatures::error(env,"Scaffold visual cleanup");
    if(saved)(*env)->DeleteLocalRef(env,saved);if(original)(*env)->DeleteLocalRef(env,original);
    (*env)->DeleteLocalRef(env,scope);
    if(pending){(*env)->Throw(env,pending);(*env)->DeleteLocalRef(env,pending);}
}
extern "C" unsigned char data_2f7259;   // unload flag
struct ScaffoldPacketRotationScope{
    JNIEnv* env;jobject player;jfieldID yaw,pitch;float oldYaw=0,oldPitch=0,writtenYaw=0,writtenPitch=0;bool owned=false;
    void apply(float y,float p){
        if(!yaw||!pitch||y-y!=0||p-p!=0)return;
        oldYaw=(*env)->GetFloatField(env,player,yaw);oldPitch=(*env)->GetFloatField(env,player,pitch);
        writtenYaw=y;writtenPitch=p;owned=true;
        (*env)->SetFloatField(env,player,yaw,y);(*env)->SetFloatField(env,player,pitch,p);
    }
    ~ScaffoldPacketRotationScope(){
        if(!owned)return;
        auto pending=(*env)->ExceptionOccurred(env);if(pending)(*env)->ExceptionClear(env);
        if((*env)->GetFloatField(env,player,yaw)==writtenYaw)(*env)->SetFloatField(env,player,yaw,oldYaw);
        if((*env)->GetFloatField(env,player,pitch)==writtenPitch)(*env)->SetFloatField(env,player,pitch,oldPitch);
        if(pending){if((*env)->ExceptionCheck(env))(*env)->ExceptionClear(env);(*env)->Throw(env,pending);(*env)->DeleteLocalRef(env,pending);}
    }
};
extern "C" JIds* data_2f6ef0;
extern "C" void hook_scaffold_jump(JNIEnv* env,jclass,jobject entity){
    using namespace sourcefeatures;
    if(data_2f7259)return;
    auto mapping=ids?ids:data_2f6ef0;
    if(ready&&bridge&&jumpMethod&&!menu_pause(env)){
        (*env)->CallStaticVoidMethod(env,bridge,jumpMethod,entity);return;
    }
    auto cls=(*env)->GetObjectClass(env,entity);
    auto method=cls?(*env)->GetMethodID(env,cls,mapping&&mapping->names==1?"jump":mapping&&mapping->names==2?"func_70664_aZ":"bF","()V"):nullptr;
    if(cls)(*env)->DeleteLocalRef(env,cls);
    if(method)(*env)->CallVoidMethod(env,entity,method);
}
extern "C" void hook_scaffold_move_entity(JNIEnv* env,jclass,jobject entity,jdouble x,jdouble y,jdouble z){
    using namespace sourcefeatures;
    if(data_2f7259)return;
    auto mapping=ids?ids:data_2f6ef0;
    if(ready&&bridge&&moveEntityMethod&&mapping&&mapping->variant==1){
        if((*env)->PushLocalFrame(env,32)!=0)return;
        sync(env,4);bool valid=!error(env,"Scaffold move settings");
        if(valid)(*env)->CallStaticVoidMethod(env,bridge,moveEntityMethod,entity,x,y,z);
        (*env)->PopLocalFrame(env,nullptr);
        if(valid)return; // Preserve exceptions from the original virtual moveEntity call.
    }
    auto cls=(*env)->GetObjectClass(env,entity);
    const char* name=mapping&&mapping->names==1?"moveEntity":mapping&&mapping->names==2?"func_70091_d":"d";
    auto method=cls?(*env)->GetMethodID(env,cls,name,"(DDD)V"):nullptr;
    if(cls)(*env)->DeleteLocalRef(env,cls);
    if(method)(*env)->CallVoidMethod(env,entity,method,x,y,z);
}
extern "C" void hook_movement_input(JNIEnv* env,jclass,jobject input){
    using namespace sourcefeatures;
    auto mapping=ids?ids:data_2f6ef0;
    if(!mapping)return;
    auto cls=(*env)->GetObjectClass(env,input);
    auto method=cls?(*env)->GetMethodID(env,cls,mapping->names==1?"updatePlayerMoveState":mapping->names==2?"func_78898_a":"a","()V"):nullptr;
    if(cls)(*env)->DeleteLocalRef(env,cls);
    if(method)(*env)->CallVoidMethod(env,input,method);
    if((*env)->ExceptionCheck(env)||!ready||!bridge||!inputMethod)return;
    if(menu_pause(env))return;
    Jvm java={mapping,env};auto mc=(jobject)get_minecraft(&java);
    auto world=mc?(jobject)get_world(&java,mc):nullptr;
    auto player=mc?(jobject)get_local_player(&java,mc):nullptr;
    sync(env,4);
    if(!error(env,"input settings")){
        (*env)->CallStaticVoidMethod(env,bridge,inputMethod,input);
        error(env,"movement input");
    }
    if(player)(*env)->DeleteLocalRef(env,player);
    if(world)(*env)->DeleteLocalRef(env,world);
    if(mc)(*env)->DeleteLocalRef(env,mc);
}
extern "C" void hook_walking_player(JNIEnv* env,jclass,jobject player){
    using namespace sourcefeatures;
    auto mapping=ids?ids:data_2f6ef0;
    ScaffoldPacketRotationScope rotation{env,player,mapping->Entity_rotationYaw,mapping->Entity_rotationPitch};
    bool scaffoldOwned=false;
    if(ready&&bridge&&!menu_pause(env)){
        auto angles=(jfloatArray)(*env)->CallStaticObjectMethod(env,bridge,motionRotationMethod);
        if(!error(env,"Mayu packet rotation")&&angles&&(*env)->GetArrayLength(env,angles)==2&&increment_wrap(3)){
            float value[2];(*env)->GetFloatArrayRegion(env,angles,0,2,value);rotation.apply(value[0],value[1]);scaffoldOwned=rotation.owned;
        }
        // Rescue solves are produced after entity update. Apply their angles to the next real walking packet,
        // then run source post-motion item use before the scope restores the camera.
        if(!angles){
            auto rescueMethod=(*env)->GetStaticMethodID(env,bridge,"rescueMotionRotation","()[F");
            auto rescue=rescueMethod?(jfloatArray)(*env)->CallStaticObjectMethod(env,bridge,rescueMethod):nullptr;
            if(!error(env,"Rescue packet rotation")&&rescue&&(*env)->GetArrayLength(env,rescue)==2&&increment_wrap(1000)){
                float value[2];(*env)->GetFloatArrayRegion(env,rescue,0,2,value);rotation.apply(value[0],value[1]);
            }
            if(rescue)(*env)->DeleteLocalRef(env,rescue);
        }
        if(angles)(*env)->DeleteLocalRef(env,angles);
    }
    auto method=(*env)->GetMethodID(env,mapping->EntityPlayerSP,mapping->names==1?"onUpdateWalkingPlayer":mapping->names==2?"func_175161_p":"p","()V");
    if(method)(*env)->CallVoidMethod(env,player,method);
    if(ready&&bridge&&!(*env)->ExceptionCheck(env)){
        auto rescue=(*env)->GetStaticMethodID(env,bridge,"rescuePostMotion","(Ljava/lang/Object;)V");
        if(rescue)(*env)->CallStaticVoidMethod(env,bridge,rescue,player);
        if(error(env,"rescue post-motion")){queue_module_toggle(data_2f6f08,&autoPearl,0);queue_module_toggle(data_2f6f08,&fireballPearlClutch,0);queue_module_toggle(data_2f6f08,&bridgeEgg,0);}
        auto end=(*env)->GetStaticMethodID(env,bridge,"scaffoldPacketEnd","(Z)V");
        if(end)(*env)->CallStaticVoidMethod(env,bridge,end,(jboolean)scaffoldOwned);
    }
}
// Mayu's actual update phases. These hooks contain no placement or movement policy.
extern "C" void hook_mayu_gui_update(JNIEnv* env,jclass,jobject gui){
    using namespace sourcefeatures;
    if(ready&&bridge&&!data_2f7259){
        sync(env,4);
        auto method=(*env)->GetStaticMethodID(env,bridge,"mayuGuiUpdate","(Ljava/lang/Object;)V");
        if(method&&!error(env,"Mayu GUI settings")){(*env)->CallStaticVoidMethod(env,bridge,method,gui);return;}
    }
    auto mapping=ids?ids:data_2f6ef0;
    auto cls=(*env)->GetObjectClass(env,gui);
    auto method=cls?(*env)->GetMethodID(env,cls,mapping->names==1?"updateTick":mapping->names==2?"func_73831_a":"c","()V"):nullptr;
    if(method)(*env)->CallVoidMethod(env,gui,method);
    if(cls)(*env)->DeleteLocalRef(env,cls);
}
extern "C" bool source_mayu_move(JNIEnv* env,jobject entity,float strafe,float forward,float friction){
    using namespace sourcefeatures;
    if(!ready||!bridge||menu_pause(env))return false;
    auto method=(*env)->GetStaticMethodID(env,bridge,"mayuMove","(Ljava/lang/Object;FFF)Z");
    return method&&(*env)->CallStaticBooleanMethod(env,bridge,method,entity,(double)strafe,(double)forward,(double)friction);
}
extern "C" unsigned char hook_mayu_loaded(JNIEnv* env,jclass,jobject world,jobject pos){
    using namespace sourcefeatures;
    if(ready&&bridge&&!data_2f7259){
        sync(env,4);
        auto method=(*env)->GetStaticMethodID(env,bridge,"mayuLoaded","(Ljava/lang/Object;Ljava/lang/Object;)Z");
        if(method&&!error(env,"Mayu update settings"))return (*env)->CallStaticBooleanMethod(env,bridge,method,world,pos);
    }
    auto mapping=ids?ids:data_2f6ef0;
    auto method=(*env)->GetMethodID(env,mapping->World,mapping->names==1?"isBlockLoaded":mapping->names==2?"func_175667_e":"e",mapping->names?"(Lnet/minecraft/util/BlockPos;)Z":"(Lcj;)Z");
    return method?(*env)->CallBooleanMethod(env,world,method,pos):0;
}
extern "C" void hook_mayu_living(JNIEnv* env,jclass,jobject player){
    using namespace sourcefeatures;
    auto mapping=ids?ids:data_2f6ef0;
    if(ready&&bridge&&!data_2f7259){
        auto method=(*env)->GetStaticMethodID(env,bridge,"mayuLiving","(Ljava/lang/Object;)V");
        if(method)(*env)->CallStaticVoidMethod(env,bridge,method,player);
        error(env,"Mayu living update");
    }
    auto parent=(*env)->GetSuperclass(env,mapping->EntityPlayerSP);
    auto method=parent?(*env)->GetMethodID(env,parent,mapping->names==1?"onLivingUpdate":mapping->names==2?"func_70636_d":"m","()V"):nullptr;
    if(method)(*env)->CallNonvirtualVoidMethod(env,player,parent,method);
    if(parent)(*env)->DeleteLocalRef(env,parent);
}
extern "C" void hook_mayu_scroll(JNIEnv* env,jclass,jobject inventory,jint scroll){
    using namespace sourcefeatures;
    if(ready&&bridge&&!data_2f7259){
        auto method=(*env)->GetStaticMethodID(env,bridge,"mayuScroll","(Ljava/lang/Object;I)V");
        if(method){(*env)->CallStaticVoidMethod(env,bridge,method,inventory,scroll);return;}
    }
    auto mapping=ids?ids:data_2f6ef0;
    auto method=(*env)->GetMethodID(env,mapping->InventoryPlayer,mapping->names==1?"changeCurrentItem":mapping->names==2?"func_70453_c":"d","(I)V");
    if(method)(*env)->CallVoidMethod(env,inventory,method,scroll);
}
extern "C" void hook_mayu_select(JNIEnv* env,jclass,jobject inventory,jint slot){
    using namespace sourcefeatures;
    if(ready&&bridge&&!data_2f7259){
        auto method=(*env)->GetStaticMethodID(env,bridge,"mayuSelect","(Ljava/lang/Object;I)V");
        if(method){(*env)->CallStaticVoidMethod(env,bridge,method,inventory,slot);return;}
    }
    auto mapping=ids?ids:data_2f6ef0;
    (*env)->SetIntField(env,inventory,mapping->InventoryPlayer_currentItem,slot);
}
extern "C" void hook_mayu_block_click(JNIEnv* env,jclass,jobject mc,jboolean held){
    using namespace sourcefeatures;
    if(ready&&bridge&&!data_2f7259){
        auto method=(*env)->GetStaticMethodID(env,bridge,"mayuBlockClick","()Z");
        bool blocked=method&&(*env)->CallStaticBooleanMethod(env,bridge,method);
        if(error(env,"Mayu block interaction"))blocked=false;
        if(blocked){
            auto reset=(*env)->GetStaticMethodID(env,bridge,"mayuResetBlock","()V");
            if(reset)(*env)->CallStaticVoidMethod(env,bridge,reset);
            return;
        }
    }
    auto mapping=ids?ids:data_2f6ef0;
    auto method=(*env)->GetMethodID(env,mapping->Minecraft,mapping->names==1?"sendClickBlockToController":mapping->names==2?"func_147115_a":"b","(Z)V");
    if(method)(*env)->CallVoidMethod(env,mc,method,held);
}
// Scanner service for the supplied Java Displace implementation.
extern "C" bool source_displace_void_yaw(JNIEnv* env,jobject target,float yaw,float angle,float radius,bool wall,float* result){
    using namespace sourcefeatures;
    if(!ready||!bridge||!target||!result||(*env)->ExceptionCheck(env))return false;
    auto method=(*env)->GetStaticMethodID(env,bridge,"ravenVoidYaw","(Ljava/lang/Object;FFFZ)F");
    if(!method||error(env,"Raven Void scanner method"))return false;
    float value=(*env)->CallStaticFloatMethod(env,bridge,method,target,(jdouble)yaw,(jdouble)angle,(jdouble)radius,(jboolean)wall);
    if(error(env,"Raven Void scanner")||!(value>=-180&&value<=180))return false;
    *result=value;return true;
}
extern "C" bool source_features_initialize(Jvm* java){using namespace sourcefeatures;
    ids=java->ids;auto e=java->env;if((*e)->GetJavaVM(e,&featureVm)!=0)return false;if((*e)->PushLocalFrame(e,512)!=0)return false;
    auto classClass=(*e)->FindClass(e,"java/lang/Class");auto getLoader=classClass?(*e)->GetMethodID(e,classClass,"getClassLoader","()Ljava/lang/ClassLoader;"):nullptr;
    auto parent=getLoader?(*e)->CallObjectMethod(e,ids->Minecraft,getLoader):nullptr;
    auto loader=parent?(*e)->DefineClass(e,"rice/features/RiceFeatureLoader2",parent,(const jbyte*)feature_loader,feature_loader_size):nullptr;
    if(!loader&&parent&&(*e)->ExceptionCheck(e)){
        (*e)->ExceptionClear(e);auto parentClass=(*e)->GetObjectClass(e,parent);
        auto parentLoad=(*e)->GetMethodID(e,parentClass,"loadClass","(Ljava/lang/String;)Ljava/lang/Class;");
        auto loaderName=(*e)->NewStringUTF(e,"rice.features.RiceFeatureLoader2");
        if(parentLoad)loader=(jclass)(*e)->CallObjectMethod(e,parent,parentLoad,loaderName);
    }
    auto constructor=loader?(*e)->GetMethodID(e,loader,"<init>","([BLjava/lang/ClassLoader;)V"):nullptr;
    const unsigned char* payload=ids->names==1?feature_named:ids->names==2?feature_srg:feature_official;
    unsigned length=ids->names==1?feature_named_size:ids->names==2?feature_srg_size:feature_official_size;
    auto bytes=constructor?(*e)->NewByteArray(e,length):nullptr;
    if(bytes)(*e)->SetByteArrayRegion(e,bytes,0,length,(const jbyte*)payload);
    auto instance=bytes?(*e)->NewObject(e,loader,constructor,bytes,parent):nullptr;
    auto load=loader?(*e)->GetMethodID(e,loader,"loadClass","(Ljava/lang/String;)Ljava/lang/Class;"):nullptr;
    auto name=(*e)->NewStringUTF(e,"dev.rice.module.NativeFeatureBridge");
    auto cls=instance&&load?(jclass)(*e)->CallObjectMethod(e,instance,load,name):nullptr;
    if(cls){JNINativeMethod natives[]={{(char*)"rotation",(char*)"(IDD)D",(void*)rotation},{(char*)"flags",(char*)"(Ljava/lang/Object;)I",(void*)flags},{(char*)"guiOpen",(char*)"()Z",(void*)guiOpen},{(char*)"timerBlocked",(char*)"()Z",(void*)timerBlocked},{(char*)"packetSilent",(char*)"(Ljava/lang/Object;Z)Z",(void*)packetSilent},{(char*)"rescueHost",(char*)"(ID)D",(void*)rescueHost}};
        if((*e)->RegisterNatives(e,cls,natives,6)==0){bridge=(jclass)(*e)->NewGlobalRef(e,cls);auto init=(*e)->GetStaticMethodID(e,bridge,"initialize","(Ljava/lang/Object;)V");
            invoke=(*e)->GetStaticMethodID(e,bridge,"event","(IIZLjava/lang/Object;[F)[F");
            configure=(*e)->GetStaticMethodID(e,bridge,"configure","(I[D)V");
            getStatus=(*e)->GetStaticMethodID(e,bridge,"status","(I)Ljava/lang/String;");
            inputMethod=(*e)->GetStaticMethodID(e,bridge,"input","(Ljava/lang/Object;)V");
            visualMethod=(*e)->GetStaticMethodID(e,bridge,"scaffoldVisual","(Ljava/lang/Object;[F)[F");
            hudMethod=(*e)->GetStaticMethodID(e,bridge,"scaffoldHud","(ID)V");
            moveEntityMethod=(*e)->GetStaticMethodID(e,bridge,"scaffoldMove","(Ljava/lang/Object;DDD)V");
            suspendMethod=(*e)->GetStaticMethodID(e,bridge,"suspendScaffold","()V");
            jumpMethod=(*e)->GetStaticMethodID(e,bridge,"jumpEntity","(Ljava/lang/Object;)V");
            motionRotationMethod=(*e)->GetStaticMethodID(e,bridge,"scaffoldMotionRotation","()[F");
            auto mc=(*e)->GetStaticObjectField(e,ids->Minecraft,ids->Minecraft_Lbao_);
            if(init&&invoke&&configure&&getStatus&&inputMethod&&visualMethod&&hudMethod&&moveEntityMethod&&suspendMethod&&jumpMethod&&motionRotationMethod&&mc){
                (*e)->CallStaticVoidMethod(e,bridge,init,mc);
                if(!error(e,"initialize"))ready=metadata(e,0)&&metadata(e,1)&&metadata(e,2)&&metadata(e,3)&&metadata(e,4)&&metadata(e,5)&&metadata(e,6)&&metadata(e,7)&&metadata(e,8);
            }
        }
    }
    error(e,"class loader");(*e)->PopLocalFrame(e,nullptr);slinky_logf("Source features: %s",ready?"ready (supplied manual Displace + Bow Aimbot + Fog + Shaders + Scaffold + Fireball Boost)":"unavailable");return ready;
}
extern "C" void displace_install(){using namespace sourcefeatures;if(installed)return;installed=true;
    // Replace callbacks on the existing registry entry; retain its toggle/key identity.
    auto displace=descriptors[0];
    for(auto& slot:displace->events)slot={};
    displace->description="Allows dealing knockback at a different angle.";
    displace->settings=show<0>;displace->visit=visit<0>;displace->status=status<0>;
    displace->events[3]={dispatch<0>,0,0};displace->events[13]={dispatch<0>,-1000,0};
    displace->events[14]={dispatch<0>,1000,0};displace->events[15]={dispatch<0>,0,0};
    displace->events[18]={dispatch<0>,1000,0};
    displace->events[5]={dispatch<0>,0,0};displace->events[26]={dispatch<0>,0,0};
    bow.name="Bow Aimbot";bow.description="Supplied predictive bow and rod aimbot";bow.settings=show<1>;bow.visit=visit<1>;bow.status=status<1>;
    const unsigned bowEvents[]={2,5,13,16,17,26};
    for(unsigned t:bowEvents)bow.events[t]={dispatch<1>,-1000,0};
    fog.name="Fog";fog.description="Custom fog color, intensity, opacity and falloff";fog.category=2;fog.settings=show<2>;fog.visit=visit<2>;fog.status=status<2>;
    fog.events[5]={dispatch<2>,0,0};fog.events[26]={dispatch<2>,0,0};
    shaders.name="Shaders";shaders.description="Minecraft fullscreen shader effects";shaders.category=2;shaders.settings=show<3>;shaders.visit=visit<3>;shaders.status=status<3>;
    shaders.events[5]={dispatch<3>,0,0};shaders.events[18]={dispatch<3>,0,0};shaders.events[26]={dispatch<3>,0,0};
    scaffold.name="Scaffold";scaffold.description="Slinky placement with Mayu movement";scaffold.category=3;
    scaffold.settings=show<4>;scaffold.visit=visit<4>;scaffold.status=status<4>;
    const unsigned mayuEvents[]={5,6,14,18,21,26};
    for(unsigned t:mayuEvents)scaffold.events[t]={dispatch<4>,50,0};
    // Valen Fireball Boost: nanotime-driven freeze/burst timer cycle. Per-frame loop(1), incoming(2) and
    // outgoing(3) packets, toggle(5), world unload(26). events[1] routing confirmed against anti_fireball.cpp.
    fireballBoost.name="Fireball Boost";fireballBoost.description="Fireball boost timer burst after a fireball jump";fireballBoost.category=1;
    fireballBoost.settings=show<5>;fireballBoost.visit=visit<5>;fireballBoost.status=status<5>;
    const unsigned fireballBoostEvents[]={1,2,3,5,26};
    for(unsigned t:fireballBoostEvents)fireballBoost.events[t]={dispatch<5>,0,0};
    autoPearl.name="Auto Pearl";autoPearl.description="Recovered Valen pearl rescue and landing prediction";autoPearl.category=4;
    autoPearl.settings=show<6>;autoPearl.visit=visit<6>;autoPearl.status=status<6>;
    const unsigned autoPearlEvents[]={1,2,3,5,13,18,26};
    for(unsigned t:autoPearlEvents)autoPearl.events[t]={dispatch<6>,1000,0};
    fireballPearlClutch.name="Fireball Pearl Clutch";fireballPearlClutch.description="Recovered Valen pearl and fireball rescue sequence";fireballPearlClutch.category=4;
    fireballPearlClutch.settings=show<7>;fireballPearlClutch.visit=visit<7>;fireballPearlClutch.status=status<7>;
    for(unsigned t:autoPearlEvents)fireballPearlClutch.events[t]={dispatch<7>,1000,0};
    bridgeEgg.name="Bridge Egg Clutch";bridgeEgg.description="Recovered bridge egg rescue with explicit support-scan adaptation";bridgeEgg.category=4;
    bridgeEgg.settings=show<8>;bridgeEgg.visit=visit<8>;bridgeEgg.status=status<8>;
    for(unsigned t:autoPearlEvents)bridgeEgg.events[t]={dispatch<8>,1000,0};
}
extern "C" void* mayu_scaffold_descriptor(){displace_install();return &sourcefeatures::scaffold;}
extern "C" void* bow_aimbot_descriptor(){displace_install();return &sourcefeatures::bow;}
extern "C" void* fog_descriptor(){displace_install();return &sourcefeatures::fog;}
extern "C" void* shaders_descriptor(){displace_install();return &sourcefeatures::shaders;}
extern "C" void* fireball_boost_descriptor(){displace_install();return &sourcefeatures::fireballBoost;}
extern "C" void* auto_pearl_descriptor(){displace_install();return &sourcefeatures::autoPearl;}
extern "C" void* fireball_pearl_clutch_descriptor(){displace_install();return &sourcefeatures::fireballPearlClutch;}
extern "C" void* bridge_egg_descriptor(){displace_install();return &sourcefeatures::bridgeEgg;}
extern "C" void source_fog_render(JNIEnv* env){using namespace sourcefeatures;if(!fog.enabled||!ready)return;Jvm java={ids,env};
    if((*env)->PushLocalFrame(env,16)!=0)return;auto mc=get_minecraft(&java);auto world=mc?get_world(&java,mc):nullptr;auto player=mc?get_local_player(&java,mc):nullptr;
    Context context={&java,nullptr,nullptr,mc,world,player};Event event={30,&context};dispatch<2>(&event);(*env)->PopLocalFrame(env,nullptr);
}
extern "C" void hook_setup_fog(JNIEnv* env,jclass,jobject renderer,jint mode,jfloat partial){
    if(data_2f7259)return;
    auto e=env;auto j=sourcefeatures::ids;const char* name=j->names==1?"setupFog":j->names==2?"func_78468_a":"a";
    if(!sourcefeatures::fogSetup)sourcefeatures::fogSetup=(*e)->GetMethodID(e,j->EntityRenderer,name,"(IF)V");if(!sourcefeatures::fogSetup){(*e)->ExceptionClear(e);return;}
    (*e)->CallVoidMethod(e,renderer,sourcefeatures::fogSetup,mode,(double)partial);if(!(*e)->ExceptionCheck(e))source_fog_render(e);
}
extern "C" void displace_cleanup(JNIEnv* env){using namespace sourcefeatures;
    if(bridge){auto cleanup=(*env)->GetStaticMethodID(env,bridge,"cleanupScaffold","()V");if(cleanup)(*env)->CallStaticVoidMethod(env,bridge,cleanup);error(env,"Scaffold unload cleanup");}
    if(bridge){auto cleanup=(*env)->GetStaticMethodID(env,bridge,"cleanupRiceUtilities","()V");if(cleanup)(*env)->CallStaticVoidMethod(env,bridge,cleanup);error(env,"Rice utility cleanup");}
    ready=false;clear_silent(env);if(bridge)(*env)->DeleteGlobalRef(env,bridge);bridge=nullptr;fogSetup=nullptr;current={};featureVm=nullptr;
    for(unsigned i=0;i<9;++i){for(unsigned n=0;n<counts[i];++n){auto& s=settings[i][n];libc_free(s.key);libc_free(s.persistence);if(s.kind==0)libc_free((void*)s.boolean.label);else if(s.kind==1)libc_free((void*)s.number.label);else if(s.kind==3)libc_free((void*)s.color.label);else{libc_free((void*)s.choice.label);for(unsigned k=0;k<s.choice.count;++k)libc_free((void*)s.choice.names[k]);libc_free(s.choice.names);}s={};}counts[i]=0;}
}

#include "../visual/bedplates_adapter.inc"

#include "../utility/rice_anticheat_adapter.inc"
