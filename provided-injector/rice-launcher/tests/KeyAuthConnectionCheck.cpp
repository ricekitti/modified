// Manual app initialization only: no license, HWID, or injection.
#include "KeyAuthTransport.h"
#include "KeyAuthConfig.h"
#include "KeyAuthJson.h"
#include <iostream>
int main() {
    std::wstring error;
    auto body=soju::keyAuthPost(std::string("type=init&ver=")+soju::keyauth::version+"&name="+soju::keyauth::name+"&ownerid="+soju::keyauth::ownerid,error);
    if(body.empty()){std::wcerr<<error<<L'\n';return 1;}
    try {
        soju::keyauth::JsonResponse response(body);
        if(!response.success()||response.string("ownerid")!=soju::keyauth::ownerid||response.string("sessionid").empty()){
            std::cerr<<"App initialization rejected. No license was tested.\n";return 1;}
    } catch (...) {std::cerr<<"Invalid initialization response.\n";return 1;}
    std::cout<<"Soju app initialization succeeded over verified HTTPS at keyauth.win. No license or HWID sent.\n";return 0;
}

