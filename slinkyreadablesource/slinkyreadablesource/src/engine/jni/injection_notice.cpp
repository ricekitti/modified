#include "slinky/engine.h"
#include "slinky/injection_notice.h"
#if defined(_MSC_VER) && !defined(__clang__)
#include <intrin.h>
#endif
extern "C" InjectionNoticeRenderer* data_2f7280;
extern "C" int data_2eb75c; // Actual configured GUI key, including saved overrides.
extern "C" const char* mouse_button_name(int key);
static volatile unsigned char state; // 0=not successful, 1=pending, 2=visible, 3=finished.
static double started;
static bool transition(unsigned char before,unsigned char after){
#if defined(_MSC_VER) && !defined(__clang__)
    return (unsigned char)_InterlockedCompareExchange8((volatile char*)&state,(char)after,(char)before)==before;
#else
    return __atomic_compare_exchange_n(&state,&before,after,false,__ATOMIC_ACQ_REL,__ATOMIC_ACQUIRE);
#endif
}
static unsigned char current(){
#if defined(_MSC_VER) && !defined(__clang__)
    return (unsigned char)_InterlockedCompareExchange8((volatile char*)&state,0,0);
#else
    return __atomic_load_n(&state,__ATOMIC_ACQUIRE);
#endif
}
extern "C" void injection_notice_ready(){transition(0,1);}
extern "C" bool injection_notice_visible(Ctx* c,double now){
    // A startup hint must work on Lunar's title screen. Do not activate gameplay
    // modules or depend on Notifications being enabled to display this one toast.
    auto* r=data_2f7280;
    if(!c||!c->a||!c->c18||!r||!r->ig)return false;
    if(transition(1,2))started=now;
    if(current()!=2)return false;
    if(now-started>=8.0){transition(2,3);return false;}
    return true;
}
struct NoticeColor {float r,g,b,a;};
extern "C" void nvg_Save(void*);
extern "C" void nvg_Restore(void*);
extern "C" void nvg_GlobalAlpha(void*,float);
extern "C" void nvg_FontFaceId(void*,int);
extern "C" void nvg_FontSize(void*,float);
extern "C" void nvg_TextAlign(void*,int);
extern "C" float nvg_text_bounds(void*,float,float,const char*,const char*,float*);
extern "C" void nvg_FillColor(void*,NoticeColor);
extern "C" void nvg_BeginPath(void*);
extern "C" void nvg_RoundedRect(void*,float,float,float,float,float);
extern "C" void nvg_Fill(void*);
extern "C" float nvg_text(void*,float,float,const char*,const char*);
extern "C" void injection_notice_draw(void* renderer,double now){
    auto* r=static_cast<InjectionNoticeRenderer*>(renderer);
    if(!r||!r->ig||current()!=2)return;
    double age=now-started;
    if(age<0||age>=8)return;
    float alpha=age<0.2?static_cast<float>(age/0.2):
        age>7.6?static_cast<float>((8-age)/0.4):1.0f;
    const char* key=mouse_button_name(data_2eb75c);
    char hint[128];
    if(data_2eb75c&&key&&key[0])log_fmt(hint,sizeof hint,"Press %s to open GUI",key);
    else log_fmt(hint,sizeof hint,"%s","GUI key is unbound");
    void* vg=r->ig;
    nvg_Save(vg);
    nvg_GlobalAlpha(vg,alpha);
    nvg_FontFaceId(vg,r->font[0]);
    nvg_FontSize(vg,14.0f);
    nvg_TextAlign(vg,1|8); // left/top
    float width=nvg_text_bounds(vg,0,0,hint,nullptr,nullptr)+32.0f;
    if(width<260)width=260;
    const float x=20.0f,y=20.0f+(1-alpha)*8.0f;
    nvg_BeginPath(vg);nvg_RoundedRect(vg,x,y,width,66.0f,8.0f);
    nvg_FillColor(vg,{0.065f,0.06f,0.075f,0.97f});nvg_Fill(vg);
    nvg_BeginPath(vg);nvg_RoundedRect(vg,x,y,3.0f,66.0f,1.5f);
    nvg_FillColor(vg,{0.96f,0.69f,0.81f,1});nvg_Fill(vg);
    nvg_FontFaceId(vg,r->font[1]);nvg_FontSize(vg,15.0f);
    nvg_FillColor(vg,{0.96f,0.69f,0.81f,1});
    nvg_text(vg,x+16,y+12,"Slinky injected",nullptr);
    nvg_FontFaceId(vg,r->font[0]);nvg_FontSize(vg,14.0f);
    nvg_FillColor(vg,{0.91f,0.9f,0.93f,1});
    nvg_text(vg,x+16,y+37,hint,nullptr);
    nvg_Restore(vg);
}

