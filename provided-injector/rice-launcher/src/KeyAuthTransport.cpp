#include "KeyAuthTransport.h"
#include "KeyAuthConfig.h"
#include <windows.h>
#include <wininet.h>
#include <limits>

namespace soju {
namespace {
struct InternetHandle {
    HINTERNET value;
    ~InternetHandle() { if (value) InternetCloseHandle(value); }
};
std::wstring networkError(const wchar_t* stage, DWORD code) {
    std::wstring message;
    switch (code) {
    case ERROR_INTERNET_TIMEOUT: message=L"KeyAuth timed out. Please try again."; break;
    case ERROR_INTERNET_NAME_NOT_RESOLVED: message=L"KeyAuth DNS lookup failed."; break;
    case ERROR_INTERNET_SEC_CERT_DATE_INVALID: message=L"KeyAuth certificate date is invalid. Check Windows date and time."; break;
    case ERROR_INTERNET_SEC_CERT_CN_INVALID: message=L"KeyAuth certificate hostname does not match. Check DNS or HTTPS inspection."; break;
    case ERROR_INTERNET_INVALID_CA: message=L"Windows does not trust the KeyAuth certificate issuer. Check certificate updates or HTTPS inspection."; break;
    case ERROR_INTERNET_SEC_CERT_REVOKED: message=L"Windows reports that the KeyAuth certificate is revoked."; break;
    case ERROR_INTERNET_SEC_CERT_REV_FAILED: message=L"Windows could not check KeyAuth certificate revocation."; break;
    case ERROR_INTERNET_SECURITY_CHANNEL_ERROR: case ERROR_INTERNET_SEC_INVALID_CERT:
        message=L"Windows could not validate the KeyAuth TLS connection. Check Windows/security-software updates."; break;
    case ERROR_INTERNET_CANNOT_CONNECT: case ERROR_INTERNET_CONNECTION_ABORTED: case ERROR_INTERNET_CONNECTION_RESET:
        message=L"The KeyAuth connection was blocked or interrupted."; break;
    default: message=L"KeyAuth network operation failed."; break;
    }
    return message+L" (WinINet "+std::to_wstring(code)+L", "+stage+L", keyauth.win)";
}
}
std::string keyAuthPost(const std::string& form, std::wstring& error) {
    error.clear();
    if(form.size()>(std::numeric_limits<DWORD>::max)()) { error=L"KeyAuth request is too large."; return {}; }
    // Use the interactive user's Windows Internet/proxy settings. No global TLS
    // options, certificate exceptions, cookie persistence, or credential logging.
    InternetHandle session{InternetOpenW(L"RiceSlinky/Auth2",INTERNET_OPEN_TYPE_PRECONFIG,nullptr,nullptr,0)};
    if(!session.value) { error=networkError(L"initializing",GetLastError()); return {}; }
    DWORD timeout=15000;
    for(DWORD option : {INTERNET_OPTION_CONNECT_TIMEOUT, INTERNET_OPTION_SEND_TIMEOUT, INTERNET_OPTION_RECEIVE_TIMEOUT}) {
        if(!InternetSetOptionW(session.value,option,&timeout,sizeof(timeout))) {
            error=networkError(L"configuring timeouts",GetLastError()); return {};
        }
    }
    InternetHandle connection{InternetConnectW(session.value,keyauth::host,INTERNET_DEFAULT_HTTPS_PORT,
        nullptr,nullptr,INTERNET_SERVICE_HTTP,0,0)};
    if(!connection.value) { error=networkError(L"connecting",GetLastError()); return {}; }
    const wchar_t* accepts[]={L"application/json",nullptr};
    constexpr DWORD flags=INTERNET_FLAG_SECURE|INTERNET_FLAG_RELOAD|INTERNET_FLAG_NO_CACHE_WRITE|
        INTERNET_FLAG_NO_COOKIES|INTERNET_FLAG_NO_AUTO_REDIRECT|INTERNET_FLAG_NO_UI|INTERNET_FLAG_NO_AUTH;
    InternetHandle request{HttpOpenRequestW(connection.value,L"POST",keyauth::path,nullptr,nullptr,accepts,flags,0)};
    if(!request.value) { error=networkError(L"creating request",GetLastError()); return {}; }
    constexpr wchar_t headers[]=L"Content-Type: application/x-www-form-urlencoded\r\nAccept: application/json\r\n";
    // Do not automatically replay license requests after ambiguous network failures.
    if(!HttpSendRequestW(request.value,headers,static_cast<DWORD>(_countof(headers)-1),
        const_cast<char*>(form.data()),static_cast<DWORD>(form.size()))) {
        error=networkError(L"sending request",GetLastError()); return {};
    }
    DWORD status=0,bytes=sizeof(status);
    if(!HttpQueryInfoW(request.value,HTTP_QUERY_STATUS_CODE|HTTP_QUERY_FLAG_NUMBER,&status,&bytes,nullptr)) {
        error=networkError(L"reading HTTP status",GetLastError()); return {};
    }
    if(status<200||status>=300) {
        error=status==429 ? L"KeyAuth rate limited the request. Wait before trying again (HTTP 429)." :
            L"KeyAuth returned HTTP "+std::to_wstring(status)+L".";
        return {};
    }
    constexpr size_t maxResponse=256*1024;
    std::string body;
    char buffer[4096];
    for(;;) {
        DWORD read=0;
        if(!InternetReadFile(request.value,buffer,sizeof(buffer),&read)) {
            error=networkError(L"reading response",GetLastError()); return {};
        }
        if(!read) break;
        if(read>maxResponse-body.size()) { error=L"KeyAuth response is too large."; return {}; }
        body.append(buffer,read);
    }
    const auto first=body.find_first_not_of(" \t\r\n"),last=body.find_last_not_of(" \t\r\n");
    if(first==std::string::npos||body[first]!='{'||body[last]!='}') {
        error=L"KeyAuth returned an unexpected response instead of JSON."; return {};
    }
    return body;
}
}

