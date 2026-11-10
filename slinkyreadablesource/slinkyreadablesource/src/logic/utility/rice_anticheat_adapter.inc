// RiceSlinky anticheat and a scoped extension of the existing Fast Mine descriptor.
namespace riceutility {
using namespace sourcefeatures;
extern "C" char data_2f1d90[];
extern "C" float data_2f2028;
static Descriptor detector;
static Bool checks[]={
    {{},"Ignore teammates",0,0,{}},{{},"Should ping",1,1,{}},{{},"Tab Icon",1,1,{}},
    {{},"Cheater popup",1,1,{}},{{},"Nametag mark",1,1,{}},{{},"Blacklist warning",1,1,{}},
    {{},"Autoblock",1,1,{}},{{},"Keep-Y",1,1,{}},{{},"Tower",1,1,{}},{{},"Bridge Assist",1,1,{}},
    {{},"Stasis",1,1,{}},{{},"Fast Mine",1,1,{}},{{},"No Break Delay",1,1,{}}
};
static Number interval={{},"Flag interval","%.0f seconds",1,1,0,60,20,20};
static Number threshold={{},"Cheater threshold","%.0f",1,1,1,20,5,5};
static Number popupX={{},"Popup x","%.2f",.01f,1,0,1,.5f,.5f},popupY={{},"Popup y","%.2f",.01f,1,0,1,.12f,.12f};
static const char* fatigueNames[]={"Off","Hold","Increase"};
static Choice fatigue={{},"Ignore fatigue",3,0,fatigueNames,0,0,0,{}};
static Number digSpeed={{},"Dig speed","%.0f%%",1,1,30,150,75,75};
static void (*originalLate)(void*),(*originalToggle)(void*),(*originalWorld)(void*),(*originalPacket)(void*);
static void (*originalGui)(void*,void*),(*originalVisit)(void*);
static Descriptor* fastmine(){return (Descriptor*)data_2f1d90;}
static bool fastEnabled(){return fastmine()->enabled!=0;}
static void frame(double* values){
    values[0]=detector.enabled;values[1]=interval.value;values[2]=checks[0].value;values[3]=checks[1].value;values[4]=checks[2].value;values[5]=threshold.value;
    for(int i=3;i<13;++i)values[i+3]=checks[i].value;
    values[16]=popupX.value;values[17]=popupY.value;
}
static void gui(void* a,void* b){
    W_slider(b,a,&interval);for(int i=0;i<3;++i)W_bool(b,a,&checks[i]);W_slider(b,a,&threshold);
    for(int i=3;i<6;++i)W_bool(b,a,&checks[i]);
    W_label(a,b,"Checks");for(int i=6;i<13;++i)W_bool(b,a,&checks[i]);
    if(checks[3].value){W_label(a,b,"Popup");W_slider(b,a,&popupX);W_slider(b,a,&popupY);}
}
static void visit(void* out){
    R_num(out,"flag_interval",&interval);R_num(out,"cheater_threshold",&threshold);
    const char* keys[]={"ignore_teammates","should_ping","tab_icon","cheater_popup","nametag_mark","blacklist_warning","autoblock","keep_y","tower","bridge_assist","stasis","fast_mine","no_break_delay"};
    for(int i=0;i<13;++i)R_bool(out,keys[i],&checks[i]);R_num(out,"popup_x",&popupX);R_num(out,"popup_y",&popupY);
}
static JNIEnv* environment(Context* c){return c&&c->java?c->java->env:threadEnvironment();}
static void dispatch(void* raw){
    if(!raw||!ready||!bridge||data_2f7259)return;
    unsigned type=*(unsigned*)raw;Context* c=nullptr;Packet* packet=nullptr;
    if(type==5){auto e=(Toggle*)raw;if(e->module!=&detector)return;c=e->ctx;}
    else if(type==2||type==3)packet=(Packet*)raw;
    else if(type==7)c=((bedplatesnative::Render*)raw)->ctx;
    else c=((Event*)raw)->ctx;
    auto e=packet?packet->java->env:environment(c);if(!e||(*e)->PushLocalFrame(e,64)!=0)return;
    if(packet){
        auto method=(*e)->GetStaticMethodID(e,bridge,"riceAnticheatPacket","(Ljava/lang/Object;ZZ)I");
        int result=method?(*e)->CallStaticIntMethod(e,bridge,method,packet->packet,(jboolean)(type==2),(jboolean)(detector.enabled!=0)):0;
        if(result==2&&packet->result!=3)packet->result=2;
    }else{
        double values[64]={};frame(values);
        if(type==7){auto r=(bedplatesnative::Render*)raw;if(r->modelview&&r->projection){
            for(int i=0;i<3;++i)values[26+i]=r->camera[i];values[29]=r->width;values[30]=r->height;values[31]=r->partial;
            for(int i=0;i<16;++i){values[32+i]=r->modelview[i];values[48+i]=r->projection[i];}
        }}
        auto method=(*e)->GetStaticMethodID(e,bridge,"riceAnticheat","(I[D)V");auto array=method?(*e)->NewDoubleArray(e,64):nullptr;
        if(array){(*e)->SetDoubleArrayRegion(e,array,0,64,values);(*e)->CallStaticVoidMethod(e,bridge,method,(jint)type,array);}
    }
    if(error(e,"RiceSlinky Anticheat")&&c&&detector.enabled)queue_module_toggle(c->manager,&detector,0);
    (*e)->PopLocalFrame(e,nullptr);
}
static double noSpeed(){union Bits{unsigned long long raw;double value;} bits={0x7ff8000000000000ULL};return bits.value;}
static double fatigueCall(JNIEnv* e,unsigned type,jobject packet){
    if(!e||!ready||!bridge||data_2f7259)return noSpeed();
    if((*e)->PushLocalFrame(e,32)!=0)return noSpeed();
    auto method=(*e)->GetStaticMethodID(e,bridge,"fatigueEvent","(ILjava/lang/Object;ZID)D");
    double result=method?(*e)->CallStaticDoubleMethod(e,bridge,method,(jint)type,packet,(jboolean)fastEnabled(),(jint)fatigue.value,(jdouble)digSpeed.value):noSpeed();
    if(error(e,"Fast Mine fatigue"))result=noSpeed();(*e)->PopLocalFrame(e,nullptr);return result;
}
static void late(void* raw){
    auto c=((Event*)raw)->ctx;float previous=data_2f2028;double speed=fatigueCall(environment(c),18,nullptr);
    if(speed>=1&&speed<100000)data_2f2028=(float)speed;
    if(originalLate)originalLate(raw);data_2f2028=previous;
}
static void toggle(void* raw){
    auto event=(Toggle*)raw;if(event->module!=fastmine())return;
    if(originalToggle)originalToggle(raw);fatigueCall(environment(event->ctx),5,nullptr);
}
static void world(void* raw){if(originalWorld)originalWorld(raw);fatigueCall(environment(((Event*)raw)->ctx),26,nullptr);}
static void packet(void* raw){
    if(originalPacket)originalPacket(raw);auto p=(Packet*)raw;if(p->result==2||p->result==3)return;
    if(fatigueCall(p->java->env,2,p->packet)==2)p->result=2;
}
static void mineGui(void* a,void* b){if(originalGui)originalGui(a,b);W_enumw(b,a,&fatigue);if(fatigue.value==2)W_slider(b,a,&digSpeed);}
static void mineVisit(void* out){if(originalVisit)originalVisit(out);R_enum(out,"riceslinky_ignore_fatigue",&fatigue);R_num(out,"riceslinky_fatigue_dig_speed",&digSpeed);}
}
extern "C" void* rice_anticheat_descriptor(){
    using namespace riceutility;static bool initialized=false;if(initialized)return &detector;initialized=true;
    detector.name="Anticheat";detector.description="RiceSlinky player cheat detection";detector.category=4;detector.settings=gui;detector.visit=visit;
    const unsigned events[]={2,3,5,7,18,26};for(unsigned type:events)detector.events[type]={dispatch,0,0};
    auto mine=fastmine();originalLate=mine->events[18].fn;mine->events[18].fn=late;
    originalPacket=mine->events[2].fn;mine->events[2].fn=packet;
    originalToggle=mine->events[5].fn;mine->events[5].fn=toggle;
    originalWorld=mine->events[26].fn;mine->events[26].fn=world;
    originalGui=mine->settings;mine->settings=mineGui;originalVisit=mine->visit;mine->visit=mineVisit;
    return &detector;
}
