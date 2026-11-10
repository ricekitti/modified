// 0x21000: class-hook installer (JVMTI) and the table of native hook bodies
// Defines SlinkyHooks18 in the game's class loader, registers its natives and rewrites the nine hooked game classes
// through the ClassFileLoadHook (class_file_load_hook, see e_22.cpp). The tables of what gets rewritten are data
// (hook_tables.inc, generated from the image by tools/gen_hooks.py): the original assembled them on its stack.
#include "slinky/engine.h"
#include "slinky/jnienv.h"
#include "slinky/jids.h"
FN(void *, libc_memcpy_alt, (void *d, const void *s, unsigned long long n));
extern "C" char data_2f7810[];
// Copies the 23 native pointers to data_2f7810 (the original installer read them back from there; this
// implementation takes them from kNatives, so nothing calls this any more)
extern "C" void *copies_23_native_pointers_data(const void *natives) { return libc_memcpy_alt(data_2f7810, natives, 0xb8); }

// ---- run-time layouts shared with the ClassFileLoadHook code (e_22.cpp, e_20.cpp) ----
struct MHook { const char *name, *desc; unsigned short ref; };                       // a native of SlinkyHooks18; ref = its Methodref
struct HookEnt { const char *owner, *name, *desc; long long kind; unsigned short *ref; };   // instruction to replace
struct MethodPat { const char *name, *desc; HookEnt *ents; unsigned long long n; };   // method to rewrite (-1, -1 = all)
struct HookRec { jclass cls; MHook *natives; unsigned long long nnat; MethodPat *pats; unsigned long long npats; };

// ---- compile-time description of the hooks ----
enum { INVOKE_VIRTUAL = 1, INVOKE_STATIC = 2, GETFIELD = 3, GETSTATIC = 4, PUTFIELD = 5, PUTSTATIC = 6 };
#define ANY nullptr
struct NativeRef { const char *name, *desc; };
struct Site { const char *owner, *name, *desc; int kind; int native; };   // `native` indexes the class's NativeRef list
struct Method { const char *name, *desc; const Site *sites; int nsites; };
struct HookedClass { jclass JIds::*cls; const NativeRef *natives; int nnatives; const Method *methods; int nmethods; };
#include "hook_tables.inc"

// ---- JVMTI (function table indices) ----
struct JvmtiTable { void *fn[160]; };
typedef int (*SetEventMode)(JvmtiTable **, int mode, int event, void *thread);
typedef int (*AddCaps)(JvmtiTable **, const void *caps);
typedef int (*SetCallbacks)(JvmtiTable **, const void *callbacks, int size);
typedef int (*Retransform)(JvmtiTable **, int count, jclass *classes);
#define JVMTI(env, idx, T) ((T)(*(env))->fn[idx])
enum { JVMTI_DISABLE = 0, JVMTI_ENABLE = 1, EV_CLASS_FILE_LOAD_HOOK = 0x36 };
enum { SET_EVENT_NOTIFICATION_MODE = 1, SET_EVENT_CALLBACKS = 121, ADD_CAPABILITIES = 141, RETRANSFORM_CLASSES = 151 };
enum { JVMTI_ERROR_BASE = 12000 };   // client error code = 12000 + jvmtiError

extern "C" unsigned char data_2f78c8;   // 1 while the hooks are installed
extern "C" jobject data_2f78d0;          // global ref of SlinkyHooks18
extern "C" int data_2f7a48;              // first error seen by the ClassFileLoadHook (sticky)
extern "C" HookRec data_2f78e0[9];       // the hooked classes, read by the ClassFileLoadHook
extern "C" const char rdata_14f710[];    // "SlinkyHooks18"
extern "C" const char rdata_14f720[];    // jvmtiCapabilities: can_retransform_classes
extern "C" const char rdata_14d480[];    // jvmtiEventCallbacks (0x138 bytes): ClassFileLoadHook = class_file_load_hook
extern "C" const jbyte rdata_2b3a70[];   // class file of SlinkyHooks18
extern "C" const int rdata_2b3f20;       // its length
extern "C" const unsigned char feature_hooks[];
extern "C" const unsigned feature_hooks_size;
FN(int, define_class_with_natives, (JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize len, const JNINativeMethod *methods, jint n,
               jclass *out));

// static storage for the records of the chosen mapping (the ClassFileLoadHook reads them for as long as the hooks
// are installed)
static MHook g_natives[9][10];
static MethodPat g_pats[9][3];
static HookEnt g_ents[96];
static unsigned long long g_nents;

