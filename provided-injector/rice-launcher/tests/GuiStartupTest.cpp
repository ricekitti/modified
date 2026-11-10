#include <stdexcept>
#include <iostream>
#include <vector>
struct Module {char header[0x14];unsigned char enabled;char pad[3];unsigned flags;};
struct Modules {Module** data;unsigned long long count;};
extern "C" {alignas(8) char data_2eb740[sizeof(Module)]={};}
extern "C" void enable_startup_modules(void*);
static std::vector<void*> queued;
extern "C" bool queue_module_toggle(void*,void* module,unsigned char on){
    if(on!=1)throw std::runtime_error("Unexpected startup disable");queued.push_back(module);return true;
}
static void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
int main(){try{
    auto* gui=reinterpret_cast<Module*>(data_2eb740);gui->flags=0x1e;
    Module helper={},alreadyEnabled={},optional={};helper.flags=2;alreadyEnabled.flags=2;alreadyEnabled.enabled=1;
    Module* all[]={gui,&helper,&alreadyEnabled,&optional};Modules modules={all,4};
    enable_startup_modules(&modules);
    check(queued.size()==1&&queued[0]==&helper,"Startup must enable helpers without opening GUI or optional modules");
    check(gui->enabled==0&&gui->flags==0x1e,"GUI state/settings flags must remain intact for normal input");
    helper.enabled=1;queued.clear();enable_startup_modules(&modules);
    check(queued.empty(),"Joining again must not reopen GUI or reenable active helpers");
    enable_startup_modules(nullptr);
    std::cout<<"GUI stays closed on startup/join; helper startup and GUI flags preserved\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}
