#include <windows.h>
#include <wininet.h>
#include <algorithm>
#include <cstring>
#include <iostream>
#include <stdexcept>
#include <string>
namespace fake {
int live=0,sends=0,reads=0;
DWORD sendError=0,status=200,readErrorAt=MAXDWORD,flags=0;
bool unsafe=false,optionFail=false;
size_t cursor=0;
std::string response,posted;
std::wstring verb,path,headers;
void reset(){live=sends=reads=0;sendError=0;status=200;readErrorAt=MAXDWORD;flags=0;unsafe=optionFail=false;cursor=0;response="{\"success\":true}";posted.clear();}
HINTERNET open(LPCWSTR,DWORD mode,LPCWSTR,LPCWSTR,DWORD){if(mode!=INTERNET_OPEN_TYPE_PRECONFIG)throw std::runtime_error("Must use user proxy settings");++live;return (HINTERNET)1;}
BOOL close(HINTERNET){--live;return TRUE;}
BOOL option(HINTERNET,DWORD name,LPVOID,DWORD){if(name==INTERNET_OPTION_SECURITY_FLAGS)unsafe=true;if(optionFail){SetLastError(ERROR_INVALID_PARAMETER);return FALSE;}return TRUE;}
HINTERNET connect(HINTERNET,LPCWSTR host,INTERNET_PORT port,LPCWSTR user,LPCWSTR password,DWORD service,DWORD,DWORD_PTR){
    if(std::wstring(host)!=L"keyauth.win"||port!=443||user||password||service!=INTERNET_SERVICE_HTTP)throw std::runtime_error("Wrong endpoint");++live;return (HINTERNET)2;}
HINTERNET request(HINTERNET,LPCWSTR method,LPCWSTR route,LPCWSTR,LPCWSTR,LPCWSTR*,DWORD f,DWORD_PTR){verb=method;path=route;flags=f;++live;return (HINTERNET)3;}
BOOL send(HINTERNET,LPCWSTR head,DWORD length,LPVOID data,DWORD bytes){++sends;headers.assign(head,length);posted.assign((char*)data,bytes);if(sendError){SetLastError(sendError);return FALSE;}return TRUE;}
BOOL query(HINTERNET,DWORD what,LPVOID out,LPDWORD,LPDWORD){if(what!=(HTTP_QUERY_STATUS_CODE|HTTP_QUERY_FLAG_NUMBER))throw std::runtime_error("Wrong status query");*(DWORD*)out=status;return TRUE;}
BOOL read(HINTERNET,LPVOID data,DWORD count,LPDWORD bytes){++reads;if(cursor>=readErrorAt){SetLastError(ERROR_INTERNET_CONNECTION_RESET);return FALSE;}
    *bytes=(DWORD)std::min<size_t>({count,16,response.size()-cursor});std::memcpy(data,response.data()+cursor,*bytes);cursor+=*bytes;return TRUE;}
}
#define InternetOpenW fake::open
#define InternetCloseHandle fake::close
#define InternetSetOptionW fake::option
#define InternetConnectW fake::connect
#define HttpOpenRequestW fake::request
#define HttpSendRequestW fake::send
#define HttpQueryInfoW fake::query
#define InternetReadFile fake::read
#include "../src/KeyAuthTransport.cpp"
void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
int main(){try{
    const std::string form="type=license&key=TEST_SECRET";std::wstring error;
    fake::reset();auto body=soju::keyAuthPost(form,error);
    check(body==fake::response&&error.empty()&&fake::live==0,"Complete response and handle cleanup");
    check(fake::verb==L"POST"&&fake::path==L"/api/1.3/"&&fake::posted==form&&fake::headers.find(L"application/x-www-form-urlencoded")!=std::wstring::npos,"Form POST");
    constexpr DWORD required=INTERNET_FLAG_SECURE|INTERNET_FLAG_NO_AUTO_REDIRECT|INTERNET_FLAG_NO_CACHE_WRITE|INTERNET_FLAG_NO_COOKIES|INTERNET_FLAG_NO_UI|INTERNET_FLAG_NO_AUTH;
    check((fake::flags&required)==required&&!fake::unsafe&&!(fake::flags&(INTERNET_FLAG_IGNORE_CERT_DATE_INVALID|INTERNET_FLAG_IGNORE_CERT_CN_INVALID)),"Certificates, redirects, cache and cookies");
    for(DWORD code:{ERROR_INTERNET_INVALID_CA,ERROR_INTERNET_SEC_CERT_DATE_INVALID,ERROR_INTERNET_SEC_CERT_CN_INVALID,ERROR_INTERNET_SEC_CERT_REVOKED,ERROR_INTERNET_SECURITY_CHANNEL_ERROR,ERROR_INTERNET_NAME_NOT_RESOLVED}) {
        fake::reset();fake::sendError=code;
        check(soju::keyAuthPost(form,error).empty()&&error.find(std::to_wstring(code))!=std::wstring::npos&&error.find(L"TEST_SECRET")==std::wstring::npos&&fake::live==0&&fake::sends==1&&!fake::unsafe,"Connection failure must fail closed without replay or leaks");
    }
    for(DWORD code:{301,302,403,429,500}){fake::reset();fake::status=code;check(soju::keyAuthPost(form,error).empty()&&fake::reads==0&&fake::live==0,"Redirect/error response");}
    fake::reset();fake::response="{\"success\":true,\"message\":\"partial\"}";fake::readErrorAt=16;
    check(soju::keyAuthPost(form,error).empty()&&error.find(L"reading response")!=std::wstring::npos&&fake::live==0,"Partial success body must fail closed");
    fake::reset();fake::response="<html>Browser challenge</html>";check(soju::keyAuthPost(form,error).empty(),"HTML rejection");
    fake::reset();fake::response="{"+std::string(256*1024,' ')+"}";check(soju::keyAuthPost(form,error).empty(),"Response size limit");
    fake::reset();fake::optionFail=true;check(soju::keyAuthPost(form,error).empty()&&fake::live==0,"Option failure cleanup");
    std::cout<<"WinINet POST, certificate rejection, redirects, errors, limits, partial reads, and cleanup passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}}

