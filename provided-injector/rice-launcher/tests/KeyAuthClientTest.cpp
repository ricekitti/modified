#include "../src/KeyAuthClient.cpp"
#include <iostream>
#include <stdexcept>
namespace fixture {int calls=0;std::string init="{\"success\":true,\"ownerid\":\"UVLPXieCUS\",\"sessionid\":\"test-session\"}";
std::string license="{\"success\":true,\"ownerid\":\"UVLPXieCUS\",\"username\":\"test-user\"}",form;bool transportFail=false;}
namespace soju {
std::string keyAuthPost(const std::string& form,std::wstring& error){fixture::form=form;++fixture::calls;
    if(form.find("type=init&")==0)return fixture::init;
    if(fixture::transportFail){error=L"Connection interrupted (WinINet 12030)";return {};}
    return fixture::license;
}}
void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
int main(){try{soju::KeyAuthClient client;
    auto result=client.authenticateLicense(L"test key&+");check(result.success&&fixture::calls==2,"License needs initialization and validation");
    check(fixture::form.find("type=license&key=test%20key%26%2B&sessionid=test-session")==0&&fixture::form.find("&hwid=")!=std::string::npos,"Encoded key, session, HWID");
    check(fixture::form.find("&name=Soju&ownerid=UVLPXieCUS")!=std::string::npos,"Soju identity");
    for(const std::string body:{
        "{\"success\":false,\"ownerid\":\"UVLPXieCUS\",\"message\":\"Invalid license\"}",
        "{\"success\":true,\"ownerid\":\"WRONG_OWNER\"}",
        "{\"info\":{\"success\":true},\"ownerid\":\"UVLPXieCUS\"}",
        "{\"success\":\"true\",\"ownerid\":\"UVLPXieCUS\"}",
        "{\"success\":true,\"success\":false,\"ownerid\":\"UVLPXieCUS\"}",
        "{\"success\":truex,\"ownerid\":\"UVLPXieCUS\"}",
        "{\"success\":true,\"ownerid\":\"UVLPXieCUS\",}",
        "{\"success\":true,\"ownerid\":\"UVLPXieCUS\",\"broken\":[}",
        "{\"success\":true,\"ownerid\":\"UVLPXieCUS\"} trailing"}) {
        fixture::license=body;check(!client.authenticateLicense(L"test-key").success,"Invalid response must fail closed");
    }
    fixture::transportFail=true;result=client.authenticateLicense(L"test-key");check(!result.success&&result.message.find(L"12030")!=std::wstring::npos,"Transport diagnostics");
    fixture::transportFail=false;
    for(const std::string body:{"{\"success\":false,\"ownerid\":\"UVLPXieCUS\"}","{\"success\":true,\"ownerid\":\"UVLPXieCUS\"}","{\"success\":true,\"ownerid\":\"WRONG\",\"sessionid\":\"s\"}","{\"success\":true"}) {
        fixture::init=body;int previous=fixture::calls;check(!client.authenticateLicense(L"test-key").success&&fixture::calls==previous+1,"Bad init cannot reach license request");
    }
    soju::keyauth::JsonResponse parsed("{\"success\":true,\"message\":\"hello \\u2665\",\"other\":[null,1,-2.5e+3,{}]}");
    check(parsed.success()&&parsed.string("message")=="hello \xe2\x99\xa5","Unicode and nested JSON");
    std::cout<<"Soju identity, license/HWID flow, strict JSON, owner validation, and fail-closed authentication passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}

