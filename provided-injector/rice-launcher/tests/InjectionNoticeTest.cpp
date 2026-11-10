#include "slinky/engine.h"
#include "slinky/injection_notice.h"
#include <iostream>
#include <stdexcept>
#include <string>
#include <vector>
#include <cstdarg>
#include <cstdio>
extern "C" {InjectionNoticeRenderer* data_2f7280=nullptr;int data_2eb75c=99;}
struct NoticeColor{float r,g,b,a;};
static int saves,restores,draws,boxes;
static float alpha;
static std::vector<std::string> text;
extern "C" const char* mouse_button_name(int key){return key==99?"RSHIFT":key==90?"INSERT":"";}
extern "C" void log_fmt(void* dest,int length,const char* format,...){va_list args;va_start(args,format);vsnprintf((char*)dest,length,format,args);va_end(args);}
extern "C" void nvg_Save(void*){++saves;}
extern "C" void nvg_Restore(void*){++restores;}
extern "C" void nvg_GlobalAlpha(void*,float a){alpha=a;}
extern "C" void nvg_FontFaceId(void*,int){}
extern "C" void nvg_FontSize(void*,float){}
extern "C" void nvg_TextAlign(void*,int){}
extern "C" float nvg_text_bounds(void*,float,float,const char*,const char*,float*){return 230;}
extern "C" void nvg_FillColor(void*,NoticeColor){}
extern "C" void nvg_BeginPath(void*){}
extern "C" void nvg_RoundedRect(void*,float,float,float,float,float){++boxes;}
extern "C" void nvg_Fill(void*){}
extern "C" float nvg_text(void*,float,float,const char* s,const char*){text.emplace_back(s);++draws;return 0;}
void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
int main(){try{
    int java,mc,vg,world,player;
    Ctx c{};c.a=&java;c.c18=&mc;
    InjectionNoticeRenderer renderer{};renderer.ig=&vg;renderer.width=1920;renderer.height=1080;
    data_2f7280=&renderer;
    check(!injection_notice_visible(&c,1),"Cannot claim injection before successful setup");
    injection_notice_draw(&renderer,1);check(!draws,"No pre-success rendering");
    injection_notice_ready();
    data_2f7280=nullptr;check(!injection_notice_visible(&c,20),"Missing renderer must defer start");
    data_2f7280=&renderer;renderer.ig=nullptr;check(!injection_notice_visible(&c,30),"Missing graphics context must defer start");
    renderer.ig=&vg;check(!injection_notice_visible(nullptr,40),"Missing context must defer start");
    c.c18=nullptr;check(!injection_notice_visible(&c,50),"Minecraft instance is required");
    c.c18=&mc;
    check(injection_notice_visible(&c,60),"Title screen with no world/player must display startup hint");
    check(!c.p20&&!c.b,"Test must have no world or player");
    injection_notice_draw(&renderer,60.5);
    check(draws==2&&text[0]=="Slinky injected"&&text[1]=="Press RSHIFT to open GUI","Toast must show success plus actual GUI key");
    check(alpha==1&&saves==restores&&boxes==2,"Drawing must restore NanoVG state");
    data_2eb75c=90;injection_notice_draw(&renderer,61);
    check(text.back()=="Press INSERT to open GUI","Saved/custom bind must be reflected");
    data_2eb75c=0;injection_notice_draw(&renderer,62);check(text.back().find("unbound")!=std::string::npos,"Unbound key must be explicit");
    c.p20=&world;c.b=&player;check(injection_notice_visible(&c,63),"Joining must not reset or hide active hint");
    injection_notice_ready();c.p20=c.b=nullptr;
    check(injection_notice_visible(&c,67.8),"Hint must survive menu/world transitions");
    injection_notice_draw(&renderer,67.8);check(alpha>0&&alpha<1,"Toast must fade out");
    check(!injection_notice_visible(&c,68),"Hint expires after eight visible seconds");
    int count=draws;injection_notice_draw(&renderer,69);check(draws==count,"Expired hint must stop drawing");
    injection_notice_ready();check(!injection_notice_visible(&c,100),"Repeated readiness/rejoin cannot duplicate hint");
    std::cout<<"Menu startup, success gating, renderer readiness, configured GUI key, fade, and one-time display passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}

