#include <cmath>
#include <iostream>
#include <stdexcept>
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/block/block_in_toggle.cpp"
namespace fixture {
int java, world, player, mc, settings, keys[4];
int rotations=0, restores=0;
bool missingSettings=false, missingKey=false;
void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
}
extern "C" {
char data_2f6108[1];
void* data_304b10;
int data_304b18, data_304b20;
unsigned char data_304b1c;
extern const float rdata_156870=90.0f;
Rot get_entity_rotation(void* a,void* p){fixture::check(a==&fixture::java&&p==&fixture::player,"Invalid player reached rotation JNI");++fixture::rotations;return {90,0};}
float libc_roundf(float x){return std::round(x);}
void* get_net_handler(void* a,void* m){fixture::check(a==&fixture::java&&m==&fixture::mc,"Invalid settings JNI");return fixture::missingSettings?nullptr:&fixture::settings;}
void* java_get_game_settings_key_bindings_4(void*,void*){return &fixture::keys[0];}
void* java_get_game_settings_key_bindings_5(void*,void*){return fixture::missingKey?nullptr:&fixture::keys[1];}
void* java_get_game_settings_key_bindings_6(void*,void*){return &fixture::keys[2];}
void* java_get_game_settings_key_bindings_7(void*,void*){return &fixture::keys[3];}
unsigned char java_get_key_binding_key_code(void*,void* k){fixture::check(k!=nullptr,"Null key query");return 1;}
void java_set_key_binding_pressed(void*,void* k,unsigned char on){fixture::check(k&&on==1,"Physical key state was not restored");++fixture::restores;}
}
int main(){using namespace fixture;try{
    block_in_enable_remember_facing(nullptr);
    EvHook event{};event.ev=data_2f6108;event.on=1;
    block_in_enable_remember_facing(&event);
    Ctx context{};event.ctx=&context;
    block_in_enable_remember_facing(&event);
    context.a=&java;
    block_in_enable_remember_facing(&event);
    context.p20=&world;
    block_in_enable_remember_facing(&event);
    context.p20=nullptr;context.b=&player;
    block_in_enable_remember_facing(&event);
    check(!rotations,"Menu/partial join must not read player rotation");
    context.p20=&world;
    block_in_enable_remember_facing(&event);
    check(rotations==1&&data_304b18==1,"Joined activation must remember facing");
    data_304b10=&player;data_304b1c=1;data_304b20=2;
    context.b=context.p20=nullptr;event.on=0;
    block_in_enable_remember_facing(&event);
    check(!data_304b10&&!data_304b1c&&!data_304b20&&!restores,"Disconnect must clear placement without missing settings JNI");
    context.c18=&mc;
    block_in_enable_remember_facing(&event);
    check(restores==4,"Disconnect must restore keys when settings remain available");
    missingSettings=true;block_in_enable_remember_facing(&event);check(restores==4,"Missing settings must be safe");
    missingSettings=false;missingKey=true;block_in_enable_remember_facing(&event);check(restores==7,"Missing key must be skipped");
    char other;event.ev=&other;event.on=1;block_in_enable_remember_facing(&event);check(rotations==1,"Other module toggles must be ignored");
    std::cout<<"Block In menu/partial join activation, facing, disconnect cleanup, and key restoration passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}

