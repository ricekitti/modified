#pragma once

// Purpose: minimal KeyAuth 1.3 HTTP client for the soju launcher license gate.
// Dependencies: WinINet for HTTPS requests, BCrypt/registry data for a stable HWID.
// Integration notes: AuthManager owns this client and calls it from a background thread.

#include <string>

namespace soju {

struct KeyAuthResult {
    bool success = false;
    std::wstring message;
    std::wstring username;
    std::wstring subscription;
    std::wstring expiry;
};

class KeyAuthClient {
public:
    KeyAuthResult authenticateLicense(const std::wstring& licenseKey);

private:
    bool init(std::string& sessionId, std::wstring& error);
    KeyAuthResult license(const std::wstring& licenseKey, const std::string& sessionId);
};

} // namespace soju