// ---- separate hook registry (does NOT grow the fixed 9-entry image table data_2f78e0) ----
// Mayu player phases, isolated from the fixed original-client class table.
extern "C" { HookRec g_extra;HookRec g_inventory;HookRec g_hotbar; }
static MHook g_extra_nat[4];
static MethodPat g_extra_pat[2];
static HookEnt g_extra_ent[4];
static void build_extra(JIds* ids){
    g_extra={};if(ids->variant!=1||!ids->EntityPlayerSP)return;
    const char* player=ids->names?"net/minecraft/client/entity/EntityPlayerSP":"bew";
    g_extra_nat[0]={"walkingPlayer","(Ljava/lang/Object;)V",0};
    g_extra_nat[1]={"mayuLoaded","(Ljava/lang/Object;Ljava/lang/Object;)Z",0};
    g_extra_nat[2]={"movementInput","(Ljava/lang/Object;)V",0};
    g_extra_nat[3]={"mayuLiving","(Ljava/lang/Object;)V",0};
    g_extra_ent[0]={player,ids->names==1?"onUpdateWalkingPlayer":ids->names==2?"func_175161_p":"p","()V",INVOKE_VIRTUAL,&g_extra_nat[0].ref};
    g_extra_ent[1]={ids->names?"net/minecraft/world/World":"adm",ids->names==1?"isBlockLoaded":ids->names==2?"func_175667_e":"e",ids->names?"(Lnet/minecraft/util/BlockPos;)Z":"(Lcj;)Z",INVOKE_VIRTUAL,&g_extra_nat[1].ref};
    g_extra_ent[2]={ids->names?"net/minecraft/util/MovementInput":"beu",ids->names==1?"updatePlayerMoveState":ids->names==2?"func_78898_a":"a","()V",INVOKE_VIRTUAL,&g_extra_nat[2].ref};
    g_extra_ent[3]={ids->names?"net/minecraft/client/entity/AbstractClientPlayer":"bet",ids->names==1?"onLivingUpdate":ids->names==2?"func_70636_d":"m","()V",INVOKE_VIRTUAL,&g_extra_nat[3].ref};
    g_extra_pat[0]={ids->names==1?"onUpdate":ids->names==2?"func_70071_h_":"t_","()V",g_extra_ent,2};
    g_extra_pat[1]={ids->names==1?"onLivingUpdate":ids->names==2?"func_70636_d":"m","()V",g_extra_ent+2,2};
    g_extra={ids->EntityPlayerSP,g_extra_nat,4,g_extra_pat,2};
}
static void build_inventory(JIds*){g_inventory={};g_hotbar={};}

static void build_records(JIds *ids, const HookedClass *tab) {
    g_nents = 0;
    for (int c = 0; c < 9; c++) {
        const HookedClass &hc = tab[c];
        HookRec &rec = data_2f78e0[c];
        int count=hc.nnatives+((c==0&&ids->variant==1)?4:0)+((c==1&&ids->variant==1)?1:0)+((c==8&&ids->variant==1)?2:0);
        for (int i = 0; i < count; i++) g_natives[c][i] = MHook{hc.natives[i].name, hc.natives[i].desc, 0};
        for (int m = 0; m < hc.nmethods; m++) {
            const Method &meth = hc.methods[m];
            HookEnt *first = &g_ents[g_nents];
            for (int s = 0; s < meth.nsites; s++) {
                const Site &site = meth.sites[s];
                g_ents[g_nents++] = HookEnt{site.owner, site.name, site.desc, site.kind, &g_natives[c][site.native].ref};
            }
            bool fogSite=c==1&&ids->variant==1&&m==0;
            bool moveSite=c==8&&ids->variant==1&&m==0;
            if(fogSite)g_ents[g_nents++]={ids->names?"net/minecraft/client/renderer/EntityRenderer":"bfk",ids->names==1?"setupFog":ids->names==2?"func_78468_a":"a","(IF)V",INVOKE_VIRTUAL,&g_natives[c][5].ref};
            if(moveSite)g_ents[g_nents++]={ids->names?"net/minecraft/entity/EntityLivingBase":"pr",ids->names==1?"moveEntity":ids->names==2?"func_70091_d":"d","(DDD)V",INVOKE_VIRTUAL,&g_natives[c][1].ref};
            bool mayuInput=c==0&&ids->variant==1&&m<2;
            if(mayuInput){
                const char* inventory=ids->names?"net/minecraft/entity/player/InventoryPlayer":"wm";
                g_ents[g_nents++]={inventory,ids->names==1?"changeCurrentItem":ids->names==2?"func_70453_c":"d","(I)V",INVOKE_VIRTUAL,&g_natives[c][6].ref};
                g_ents[g_nents++]={inventory,ids->names==1?"currentItem":ids->names==2?"field_70461_c":"c","I",PUTFIELD,&g_natives[c][7].ref};
                g_ents[g_nents++]={ids->names?"net/minecraft/client/Minecraft":"ave",ids->names==1?"sendClickBlockToController":ids->names==2?"func_147115_a":"b","(Z)V",INVOKE_VIRTUAL,&g_natives[c][8].ref};
                g_ents[g_nents++]={ids->names?"net/minecraft/client/gui/GuiIngame":"avo",ids->names==1?"updateTick":ids->names==2?"func_73831_a":"c","()V",INVOKE_VIRTUAL,&g_natives[c][9].ref};
            }
            const char *any = (const char *)-1;
            g_pats[c][m] = MethodPat{meth.name ? meth.name : any, meth.desc ? meth.desc : any, first,
                                     (unsigned long long)meth.nsites+(fogSite||moveSite?1u:0u)+(mayuInput?4u:0u)};
        }
        if(c==8&&ids->variant==1){
            HookEnt* first=&g_ents[g_nents];
            g_ents[g_nents++]={ids->names?"net/minecraft/entity/EntityLivingBase":"pr",ids->names==1?"jump":ids->names==2?"func_70664_aZ":"bF","()V",INVOKE_VIRTUAL,&g_natives[c][2].ref};
            g_pats[c][hc.nmethods]={ids->names==1?"onLivingUpdate":ids->names==2?"func_70636_d":"m","()V",first,1};
        }
        rec = HookRec{ids->*hc.cls, g_natives[c], (unsigned long long)count, g_pats[c], (unsigned long long)hc.nmethods+((c==8&&ids->variant==1)?1u:0u)};
    }
}

