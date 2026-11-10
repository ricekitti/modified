#include "AuthManager.h"

#include <windows.h>

#include <algorithm>

namespace soju {

namespace {

void trim(std::wstring& value) {
    auto isSpace = [](wchar_t c) { return c == L' ' || c == L'\t' || c == L'\r' || c == L'\n'; };
    while (!value.empty() && isSpace(value.front())) value.erase(value.begin());
    while (!value.empty() && isSpace(value.back())) value.pop_back();
}

} // namespace

KeyAuthResult AuthManager::authenticateLicense(std::wstring licenseKey) {
    state_ = AuthState::Loading;
    trim(licenseKey);
    if (licenseKey.empty()) {
        state_ = AuthState::Failed;
        return { false, L"Enter your license key first.", L"", L"", L"" };
    }

    KeyAuthResult result = client_.authenticateLicense(licenseKey);
    secureClear(licenseKey);

    if (result.success) {
        username_ = result.username;
        subscription_ = result.subscription;
        expiry_ = result.expiry;
        state_ = AuthState::Success;
    } else {
        username_.clear();
        subscription_.clear();
        expiry_.clear();
        state_ = AuthState::Failed;
    }
    return result;
}

void AuthManager::logout() {
    username_.clear();
    subscription_.clear();
    expiry_.clear();
    state_ = AuthState::Idle;
}

void AuthManager::secureClear(std::wstring& value) {
    if (!value.empty()) {
        SecureZeroMemory(&value[0], value.size() * sizeof(wchar_t));
        value.clear();
    }
}

} // namespace soju
