#pragma once

// Purpose: launcher-facing auth state wrapper for KeyAuth license login.
// Dependencies: KeyAuthClient performs the blocking HTTP work.
// Integration notes: callers invoke authenticateLicense from a background thread.

#include "KeyAuthClient.h"

#include <atomic>
#include <string>

namespace soju {

enum class AuthState {
    Idle,
    Loading,
    Success,
    Failed
};

class AuthManager {
public:
    KeyAuthResult authenticateLicense(std::wstring licenseKey);
    void logout();

    AuthState state() const { return state_.load(); }
    const std::wstring& username() const { return username_; }
    const std::wstring& subscription() const { return subscription_; }
    const std::wstring& expiry() const { return expiry_; }

private:
    static void secureClear(std::wstring& value);

    KeyAuthClient client_;
    std::atomic<AuthState> state_{ AuthState::Idle };
    std::wstring username_;
    std::wstring subscription_;
    std::wstring expiry_;
};

} // namespace soju