// Install the hooks: 0 or an error (JNI/loader errors as the client defines them, 12000 + jvmtiError)
extern "C" int install_class_hooks(Jvm *j, JvmtiTable **jvmti) {
    if (data_2f78c8) return 0;
    JNIEnv *env = j->env;
    JIds *ids = j->ids;

    constexpr int nativeCount=sizeof(kNatives)/sizeof(kNatives[0]);
    JNINativeMethod natives[nativeCount];
    for (int i = 0; i < nativeCount; i++) natives[i] = JNINativeMethod{(char *)kNatives[i].name, (char *)kNatives[i].desc, kNatives[i].fn};
    jclass cls;
    int err = define_class_with_natives(env, rdata_14f710, (jobject)((char *)ids + 0x10 /* game loader + loadClass */), (const jbyte*)feature_hooks,
                      feature_hooks_size, natives, nativeCount, &cls);
    if (err) return err;
    data_2f78d0 = (*env)->NewGlobalRef(env, cls);

    if ((err = JVMTI(jvmti, ADD_CAPABILITIES, AddCaps)(jvmti, rdata_14f720))) return JVMTI_ERROR_BASE + err;
    char callbacks[0x138];
    __builtin_memcpy(callbacks, rdata_14d480, sizeof callbacks);
    if ((err = JVMTI(jvmti, SET_EVENT_CALLBACKS, SetCallbacks)(jvmti, callbacks, sizeof callbacks)))
        return JVMTI_ERROR_BASE + err;
    if ((err = JVMTI(jvmti, SET_EVENT_NOTIFICATION_MODE, SetEventMode)(jvmti, JVMTI_ENABLE, EV_CLASS_FILE_LOAD_HOOK, 0)))
        return JVMTI_ERROR_BASE + err;

    build_records(ids, kHooks[ids->variant][ids->names]);
    build_extra(ids);
    build_inventory(ids);
    __atomic_exchange_n(&data_2f7a48, 0, __ATOMIC_SEQ_CST);
    jclass classes[9];
    for (int i = 0; i < 9; i++) classes[i] = data_2f78e0[i].cls;
    // the retransformation runs the ClassFileLoadHook (class_file_load_hook) for each class on this thread
    if ((err = JVMTI(jvmti, RETRANSFORM_CLASSES, Retransform)(jvmti, 9, classes))) return JVMTI_ERROR_BASE + err;
    // separate registry: retransform the extra class (EntityPlayerSP) so its onUpdateWalkingPlayer site is wrapped
    if (g_extra.cls) {
        jclass extra[1] = {g_extra.cls};
        if ((err = JVMTI(jvmti, RETRANSFORM_CLASSES, Retransform)(jvmti, 1, extra))) return JVMTI_ERROR_BASE + err;
    }
    if(g_inventory.cls){jclass extra[1]={g_inventory.cls};if((err=JVMTI(jvmti,RETRANSFORM_CLASSES,Retransform)(jvmti,1,extra)))return JVMTI_ERROR_BASE+err;}
    if(g_hotbar.cls){jclass extra[1]={g_hotbar.cls};if((err=JVMTI(jvmti,RETRANSFORM_CLASSES,Retransform)(jvmti,1,extra)))return JVMTI_ERROR_BASE+err;}
    __atomic_exchange_n(&data_2f78c8, 1, __ATOMIC_SEQ_CST);
    // the hook only has to run for those classes: switch it off again
    if ((err = JVMTI(jvmti, SET_EVENT_NOTIFICATION_MODE, SetEventMode)(jvmti, JVMTI_DISABLE, EV_CLASS_FILE_LOAD_HOOK, 0)))
        return JVMTI_ERROR_BASE + err;
    return data_2f7a48;
}
