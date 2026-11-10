// Native port of Mayu AntiFireballModule.java, blob 6fa707a931d5a4412319b72eca11a3362f20b144.
// The unchanged Java reference is retained in references/mayu/.
#include "slinky/engine.h"
#include "slinky/jids.h"
#include "slinky/anti_fireball.h"
#include <stddef.h>
#if defined(_MSC_VER) && !defined(__clang__)
#include <intrin.h>
#endif
using namespace slinky;
FN(void*, libc_calloc, (unsigned long long,unsigned long long));
FN(void, libc_free, (void*));
FN(void, slinky_logf, (const char*,...));
FN(bool, queue_module_toggle, (void*,void*,unsigned char));
FN(jobject, java_get_world, (Jvm*,jobject));
FN(int, java_call_list_size, (Jvm*,jobject));
FN(jobject, java_call_list_get, (Jvm*,jobject,int));
FN(jobject, java_get_entity_player_2, (Jvm*,jobject));
FN(int, java_get_inventory_player_current_item, (Jvm*,jobject));
FN(jobjectArray, java_get_inventory_player_main_inventory, (Jvm*,jobject));
FN(jobject, java_call_item_stack_get_item, (Jvm*,jobject));
FN(jobject, get_net_handler, (Jvm*,jobject));
FN(jobject, java_get_net_handler_play_client_2, (Jvm*,jobject));
FN(void, java_call_generic_future_listener, (Jvm*,jobject,jobject));
FN(void*, slinky_gl_get_proc, (const char*));
FN(FireRotation, get_render_rotation, ());
FN(unsigned char, is_silent_rotation_active, ());
extern "C" float data_2ed240,data_2ed244,data_2ed248; // Native HUD name color.
namespace antifireball {
struct Slot {void(*fn)(void*);int priority,reserved;};
struct Descriptor {
    const char* name;const char* description;unsigned category;unsigned char enabled;char pad15[3];
    unsigned flags;int key;unsigned char hold;char pad21[7];char animation[16];float height;char pad3c[4];
    unsigned char expanded;char pad41[7];Slot events[27];void(*settings)(void*,void*);void(*visit)(void*);void(*status)(void*);
};
struct Context {Jvm* java;void* manager;void* profiles;jobject mc,world,player;void* keys;void* targets;unsigned char inGame;};
struct Event {long long type;Context* ctx;};
struct Toggle {long long type;Context* ctx;void* module;unsigned char on;};
struct Rotation {long long type;unsigned char on,moveFix,useTarget,reserved;float yaw,pitch;};
struct Move {long long type;Context* ctx;float strafe,forward,friction;};
struct Packet {long long type;Jvm* java;jobject packet;long long time;int id,result;};
struct Render {long long type;FireVec camera;Context* ctx;float partial;};
struct Bool {void* unused[2];const char* label;unsigned char value,saved;char pad[6];};
struct Number {void* unused[2];const char* label;const char* format;float step,scale,min,max,value,saved;};
struct Choice {void* unused[3];const char* label;unsigned count,pad;const char** names;unsigned value,saved;unsigned char flags;char rest[7];};
static_assert(sizeof(Descriptor)==0x210&&offsetof(Descriptor,events)==0x48,"Module ABI");
static_assert(offsetof(Packet,result)==0x24&&offsetof(Render,ctx)==0x20,"Event ABI");
static_assert(sizeof(Bool)==32&&sizeof(Number)==56&&sizeof(Choice)==64,"Settings ABI");
static Number range={{},"Range","%.1f blocks",.1f,1,3,8,5,5};
static Number fov={{},"FOV","%.0f degrees",1,1,1,360,360,360};
static Bool rotations={{},"Rotations",1,1},swing={{},"Swing",1,1};
static const char* moveNames[]={"NONE","SILENT","STRICT"};
static const char* targetNames[]={"NONE","DEFAULT","HUD"};
static Choice moveFix={{},"Move-fix",3,0,moveNames,1,1},showTarget={{},"Show-target",3,0,targetNames,0,0};
static Descriptor descriptor;
struct Mappings {
    jclass fireball,animation,capabilities,itemBlock,itemBow,itemFireball;
    jobject attackAction;
    jmethodID packetCtor,animationCtor,eyeHeight,borderSize,attackLocal;
    jfieldID capabilitiesField,allowFlying,input,forward,strafe,jump,sneak;
    bool ready;
};
static Mappings ids;
struct Tracked {jobject entity;bool ignored,seen;Tracked* next;};
static Tracked* tracked;
static Tracked* target;
static jobject ownerWorld,ownerPlayer,ownerMinecraft;
static JNIEnv* mainEnv;
static JIds* gameIds;
static void* manager;
static bool ownsRotation;
static float movementYaw,viewYaw;
static volatile unsigned char actionMask;
static unsigned char actions(){
#if defined(_MSC_VER) && !defined(__clang__)
    return (unsigned char)_InterlockedCompareExchange8((volatile char*)&actionMask,0,0);
#else
    return __atomic_load_n(&actionMask,__ATOMIC_ACQUIRE);
#endif
}
static void clearActions(){
#if defined(_MSC_VER) && !defined(__clang__)
    _InterlockedExchange8((volatile char*)&actionMask,0);
#else
    __atomic_store_n(&actionMask,(unsigned char)0,__ATOMIC_RELEASE);
#endif
}
static void recordAction(unsigned char bits){
#if defined(_MSC_VER) && !defined(__clang__)
    _InterlockedOr8((volatile char*)&actionMask,(char)bits);
#else
    __atomic_fetch_or(&actionMask,bits,__ATOMIC_ACQ_REL);
#endif
}
static bool valid(Context* c){return c&&c->java&&c->world&&c->player&&c->mc;}
static jmethodID method(JNIEnv* e,jclass c,const char* sig,const char* mcp,const char* srg,const char* obf){
    const char* names[]={mcp,srg,obf};for(auto n:names){auto id=(*e)->GetMethodID(e,c,n,sig);if(id)return id;(*e)->ExceptionClear(e);}return nullptr;
}
static jfieldID field(JNIEnv* e,jclass c,const char* sig,const char* mcp,const char* srg,const char* obf){
    const char* names[]={mcp,srg,obf};for(auto n:names){auto id=(*e)->GetFieldID(e,c,n,sig);if(id)return id;(*e)->ExceptionClear(e);}return nullptr;
}
static void append(char*& p,const char* text){while(*text)*p++=*text++;*p=0;}
static bool classSig(JNIEnv* e,jclass c,char*& p){
    auto cl=(*e)->FindClass(e,"java/lang/Class");auto get=cl?(*e)->GetMethodID(e,cl,"getName","()Ljava/lang/String;"):nullptr;
    auto name=get?(jstring)(*e)->CallObjectMethod(e,c,get):nullptr;if(!name)return false;
    auto s=(*e)->GetStringUTFChars(e,name,nullptr);if(!s)return false;
    *p++='L';unsigned n=0;for(auto t=s;*t&&n<220;++t,++n)*p++=*t=='.'?'/':*t;*p++=';';*p=0;
    (*e)->ReleaseStringUTFChars(e,name,s);(*e)->DeleteLocalRef(e,name);(*e)->DeleteLocalRef(e,cl);return n<220;
}
static jclass loadClass(JNIEnv* e,jobject loader,const char* named,const char* obfuscated){
    auto cls=(*e)->FindClass(e,"java/lang/Class");auto forName=cls?(*e)->GetStaticMethodID(e,cls,"forName","(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"):nullptr;
    if(!forName)return nullptr;const char* names[]={named,obfuscated};jclass result=nullptr;
    for(auto name:names){auto string=(*e)->NewStringUTF(e,name);if(!string)break;
        result=(jclass)(*e)->CallStaticObjectMethod(e,cls,forName,string,0,loader);(*e)->DeleteLocalRef(e,string);
        if(result)break;(*e)->ExceptionClear(e);}
    (*e)->DeleteLocalRef(e,cls);return result;
}
static void clearMappings(JNIEnv* e){
    jclass classes[]={ids.fireball,ids.animation,ids.capabilities,ids.itemBlock,ids.itemBow,ids.itemFireball};
    for(auto c:classes)if(c)(*e)->DeleteGlobalRef(e,c);if(ids.attackAction)(*e)->DeleteGlobalRef(e,ids.attackAction);ids={};
}
static bool resolveMovement(Jvm* j,jobject player){
    auto e=j->env;if(ids.input)return true;
    auto cc=(*e)->FindClass(e,"java/lang/Class"),fc=(*e)->FindClass(e,"java/lang/reflect/Field");if(!cc||!fc)return false;
    auto fields=(*e)->GetMethodID(e,cc,"getDeclaredFields","()[Ljava/lang/reflect/Field;");
    auto type=(*e)->GetMethodID(e,fc,"getType","()Ljava/lang/Class;"),mods=(*e)->GetMethodID(e,fc,"getModifiers","()I");
    auto primitive=(*e)->GetMethodID(e,cc,"isPrimitive","()Z"),array=(*e)->GetMethodID(e,cc,"isArray","()Z");
    if(!fields||!type||!mods||!primitive||!array)return false;
    for(jclass c=(*e)->GetObjectClass(e,player);c&&!ids.input;c=(*e)->GetSuperclass(e,c)){
        auto declared=(jobjectArray)(*e)->CallObjectMethod(e,c,fields);if(!declared||(*e)->ExceptionCheck(e))return false;
        for(int n=0;n<(*e)->GetArrayLength(e,declared)&&!ids.input;++n){auto f=(*e)->GetObjectArrayElement(e,declared,n);
            if(f&&((*e)->CallIntMethod(e,f,mods)&8)==0){auto t=(jclass)(*e)->CallObjectMethod(e,f,type);
                if(t&&!(*e)->CallBooleanMethod(e,t,primitive)&&!(*e)->CallBooleanMethod(e,t,array)){
                    auto s=field(e,t,"F","moveStrafe","field_78902_a","a"),fw=field(e,t,"F","moveForward","field_78900_b","b");
                    auto jump=field(e,t,"Z","jump","field_78901_c","c"),sneak=field(e,t,"Z","sneak","field_78899_d","d");
                    if(s&&fw&&jump&&sneak){ids.input=(*e)->FromReflectedField(e,f);ids.strafe=s;ids.forward=fw;ids.jump=jump;ids.sneak=sneak;}}
                if(t)(*e)->DeleteLocalRef(e,t);}
            if(f)(*e)->DeleteLocalRef(e,f);}
        (*e)->DeleteLocalRef(e,declared);}
    return ids.input&&!(*e)->ExceptionCheck(e);
}
static bool resolve(Context* c){
    if(ids.ready)return true;auto j=c->java;auto e=j->env;auto i=j->ids;
    if(!i->BlockPos)return false; // This port uses the 1.8.9 mappings below.
    clearMappings(e);
    auto cc=(*e)->FindClass(e,"java/lang/Class");auto getLoader=cc?(*e)->GetMethodID(e,cc,"getClassLoader","()Ljava/lang/ClassLoader;"):nullptr;
    auto loader=getLoader?(*e)->CallObjectMethod(e,i->Entity,getLoader):nullptr;if(!loader)return false;
    struct ClassSpec {jclass* out;const char* named;const char* obf;};
    ClassSpec specs[]={{&ids.fireball,"net.minecraft.entity.projectile.EntityFireball","ws"},
        {&ids.animation,"net.minecraft.network.play.client.C0APacketAnimation","iy"},
        {&ids.capabilities,"net.minecraft.entity.player.PlayerCapabilities","wl"},
        {&ids.itemBlock,"net.minecraft.item.ItemBlock","yo"},{&ids.itemBow,"net.minecraft.item.ItemBow","yt"},{&ids.itemFireball,"net.minecraft.item.ItemFireball","zm"}};
    for(auto spec:specs){auto cls=loadClass(e,loader,spec.named,spec.obf);if(!cls)return false;*spec.out=(jclass)(*e)->NewGlobalRef(e,cls);(*e)->DeleteLocalRef(e,cls);if(!*spec.out)return false;}
    auto action=loadClass(e,loader,"net.minecraft.network.play.client.C02PacketUseEntity$Action","in$a");if(!action)return false;
    char signature[1024]={};char* p=signature;if(!classSig(e,action,p))return false;
    auto attack=(*e)->GetStaticFieldID(e,action,"ATTACK",signature);if(!attack){(*e)->ExceptionClear(e);attack=(*e)->GetStaticFieldID(e,action,"b",signature);}if(!attack)return false;
    auto value=(*e)->GetStaticObjectField(e,action,attack);ids.attackAction=value?(*e)->NewGlobalRef(e,value):nullptr;
    p=signature;append(p,"(");if(!classSig(e,i->Entity,p)||!classSig(e,action,p))return false;append(p,")V");
    ids.packetCtor=(*e)->GetMethodID(e,i->C02PacketUseEntity,"<init>",signature);
    ids.animationCtor=(*e)->GetMethodID(e,ids.animation,"<init>","()V");
    ids.eyeHeight=method(e,i->Entity,"()F","getEyeHeight","func_70047_e","aS");
    ids.borderSize=method(e,i->Entity,"()F","getCollisionBorderSize","func_70111_Y","ao");
    p=signature;append(p,"(");if(!classSig(e,i->Entity,p))return false;append(p,")V");
    ids.attackLocal=method(e,i->EntityPlayer,signature,"attackTargetEntityWithCurrentItem","func_71059_n","f");
    p=signature;if(!classSig(e,ids.capabilities,p))return false;
    ids.capabilitiesField=field(e,i->EntityPlayer,signature,"capabilities","field_71075_bZ","bA");
    ids.allowFlying=field(e,ids.capabilities,"Z","allowFlying","field_75101_c","c");
    ids.ready=ids.attackAction&&ids.packetCtor&&ids.animationCtor&&ids.eyeHeight&&ids.borderSize&&ids.attackLocal&&ids.capabilitiesField&&ids.allowFlying&&resolveMovement(j,c->player);
    return ids.ready&&!(*e)->ExceptionCheck(e);
}
static FireVec position(Jvm* j,jobject o,bool previous=false){auto e=j->env;auto i=j->ids;
    return {(*e)->GetDoubleField(e,o,previous?i->Entity_prevPosX:i->Entity_posX),(*e)->GetDoubleField(e,o,previous?i->Entity_prevPosY:i->Entity_posY),(*e)->GetDoubleField(e,o,previous?i->Entity_prevPosZ:i->Entity_posZ)};}
static bool bounds(Jvm* j,jobject entity,FireBox& result){auto e=j->env;auto i=j->ids;auto box=(*e)->GetObjectField(e,entity,i->Entity_Lazt_);if(!box)return false;
    result={{(*e)->GetDoubleField(e,box,i->AxisAlignedBB_minX),(*e)->GetDoubleField(e,box,i->AxisAlignedBB_minY),(*e)->GetDoubleField(e,box,i->AxisAlignedBB_minZ)},
        {(*e)->GetDoubleField(e,box,i->AxisAlignedBB_maxX),(*e)->GetDoubleField(e,box,i->AxisAlignedBB_maxY),(*e)->GetDoubleField(e,box,i->AxisAlignedBB_maxZ)}};
    (*e)->DeleteLocalRef(e,box);return !fireBoxNaN(result)&&!(*e)->ExceptionCheck(e);}
static FireVec eyes(Jvm* j,jobject player){auto p=position(j,player);p.y+=(*j->env)->CallFloatMethod(j->env,player,ids.eyeHeight);return p;}
static FireRotation view(Jvm* j,jobject player){auto e=j->env;return {(*e)->GetFloatField(e,player,j->ids->Entity_rotationYaw),(*e)->GetFloatField(e,player,j->ids->Entity_rotationPitch)};}
static void clearTracked(JNIEnv* e){target=nullptr;ownsRotation=false;while(tracked){auto next=tracked->next;(*e)->DeleteGlobalRef(e,tracked->entity);libc_free(tracked);tracked=next;}}
static void clearOwners(JNIEnv* e){clearTracked(e);jobject refs[]={ownerWorld,ownerPlayer,ownerMinecraft};for(auto r:refs)if(r)(*e)->DeleteGlobalRef(e,r);
    ownerWorld=ownerPlayer=ownerMinecraft=nullptr;mainEnv=nullptr;gameIds=nullptr;manager=nullptr;clearActions();}
static bool sameOwner(Context* c){auto e=c->java->env;return ownerWorld&&(*e)->IsSameObject(e,c->world,ownerWorld)&&(*e)->IsSameObject(e,c->player,ownerPlayer);}
static bool bindOwner(Context* c){auto e=c->java->env;if(sameOwner(c))return true;clearOwners(e);
    ownerWorld=(*e)->NewGlobalRef(e,c->world);ownerPlayer=(*e)->NewGlobalRef(e,c->player);ownerMinecraft=(*e)->NewGlobalRef(e,c->mc);
    mainEnv=e;gameIds=c->java->ids;manager=c->manager;return ownerWorld&&ownerPlayer&&ownerMinecraft;}
static void fail(Context* c){auto e=c->java->env;if((*e)->ExceptionCheck(e))(*e)->ExceptionClear(e);clearOwners(e);clearMappings(e);
    queue_module_toggle(c->manager,&descriptor,0);slinky_logf("AntiFireball: required mapping or JNI operation unavailable; safely disabled");}
static void onToggle(void* event){auto ev=(Toggle*)event;if(ev->module!=&descriptor||!ev->ctx||!ev->ctx->java)return;auto c=ev->ctx;auto e=c->java->env;
    clearOwners(e);if(!ev->on)return;if(!valid(c)||(*e)->PushLocalFrame(e,128)!=0){fail(c);return;}
    bool ok=resolve(c)&&bindOwner(c);(*e)->PopLocalFrame(e,nullptr);if(!ok)fail(c);}
static void onFrame(void* event){auto c=((Event*)event)->ctx;if(!c||!c->java)return;if(ownerWorld&&(!valid(c)||!sameOwner(c)))clearOwners(c->java->env);}
static void onPreTick(void* event){auto c=((Event*)event)->ctx;if(!valid(c))return;auto j=c->java;auto e=j->env;
    if((*e)->PushLocalFrame(e,128)!=0)return;if(!ids.ready||!bindOwner(c)){(*e)->PopLocalFrame(e,nullptr);fail(c);return;}
    target=nullptr;for(auto t=tracked;t;t=t->next)t->seen=false;
    FireVec eye=eyes(j,c->player);auto rotation=view(j,c->player);viewYaw=rotation.yaw;
    auto list=java_get_world(j,c->world);int count=list?java_call_list_size(j,list):0;bool ok=true;
    for(int n=0;n<count&&ok;++n){auto entity=java_call_list_get(j,list,n);
        if(entity&&(*e)->IsInstanceOf(e,entity,ids.fireball)){
            Tracked* found=nullptr;for(auto t=tracked;t;t=t->next)if((*e)->IsSameObject(e,t->entity,entity)){found=t;break;}
            if(!found){FireBox box={};bool sane=bounds(j,entity,box);float border=(*e)->CallFloatMethod(e,entity,ids.borderSize);
                found=(Tracked*)libc_calloc(1,sizeof(Tracked));if(!found){ok=false;break;}
                found->entity=(*e)->NewGlobalRef(e,entity);if(!found->entity){libc_free(found);ok=false;break;}
                // A NaN distance is not >3 in Java and therefore joins the ignored list.
                found->ignored=!sane||!(fireDistance(eye,box,border)>3);Tracked** tail=&tracked;while(*tail)tail=&(*tail)->next;*tail=found;}
            found->seen=true;}
        if(entity)(*e)->DeleteLocalRef(e,entity);
        if((*e)->ExceptionCheck(e))ok=false;}
    for(Tracked** slot=&tracked;*slot;){auto t=*slot;if(!t->seen){*slot=t->next;(*e)->DeleteGlobalRef(e,t->entity);libc_free(t);}else slot=&t->next;}
    auto caps=(*e)->GetObjectField(e,c->player,ids.capabilitiesField);bool canFly=caps&&(*e)->GetBooleanField(e,caps,ids.allowFlying);
    if(!caps)ok=false;double best=1.7976931348623157e308;
    for(auto t=tracked;t&&ok;t=t->next){if(t->ignored||canFly)continue;FireBox box={};if(!bounds(j,t->entity,box))continue;
        float border=(*e)->CallFloatMethod(e,t->entity,ids.borderSize);double distance=fireDistance(eye,box,border);
        float error=fireAimError(eye,box,border,rotation);
        if(fireCandidate(false,false,distance,error,range.value,(int)fov.value,false)&&distance<best){best=distance;target=t;}}
    if(!target)ownsRotation=false;
    if((*e)->ExceptionCheck(e))ok=false;(*e)->PopLocalFrame(e,nullptr);if(!ok)fail(c);
}
static bool loadedTarget(Jvm* j){if(!target||!ownerWorld)return false;auto e=j->env;
    int id=(*e)->GetIntField(e,target->entity,j->ids->Entity_entityId);auto current=(*e)->CallObjectMethod(e,ownerWorld,j->ids->World_getEntityByID,id);
    bool same=current&&(*e)->IsSameObject(e,current,target->entity);if(current)(*e)->DeleteLocalRef(e,current);return same;}
static bool holdingExcluded(Jvm* j){auto e=j->env;auto inv=java_get_entity_player_2(j,ownerPlayer);if(!inv)return false;
    int slot=java_get_inventory_player_current_item(j,inv);auto arr=java_get_inventory_player_main_inventory(j,inv);if(!arr||slot<0||slot>8)return false;
    auto stack=(*e)->GetObjectArrayElement(e,arr,slot);if(!stack)return false;auto item=java_call_item_stack_get_item(j,stack);if(!item)return false;
    if((*e)->IsInstanceOf(e,item,ids.itemBow))return true;
    return (*e)->GetIntField(e,stack,j->ids->ItemStack_stackSize)>=1&&
        ((*e)->IsInstanceOf(e,item,ids.itemBlock)||(*e)->IsInstanceOf(e,item,ids.itemFireball));}
static bool send(Jvm* j,jobject packet){if(!packet)return false;auto e=j->env;
    auto net=(*e)->CallObjectMethod(e,ownerMinecraft,j->ids->Minecraft_getNetHandler);if(!net)return false;
    auto connection=java_get_net_handler_play_client_2(j,net);if(!connection)return false;
    java_call_generic_future_listener(j,connection,packet);return !(*e)->ExceptionCheck(e);}
static void onRotation(void* event){
    if(!mainEnv||!gameIds||!target)return;Jvm j={gameIds,mainEnv};auto e=mainEnv;
    if((*e)->PushLocalFrame(e,64)!=0)return;Context c={&j,manager,nullptr,ownerMinecraft,ownerWorld,ownerPlayer};
    if(!loadedTarget(&j)){ownsRotation=false;bool error=(*e)->ExceptionCheck(e);(*e)->PopLocalFrame(e,nullptr);if(error)fail(&c);return;}
    auto ev=(Rotation*)event;ownsRotation=false;FireBox box={};bool ok=bounds(&j,target->entity,box);
    if(ok&&rotations.value&&!holdingExcluded(&j)&&!ev->on){auto r=fireAim(eyes(&j,ownerPlayer),box);
        auto settings=get_net_handler(&j,ownerMinecraft);
        if(!settings){(*e)->PopLocalFrame(e,nullptr);fail(&c);return;}
        float sensitivity=(*e)->GetFloatField(e,settings,gameIds->GameSettings_mouseSensitivity);
        ev->yaw=fireSnap(ev->yaw+fireWrap(r.yaw-ev->yaw),ev->yaw,sensitivity);
        ev->pitch=fireSnap(r.pitch,ev->pitch,sensitivity);
        if(ev->pitch>90)ev->pitch=90;if(ev->pitch< -90)ev->pitch=-90;
        ev->on=1;ev->moveFix=0;ev->useTarget=0;ownsRotation=moveFix.value!=0;movementYaw=ev->yaw;}
    if(ok&&!actions()){
        if(swing.value)(*e)->CallVoidMethod(e,ownerPlayer,gameIds->EntityLivingBase_swingItem);
        else ok=send(&j,(*e)->NewObject(e,ids.animation,ids.animationCtor));
        double distance=fireDistance(eyes(&j,ownerPlayer),box,(*e)->CallFloatMethod(e,target->entity,ids.borderSize));
        if(ok&&distance<=range.value){auto packet=(*e)->NewObject(e,gameIds->C02PacketUseEntity,ids.packetCtor,target->entity,ids.attackAction);
            ok=send(&j,packet);if(ok){recordAction(1);(*e)->CallVoidMethod(e,ownerPlayer,ids.attackLocal,target->entity);}}
    }
    if((*e)->ExceptionCheck(e))ok=false;(*e)->PopLocalFrame(e,nullptr);if(!ok)fail(&c);
}
static void onMove(void* event){auto ev=(Move*)event;auto c=ev->ctx;if(!valid(c)||!sameOwner(c)||!ownsRotation||moveFix.value!=1)return;
    // A later rotation handler (for example Displace) may have won arbitration.
    float difference=fireWrap(get_render_rotation().yaw-movementYaw);
    if(!is_silent_rotation_active()||difference<-.001f||difference>.001f)return;
    auto j=c->java;auto e=j->env;auto input=(*e)->GetObjectField(e,c->player,ids.input);if(!input)return;
    float forward=(*e)->GetFloatField(e,input,ids.forward),strafe=(*e)->GetFloatField(e,input,ids.strafe);
    bool sneak=(*e)->GetBooleanField(e,input,ids.sneak);fireFixMovement(viewYaw,movementYaw,forward,strafe,sneak);
    ev->forward=forward;ev->strafe=strafe;
    (*e)->SetFloatField(e,input,ids.forward,forward);(*e)->SetFloatField(e,input,ids.strafe,strafe);
    (*e)->SetFloatField(e,c->player,j->ids->EntityLivingBase_moveForward,forward);(*e)->SetFloatField(e,c->player,j->ids->EntityLivingBase_moveStrafing,strafe);
    (*e)->DeleteLocalRef(e,input);
}
static void onPacket(void* event){auto ev=(Packet*)event;auto j=ev->java;auto e=j->env;
    bool movement=(*e)->IsInstanceOf(e,ev->packet,j->ids->C03PacketPlayer);
    if(movement){clearActions();return;}
    unsigned char mask=0;if((*e)->IsInstanceOf(e,ev->packet,j->ids->C02PacketUseEntity))mask|=1;
    if((*e)->IsInstanceOf(e,ev->packet,j->ids->C07PacketPlayerDigging))mask|=2;
    if((*e)->IsInstanceOf(e,ev->packet,j->ids->C08PacketPlayerBlockPlacement))mask|=4;
    if(mask)recordAction(mask);
}
static void showSettings(void* a,void* b){W_slider(b,a,&range);W_slider(b,a,&fov);W_bool(b,a,&rotations);W_bool(b,a,&swing);W_enumw(b,a,&moveFix);W_enumw(b,a,&showTarget);}
static void visitSettings(void* v){R_num(v,"range",&range);R_num(v,"fov",&fov);R_bool(v,"rotations",&rotations);R_bool(v,"swing",&swing);R_enum(v,"move_fix",&moveFix);R_enum(v,"show_target",&showTarget);}
struct GL {void(*push)(unsigned);void(*pop)();void(*enable)(unsigned);void(*disable)(unsigned);void(*depth)(unsigned char);
    void(*blend)(unsigned,unsigned);void(*color)(float,float,float,float);void(*begin)(unsigned);void(*end)();void(*vertex)(double,double,double);};
static GL gl;
static bool resolveGL(){if(gl.vertex)return true;
#define GET(field,name) gl.field=(decltype(gl.field))slinky_gl_get_proc(name);if(!gl.field)return false;
    GET(push,"glPushAttrib") GET(pop,"glPopAttrib") GET(enable,"glEnable") GET(disable,"glDisable") GET(depth,"glDepthMask")
    GET(blend,"glBlendFunc") GET(color,"glColor4f") GET(begin,"glBegin") GET(end,"glEnd") GET(vertex,"glVertex3d")
#undef GET
    return true;}
static void onRender(void* event){auto ev=(Render*)event;auto c=ev->ctx;if(!showTarget.value||!valid(c)||!sameOwner(c)||!target||!resolveGL())return;
    auto j=c->java;auto e=j->env;if((*e)->PushLocalFrame(e,32)!=0)return;FireBox box={};
    if(loadedTarget(j)&&bounds(j,target->entity,box)){
        auto p=position(j,target->entity),prev=position(j,target->entity,true),eye=eyes(j,c->player);
        float r=1,g=1,b=1;
        if(showTarget.value==1){double dot=(p.x-prev.x)*(eye.x-p.x)+(p.y-prev.y)*(eye.y-p.y-(box.max.y-box.min.y)*.5)+(p.z-prev.z)*(eye.z-p.z);
            r=dot<0?1.f:85.f/255;g=dot<0?85.f/255:1.f;b=85.f/255;
        }else if(showTarget.value==2){r=data_2ed240;g=data_2ed244;b=data_2ed248;}
        FireVec offset={(prev.x-p.x)*(1-ev->partial)-ev->camera.x,(prev.y-p.y)*(1-ev->partial)-ev->camera.y,(prev.z-p.z)*(1-ev->partial)-ev->camera.z};
        FireVec corners[8];for(int n=0;n<8;++n)corners[n]={(n&1?box.max.x+.1:box.min.x-.1)+offset.x,(n&2?box.max.y+.1:box.min.y-.1)+offset.y,(n&4?box.max.z+.1:box.min.z-.1)+offset.z};
        const int faces[]={0,1,3,2,4,6,7,5,0,4,5,1,2,3,7,6,0,2,6,4,1,5,7,3};
        gl.push(0x2000|0x4000|0x100|0x1|0x8);gl.enable(0x0be2);gl.blend(0x0302,0x0303);gl.disable(0x0de1);gl.disable(0x0b50);gl.disable(0x0b71);gl.disable(0x0b44);gl.depth(0);
        gl.color(r,g,b,63.f/255);gl.begin(7);for(int index:faces)gl.vertex(corners[index].x,corners[index].y,corners[index].z);gl.end();gl.pop();
    }
    if((*e)->ExceptionCheck(e))(*e)->ExceptionClear(e);(*e)->PopLocalFrame(e,nullptr);
}
static void shutdown(void* event){auto c=((Event*)event)->ctx;if(c&&c->java)clearOwners(c->java->env);}
}
extern "C" void* anti_fireball_descriptor(){using namespace antifireball;
    descriptor.name="AntiFireball";descriptor.description="Automatically deflects approaching fireballs.";descriptor.category=0;
    descriptor.events[1]={onFrame,0};descriptor.events[3]={onPacket,10000};descriptor.events[5]={onToggle,0};descriptor.events[6]={onRender,0};
    descriptor.events[13]={onRotation,-100};descriptor.events[16]={onPreTick,0};descriptor.events[21]={onMove,100};descriptor.events[26]={shutdown,0};
    descriptor.settings=showSettings;descriptor.visit=visitSettings;return &descriptor;}
extern "C" void anti_fireball_cleanup(JNIEnv* e){antifireball::clearOwners(e);antifireball::clearMappings(e);}
// Rescue notification uses the existing native near-origin exclusion, not Valen's missing OwnLaunch heuristic.
// Future projectiles are classified by the same <=3-block rule on their first native observation.
extern "C" void anti_fireball_record_rescue_launch(JNIEnv* env){using namespace antifireball;
    if(!ids.ready||!gameIds||!ownerPlayer||!ownerWorld)return;Jvm j={gameIds,env};
    auto origin=eyes(&j,ownerPlayer);
    for(auto t=tracked;t;t=t->next){FireBox box={};
        if(bounds(&j,t->entity,box)){float border=(*env)->CallFloatMethod(env,t->entity,ids.borderSize);
            if(fireDistance(origin,box,border)<=3){t->ignored=true;if(target==t){target=nullptr;ownsRotation=false;}}}
    }
}
