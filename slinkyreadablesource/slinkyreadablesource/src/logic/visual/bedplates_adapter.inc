// Bedplates shares only the existing payload loader; it has its own descriptor and settings.
namespace bedplatesnative {
using namespace sourcefeatures;
static Descriptor module;
static Bool align={{},"Align Top Center",0,0,{}},autoscale={{},"Auto Scale",1,1,{}},counters={{},"Show Block Count",1,1,{}};
static Number scale={{},"Scale","%.1f",.1f,1,.1f,1.5f,.8f,.8f};
static Number distance={{},"Render Distance","%.0f blocks",1,1,10,200,150,150};
static Number offset={{},"Y Offset","%.1f blocks",.5f,1,-10,10,1,1};
static const char* modes[]={"Static","Toggle","Hold"};
static Choice mode={{},"Mode",3,0,modes,0,0,0,{}};
static const char* keys[]={"None","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9","Space","Left Shift","Left Control","Left Alt","Mouse 4","Mouse 5"};
static Choice key={{},"Keybind",43,0,keys,0,0,0,{}};
static Choice customize={{},"Customize Key",43,0,keys,0,0,0,{}};
struct Render {long long type;double camera[3];Context* ctx;float width,height;const double *modelview,*projection;float partial;unsigned char flags;};
static_assert(offsetof(Render,partial)==0x40&&sizeof(Bool)==32&&sizeof(Number)==56,"Bedplates ABI");
static void show(void* a,void* b){
    W_bool(b,a,&align);W_bool(b,a,&autoscale);W_bool(b,a,&counters);
    W_slider(b,a,&scale);W_slider(b,a,&distance);W_slider(b,a,&offset);W_enumw(b,a,&mode);
    if(mode.value)W_enumw(b,a,&key);
    W_enumw(b,a,&customize);
}
static void visit(void* out){
    R_bool(out,"align_top_center",&align);R_bool(out,"auto_scale",&autoscale);R_bool(out,"show_block_count",&counters);
    R_num(out,"scale",&scale);R_num(out,"render_distance",&distance);R_num(out,"y_offset",&offset);
    R_enum(out,"mode",&mode);R_enum(out,"display_keybind",&key);R_enum(out,"customize_keybind",&customize);
}
static void dispatch(void* raw){
    if(!raw||!ready||!bridge||data_2f7259)return;
    unsigned type=*(unsigned*)raw;Context* context=nullptr;
    if(type==5){auto event=(Toggle*)raw;if(event->module!=&module)return;context=event->ctx;}
    else if(type==7)context=((Render*)raw)->ctx;
    else context=((Event*)raw)->ctx;
    auto env=context&&context->java?context->java->env:threadEnvironment();if(!env)return;
    if((*env)->PushLocalFrame(env,64)!=0)return;
    auto method=(*env)->GetStaticMethodID(env,bridge,"bedplates","(Ljava/lang/Object;I[D)V");
    double frame[46]={align.value?1.:0.,autoscale.value?1.:0.,counters.value?1.:0.,scale.value,distance.value,offset.value,(double)mode.value,(double)key.value};
    frame[8]=(double)customize.value;
    if(type==7){auto render=(Render*)raw;
        if(!render->modelview||!render->projection){(*env)->PopLocalFrame(env,nullptr);return;}
        for(int i=0;i<3;++i)frame[8+i]=render->camera[i];
        frame[11]=render->width;frame[12]=render->height;frame[13]=render->partial;
        for(int i=0;i<16;++i){frame[14+i]=render->modelview[i];frame[30+i]=render->projection[i];}
    }
    auto array=method?(*env)->NewDoubleArray(env,46):nullptr;
    if(array){(*env)->SetDoubleArrayRegion(env,array,0,46,frame);(*env)->CallStaticVoidMethod(env,bridge,method,context?context->mc:nullptr,(jint)type,array);}
    if(error(env,"Bedplates")&&context&&module.enabled)queue_module_toggle(context->manager,&module,0);
    (*env)->PopLocalFrame(env,nullptr);
}
}
extern "C" void* bedplates_descriptor(){
    using namespace bedplatesnative;static bool initialized=false;
    if(!initialized){initialized=true;module.name="Bedplates";module.description="Shows the blocks covering Bedwars beds";module.category=2;module.settings=show;module.visit=visit;
        const unsigned events[]={5,7,18,26};for(unsigned type:events)module.events[type]={dispatch,0,0};
    }
    return &module;
}
