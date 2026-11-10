// Native adaptation of the three Java sources in references/fireball-pearl/.
#include "slinky/engine.h"
#include "slinky/jids.h"
#include "slinky/fireball_pearl.h"
#include "slinky/anti_fireball.h"
#include <stddef.h>
FN(void, slinky_logf, (const char*,...));
FN(bool, queue_module_toggle, (void*,void*,unsigned char));
FN(jobject, java_get_entity_player_2, (Jvm*,jobject));
FN(int, java_get_inventory_player_current_item, (Jvm*,jobject));
FN(void, java_set_inventory_player_current_item, (Jvm*,jobject,int));
FN(jobjectArray, java_get_inventory_player_main_inventory, (Jvm*,jobject));
FN(jobject, java_call_item_stack_get_item, (Jvm*,jobject));
FN(jobject, java_get_minecraft_4, (Jvm*,jobject));
FN(jobject, get_net_handler, (Jvm*,jobject));
FN(jobject, java_get_net_handler_play_client_2, (Jvm*,jobject));
FN(void, java_call_generic_future_listener, (Jvm*,jobject,jobject));
FN(bool, claim_release_hotbar_module, (void*));
FN(void, release_owner, (void*));
namespace fireballpearl {
struct Slot {void(*fn)(void*);int priority,reserved;};
struct Descriptor {const char* name;const char* description;unsigned category;unsigned char enabled;char pad15[3];unsigned flags;int key;
    unsigned char hold;char pad21[7];char animation[16];float height;char pad3c[4];unsigned char expanded;char pad41[7];
    Slot events[27];void(*settings)(void*,void*);void(*visit)(void*);void(*status)(void*);};
struct Context {Jvm* java;void* manager;void* profiles;jobject mc,world,player;void* keys;void* targets;unsigned char inGame;};
struct Event {long long type;Context* ctx;};
struct Toggle {long long type;Context* ctx;void* module;unsigned char on;};
struct Rotation {long long type;unsigned char on,moveFix,useTarget,reserved;float yaw,pitch;};
struct Bool {void* unused[2];const char* label;unsigned char value,saved;char pad[6];};
static_assert(sizeof(Descriptor)==0x210&&offsetof(Descriptor,events)==0x48,"Module ABI");
static Descriptor descriptor;
static Bool realRotations={{},"Real Rotations",0,0};
static slinky::FireballPearlState sequence;
static jobject ownerWorld,ownerPlayer;
static bool temporaryRotation,published,claimed;
static float savedYaw,savedPitch,savedPrevYaw,savedPrevPitch;
struct Mappings {jclass pearl,fireball,animation,capabilities;jmethodID useCtor,animationCtor,syncSlot;jfieldID capabilitiesField,creative;bool ready;};
static Mappings ids;
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

static bool valid(Context* c){return c&&c->java&&c->mc&&c->world&&c->player;}
static bool sameOwner(Context* c){if(!valid(c)||!ownerWorld||!ownerPlayer)return false;auto e=c->java->env;
    return (*e)->IsSameObject(e,ownerWorld,c->world)&&(*e)->IsSameObject(e,ownerPlayer,c->player);}
static void clearMappings(JNIEnv* e){jclass refs[]={ids.pearl,ids.fireball,ids.animation,ids.capabilities};for(auto r:refs)if(r)(*e)->DeleteGlobalRef(e,r);ids={};}
static bool resolve(Context* c){if(ids.ready)return true;auto j=c->java;auto e=j->env;auto i=j->ids;if(!i->BlockPos||!i->C08PacketPlayerBlockPlacement)return false;
    clearMappings(e);auto cc=(*e)->FindClass(e,"java/lang/Class");auto get=cc?(*e)->GetMethodID(e,cc,"getClassLoader","()Ljava/lang/ClassLoader;"):nullptr;
    auto loader=get?(*e)->CallObjectMethod(e,i->Entity,get):nullptr;if(!loader)return false;
    struct Spec {jclass* out;const char* named;const char* obf;};
    Spec specs[]={{&ids.pearl,"net.minecraft.item.ItemEnderPearl","zk"},{&ids.fireball,"net.minecraft.item.ItemFireball","zm"},
        {&ids.animation,"net.minecraft.network.play.client.C0APacketAnimation","iy"},{&ids.capabilities,"net.minecraft.entity.player.PlayerCapabilities","wl"}};
    for(auto s:specs){auto cls=loadClass(e,loader,s.named,s.obf);if(!cls)return false;*s.out=(jclass)(*e)->NewGlobalRef(e,cls);(*e)->DeleteLocalRef(e,cls);if(!*s.out)return false;}
    char signature[1024]={};char* p=signature;append(p,"(");if(!classSig(e,i->BlockPos,p))return false;append(p,"I");if(!classSig(e,i->ItemStack,p))return false;append(p,"FFF)V");
    ids.useCtor=(*e)->GetMethodID(e,i->C08PacketPlayerBlockPlacement,"<init>",signature);ids.animationCtor=(*e)->GetMethodID(e,ids.animation,"<init>","()V");
    ids.syncSlot=method(e,i->PlayerControllerMP,"()V","syncCurrentPlayItem","func_78750_j","n");
    p=signature;if(!classSig(e,ids.capabilities,p))return false;
    ids.capabilitiesField=field(e,i->EntityPlayer,signature,"capabilities","field_71075_bZ","bA");
    ids.creative=field(e,ids.capabilities,"Z","isCreativeMode","field_75098_d","d");
    ids.ready=ids.useCtor&&ids.animationCtor&&ids.syncSlot&&ids.capabilitiesField&&ids.creative;return ids.ready&&!(*e)->ExceptionCheck(e);
}
static void restoreTemporary(Context* c){if(temporaryRotation&&sameOwner(c)){auto e=c->java->env;auto i=c->java->ids;
    (*e)->SetFloatField(e,c->player,i->Entity_rotationYaw,savedYaw);(*e)->SetFloatField(e,c->player,i->Entity_rotationPitch,savedPitch);
    (*e)->SetFloatField(e,c->player,i->Entity_prevRotationYaw,savedPrevYaw);(*e)->SetFloatField(e,c->player,i->Entity_prevRotationPitch,savedPrevPitch);}
    temporaryRotation=false;
}
static bool selectSlot(Context* c,int slot){if(slot<0||slot>8)return false;auto j=c->java;auto e=j->env;auto inv=java_get_entity_player_2(j,c->player);if(!inv)return false;
    if(java_get_inventory_player_current_item(j,inv)!=slot){auto controller=java_get_minecraft_4(j,c->mc);if(!controller)return false;
        java_set_inventory_player_current_item(j,inv,slot);(*e)->CallVoidMethod(e,controller,ids.syncSlot);}
    return !(*e)->ExceptionCheck(e);
}
static void stop(Context* c,bool restore){if(!c||!c->java)return;auto e=c->java->env;restoreTemporary(c);
    if(restore&&sameOwner(c)&&sequence.previous>=0)selectSlot(c,sequence.previous);
    if((*e)->ExceptionCheck(e))(*e)->ExceptionClear(e);
    sequence.clear();published=false;if(claimed)release_owner(&descriptor);claimed=false;
    if(ownerWorld)(*e)->DeleteGlobalRef(e,ownerWorld);if(ownerPlayer)(*e)->DeleteGlobalRef(e,ownerPlayer);ownerWorld=ownerPlayer=nullptr;
}
static void fail(Context* c,const char* why){auto e=c->java->env;if((*e)->ExceptionCheck(e))(*e)->ExceptionClear(e);stop(c,true);queue_module_toggle(c->manager,&descriptor,0);slinky_logf("Fireball Pearl: %s",why);}
static bool useItem(Context* c){auto j=c->java;auto e=j->env;auto i=j->ids;auto inv=java_get_entity_player_2(j,c->player);if(!inv)return false;
    int slot=java_get_inventory_player_current_item(j,inv);auto array=java_get_inventory_player_main_inventory(j,inv);if(!array||slot<0||slot>8)return false;
    auto stack=(*e)->GetObjectArrayElement(e,array,slot);if(!stack)return true; // Source skips empty held stacks.
    auto net=(*e)->CallObjectMethod(e,c->mc,i->Minecraft_getNetHandler);auto connection=net?java_get_net_handler_play_client_2(j,net):nullptr;if(!connection)return false;
    auto pos=(*e)->NewObject(e,i->BlockPos,i->BlockPos__init_,-1,-1,-1);if(!pos)return false;
    auto use=(*e)->NewObject(e,i->C08PacketPlayerBlockPlacement,ids.useCtor,pos,255,stack,0.0,0.0,0.0);if(!use)return false;
    java_call_generic_future_listener(j,connection,use);if((*e)->ExceptionCheck(e))return false;
    auto animation=(*e)->NewObject(e,ids.animation,ids.animationCtor);if(!animation)return false;
    java_call_generic_future_listener(j,connection,animation);if((*e)->ExceptionCheck(e))return false;
    auto caps=(*e)->GetObjectField(e,c->player,ids.capabilitiesField);if(!caps)return false;
    auto item=java_call_item_stack_get_item(j,stack);
    if(!(*e)->GetBooleanField(e,caps,ids.creative)&&item&&(*e)->IsInstanceOf(e,item,ids.fireball)){
        int count=(*e)->GetIntField(e,stack,i->ItemStack_stackSize)-1;(*e)->SetIntField(e,stack,i->ItemStack_stackSize,count);
        if(count<=0)(*e)->SetObjectArrayElement(e,array,slot,nullptr);
    }
    return !(*e)->ExceptionCheck(e);
}
static void onToggle(void* event){auto ev=(Toggle*)event;if(ev->module!=&descriptor||!ev->ctx||!ev->ctx->java)return;auto c=ev->ctx;auto e=c->java->env;
    if(!ev->on){stop(c,true);return;}stop(c,true);
    if(!valid(c)){queue_module_toggle(c->manager,&descriptor,0);return;}if((*e)->PushLocalFrame(e,64)!=0){fail(c,"local frame unavailable");return;}
    bool ok=resolve(c);auto inv=ok?java_get_entity_player_2(c->java,c->player):nullptr;auto array=inv?java_get_inventory_player_main_inventory(c->java,inv):nullptr;
    int pearl=-1,fireball=-1,previous=inv?java_get_inventory_player_current_item(c->java,inv):-1;
    if(array)for(int slot=0;slot<9;++slot){auto stack=(*e)->GetObjectArrayElement(e,array,slot);if(!stack)continue;
        auto item=java_call_item_stack_get_item(c->java,stack);if(item){if(pearl<0&&(*e)->IsInstanceOf(e,item,ids.pearl))pearl=slot;if(fireball<0&&(*e)->IsInstanceOf(e,item,ids.fireball))fireball=slot;}}
    if(ok&&array&&pearl>=0&&previous>=0&&previous<=8&&claim_release_hotbar_module(&descriptor)){
        claimed=true;ownerWorld=(*e)->NewGlobalRef(e,c->world);ownerPlayer=(*e)->NewGlobalRef(e,c->player);
        ok=ownerWorld&&ownerPlayer&&sequence.begin(pearl,fireball,previous,(*e)->GetFloatField(e,c->player,c->java->ids->Entity_rotationYaw));
    }else ok=false;
    if((*e)->ExceptionCheck(e))ok=false;(*e)->PopLocalFrame(e,nullptr);if(!ok)fail(c,"pearl unavailable, hotbar busy, or mapping unavailable");
}
static void onTick(void* event){auto c=((Event*)event)->ctx;if(!sequence.active)return;
    if(!sameOwner(c)){if(c&&c->java){stop(c,false);queue_module_toggle(c->manager,&descriptor,0);}return;}
    auto e=c->java->env;if((*e)->PushLocalFrame(e,64)!=0){fail(c,"local frame unavailable");return;}
    auto action=sequence.tick();bool ok=true;
    if(action.slot>=0)ok=selectSlot(c,action.slot);if(ok&&action.use)ok=useItem(c);
    if((*e)->ExceptionCheck(e))ok=false;(*e)->PopLocalFrame(e,nullptr);
    if(!ok){fail(c,"item use or slot synchronization failed");return;}
    if(action.disable){stop(c,true);queue_module_toggle(c->manager,&descriptor,0);}
}
static void onPreUpdate(void* event){auto c=((Event*)event)->ctx;published=false;if(!sequence.active||!sameOwner(c))return;
    auto j=c->java;auto e=j->env;auto i=j->ids;
    if(sequence.silentRotation(realRotations.value!=0)){
        savedYaw=(*e)->GetFloatField(e,c->player,i->Entity_rotationYaw);savedPitch=(*e)->GetFloatField(e,c->player,i->Entity_rotationPitch);
        savedPrevYaw=(*e)->GetFloatField(e,c->player,i->Entity_prevRotationYaw);savedPrevPitch=(*e)->GetFloatField(e,c->player,i->Entity_prevRotationPitch);
        temporaryRotation=true;published=true;
        (*e)->SetFloatField(e,c->player,i->Entity_rotationYaw,sequence.yaw);(*e)->SetFloatField(e,c->player,i->Entity_rotationPitch,-90);
    }else if(sequence.realRotation(realRotations.value!=0)){
        auto settings=get_net_handler(j,c->mc);if(!settings){fail(c,"game settings unavailable");return;}
        float sensitivity=(*e)->GetFloatField(e,settings,i->GameSettings_mouseSensitivity);
        float yaw=(*e)->GetFloatField(e,c->player,i->Entity_rotationYaw),pitch=(*e)->GetFloatField(e,c->player,i->Entity_rotationPitch);
        yaw=slinky::fireSnap(yaw+slinky::fireWrap(sequence.yaw-yaw),yaw,sensitivity);pitch=slinky::fireSnap(-90,pitch,sensitivity);
        if(pitch>90)pitch=90;if(pitch< -90)pitch=-90;
        (*e)->SetFloatField(e,c->player,i->Entity_rotationYaw,yaw);(*e)->SetFloatField(e,c->player,i->Entity_rotationPitch,pitch);
    }
    if((*e)->ExceptionCheck(e))fail(c,"rotation update failed");
}
static void onPostUpdate(void* event){auto c=((Event*)event)->ctx;if(c&&c->java)restoreTemporary(c);}
static void onRotation(void* event){if(!published||!sequence.silentRotation(realRotations.value!=0))return;auto ev=(Rotation*)event;
    ev->on=1;ev->moveFix=1;ev->useTarget=0;ev->yaw=sequence.yaw;ev->pitch=-90;
}
static void onFrame(void* event){auto c=((Event*)event)->ctx;if(ownerWorld&&c&&c->java&&!sameOwner(c)){stop(c,false);queue_module_toggle(c->manager,&descriptor,0);}}
static void shutdown(void* event){auto c=((Event*)event)->ctx;if(c&&c->java)stop(c,true);}
static void showSettings(void* a,void* b){W_bool(b,a,&realRotations);}
static void visitSettings(void* v){R_bool(v,"real_rotations",&realRotations);}
}
extern "C" void* fireball_pearl_descriptor(){using namespace fireballpearl;
    descriptor.name="Fireball Pearl";descriptor.description="Throws a pearl upward, optionally uses a fireball, then restores your slot.";descriptor.category=4;
    descriptor.events[1]={onFrame,-1000};descriptor.events[5]={onToggle,0};descriptor.events[13]={onRotation,-2000};descriptor.events[16]={onTick,600};
    descriptor.events[17]={onPreUpdate,-2000};descriptor.events[18]={onPostUpdate,-2000};descriptor.events[26]={shutdown,0};
    descriptor.settings=showSettings;descriptor.visit=visitSettings;return &descriptor;
}
extern "C" void fireball_pearl_cleanup(JNIEnv* e){using namespace fireballpearl;
    if(ownerWorld)(*e)->DeleteGlobalRef(e,ownerWorld);if(ownerPlayer)(*e)->DeleteGlobalRef(e,ownerPlayer);ownerWorld=ownerPlayer=nullptr;
    if(claimed)release_owner(&descriptor);claimed=false;sequence.clear();published=temporaryRotation=false;clearMappings(e);
}
