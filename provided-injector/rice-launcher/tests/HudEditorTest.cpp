#include <stdexcept>
#include <iostream>
#include <string>
#include <vector>
#include <cmath>
#include <cstring>
#include "slinky/gui_viewport.h"
#include "../../../slinkyreadablesource/slinkyreadablesource/src/engine/gui/hud_editor.cpp"
extern "C" {
alignas(8) char data_2eb740[0x210]={},data_2ecef0[0x210]={},data_2ed320[0x210]={};
extern const float rdata_151e64=12;
extern const float rdata_151e60=13,rdata_151ea0=20;
extern const double rdata_151e68=.15,rdata_151e78=3;
float data_2ed2e0=5,data_2ed318=2,data_2fbfa0=.7f;
unsigned char data_2faff5=0,data_2ed1d8=0,data_2ed1f8=1,data_2ed628=1;
unsigned long long data_2faff8=0,data_2fbfd8=0,data_2fc440=4;
}
namespace hudeditor {extern "C" {OverlayEntry data_2fb000[50]={};Note data_2fbfe0[10]={};}}
static int saves,restores,draws,renderCalls[2],guiVisits,toggles,dirty,persisted;
static float translatedX,translatedY;
static float gameTextWidth=200,gameFontHeight=14;
static bool popup;
static bool wrongOwner;
static std::vector<NVGcolor> colors;
static std::vector<std::string> labels,keys;
extern "C" void R_num(void*,const char* name,void*){keys.emplace_back(name);}
extern "C" void set_profile_dirty(unsigned char){++dirty;}
extern "C" int save_settings(void* manager,void* profiles){wrongOwner|=manager!=(void*)10||profiles!=(void*)20;++persisted;return wrongOwner?1:0;}
extern "C" bool popup_is_open(char*){return popup;}
extern "C" void nvgSave(NVGcontext*){++saves;}
extern "C" void nvgRestore(NVGcontext*){++restores;}
extern "C" void nvgTranslate(NVGcontext*,float x,float y){translatedX=x;translatedY=y;}
extern "C" void nvgBeginPath(NVGcontext*){}
extern "C" void nvgRoundedRect(NVGcontext*,float,float,float,float,float){++draws;}
extern "C" void nvgFillColor(NVGcontext*,NVGcolor c){colors.push_back(c);}
extern "C" void nvgFill(NVGcontext*){}
extern "C" void nvgFontSize(NVGcontext*,float){}
extern "C" void nvgTextAlign(NVGcontext*,int){}
extern "C" float nvgText(NVGcontext*,float,float,const char* text,const char*){labels.emplace_back(text);return 0;}
extern "C" void nvgStrokeColor(NVGcontext*,NVGcolor c){colors.push_back(c);}
extern "C" void nvgStrokeWidth(NVGcontext*,float){}
extern "C" void nvgStroke(NVGcontext*){}
extern "C" void nvgResetScissor(NVGcontext*){}
extern "C" void nvgScissor(NVGcontext*,float,float,float,float){}
extern "C" float nvgTextBounds(NVGcontext*,float,float,const char*,const char*,float*){return 240;}
extern "C" void nvgGlobalAlpha(NVGcontext*,float){}
extern "C" void nvgFontFaceId(NVGcontext*,int){}
extern "C" void nvgMoveTo(NVGcontext*,float,float){}
extern "C" void nvgLineTo(NVGcontext*,float,float){}
static void list(void*){++renderCalls[0];for(unsigned n=0;n<data_2faff8&&n<50;++n){hudeditor::data_2fb000[n].w=gameTextWidth-n*30;hudeditor::data_2fb000[n].h=gameFontHeight;labels.emplace_back(hudeditor::data_2fb000[n].text);}data_2faff5=0;}
static void notes(void*){++renderCalls[1];if(data_2fbfd8)labels.emplace_back(hudeditor::data_2fbfe0[data_2fc440%10].text);}
static void visitGui(void*){++guiVisits;}static void toggleGui(void*){++toggles;}
static void check(bool ok,const char* text){if(!ok)throw std::runtime_error(text);}
static bool near(float a,float b){return std::abs(a-b)<.001f;}
static bool hasColor(float r,float g,float b){for(auto color:colors)if(near(color.r,r)&&near(color.g,g)&&near(color.b,b))return true;return false;}
int main(){try{using namespace hudeditor;
    for(float h : {300.f,600.f,1080.f}){
        float bottom=guiviewport::contentHeight(h);
        check(bottom<=h-56.f-20.f,"Settings viewport must clear the Edit HUD footer");
        check(guiviewport::contains(bottom-1,h)&&!guiviewport::contains(bottom,h)&&!guiviewport::contains(h-39,h),"Footer clicks must never target scrolled settings");
        float lastRowBottom=1600.f,scroll=lastRowBottom-bottom;
        check(guiviewport::contains(lastRowBottom-1-scroll,h),"Last setting must remain reachable above footer after scrolling");
    }
    check(guiviewport::contentHeight(50)==0&&!guiviewport::contains(0,50),"Tiny windows must not create negative viewports");
    auto gui=(Descriptor*)data_2eb740;auto overlay=(Descriptor*)data_2ecef0;auto notifications=(Descriptor*)data_2ed320;
    gui->visit=visitGui;gui->events[5].fn=toggleGui;overlay->events[10].fn=list;notifications->events[10].fn=notes;hud_editor_install();hud_editor_install();
    InjectionNoticeRenderer renderer{};renderer.ig=(void*)1;renderer.scale=2;renderer.scaledWidth=800;renderer.scaledHeight=600;renderer.time=10;
    char state[0x4d0]={};auto theme=(float*)(state+0x360);theme[0]=.8f;theme[1]=.35f;theme[2]=.6f;theme[3]=1;
    auto pointer=[&](float x,float y,bool down){*(float*)(state+0x4b0)=x*renderer.scale;*(float*)(state+0x4b4)=y*renderer.scale;state[0x4c8]=(char)down;};
    auto frame=[&](){renderer.time+=.016;return hud_editor_frame(state,&renderer,(void*)10,(void*)20);};
    check(!frame(),"Closed editor must leave original menu visible");hud_editor_button(state,&renderer);check(!labels.empty()&&labels.back()=="Edit HUD"&&hasColor(.8f,.35f,.6f),"Button must use current GUI accent");
    colors.clear();theme[0]=.15f;theme[1]=.6f;theme[2]=1;hud_editor_button(state,&renderer);check(hasColor(.15f,.6f,1),"Button accent must update immediately with GUI theme");
    RenderEvent event={10,nullptr,&renderer};overlay->events[10].fn(&event);notifications->events[10].fn(&event);check(renderCalls[0]==1&&renderCalls[1]==1&&translatedX==0,"Default HUD rendering must delegate unchanged");
    popup=true;pointer(400,561,true);check(!frame()&&!active(),"Modal popups must block the HUD footer");int beforePopup=draws;hud_editor_button(state,&renderer);check(draws==beforePopup,"HUD footer must stay below modal popups");popup=false;pointer(400,561,false);frame();
    std::strcpy(data_2fb000[0].text,"Old cached row");std::strcpy(data_2fbfe0[0].text,"Old queued toast");data_2fbfe0[0].age=2;
    hudeditor::OverlayEntry savedRows[50];hudeditor::Note savedNotes[10];std::memcpy(savedRows,data_2fb000,sizeof savedRows);std::memcpy(savedNotes,data_2fbfe0,sizeof savedNotes);
    *(int*)(state+0x370)=3;pointer(400,561,true);check(frame()&&active()&&*(int*)(state+0x370)==0,"Scaled footer hit must open editor and release old text capture");int previewCalls=renderCalls[0];overlay->events[10].fn(&event);check(previewCalls>1&&renderCalls[0]==previewCalls,"Editor must use original renderers for previews without an extra live render pass");
    check(std::memcmp(savedRows,data_2fb000,sizeof savedRows)==0&&std::memcmp(savedNotes,data_2fbfe0,sizeof savedNotes)==0&&data_2faff8==0&&data_2fbfd8==0&&data_2fc440==4&&data_2faff5==0&&near(data_2fbfa0,.7f)&&data_2ed628==1,"Rendering previews must restore live cache, toast ring, visibility, and animation state");
    check(near(bounds(0,800,600).w,210)&&near(bounds(0,800,600).h,54)&&near(bounds(1,800,600).h,42),"Drag bounds must use actual renderer text metrics/padding and toast size");
    pointer(400,561,false);frame();auto original=bounds(0,800,600);pointer(original.x+20,original.y+22,true);frame();check(dragging==0,"Module list must be draggable");
    pointer(320,202,true);frame();check(near(offsets[0][0].value,-.3625f)&&near(offsets[0][1].value,.3f)&&dirty>0,"Drag must store normalized offsets");
    pointer(-1000,-1000,true);frame();auto clipped=bounds(0,800,600);check(clipped.x==0&&clipped.y==0,"Drag must clamp to screen");
    pointer(320,202,true);frame();pointer(320,202,false);frame();
    auto noteBox=bounds(1,800,600);pointer(noteBox.x+10,noteBox.y+15,true);frame();check(dragging==1,"Notifications must be independently draggable");pointer(110,365,true);frame();pointer(110,365,false);frame();
    float counterData[4]={.12f,.04f,.035f,128};hud_editor_scaffold(counterData);frame();
    auto counterBox=bounds(2,800,600);check(near(counterBox.w,96)&&near(counterBox.h,24),"Block counter bounds must use game HUD metrics");
    pointer(counterBox.x+8,counterBox.y+8,true);frame();check(dragging==2,"Block counter must be independently draggable");
    pointer(208,408,true);frame();pointer(208,408,false);frame();
    float savedCounterX=offsets[2][0].value,savedCounterY=offsets[2][1].value;
    check(near(bounds(2,800,600).x,200)&&near(bounds(2,800,600).y,400),"Block counter drag must move the full card");
    float liveLayout[4]={.12f,.04f,.035f,128};hud_editor_scaffold(liveLayout);
    check(near(liveLayout[0],savedCounterX)&&near(liveLayout[1],savedCounterY),"Gameplay counter must receive saved normalized offsets");
    Input escape={2,0x57,0,1};check(hud_editor_input(&escape),"Esc must be consumed while editing");frame();check(!active()&&persisted==1,"Esc must save and return to the menu");check(!hud_editor_input(&escape),"Esc outside editor must retain original GUI behavior");
    translatedX=translatedY=0;overlay->events[10].fn(&event);check(near(translatedX,-290)&&near(translatedY,180),"Saved layout must translate actual HUD renderer");
    notifications->events[10].fn(&event);check(near(translatedX,88)&&near(translatedY,338),"Notification renderer must use its own saved position");
    renderer.scaledWidth=400;renderer.scaledHeight=300;overlay->events[10].fn(&event);check(near(translatedX,-145)&&near(translatedY,90),"Layout must remain proportional after resolution change");renderer.scaledWidth=800;renderer.scaledHeight=600;
    gui->visit(nullptr);check(guiVisits==1&&keys.size()==6&&keys[0]=="hud_module_list_x"&&keys[3]=="hud_notifications_y"&&keys[4]=="hud_scaffold_counter_x"&&keys[5]=="hud_scaffold_counter_y","Profile registration must preserve original settings and save all HUD coordinates");
    pointer(400,561,true);state[0x4c9]=0;frame();pointer(400,561,false);frame();pointer(336,561,true);frame();check(offsets[0][0].value==0&&offsets[0][1].value==0&&offsets[2][0].value==0&&offsets[2][1].value==0,"Reset positions must restore defaults");pointer(336,561,false);frame();pointer(464,561,true);frame();check(!active()&&persisted==2,"Done must save without closing ClickGUI");
    pointer(400,561,false);frame();pointer(400,561,true);state[0x4c9]=0;frame();Toggle off={5,nullptr,data_2eb740,0};gui->events[5].fn(&off);check(!active()&&toggles==1,"Closing GUI must terminate editor and preserve original toggle callback");
    data_2faff8=1;data_2fb000[0].alive=1;data_2fb000[0].anim=.25f;std::strcpy(data_2fb000[0].text,"Actual active mod");labels.clear();pointer(400,561,false);frame();pointer(400,561,true);state[0x4c9]=0;frame();bool actual=false;for(auto& label:labels)actual|=label=="Actual active mod";check(actual&&data_2faff8==1&&near(data_2fb000[0].anim,.25f),"Live module contents must be previewed without changing animation state");
    gameTextWidth=320;gameFontHeight=22;data_2ed2e0=10;data_2ed318=4;frame();check(near(sizes[0][0],340)&&near(sizes[0][1],30),"Preview hit area must follow gameplay font and padding metrics");
    data_2fbfd8=2;data_2fc440=4;data_2fbfe0[5].kind=2;std::strcpy(data_2fbfe0[5].text,"Actual recent notification");data_2fbfe0[5].age=.2f;labels.clear();frame();bool recent=false;for(auto& label:labels)recent|=label=="Actual recent notification";check(recent&&data_2fbfd8==2&&data_2fc440==4&&near(data_2fbfe0[5].age,.2f),"Real toast contents must be previewed without consuming/aging the queue");
    gui->events[5].fn(&off);
    check(saves==restores&&!wrongOwner,"HUD editor must balance NanoVG state and save to the correct settings owner");
    std::cout<<"HUD footer/theme, scaled input, drag/clamping, normalized layouts, renderer handoff, save/reset/Esc, profile registration, and state restoration passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}
