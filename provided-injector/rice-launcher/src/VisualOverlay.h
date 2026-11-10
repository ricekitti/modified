#pragma once
#include <windows.h>
#include <string>
#include <vector>
#include <cstdint>

namespace soju {
bool startVisualOverlay(DWORD targetPid, const std::string& nonce);
int runVisualOverlay(DWORD targetPid, const std::wstring& nonce);
// Bounds-checked protocol decoder, also exercised without a game or visible window.
bool validateVisualFrame(const std::vector<uint8_t>& frame);
int testVisualOverlay();
int testVisualFrameFile(const std::wstring& path);
}
