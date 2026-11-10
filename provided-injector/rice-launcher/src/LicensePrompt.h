#pragma once
#include "KeyAuthClient.h"
#include <windows.h>
#include <cwctype>
#include <string>

namespace soju {
inline void clearLicenseInput(std::wstring& key) {
    if (!key.empty()) SecureZeroMemory(key.data(), key.size() * sizeof(wchar_t));
    key.clear();
}

inline bool isLicenseSeparator(wchar_t value) {
    return value == L'-' || value == L'\u2010' || value == L'\u2011' ||
           value == L'\u2012' || value == L'\u2013' || value == L'\u2014' ||
           value == L'\u2212' || value == L'\uff0d';
}

// Discord and rich-text copy can add quotes, backticks, spaces, or replace a
// normal hyphen with a Unicode dash. Canonicalize those harmless changes
// before checking the exact KeyAuth mask used by this application.
inline bool normalizeLicenseInput(std::wstring& key) {
    std::wstring normalized;
    normalized.reserve(key.size());
    for (wchar_t value : key) {
        if (iswspace(value)) continue;
        normalized.push_back(isLicenseSeparator(value) ? L'-' : value);
    }
    if (normalized.size() >= 2) {
        const wchar_t first = normalized.front();
        const wchar_t last = normalized.back();
        if ((first == L'`' && last == L'`') || (first == L'"' && last == L'"') ||
            (first == L'\'' && last == L'\'')) {
            normalized = normalized.substr(1, normalized.size() - 2);
        }
    }
    clearLicenseInput(key);
    key.swap(normalized);

    if (key.size() != 41) return false;
    for (size_t i = 0; i < key.size(); ++i) {
        if ((i + 1) % 7 == 0) {
            if (key[i] != L'-') return false;
        } else if (!iswalnum(key[i])) {
            return false;
        }
    }
    return true;
}

// Authenticate an accepted cached key without requiring console input. Missing,
// malformed or rejected keys fall back to entry; rejected keys are not retried.
template<class ReadKey, class Authenticate, class SaveKey, class Report>
bool promptForLicense(std::wstring saved, ReadKey readKey, Authenticate authenticate,
                      SaveKey saveKey, Report report) {
    struct ClearOnExit {
        std::wstring& key;
        ~ClearOnExit() { clearLicenseInput(key); }
    } clearSaved{saved};
    if (!saved.empty()) {
        if (normalizeLicenseInput(saved)) {
            const auto result = authenticate(saved);
            if (result.success) {
                saveKey(saved);
                return true;
            }
            report(L"Saved license failed: " + (result.message.empty() ? std::wstring(L"Authentication failed.") : result.message));
        } else {
            report(L"The saved license could not be read. Enter your license key again.");
        }
        clearLicenseInput(saved);
    }
    for (;;) {
        std::wstring entered;
        ClearOnExit clearEntered{entered};
        if (!readKey(!saved.empty(), entered)) return false;
        if (entered.empty()) entered.swap(saved);
        clearLicenseInput(saved);
        if (entered.empty()) {
            report(L"No license key entered. Paste a key or close the window to cancel.");
            continue;
        }
        if (!normalizeLicenseInput(entered)) {
            report(L"The pasted license is incomplete or altered. Copy the full 41-character key and paste it again.");
            continue;
        }
        const auto result = authenticate(entered);
        if (result.success) {
            saveKey(entered);
            return true;
        }
        report(L"License failed: " + (result.message.empty() ? std::wstring(L"Authentication failed.") : result.message));
        report(L"Enter a replacement license key. If your license was reset, open a ticket.");
    }
}
} // namespace soju
