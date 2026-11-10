#include "slinky/engine.h"
#include "slinky/hud_editor.h"
#include "slinky/injection_notice.h"
#include "nanovg.h"
#if defined(_MSC_VER) && !defined(__clang__)
#include <intrin.h>
#endif
extern "C" {
extern char data_2eb740[],data_2ecef0[],data_2ed320[];
extern const float rdata_151e64;
extern const float rdata_151e60,rdata_151ea0;
extern const double rdata_151e68,rdata_151e78;
extern float data_2ed2e0,data_2ed318,data_2fbfa0;
extern unsigned char data_2faff5,data_2ed1d8,data_2ed1f8,data_2ed628;
extern unsigned long long data_2faff8,data_2fbfd8,data_2fc440;
void R_num(void*,const char*,void*);
void set_profile_dirty(unsigned char);
int save_settings(void*,void*);
bool popup_is_open(char*);
}
namespace hudeditor {
struct Number{void* unused[2];const char* label;const char* format;float step,scale,min,max,value,saved;};
struct Slot{void(*fn)(void*);int priority,reserved;};
struct Descriptor{char prefix[0x48];Slot events[27];void(*settings)(void*,void*);void(*visit)(void*);void(*status)(void*);};
struct RenderEvent{long long type;void* context;InjectionNoticeRenderer* ui;float dt;};
struct OverlayEntry{void* module;char text[20],suffix[32];float w,h,anim;unsigned char alive;char pad[7];};
struct Note{int kind;char text[104];float age;};
static_assert(sizeof(OverlayEntry)==80&&sizeof(Note)==112,"HUD preview state ABI");
extern "C" OverlayEntry data_2fb000[50];
extern "C" Note data_2fbfe0[10];
struct Toggle{long long type;void* context;void* module;unsigned char on;};
struct Input{int type,code,y;unsigned char down;};
struct Rect{float x,y,w,h;};
static Number offsets[3][2]={
    {{{},"Module list X","%.3f",.001f,1,-1,1,0,0},{{},"Module list Y","%.3f",.001f,1,-1,1,0,0}},
    {{{},"Notifications X","%.3f",.001f,1,-1,1,0,0},{{},"Notifications Y","%.3f",.001f,1,-1,1,0,0}},
    {{{},"Block counter X","%.3f",.001f,1,-1,1,0,0},{{},"Block counter Y","%.3f",.001f,1,-1,1,0,0}}
};
static void(*renderers[2])(void*);static void(*guiToggle)(void*);static void(*guiVisit)(void*);
static bool installed,wasDown;static int dragging=-1;static float grabX,grabY;
static long activeFlag,closeFlag;static void* lastManager;static void* lastProfiles;
static float sizes[3][2]={{210,116},{260,42},{110,24}};
static float counterWidth=.1375f,counterHeight=.04f,counterGap=.035f;static int counterCount=64;
static long read(long* p){
#if defined(_MSC_VER) && !defined(__clang__)
    return _InterlockedCompareExchange(p,0,0);
#else
    return __atomic_load_n(p,__ATOMIC_ACQUIRE);
#endif
}
static void write(long* p,long value){
#if defined(_MSC_VER) && !defined(__clang__)
    _InterlockedExchange(p,value);
#else
    __atomic_store_n(p,value,__ATOMIC_RELEASE);
#endif
}
static bool active(){return read(&activeFlag)!=0;}
static float clamp(float value,float lo,float hi){return value<lo?lo:value>hi?hi:value;}
static NVGcolor rgba(float r,float g,float b,float a){NVGcolor result;result.r=r;result.g=g;result.b=b;result.a=a;return result;}
static NVGcolor accent(char* state,float alpha=1){auto c=(float*)(state+0x360);return rgba(clamp(c[0],0,1),clamp(c[1],0,1),clamp(c[2],0,1),alpha);}
static bool inside(Rect r,float x,float y){return x>=r.x&&y>=r.y&&x<=r.x+r.w&&y<=r.y+r.h;}
static Rect footer(InjectionNoticeRenderer* r){return {(r->scaledWidth-132)/2,r->scaledHeight-56,132,34};}
static Rect bounds(unsigned widget,float width,float height){
    float w=sizes[widget][0],h=sizes[widget][1];w=clamp(w,1,width);h=clamp(h,1,height-90>1?height-90:1);
    float x=widget==2?(width-w)/2:widget?rdata_151e64:width-w,y=widget==2?height*(.5f+counterGap):widget?rdata_151e64:0;
    return {clamp(x+offsets[widget][0].value*width,0,width-w),clamp(y+offsets[widget][1].value*height,0,height-h>64?height-h-64:0),w,h};
}
static void move(unsigned widget,float x,float y,float width,float height){
    Rect box=bounds(widget,width,height);x=clamp(x,0,width-box.w);y=clamp(y,0,height-box.h>64?height-box.h-64:0);
    float baseX=widget==2?(width-box.w)/2:widget?rdata_151e64:width-box.w,baseY=widget==2?height*(.5f+counterGap):widget?rdata_151e64:0;
    float nx=(x-baseX)/width,ny=(y-baseY)/height;
    float dx=nx-offsets[widget][0].value,dy=ny-offsets[widget][1].value;
    if(dx>1e-6f||dx< -1e-6f||dy>1e-6f||dy< -1e-6f){offsets[widget][0].value=nx;offsets[widget][1].value=ny;set_profile_dirty(1);}
}
static void close(bool save){write(&activeFlag,0);write(&closeFlag,0);dragging=-1;wasDown=false;if(save&&lastManager)save_settings(lastManager,lastProfiles);}
static void reset(){for(auto& widget:offsets)for(auto& axis:widget)axis.value=0;dragging=-1;set_profile_dirty(1);}
static void box(NVGcontext* vg,Rect r,NVGcolor fill,float radius){nvgBeginPath(vg);nvgRoundedRect(vg,r.x,r.y,r.w,r.h,radius);nvgFillColor(vg,fill);nvgFill(vg);}
static void text(NVGcontext* vg,float x,float y,const char* value,float size,NVGcolor color,int align=NVG_ALIGN_LEFT|NVG_ALIGN_MIDDLE){nvgFontSize(vg,size);nvgTextAlign(vg,align);nvgFillColor(vg,color);nvgText(vg,x,y,value,nullptr);}
static void border(NVGcontext* vg,Rect r,NVGcolor color,float radius=8){nvgBeginPath(vg);nvgRoundedRect(vg,r.x,r.y,r.w,r.h,radius);nvgStrokeColor(vg,color);nvgStrokeWidth(vg,1.2f);nvgStroke(vg);}
static void button(NVGcontext* vg,Rect r,const char* label,NVGcolor theme,bool hot){box(vg,r,rgba(.065f,.065f,.075f,.96f),17);if(hot){auto glow=theme;glow.a=.13f;box(vg,r,glow,17);}border(vg,r,rgba(theme.r,theme.g,theme.b,hot?.85f:.28f),17);text(vg,r.x+r.w/2,r.y+r.h/2,label,13,rgba(.94f,.94f,.96f,1),NVG_ALIGN_CENTER|NVG_ALIGN_MIDDLE);}
static void copyText(char* target,const char* source,unsigned capacity){unsigned n=0;while(source[n]&&n+1<capacity){char c=source[n];target[n]=data_2ed1d8&&c>='A'&&c<='Z'?c+('a'-'A'):c;++n;}target[n]=0;}
// Scratch snapshots allow the unmodified gameplay renderers to produce a
// frozen editor frame without aging/reordering the live module or toast queues.
struct OverlayPreview {
    OverlayEntry saved[50];unsigned long long count;unsigned char dirty;float fade;
    OverlayPreview():count(data_2faff8),dirty(data_2faff5),fade(data_2fbfa0){
        for(unsigned n=0;n<50;++n)saved[n]=data_2fb000[n];
        unsigned visible=0;for(unsigned n=0;n<count&&n<50;++n)if(saved[n].alive){auto entry=saved[n];entry.anim=1;entry.alive=1;data_2fb000[visible++]=entry;}
        if(!visible){const char* names[]={"Knockback Displace","Bow Aimbot","Player ESP"};
            for(unsigned n=0;n<3;++n){OverlayEntry entry={};entry.module=data_2ecef0;entry.anim=1;entry.alive=1;copyText(entry.text,names[n],20);if(n==0&&data_2ed1f8)copyText(entry.suffix,"90",32);data_2fb000[visible++]=entry;}}
        data_2faff8=visible;data_2faff5=1;data_2fbfa0=0;
    }
    ~OverlayPreview(){for(unsigned n=0;n<50;++n)data_2fb000[n]=saved[n];data_2faff8=count;data_2faff5=dirty;data_2fbfa0=fade;}
};
struct NotePreview {
    Note saved;unsigned long long count,head;unsigned char hidden;
    NotePreview():saved(data_2fbfe0[0]),count(data_2fbfd8),head(data_2fc440),hidden(data_2ed628){
        Note sample={};if(count&&count<=10)sample=data_2fbfe0[(head+count-1)%10];else{sample.kind=1;const char* message="Player ESP enabled";for(unsigned n=0;message[n];++n)sample.text[n]=message[n];}
        sample.age=(float)((rdata_151e68+rdata_151e78)*.5);data_2fbfe0[0]=sample;data_2fbfd8=1;data_2fc440=0;data_2ed628=0;
    }
    ~NotePreview(){data_2fbfe0[0]=saved;data_2fbfd8=count;data_2fc440=head;data_2ed628=hidden;}
};
static void preview(unsigned widget,InjectionNoticeRenderer* r,bool measure){
    if(widget==2){
        sizes[2][0]=counterWidth*r->scaledWidth;sizes[2][1]=counterHeight*r->scaledHeight;
        if(measure)return;
        auto vg=(NVGcontext*)r->ig;Rect rect=bounds(2,r->scaledWidth,r->scaledHeight);
        box(vg,rect,rgba(.09f,.075f,.115f,.88f),rect.h*.3125f);
        float icon=rect.h*.65f;box(vg,{rect.x+rect.h*.25f,rect.y+(rect.h-icon)/2,icon,icon},rgba(.65f,.64f,.68f,1),2);
        char label[32];unsigned n=0;int count=counterCount;char digits[12];unsigned used=0;
        do{digits[used++]=(char)('0'+count%10);count/=10;}while(count&&used<11);
        while(used)label[n++]=digits[--used];const char* suffix=counterCount==1?" block":" blocks";
        for(unsigned i=0;suffix[i];++i)label[n++]=suffix[i];label[n]=0;
        text(vg,rect.x+rect.h*1.375f,rect.y+rect.h*.53f,label,rect.h*.56f,rgba(.96f,.945f,.965f,1));
        return;
    }
    if(!renderers[widget])return;auto vg=(NVGcontext*)r->ig;RenderEvent event={10,nullptr,r,0};
    nvgSave(vg);if(measure)nvgScissor(vg,0,0,0,0);else nvgTranslate(vg,offsets[widget][0].value*r->scaledWidth,offsets[widget][1].value*r->scaledHeight);
    if(widget==0){OverlayPreview snapshot;renderers[0](&event);float width=1,height=0;for(unsigned n=0;n<data_2faff8&&n<50;++n){auto& entry=data_2fb000[n];float row=entry.w+data_2ed2e0*2;if(row>width)width=row;height+=entry.h+data_2ed318*2;}sizes[0][0]=width;sizes[0][1]=height>0?height:1;}
    else{NotePreview snapshot;nvgFontFaceId(vg,r->font[0]);nvgFontSize(vg,rdata_151e60);sizes[1][0]=nvgTextBounds(vg,0,0,data_2fbfe0[0].text,nullptr,nullptr)+rdata_151ea0;sizes[1][1]=42;renderers[1](&event);}
    nvgRestore(vg);
}
template<unsigned I>static void render(void* event){auto* ev=(RenderEvent*)event;if(!renderers[I])return;
    if(active())return;auto r=ev->ui;if(!r||!r->ig)return;
    float x=offsets[I][0].value*r->scaledWidth,y=offsets[I][1].value*r->scaledHeight;
    if(x==0&&y==0){renderers[I](event);return;}
    auto vg=(NVGcontext*)r->ig;nvgSave(vg);nvgTranslate(vg,x,y);renderers[I](event);nvgRestore(vg);
}
static void visit(void* out){if(guiVisit)guiVisit(out);R_num(out,"hud_module_list_x",&offsets[0][0]);R_num(out,"hud_module_list_y",&offsets[0][1]);R_num(out,"hud_notifications_x",&offsets[1][0]);R_num(out,"hud_notifications_y",&offsets[1][1]);R_num(out,"hud_scaffold_counter_x",&offsets[2][0]);R_num(out,"hud_scaffold_counter_y",&offsets[2][1]);}
static void toggle(void* event){auto ev=(Toggle*)event;if(ev->module==data_2eb740&&!ev->on)close(false);if(guiToggle)guiToggle(event);}
}
extern "C" void hud_editor_install(){using namespace hudeditor;if(installed)return;installed=true;
    auto list=(Descriptor*)data_2ecef0;auto notes=(Descriptor*)data_2ed320;auto gui=(Descriptor*)data_2eb740;
    renderers[0]=list->events[10].fn;renderers[1]=notes->events[10].fn;list->events[10].fn=render<0>;notes->events[10].fn=render<1>;
    guiVisit=gui->visit;gui->visit=visit;guiToggle=gui->events[5].fn;gui->events[5].fn=toggle;
}
extern "C" bool hud_editor_input(void* raw){using namespace hudeditor;auto e=(Input*)raw;if(!active()||!e)return false;if(e->type==2&&e->down&&e->code==0x57){write(&closeFlag,1);return true;}return e->type==3;}
extern "C" bool hud_editor_frame(void* rawState,void* rawRenderer,void* manager,void* profiles){using namespace hudeditor;auto state=(char*)rawState;auto r=(InjectionNoticeRenderer*)rawRenderer;
    if(!state||!r||!r->ig||r->scaledWidth<1||r->scaledHeight<1||r->scale<=0)return false;
    lastManager=manager;lastProfiles=profiles;
    if(read(&closeFlag)){close(true);state[0x4c9]=state[0x4c8];*(double*)(state+0x498)=r->time;return true;}
    float mx=*(float*)(state+0x4b0)/r->scale,my=*(float*)(state+0x4b4)/r->scale;bool down=state[0x4c8]!=0;
    if(!active()){
        int input=*(int*)(state+0x370);
        if(!popup_is_open(state)&&(input==0||input==3)&&down&&!state[0x4c9]&&inside(footer(r),mx,my)){
            write(&activeFlag,1);wasDown=true;dragging=-1;state[0x4c9]=1;*(int*)(state+0x370)=0;
        }else return false;
    }
    auto vg=(NVGcontext*)r->ig;NVGcolor theme=accent(state);float width=r->scaledWidth,height=r->scaledHeight;
    preview(0,r,true);preview(1,r,true);preview(2,r,true);
    for(unsigned widget=0;widget<3;++widget){auto rect=bounds(widget,width,height);move(widget,rect.x,rect.y,width,height);}
    bool pressed=down&&!wasDown;Rect done={width/2+8,height-56,112,34},resetButton={width/2-120,height-56,112,34};
    if(pressed&&inside(done,mx,my)){close(true);state[0x4c9]=1;*(double*)(state+0x498)=r->time;return true;}
    if(pressed&&inside(resetButton,mx,my)){reset();pressed=false;}
    if(!down)dragging=-1;
    if(pressed)for(int widget=2;widget>=0;--widget){auto rect=bounds(widget,width,height);if(inside(rect,mx,my)){dragging=widget;grabX=mx-rect.x;grabY=my-rect.y;break;}}
    if(down&&dragging>=0)move((unsigned)dragging,mx-grabX,my-grabY,width,height);
    nvgSave(vg);nvgResetScissor(vg);nvgGlobalAlpha(vg,1);nvgFontFaceId(vg,r->font[0]);
    box(vg,{0,0,width,height},rgba(0,0,0,.18f),0);
    text(vg,width/2,28,"Edit HUD",18,rgba(1,1,1,1),NVG_ALIGN_CENTER|NVG_ALIGN_MIDDLE);
    text(vg,width/2,49,"Drag a preview to move it. Esc saves and returns to the menu.",12,rgba(.72f,.73f,.77f,1),NVG_ALIGN_CENTER|NVG_ALIGN_MIDDLE);
    nvgBeginPath(vg);nvgMoveTo(vg,width/2,70);nvgLineTo(vg,width/2,height-72);nvgMoveTo(vg,12,height/2);nvgLineTo(vg,width-12,height/2);auto guide=theme;guide.a=.16f;nvgStrokeColor(vg,guide);nvgStrokeWidth(vg,1);nvgStroke(vg);
    for(unsigned widget=0;widget<3;++widget){preview(widget,r,false);auto rect=bounds(widget,width,height);
        if(dragging==(int)widget||inside(rect,mx,my)){auto outline=theme;outline.a=dragging==(int)widget?1.f:.5f;border(vg,{rect.x-3,rect.y-3,rect.w+6,rect.h+6},outline);
            text(vg,rect.x,rect.y>17?rect.y-11:rect.y+rect.h+13,widget==2?"Block counter":widget?"Notifications":"Module list",11,theme);}}
    button(vg,resetButton,"Reset positions",theme,inside(resetButton,mx,my));button(vg,done,"Done",theme,inside(done,mx,my));
    nvgRestore(vg);wasDown=down;state[0x4c9]=(char)down;*(double*)(state+0x498)=r->time;return true;
}
extern "C" void hud_editor_button(void* rawState,void* rawRenderer){using namespace hudeditor;auto state=(char*)rawState;auto r=(InjectionNoticeRenderer*)rawRenderer;if(active()||!state||!r||!r->ig||r->scale<=0||popup_is_open(state))return;
    auto vg=(NVGcontext*)r->ig;auto rect=footer(r);auto theme=accent(state);float mx=*(float*)(state+0x4b0)/r->scale,my=*(float*)(state+0x4b4)/r->scale;
    nvgSave(vg);nvgResetScissor(vg);nvgGlobalAlpha(vg,1);nvgFontFaceId(vg,r->font[0]);button(vg,rect,"Edit HUD",theme,inside(rect,mx,my));
    for(int y=0;y<2;++y)for(int x=0;x<2;++x){Rect icon={rect.x+13+x*6.f,rect.y+12+y*6.f,4,4};box(vg,icon,theme,1);}
    nvgRestore(vg);
}

extern "C" void hud_editor_scaffold(float* layout){using namespace hudeditor;
    if(!layout)return;
    if(layout[0]>0&&layout[0]<=1&&layout[1]>0&&layout[1]<=1){
        counterWidth=layout[0];counterHeight=layout[1];
        if(layout[2]>=0&&layout[2]<.5f)counterGap=layout[2];
        counterCount=layout[3]>=0&&layout[3]<=100000?(int)layout[3]:0;
    }
    layout[0]=offsets[2][0].value;layout[1]=offsets[2][1].value;
}
