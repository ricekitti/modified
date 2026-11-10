#include "slinky/engine.h"
#include "slinky/jids.h"
#include "slinky/heavy_glow.h"
#include <stddef.h>
namespace heavyglowclient {
struct Vec {double x,y,z;};struct Color {float r,g,b;};
struct Context {Jvm* java;void* manager;void* profiles;jobject mc,world,player;void* keys;void* targets;unsigned char inGame;};
struct Render {long long type;Vec camera;Context* ctx;float partial;};
struct Toggle {long long type;Context* ctx;void* module;unsigned char on;};
struct Event {long long type;Context* ctx;};
struct Slot {void(*fn)(void*);int priority,reserved;};
struct Descriptor {char header[0x48];Slot events[27];};
struct Choice {char header[0x20];unsigned count,pad;const char** labels;};
static_assert(offsetof(Choice,labels)==0x28&&offsetof(Render,partial)==0x28,"Native ESP ABI");
extern "C" char data_2e9e80[],data_2ea0e0[];
extern "C" unsigned data_2ea110;
extern "C" float data_2ea1d0;
extern "C" unsigned char data_2ea250,data_2ea270,data_2ea158;
extern "C" int data_2ea2c0;
extern "C" Color data_2ea1e0,data_2ea200,data_2ea220;
// The legacy helper named get_render_manager actually reads renderGlobal.
// This version-aware accessor is also used by the existing working ESP path.
FN(jobject, java_get_minecraft_6, (Jvm*,jobject));
FN(jobject, java_get_world, (Jvm*,jobject));
FN(int, java_call_list_size, (Jvm*,jobject));
FN(jobject, java_call_list_get, (Jvm*,jobject,int));
FN(unsigned, entity_flags_get, (Jvm*,jobject));
FN(jobject, get_refill_state, ());
FN(int, entity_name_colour, (Jvm*,jobject));
FN(Color, hex_digit_value, (int));
FN(void, slinky_logf, (const char*,...));
FN(void*, slinky_gl_get_proc, (const char*));
static const char* modes[]={"2D","3D","Heavy Glow"};
static jmethodID renderEntity,getShadow,setShadow;
static jfieldID renderOutlines,hideGui;
static jmethodID bindTexture,activeTexture,depthMask,alphaFunc,blendFunc;
static bool mappingFailed;
static bool captureActive;
struct CaptureScope {
    bool previous;
    CaptureScope():previous(captureActive){captureActive=true;}
    ~CaptureScope(){captureActive=previous;}
};
static jmethodID method(JNIEnv* e,jclass c,const char* sig,const char* a,const char* b,const char* d){
    const char* names[]={a,b,d};for(auto n:names){auto id=(*e)->GetMethodID(e,c,n,sig);if(id)return id;(*e)->ExceptionClear(e);}return nullptr;}
static jmethodID staticMethod(JNIEnv* e,jclass c,const char* sig,const char* a,const char* b,const char* d){
    const char* names[]={a,b,d};for(auto n:names){auto id=(*e)->GetStaticMethodID(e,c,n,sig);if(id)return id;(*e)->ExceptionClear(e);}return nullptr;}
static jfieldID booleanField(JNIEnv* e,jclass cls,const char* a,const char* b,const char* c){
    const char* names[]={a,b,c};for(auto name:names){auto id=(*e)->GetFieldID(e,cls,name,"Z");if(id)return id;(*e)->ExceptionClear(e);}return nullptr;
}
static bool resolve(Jvm* j){if(mappingFailed)return false;if(renderEntity&&getShadow&&setShadow&&renderOutlines&&hideGui&&(!j->ids->GlStateManager||(bindTexture&&activeTexture&&depthMask&&alphaFunc&&blendFunc)))return true;auto e=j->env;
    renderOutlines=booleanField(e,j->ids->RenderManager,"renderOutlines","field_178639_r","r");
    hideGui=booleanField(e,j->ids->GameSettings,"hideGUI","field_74319_N","aA");
    auto cc=(*e)->FindClass(e,"java/lang/Class");auto name=cc?(*e)->GetMethodID(e,cc,"getName","()Ljava/lang/String;"):nullptr;
    auto string=name?(jstring)(*e)->CallObjectMethod(e,j->ids->Entity,name):nullptr;auto text=string?(*e)->GetStringUTFChars(e,string,nullptr):nullptr;
    char signature[300]={'(','L'};unsigned n=2;
    if(text){for(auto p=text;*p&&n<250;++p)signature[n++]=*p=='.'?'/':*p;(*e)->ReleaseStringUTFChars(e,string,text);
        signature[n++]=';';signature[n++]='F';signature[n++]='Z';signature[n++]=')';signature[n++]='Z';signature[n]=0;
        renderEntity=method(e,j->ids->RenderManager,signature,"renderEntityStatic","func_147936_a","a");
        getShadow=method(e,j->ids->RenderManager,"()Z","isRenderShadow","func_178627_a","a");
        setShadow=method(e,j->ids->RenderManager,"(Z)V","setRenderShadow","func_178633_a","a");}
    if(j->ids->GlStateManager){auto cls=j->ids->GlStateManager;
        bindTexture=staticMethod(e,cls,"(I)V","bindTexture","func_179144_i","i");activeTexture=staticMethod(e,cls,"(I)V","setActiveTexture","func_179138_g","g");
        depthMask=staticMethod(e,cls,"(Z)V","depthMask","func_179132_a","a");alphaFunc=staticMethod(e,cls,"(IF)V","alphaFunc","func_179092_a","a");
        blendFunc=staticMethod(e,cls,"(IIII)V","tryBlendFuncSeparate","func_179120_a","a");}
    bool ok=renderEntity&&getShadow&&setShadow&&renderOutlines&&hideGui&&bindTexture&&activeTexture&&depthMask&&alphaFunc&&blendFunc&&!(*e)->ExceptionCheck(e);
    if(!ok){(*e)->ExceptionClear(e);mappingFailed=true;slinky_logf("Heavy Glow: RenderManager mapping unavailable");}return ok;
}
// Entity rendering changes Minecraft's GL cache. After the GL renderer restores
// the actual context, reconcile that cache to avoid stale skin/lightmap bindings.
static void syncState(Jvm* j){if(!j->ids->GlStateManager||!activeTexture)return;auto e=j->env;auto i=j->ids;auto cls=i->GlStateManager;
    auto get=(void(*)(unsigned,int*))slinky_gl_get_proc("glGetIntegerv");auto getFloat=(void(*)(unsigned,float*))slinky_gl_get_proc("glGetFloatv");
    auto enabled=(unsigned char(*)(unsigned))slinky_gl_get_proc("glIsEnabled");auto active=(void(*)(unsigned))slinky_gl_get_proc("glActiveTexture");if(!get||!getFloat||!enabled||!active)return;
    int original=0;get(0x84e0,&original);
    struct Texture {int unit,bound;bool on;};Texture textures[]={{0x84c0},{0x84c1}};
    for(auto& t:textures){active((unsigned)t.unit);get(0x8069,&t.bound);t.on=enabled(0xde1)!=0;}
    active((unsigned)original);
    for(auto t:textures){(*e)->CallStaticVoidMethod(e,cls,activeTexture,t.unit==0x84c0?0x84c1:0x84c0);(*e)->CallStaticVoidMethod(e,cls,activeTexture,t.unit);
        (*e)->CallStaticVoidMethod(e,cls,bindTexture,0);(*e)->CallStaticVoidMethod(e,cls,bindTexture,t.bound);
        (*e)->CallStaticVoidMethod(e,cls,t.on?i->GlStateManager_enableTexture2D:i->GlStateManager_disableTexture2D);}
    (*e)->CallStaticVoidMethod(e,cls,activeTexture,original==0x84c0?0x84c1:0x84c0);(*e)->CallStaticVoidMethod(e,cls,activeTexture,original);
    struct Capability {unsigned cap;jmethodID on,off;};Capability states[]={{0xb71,i->GlStateManager_enableDepth,i->GlStateManager_disableDepth},
        {0xb50,i->GlStateManager_enableLighting,i->GlStateManager_disableLighting},{0xbe2,i->GlStateManager_enableBlend,i->GlStateManager_disableBlend},
        {0xb44,i->GlStateManager_enableCull,i->GlStateManager_disableCull},{0xbc0,i->GlStateManager_enableAlpha,i->GlStateManager_disableAlpha}};
    for(auto s:states)(*e)->CallStaticVoidMethod(e,cls,enabled(s.cap)?s.on:s.off);
    int depth=0,func=0,src=0,dst=0,srcAlpha=0,dstAlpha=0;float ref=0,color[4]={};get(0xb72,&depth);get(0xbc1,&func);getFloat(0xbc2,&ref);
    get(0x80c9,&src);get(0x80c8,&dst);get(0x80cb,&srcAlpha);get(0x80ca,&dstAlpha);getFloat(0xb00,color);
    (*e)->CallStaticVoidMethod(e,cls,depthMask,depth!=0);(*e)->CallStaticVoidMethod(e,cls,alphaFunc,func,(double)ref);
    (*e)->CallStaticVoidMethod(e,cls,blendFunc,src,dst,srcAlpha,dstAlpha);(*e)->CallStaticVoidMethod(e,cls,i->GlStateManager_color,(double)color[0],(double)color[1],(double)color[2],(double)color[3]);
}
static Vec position(Jvm* j,jobject p){auto e=j->env;return {(*e)->GetDoubleField(e,p,j->ids->Entity_posX),(*e)->GetDoubleField(e,p,j->ids->Entity_posY),(*e)->GetDoubleField(e,p,j->ids->Entity_posZ)};}
struct DrawContext {Render* event;jobject renderer;bool failed;};
static bool capture(void* context,void(*color)(float,float,float)){
    auto dc=(DrawContext*)context;auto event=dc->event;auto c=event->ctx;auto j=c->java;auto e=j->env;auto i=j->ids;
    // FBO allocation and capture setup used raw GL calls. Reconcile before the
    // first model too, so a cached skin binding cannot skip its required bind.
    syncState(j);
    if((*e)->ExceptionCheck(e)){(*e)->ExceptionClear(e);dc->failed=true;return false;}
    auto list=java_get_world(j,c->world);int count=list?java_call_list_size(j,list):0;Vec me=position(j,c->player);
    auto settings=(*e)->GetObjectField(e,c->mc,i->Minecraft_Lbbj_);
    if(!settings||(*e)->ExceptionCheck(e)){(*e)->ExceptionClear(e);dc->failed=true;return false;}
    bool oldOutlines=(*e)->GetBooleanField(e,dc->renderer,renderOutlines);
    bool oldHideGui=(*e)->GetBooleanField(e,settings,hideGui);
    // RenderManager propagates this to the player renderer, bypassing its
    // label/layer pass. hideGUI also covers client-added label renderers that
    // bypass the vanilla canRenderName hook (e.g. Lunar).
    (*e)->SetBooleanField(e,dc->renderer,renderOutlines,1);
    (*e)->SetBooleanField(e,settings,hideGui,1);
    bool shadow=(*e)->CallBooleanMethod(e,dc->renderer,getShadow),drew=false;(*e)->CallVoidMethod(e,dc->renderer,setShadow,0);
    unsigned rendered=0;
    for(int n=0;n<count&&rendered<100;++n){
        if((*e)->PushLocalFrame(e,32)!=0){dc->failed=true;break;}auto entity=java_call_list_get(j,list,n);
        if(entity&&(*e)->IsInstanceOf(e,entity,i->EntityPlayer)&&!(*e)->IsSameObject(e,entity,c->player)&&!(*e)->GetBooleanField(e,entity,i->Entity_isDead)){
            unsigned flags=entity_flags_get(j,entity);Vec p=position(j,entity);double x=p.x-me.x,y=p.y-me.y,z=p.z-me.z;
            if(!(flags&(data_2ea250?7u:2u))&&x*x+y*y+z*z<=(double)data_2ea1d0*data_2ea1d0){
                Color tint;
                if(data_2ea270&&(*e)->IsSameObject(e,entity,get_refill_state()))tint=data_2ea1e0;
                else if(data_2ea2c0)tint=(flags&5)?data_2ea200:data_2ea220;
                else tint=hex_digit_value(entity_name_colour(j,entity));
                if(data_2ea158){float hurt=(float)(*e)->GetIntField(e,entity,i->EntityLivingBase_hurtTime)-event->partial;
                    float amount=(hurt-1)/7;if(amount<0)amount=0;if(amount>1)amount=1;tint={tint.r+(1-tint.r)*amount,tint.g*(1-amount),tint.b*(1-amount)};}
                color(tint.r,tint.g,tint.b);
                {
                    // Capture only the model. Labels must stay in the normal
                    // world render, not in a through-wall glow silhouette.
                    CaptureScope scope;
                    if((*e)->CallBooleanMethod(e,dc->renderer,renderEntity,entity,(double)event->partial,1))drew=true;
                }
                ++rendered;
            }
        }
        bool error=(*e)->ExceptionCheck(e);(*e)->PopLocalFrame(e,nullptr);if(error){(*e)->ExceptionClear(e);dc->failed=true;break;}
    }
    // Restore Java's render-manager state even when a model throws.
    if((*e)->ExceptionCheck(e)){(*e)->ExceptionClear(e);dc->failed=true;}
    (*e)->SetBooleanField(e,dc->renderer,renderOutlines,(jboolean)oldOutlines);
    (*e)->SetBooleanField(e,settings,hideGui,(jboolean)oldHideGui);
    (*e)->CallVoidMethod(e,dc->renderer,setShadow,(int)shadow);
    if((*e)->ExceptionCheck(e)){(*e)->ExceptionClear(e);dc->failed=true;}
    return drew&&!dc->failed;
}
static void toggle(void* event){auto e=(Toggle*)event;if(e->module==data_2e9e80&&!e->on)heavy_glow_release();}
static void shutdown(void*){heavy_glow_release();}
}
extern "C" void heavy_glow_extend_modes(){using namespace heavyglowclient;
    auto choice=(Choice*)data_2ea0e0;choice->count=3;choice->labels=modes;
    auto module=(Descriptor*)data_2e9e80;module->events[5]={toggle,0};module->events[26]={shutdown,0};
}
extern "C" const char* heavy_glow_mode_name(unsigned mode){return heavyglowclient::modes[mode<3?mode:0];}
extern "C" bool heavy_glow_capture_active(){return heavyglowclient::captureActive;}
extern "C" bool heavy_glow_render(void* event){using namespace heavyglowclient;auto ev=(Render*)event;auto c=ev->ctx;
    if(!c||!c->java||!c->player||!c->world||!c->mc)return false;auto j=c->java;auto e=j->env;bool rendered=false;
    if((*e)->PushLocalFrame(e,64)!=0)return false;
    if(resolve(j)){auto rm=java_get_minecraft_6(j,c->mc);int w=(*e)->GetIntField(e,c->mc,j->ids->Minecraft_displayWidth),h=(*e)->GetIntField(e,c->mc,j->ids->Minecraft_displayHeight);
        // A method ID from RenderManager must never be invoked on RenderGlobal
        // or another receiver: JNI dispatch can crash the VM before throwing.
        if(rm&&j->ids->RenderManager&&(*e)->IsInstanceOf(e,rm,j->ids->RenderManager)){
            DrawContext draw={ev,rm,false};rendered=heavy_glow_draw(w,h,capture,&draw);syncState(j);if(draw.failed)rendered=false;}}
    if((*e)->ExceptionCheck(e)){(*e)->ExceptionClear(e);rendered=false;}(*e)->PopLocalFrame(e,nullptr);return rendered;
}
