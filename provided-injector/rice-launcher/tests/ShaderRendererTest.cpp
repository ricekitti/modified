#include "GlassRenderer.h"
#include <algorithm>
#include <iostream>
#include <cstring>

int main(int argc, char** argv) {
    constexpr int size = 128;
    if (argc == 2 && std::strcmp(argv[1], "--desktop") == 0) {
        HWND window = CreateWindowExW(0, L"STATIC", L"Backdrop backend check", WS_POPUP,
            0, 0, size, size, nullptr, nullptr, GetModuleHandleW(nullptr), nullptr);
        if (!window) return 10;
        soju::GlassRenderer live;
        if (!live.initialize(window, size, size, false)) { DestroyWindow(window); return 11; }
        std::vector<unsigned char> blank(size * size * 4, 0);
        bool rendered = false;
        for (int i = 0; i < 100 && !rendered; ++i) {
            rendered = live.render(blank.data(), 0, POINT{64, 64});
            if (!rendered) Sleep(10);
        }
        DestroyWindow(window);
        if (!rendered) return 12;
        std::cout << "PASS: live desktop backdrop acquired and shader-rendered on GPU. No image exported.\n";
        return 0;
    }
    soju::GlassRenderer renderer;
    if (!renderer.initialize(nullptr, size, size, true)) { std::cerr << "GPU shader initialization failed\n"; return 1; }
    std::vector<unsigned char> ui(size * size * 4, 0), pixels;
    if (!renderer.render(ui.data(), 0, POINT{64, 64}) || !renderer.readback(pixels, true)) return 2;
    int minimum = 255, maximum = 0;
    long sum = 0, count = 0;
    for (int y = 24; y < size - 24; ++y) for (int x = 24; x < size - 24; ++x) {
        int sample = pixels[(y * size + x) * 4];
        minimum = (std::min)(minimum, sample); maximum = (std::max)(maximum, sample);
        sum += sample; ++count;
    }
    // The source is a 0/255 checkerboard. Both passes must smooth it while preserving its average.
    if (minimum <= 20 || maximum >= 235 || sum / count < 100 || sum / count > 155) {
        std::cerr << "Gaussian blur failed: " << minimum << ',' << maximum << '\n'; return 3;
    }
    const int index = (64 * size + 64) * 4;
    ui[index] = 20; ui[index + 1] = 40; ui[index + 2] = 200; ui[index + 3] = 255;
    if (!renderer.render(ui.data(), 1, POINT{64, 64}) || !renderer.readback(pixels)) return 4;
    for (int c = 0; c < 4; ++c) if (pixels[index + c] != ui[index + c]) {
        std::cerr << "Opaque UI was blurred or tinted\n"; return 5;
    }
    std::cout << "PASS: two-pass Gaussian smoothing, luminance preserved, UI remains sharp. No desktop capture.\n";
}
