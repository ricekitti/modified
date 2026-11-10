#include "KeyAuthClient.h"
#include "KeyAuthTransport.h"
#include "KeyAuthConfig.h"
#include "KeyAuthJson.h"

#include <windows.h>

#include <bcrypt.h>

#include <algorithm>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>

namespace soju {
namespace {

std::wstring toWide(const std::string& value) {
    if (value.empty()) return L"";
    int needed = MultiByteToWideChar(CP_UTF8, 0, value.data(), static_cast<int>(value.size()), nullptr, 0);
    if (needed <= 0) return L"";
    std::wstring out(static_cast<size_t>(needed), L'\0');
    MultiByteToWideChar(CP_UTF8, 0, value.data(), static_cast<int>(value.size()), &out[0], needed);
    return out;
}

std::string toUtf8(const std::wstring& value) {
    if (value.empty()) return "";
    int needed = WideCharToMultiByte(CP_UTF8, 0, value.data(), static_cast<int>(value.size()), nullptr, 0, nullptr, nullptr);
    if (needed <= 0) return "";
    std::string out(static_cast<size_t>(needed), '\0');
    WideCharToMultiByte(CP_UTF8, 0, value.data(), static_cast<int>(value.size()), &out[0], needed, nullptr, nullptr);
    return out;
}

void secureClear(std::string& value) {
    if (!value.empty()) {
        SecureZeroMemory(&value[0], value.size());
        value.clear();
    }
}

std::string urlEncode(const std::string& value) {
    std::ostringstream out;
    out << std::hex << std::uppercase;
    for (unsigned char c : value) {
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ||
            c == '-' || c == '_' || c == '.' || c == '~') {
            out << static_cast<char>(c);
        } else {
            out << '%' << std::setw(2) << std::setfill('0') << static_cast<int>(c);
        }
    }
    return out.str();
}

std::string escapeQuery(const std::wstring& value) {
    std::string utf8 = toUtf8(value);
    std::string encoded = urlEncode(utf8);
    secureClear(utf8);
    return encoded;
}

std::string bytesToHex(const std::vector<unsigned char>& bytes) {
    std::ostringstream out;
    out << std::hex << std::nouppercase << std::setfill('0');
    for (unsigned char b : bytes) {
        out << std::setw(2) << static_cast<int>(b);
    }
    return out.str();
}

std::wstring readMachineGuid() {
    HKEY key = nullptr;
    if (RegOpenKeyExW(HKEY_LOCAL_MACHINE, L"SOFTWARE\\Microsoft\\Cryptography", 0,
                      KEY_READ | KEY_WOW64_64KEY, &key) != ERROR_SUCCESS) {
        return L"";
    }

    wchar_t buffer[128] = {};
    DWORD bytes = sizeof(buffer);
    DWORD type = 0;
    LONG result = RegQueryValueExW(key, L"MachineGuid", nullptr, &type,
                                   reinterpret_cast<LPBYTE>(buffer), &bytes);
    RegCloseKey(key);
    if (result != ERROR_SUCCESS || type != REG_SZ) return L"";
    return buffer;
}

std::string sha256Hex(const std::string& value) {
    BCRYPT_ALG_HANDLE alg = nullptr;
    if (BCryptOpenAlgorithmProvider(&alg, BCRYPT_SHA256_ALGORITHM, nullptr, 0) != 0) {
        return "";
    }

    DWORD objectLength = 0;
    DWORD dataLength = 0;
    BCryptGetProperty(alg, BCRYPT_OBJECT_LENGTH, reinterpret_cast<PUCHAR>(&objectLength),
                      sizeof(objectLength), &dataLength, 0);

    std::vector<unsigned char> object(objectLength);
    std::vector<unsigned char> hash(32);
    BCRYPT_HASH_HANDLE hashHandle = nullptr;
    if (BCryptCreateHash(alg, &hashHandle, object.data(), objectLength, nullptr, 0, 0) != 0) {
        BCryptCloseAlgorithmProvider(alg, 0);
        return "";
    }

    BCryptHashData(hashHandle, reinterpret_cast<PUCHAR>(const_cast<char*>(value.data())),
                   static_cast<ULONG>(value.size()), 0);
    BCryptFinishHash(hashHandle, hash.data(), static_cast<ULONG>(hash.size()), 0);
    BCryptDestroyHash(hashHandle);
    BCryptCloseAlgorithmProvider(alg, 0);
    return bytesToHex(hash);
}

std::string makeHwid() {
    wchar_t computerName[MAX_COMPUTERNAME_LENGTH + 1] = {};
    DWORD computerNameSize = MAX_COMPUTERNAME_LENGTH + 1;
    GetComputerNameW(computerName, &computerNameSize);

    std::wstring material = readMachineGuid();
    material += L"|";
    material += computerName;

    std::string utf8 = toUtf8(material);
    std::string hash = sha256Hex(utf8);
    secureClear(utf8);
    return hash.empty() ? "unknown" : hash;
}

std::wstring friendlyMessage(const std::wstring& raw) {
    std::wstring lower = raw;
    std::transform(lower.begin(), lower.end(), lower.begin(),
                   [](wchar_t c) { return static_cast<wchar_t>(towlower(c)); });

    if (lower.find(L"invalid") != std::wstring::npos || lower.find(L"does not exist") != std::wstring::npos) {
        return L"That license key is invalid.";
    }
    if (lower.find(L"expired") != std::wstring::npos) {
        return L"That license key has expired.";
    }
    if (lower.find(L"banned") != std::wstring::npos || lower.find(L"blacklisted") != std::wstring::npos) {
        return L"This device or account is banned.";
    }
    if (lower.find(L"rate") != std::wstring::npos || lower.find(L"too many") != std::wstring::npos) {
        return L"Too many attempts. Wait a moment, then try again.";
    }
    if (lower.find(L"version") != std::wstring::npos) {
        return L"This launcher version is not accepted by KeyAuth.";
    }
    return raw.empty() ? L"Authentication failed." : raw;
}

} // namespace

