#pragma once
#include <windows.h>
#include <memory>
#include <vector>

namespace soju {
// Local-only GPU backdrop. Desktop frames never leave GPU memory or go to disk.
class GlassRenderer {
public:
    GlassRenderer();
    ~GlassRenderer();
    bool initialize(HWND window, int width, int height, bool preview);
    bool render(const void* premultipliedUi, float seconds, POINT pointer);
    bool readback(std::vector<unsigned char>& pixels, bool blurOnly = false);
private:
    struct Impl;
    std::unique_ptr<Impl> impl_;
};
}
