// Rice's added mode wraps the original ESP. Classic modes delegate unchanged.
#include "slinky/api.h"
#include "slinky/heavy_glow.h"
extern "C" int data_2ea110;
extern "C" void og_player_esp_render_mode_1(void*);
extern "C" void og_player_esp_gui(void*,void*);
extern "C" void og_player_esp_status(void*);
extern "C" bool heavy_glow_render(void*);
extern "C" void player_esp_render_mode_1(void* event){
    if(data_2ea110==2){
        if(!heavy_glow_render(event)){
            // The OG renderer has its own mode-1 guard. Scope the fallback to
            // that renderer and restore the user's setting before returning.
            const int selected=data_2ea110;
            data_2ea110=1;
            og_player_esp_render_mode_1(event);
            data_2ea110=selected;
        }
        return;
    }
    heavy_glow_release();
    og_player_esp_render_mode_1(event);
}
S(150bd9);S(150bde);S(150c8e);
D(2ea1a0);D(2ea238);D(2ea0e0);D(2ea290);
D(2ea218);D(2ea1f8);D(2ea258);D(2ea1d8);D(2ea140);
extern "C" unsigned char data_2ea250,data_2ea270;
extern "C" int data_2ea2c0;
extern "C" void player_esp_gui(void* a,void* b){
    if(data_2ea110!=2){og_player_esp_gui(a,b);return;}
    W_label(a,b,rdata_150bd9);W_slider(b,a,data_2ea1a0);W_bool(b,a,data_2ea238);
    W_label(a,b,rdata_150bde);W_enumw(b,a,data_2ea0e0);W_enumw(b,a,data_2ea290);
    if(data_2ea2c0==1){
        W_color(b,a,data_2ea218);
        if(!data_2ea250)W_color(b,a,data_2ea1f8);
    }
    W_bool(b,a,data_2ea258);
    if(data_2ea270)W_color(b,a,data_2ea1d8);
    W_bool(b,a,data_2ea140);
}
extern "C" void player_esp_status(void* self){
    if(data_2ea110!=2){og_player_esp_status(self);return;}
    log_fmt(self,0x20,rdata_150c8e,heavy_glow_mode_name(data_2ea110));
}