KeyAuthResult KeyAuthClient::authenticateLicense(const std::wstring& licenseKey) {
    std::string sessionId;
    std::wstring error;
    bool initialized=false;
    try { initialized=init(sessionId,error); }
    catch (const std::exception&) { error=L"KeyAuth returned malformed initialization data."; }
    if (!initialized) {
        return { false, error.empty() ? L"Could not initialize KeyAuth." : error, L"", L"", L"" };
    }

    KeyAuthResult result;
    try { result = license(licenseKey, sessionId); }
    catch (const std::exception&) { result.message = L"KeyAuth returned malformed authentication data."; }
    secureClear(sessionId);
    return result;
}

bool KeyAuthClient::init(std::string& sessionId, std::wstring& error) {
    std::string query = "type=init&ver=" + urlEncode(keyauth::version) +
        "&name=" + urlEncode(keyauth::name) +
        "&ownerid=" + urlEncode(keyauth::ownerid) +
        "";
    std::string body = keyAuthPost(query, error);
    if (body.empty()) return false;

    keyauth::JsonResponse response(body);
    if (response.string("ownerid") != keyauth::ownerid) {
        throw std::runtime_error("KeyAuth application owner mismatch");
    }
    bool ok = response.success();
    std::wstring message = toWide(response.string("message"));
    if (!ok) {
        error = friendlyMessage(message);
        return false;
    }

    sessionId = response.string("sessionid");
    if (sessionId.empty()) {
        error = L"KeyAuth did not return a session.";
        return false;
    }
    return true;
}

KeyAuthResult KeyAuthClient::license(const std::wstring& licenseKey, const std::string& sessionId) {
    std::string key = escapeQuery(licenseKey);
    std::string hwid = makeHwid();
    std::string query = "type=license&key=" + key +
        "&sessionid=" + urlEncode(sessionId) +
        "&name=" + urlEncode(keyauth::name) +
        "&ownerid=" + urlEncode(keyauth::ownerid) +
        "&hwid=" + urlEncode(hwid) +
        "&code=";
    secureClear(key);

    std::wstring error;
    std::string body = keyAuthPost(query, error);
    secureClear(query);
    if (body.empty()) {
        return { false, error.empty() ? L"Could not reach KeyAuth." : error, L"", L"", L"" };
    }

    keyauth::JsonResponse response(body);
    if (response.string("ownerid") != keyauth::ownerid) {
        throw std::runtime_error("KeyAuth application owner mismatch");
    }
    bool ok = response.success();
    std::wstring message = friendlyMessage(toWide(response.string("message")));
    if (!ok) {
        return { false, message, L"", L"", L"" };
    }

    return {
        true,
        message.empty() ? L"License accepted." : message,
        toWide(response.string("username")),
        toWide(response.string("subscription")),
        toWide(response.string("expiry"))
    };
}

} // namespace soju
