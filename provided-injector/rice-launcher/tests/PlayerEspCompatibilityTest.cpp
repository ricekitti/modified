#include <cstdarg>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <stdexcept>
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/visual/player_esp_extensions.cpp"
static int classicCalls,classicMode,guiCalls,statusCalls,glowCalls,released,widgets;
static bool supported=true;
static char statusText[64];
extern "C" {
int data_2ea110=0,data_2ea2c0=0;
unsigned char data_2ea250=0,data_2ea270=0;
unsigned char data_2ea1a0[1],data_2ea238[1],data_2ea0e0[1],data_2ea290[1],
 data_2ea218[1],data_2ea1f8[1],data_2ea258[1],data_2ea1d8[1],data_2ea140[1];
extern const char rdata_150bd9[]="Main",rdata_150bde[]="Style",rdata_150c8e[]="%s";
void og_player_esp_render_mode_1(void*){++classicCalls;classicMode=data_2ea110;}
void og_player_esp_gui(void*,void*){++guiCalls;}
void og_player_esp_status(void*){++statusCalls;}
bool heavy_glow_render(void*){++glowCalls;return supported;}


void W_label(void*,void*,const char*){++widgets;}
void W_slider(void*,void*,void*){++widgets;}
void W_bool(void*,void*,void*){++widgets;}
void W_enumw(void*,void*,void*){++widgets;}
void W_color(void*,void*,void*){++widgets;}
void log_fmt(void*,int size,const char* format,...){va_list args;va_start(args,format);vsnprintf(statusText,size,format,args);va_end(args);}
}
void heavy_glow_release(){++released;}
extern "C" const char* heavy_glow_mode_name(unsigned){return "Heavy Glow";}
void check(bool ok,const char* message){if(!ok)throw std::runtime_error(message);}
int main(){try{
    data_2ea110=0;player_esp_render_mode_1(nullptr);
    check(classicCalls==1&&classicMode==0&&!glowCalls&&released==1,"2D mode must delegate original renderer behavior");
    data_2ea110=1;player_esp_render_mode_1(nullptr);
    check(classicCalls==2&&classicMode==1&&!glowCalls&&released==2,"3D mode must delegate original renderer behavior");
    player_esp_gui(nullptr,nullptr);player_esp_status(nullptr);
    check(guiCalls==1&&statusCalls==1&&!widgets,"Original settings/status must be used for classic modes");
    data_2ea110=2;player_esp_render_mode_1(nullptr);
    check(glowCalls==1&&classicCalls==2&&data_2ea110==2,"Supported Heavy Glow must use only the extension");
    supported=false;player_esp_render_mode_1(nullptr);
    check(classicCalls==3&&classicMode==1&&data_2ea110==2,"Fallback must use original 3D renderer and restore selected mode");
    player_esp_gui(nullptr,nullptr);player_esp_status(nullptr);
    check(widgets>0&&guiCalls==1&&statusCalls==1&&!std::strcmp(statusText,"Heavy Glow"),"Added mode must retain its own controls/status");
    std::cout<<"OG ESP delegation, Heavy Glow, fallback setting restoration, and UI separation passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}

