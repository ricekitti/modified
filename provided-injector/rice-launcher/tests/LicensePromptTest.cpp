#include "LicensePrompt.h"
#include <iostream>
#include <stdexcept>
#include <vector>
static void check(bool ok, const char* message) { if (!ok) throw std::runtime_error(message); }
static const std::wstring kValidNew = L"ABCDEF-ghijkl-123456-MnOpQr-STUVWX-yz7890";
static const std::wstring kValidSaved = L"ZYXWVU-tsrqpo-654321-RqPoNm-XWVUTS-0987zy";
static const std::wstring kRejected = L"BADBAD-badbad-111111-BadBad-222222-badbad";
struct Fixture {
    std::vector<std::wstring> input, submitted, persisted;
    std::vector<bool> offeredSaved;
    std::vector<std::wstring> notices;
    size_t next = 0;
    bool run(const std::wstring& saved) {
        return soju::promptForLicense(saved,
            [&](bool available, std::wstring& key) {
                offeredSaved.push_back(available);
                if (next == input.size()) return false;
                key = input[next++]; return true;
            },
            [&](const std::wstring& key) {
                submitted.push_back(key);
                soju::KeyAuthResult result;
                result.success = key == kValidNew || key == kValidSaved;
                result.message = L"Invalid license";
                return result;
            },
            [&](const std::wstring& key) { persisted.push_back(key); },
            [&](const std::wstring& message) { notices.push_back(message); });
    }
};
int main() { try {
    Fixture fresh; fresh.input={kValidNew};
    check(fresh.run(L""), "Fresh entry should authenticate");
    check(fresh.offeredSaved == std::vector<bool>{false}, "Fresh prompt must not offer saved key");
    Fixture replace; replace.input={kValidNew};
    check(replace.run(kRejected), "Rejected saved key must allow replacement");
    check(replace.submitted == std::vector<std::wstring>{kRejected,kValidNew}, "Try cached key once before requesting replacement");
    check(replace.persisted == std::vector<std::wstring>{kValidNew}, "Only an accepted replacement may overwrite the cache");
    Fixture keep; keep.input={kValidNew};
    check(keep.run(kValidSaved), "Saved key must authenticate automatically");
    check(keep.offeredSaved.empty() && keep.next==0, "Valid saved key must not ask for input or require Enter");
    check(keep.submitted == std::vector<std::wstring>{kValidSaved}, "Saved key must still be authenticated");
    Fixture restart;
    check(restart.run(fresh.persisted.front()), "A key saved on the first launch must work on the next");
    check(restart.offeredSaved.empty(), "Relaunch must not show the license prompt");
    Fixture reset; reset.input={L"", L"", kValidNew};
    check(reset.run(kRejected), "Reset license must allow retry without restart");
    check(reset.submitted == std::vector<std::wstring>{kRejected,kValidNew}, "Blank retry must not resubmit rejected key");
    check(reset.offeredSaved == std::vector<bool>{false,false,false}, "Rejected cache must be removed from prompt");
    check(reset.persisted == std::vector<std::wstring>{kValidNew}, "Only accepted key may be saved");
    Fixture cancel; cancel.input={kRejected};
    check(!cancel.run(L""), "EOF must cancel after rejection");
    check(cancel.persisted.empty(), "Rejected key must not be persisted");
    Fixture empty; empty.input={L""};
    check(!empty.run(L"") && empty.submitted.empty(), "Empty key must not reach authentication");
    Fixture malformed; malformed.input={L"short", kValidNew};
    check(malformed.run(L""), "Malformed paste must allow retry");
    check(malformed.submitted == std::vector<std::wstring>{kValidNew}, "Malformed paste must not reach KeyAuth");
    Fixture badCache;badCache.input={kValidNew};
    check(badCache.run(L"short"), "Malformed saved key must allow a fresh entry");
    check(badCache.submitted == std::vector<std::wstring>{kValidNew}, "Malformed cache must not reach KeyAuth");
    Fixture normalizedCache;
    check(normalizedCache.run(L"  `"+kValidSaved+L"`  "), "Saved formatting must normalize before auto-login");
    check(normalizedCache.offeredSaved.empty()&&normalizedCache.persisted==std::vector<std::wstring>{kValidSaved},
          "Normalized cached key must be remembered without prompting");
    std::wstring richText = L"`ABCDEF \u2013 ghijkl \u2013 123456 \u2013 MnOpQr \u2013 STUVWX \u2013 yz7890`";
    check(soju::normalizeLicenseInput(richText), "Rich-text license should normalize");
    check(richText == kValidNew, "Normalized license must use canonical hyphens without formatting");
    std::cout << "Automatic saved-key login, relaunch, entry, normalization, rejection/replacement, persistence, and cancellation passed\n";
    return 0;
} catch (const std::exception& e) { std::cerr << e.what() << '\n'; return 1; } }
