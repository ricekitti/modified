#include "VisualOverlay.h"
#include <objidl.h>
#include <gdiplus.h>
#include <algorithm>
#include <cmath>
#include <cstring>
#include <memory>
#include <unordered_map>
#include <fstream>
#include <filesystem>

namespace soju {
namespace {
constexpr uint32_t Magic = 1480868170, Version = 4, MaxBytes = 1048576;
struct Command {
    int type, color, id, flags;
    float f[8];
    std::string text[3];
    size_t data = 0;
    uint32_t bytes = 0;
};
uint32_t word(const std::vector<uint8_t>& b, size_t p) {
    uint32_t value; std::memcpy(&value, b.data() + p, 4); return value;
}
bool decode(const std::vector<uint8_t>& b, std::vector<Command>& out) {
    if (b.size() < 36 || b.size() > MaxBytes || word(b, 0) != b.size() - 4 || word(b, 4) != Magic || word(b, 8) != Version) return false;
    if (!word(b, 24) || !word(b, 28) || word(b, 24) > 16384 || word(b, 28) > 16384 || word(b, 32) > 8192) return false;
    size_t p = 36;
    for (uint32_t i = 0; i < word(b, 32); ++i) {
        if (b.size() - p < 48) return false;
        Command c{};
        c.type = static_cast<int>(word(b, p)); c.color = static_cast<int>(word(b, p + 4));
        c.id = static_cast<int>(word(b, p + 8)); c.flags = static_cast<int>(word(b, p + 12));
        std::memcpy(c.f, b.data() + p + 16, 32); p += 48;
        if (c.type < 1 || c.type > 10) return false;
        for (float v : c.f) if (!std::isfinite(v) || std::abs(v) > 1000000) return false;
        if (c.type == 3 || c.type == 4) {
            for (auto& s : c.text) {
                if (b.size() - p < 4) return false;
                uint32_t n = word(b, p); p += 4;
                if (n > 4096 || b.size() - p < n) return false;
                s.assign(reinterpret_cast<const char*>(b.data() + p), n); p += n;
            }
        }
        if (c.type == 7 || c.type == 9 || c.type == 10) {
            if (b.size() - p < 4) return false;
            c.bytes = word(b, p); p += 4; c.data = p;
            if (!c.bytes || b.size() - p < c.bytes) return false;
            if (c.type != 10) {
                if (c.color <= 0 || c.flags <= 0 || c.color > 2048 || c.flags > 2048 || c.bytes > 262144) return false;
                if (static_cast<uint64_t>(c.color) * c.flags * (c.type == 7 ? 4 : 1) != c.bytes) return false;
            } else {
                // PNG IHDR bounds are checked before passing the image to GDI+.
                const uint8_t signature[] = {137, 80, 78, 71, 13, 10, 26, 10};
                if (c.bytes < 33 || std::memcmp(b.data() + p, signature, 8) != 0 || std::memcmp(b.data() + p + 12, "IHDR", 4) != 0) return false;
                const auto be = [&](size_t pos) { return (uint32_t(b[pos]) << 24) | (uint32_t(b[pos + 1]) << 16) | (uint32_t(b[pos + 2]) << 8) | b[pos + 3]; };
                uint32_t w = be(p + 16), h = be(p + 20);
                if (!w || !h || w > 8192 || h > 8192 || uint64_t(w) * h > 33554432) return false;
            }
            p += c.bytes;
        }
        out.push_back(std::move(c));
    }
    return p == b.size();
}
std::wstring wide(const std::string& s) {
    int n = MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, s.data(), static_cast<int>(s.size()), nullptr, 0);
    if (n <= 0) return L"";
    std::wstring result(n, L'\0'); MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, s.data(), static_cast<int>(s.size()), result.data(), n); return result;
}
struct Texture {
    std::vector<uint8_t> pixels;
    std::unique_ptr<Gdiplus::Bitmap> bitmap;
};
using Textures = std::unordered_map<int, std::unique_ptr<Texture>>;
std::unique_ptr<Gdiplus::Bitmap> png(const std::vector<uint8_t>& frame, const Command& c) {
    HGLOBAL memory = GlobalAlloc(GMEM_MOVEABLE, c.bytes);
    if (!memory) return nullptr;
    void* data = GlobalLock(memory);
    if (!data) { GlobalFree(memory); return nullptr; }
    std::memcpy(data, frame.data() + c.data, c.bytes); GlobalUnlock(memory);
    IStream* stream = nullptr;
    if (FAILED(CreateStreamOnHGlobal(memory, TRUE, &stream))) { GlobalFree(memory); return nullptr; }
    std::unique_ptr<Gdiplus::Bitmap> image(Gdiplus::Bitmap::FromStream(stream));
    std::unique_ptr<Gdiplus::Bitmap> copy;
    if (image && image->GetLastStatus() == Gdiplus::Ok)
        copy.reset(image->Clone(0, 0, image->GetWidth(), image->GetHeight(), PixelFormat32bppPARGB));
    image.reset(); stream->Release(); return copy;
}
void rounded(Gdiplus::GraphicsPath& path, float x, float y, float w, float h, const float* radius) {
    const float rr[] = {radius[0], radius[1], radius[2], radius[3]};
    const float xx[] = {x, x + w, x + w, x}, yy[] = {y, y, y + h, y + h};
    for (int i = 0; i < 4; ++i) {
        float r = std::clamp(rr[i], 0.f, std::min(w, h) / 2);
        if (r > 0) path.AddArc(xx[i] - (i == 1 || i == 2 ? r * 2 : 0), yy[i] - (i >= 2 ? r * 2 : 0), r * 2, r * 2, 180.f + i * 90, 90);
        else path.AddLine(xx[i], yy[i], xx[i], yy[i]);
    }
    path.CloseFigure();
}
bool render(Gdiplus::Graphics& g, const std::vector<uint8_t>& frame, const std::vector<Command>& commands, Textures& textures, bool uploadsOnly = false) {
    // Uploads may follow glyph draw commands in the Java buffer.
    for (const auto& c : commands) if (c.type == 7 || c.type == 9) {
        auto texture = std::make_unique<Texture>(); texture->pixels.resize(size_t(c.color) * c.flags * 4);
        for (size_t i = 0; i < texture->pixels.size() / 4; ++i) {
            const uint8_t* source = frame.data() + c.data + i * (c.type == 7 ? 4 : 1);
            uint8_t* dest = texture->pixels.data() + i * 4;
            if (c.type == 7) { dest[0] = source[2]; dest[1] = source[1]; dest[2] = source[0]; dest[3] = source[3]; }
            else { dest[0] = dest[1] = dest[2] = 255; dest[3] = source[0]; }
        }
        texture->bitmap = std::make_unique<Gdiplus::Bitmap>(c.color, c.flags, c.color * 4, PixelFormat32bppARGB, texture->pixels.data());
        if (texture->bitmap->GetLastStatus() != Gdiplus::Ok) return false;
        textures[c.id] = std::move(texture);
    }
    if (uploadsOnly) return true;
    g.SetSmoothingMode(Gdiplus::SmoothingModeAntiAlias); g.SetInterpolationMode(Gdiplus::InterpolationModeHighQualityBilinear);
    g.SetTextRenderingHint(Gdiplus::TextRenderingHintAntiAliasGridFit);
    for (const auto& c : commands) {
        Gdiplus::Color color(static_cast<Gdiplus::ARGB>(c.color)); Gdiplus::SolidBrush brush(color);
        const float* f = c.f;
        if (c.type == 1) { Gdiplus::Pen pen(color, std::max(0.1f, f[4])); g.DrawLine(&pen, f[0], f[1], f[2], f[3]); }
        else if (c.type == 2 || c.type == 8) {
            if (f[2] < f[0] || f[3] < f[1]) return false;
            float r[] = {f[5], f[5], f[5], f[5]};
            if (c.type == 8) std::copy(f + 4, f + 8, r);
            Gdiplus::GraphicsPath path; rounded(path, f[0], f[1], f[2] - f[0], f[3] - f[1], r);
            if (c.type == 2 && f[4] > 0) { Gdiplus::Pen pen(color, f[4]); g.DrawPath(&pen, &path); }
            else g.FillPath(&brush, &path);
        } else if (c.type == 5) {
            Gdiplus::PointF points[] = {{f[0], f[1]}, {f[2], f[3]}, {f[4], f[5]}}; g.FillPolygon(&brush, points, 3);
        } else if (c.type == 3 || c.type == 4) {
            if (f[7] < 1 || f[7] > 256) return false;
            auto text = wide(c.text[0]);
            Gdiplus::Font font(L"Segoe UI", f[7], Gdiplus::FontStyleRegular, Gdiplus::UnitPixel);
            Gdiplus::StringFormat format(Gdiplus::StringFormat::GenericTypographic()); format.SetFormatFlags(Gdiplus::StringFormatFlagsNoWrap);
            if (c.flags & 4) { Gdiplus::SolidBrush shadow(Gdiplus::Color(color.GetA(), 0, 0, 0)); g.DrawString(text.c_str(), -1, &font, {f[0] + 1, f[1] + 1}, &format, &shadow); }
            g.DrawString(text.c_str(), -1, &font, {f[0], f[1]}, &format, &brush);
        } else if (c.type == 6) {
            auto it = textures.find(c.id); if (it == textures.end()) continue;
            if (f[2] <= 0 || f[3] <= 0 || f[2] > 4096 || f[3] > 4096) return false;
            Gdiplus::ColorMatrix matrix = {{{color.GetR() / 255.f, 0, 0, 0, 0}, {0, color.GetG() / 255.f, 0, 0, 0},
                {0, 0, color.GetB() / 255.f, 0, 0}, {0, 0, 0, color.GetA() / 255.f, 0}, {0, 0, 0, 0, 1}}};
            Gdiplus::ImageAttributes attrs; attrs.SetColorMatrix(&matrix);
            auto state = g.Save();
            if (!(c.flags & 1) && f[4] != 0) { g.TranslateTransform(f[0] + f[2] / 2, f[1] + f[3] / 2); g.RotateTransform(f[4]); g.TranslateTransform(-f[0] - f[2] / 2, -f[1] - f[3] / 2); }
            auto& image = *it->second->bitmap;
            g.DrawImage(&image, Gdiplus::RectF(f[0], f[1], f[2], f[3]), 0, 0, float(image.GetWidth()), float(image.GetHeight()), Gdiplus::UnitPixel, &attrs);
            g.Restore(state);
        } else if (c.type == 10) {
            auto image = png(frame, c); if (!image) return false;
            g.DrawImage(image.get(), Gdiplus::RectF(f[0], f[1], f[2], f[3]));
        }
    }
    return g.GetLastStatus() == Gdiplus::Ok;
}
LRESULT CALLBACK windowProc(HWND hwnd, UINT message, WPARAM w, LPARAM l) {
    if (message == WM_NCHITTEST) return HTTRANSPARENT;
    if (message == WM_MOUSEACTIVATE) return MA_NOACTIVATE;
    return DefWindowProcW(hwnd, message, w, l);
}
HWND createOverlay() {
    WNDCLASSW wc{}; wc.lpfnWndProc = windowProc; wc.hInstance = GetModuleHandleW(nullptr); wc.lpszClassName = L"SojuPrivateVisuals";
    RegisterClassW(&wc);
    HWND hwnd = CreateWindowExW(WS_EX_LAYERED | WS_EX_TRANSPARENT | WS_EX_TOOLWINDOW | WS_EX_NOACTIVATE,
        wc.lpszClassName, L"soju visuals", WS_POPUP, 0, 0, 1, 1, nullptr, nullptr, wc.hInstance, nullptr);
    if (!hwnd) return nullptr;
    DWORD affinity = 0;
    if (!SetWindowDisplayAffinity(hwnd, 0x11) || !GetWindowDisplayAffinity(hwnd, &affinity) || affinity != 0x11) {
        DestroyWindow(hwnd); return nullptr; // Never display an unprotected fallback.
    }
    return hwnd;
}
struct Target { DWORD pid; HWND hwnd = nullptr; };
BOOL CALLBACK findWindow(HWND hwnd, LPARAM parameter) {
    auto& target = *reinterpret_cast<Target*>(parameter); DWORD pid = 0; GetWindowThreadProcessId(hwnd, &pid);
    if (pid == target.pid && GetWindow(hwnd, GW_OWNER) == nullptr && !(GetWindowLongW(hwnd, GWL_EXSTYLE) & WS_EX_TOOLWINDOW)) {
        if (!target.hwnd || IsWindowVisible(hwnd)) target.hwnd = hwnd;
        if (IsWindowVisible(hwnd)) return FALSE;
    }
    return TRUE;
}
void pump() { MSG msg; while (PeekMessageW(&msg, nullptr, 0, 0, PM_REMOVE)) { TranslateMessage(&msg); DispatchMessageW(&msg); } }
bool waitIo(HANDLE pipe, OVERLAPPED& op, HANDLE game, HWND overlay, DWORD targetPid, DWORD& done) {
    HANDLE handles[] = {op.hEvent, game};
    for (;;) {
        DWORD wait = MsgWaitForMultipleObjects(2, handles, FALSE, 50, QS_ALLINPUT);
        pump();
        DWORD foregroundPid = 0; GetWindowThreadProcessId(GetForegroundWindow(), &foregroundPid);
        if (foregroundPid != targetPid) ShowWindow(overlay, SW_HIDE);
        if (wait == WAIT_OBJECT_0) return GetOverlappedResult(pipe, &op, &done, FALSE) != FALSE;
        if (wait == WAIT_OBJECT_0 + 1 || wait == WAIT_FAILED) { CancelIoEx(pipe, &op); GetOverlappedResult(pipe, &op, &done, TRUE); return false; }
    }
}
bool transfer(HANDLE pipe, void* data, DWORD bytes, bool writing, HANDLE game, HWND overlay, DWORD pid) {
    auto* p = static_cast<uint8_t*>(data);
    while (bytes) {
        OVERLAPPED op{}; op.hEvent = CreateEventW(nullptr, TRUE, FALSE, nullptr); if (!op.hEvent) return false;
        DWORD done = 0; BOOL ok = writing ? WriteFile(pipe, p, bytes, &done, &op) : ReadFile(pipe, p, bytes, &done, &op);
        if (!ok && GetLastError() == ERROR_IO_PENDING) ok = waitIo(pipe, op, game, overlay, pid, done);
        CloseHandle(op.hEvent); if (!ok || !done) return false; p += done; bytes -= done;
    }
    return true;
}
bool present(HWND overlay, HWND target, const std::vector<uint8_t>& frame, const std::vector<Command>& commands, Textures& textures) {
    RECT rect{}; if (!GetClientRect(target, &rect)) return false;
    POINT origin{}; ClientToScreen(target, &origin);
    int width = rect.right, height = rect.bottom;
    if (width <= 0 || height <= 0 || width > 16384 || height > 16384 || uint64_t(width) * height > 33554432) return false;
    BITMAPINFO info{}; info.bmiHeader.biSize = sizeof(BITMAPINFOHEADER); info.bmiHeader.biWidth = width; info.bmiHeader.biHeight = -height;
    info.bmiHeader.biPlanes = 1; info.bmiHeader.biBitCount = 32; info.bmiHeader.biCompression = BI_RGB;
    HDC screen = GetDC(nullptr), dc = CreateCompatibleDC(screen); void* bits = nullptr;
    HBITMAP bitmap = CreateDIBSection(screen, &info, DIB_RGB_COLORS, &bits, nullptr, 0);
    if (!bitmap || !dc) { if (bitmap) DeleteObject(bitmap); if (dc) DeleteDC(dc); ReleaseDC(nullptr, screen); return false; }
    HGDIOBJ old = SelectObject(dc, bitmap); bool drawn;
    {
        Gdiplus::Bitmap surface(width, height, width * 4, PixelFormat32bppPARGB, static_cast<BYTE*>(bits)); Gdiplus::Graphics g(&surface);
        g.Clear(Gdiplus::Color(0, 0, 0, 0)); g.ScaleTransform(width / float(word(frame, 24)), height / float(word(frame, 28)));
        drawn = render(g, frame, commands, textures);
    }
    SIZE size{width, height}; POINT source{}; BLENDFUNCTION blend{AC_SRC_OVER, 0, 255, AC_SRC_ALPHA};
    if (drawn) drawn = UpdateLayeredWindow(overlay, screen, &origin, &size, dc, &source, 0, &blend, ULW_ALPHA) != FALSE;
    SelectObject(dc, old); DeleteObject(bitmap); DeleteDC(dc); ReleaseDC(nullptr, screen); return drawn;
}
}
bool validateVisualFrame(const std::vector<uint8_t>& frame) { std::vector<Command> commands; return decode(frame, commands); }
bool startVisualOverlay(DWORD pid, const std::string& nonce) {
    if (nonce.size() != 32 || nonce.find_first_not_of("0123456789abcdef") != std::string::npos) return false;
    wchar_t exe[32768]; if (!GetModuleFileNameW(nullptr, exe, 32768)) return false;
    std::wstring cmd = L"\"" + std::wstring(exe) + L"\" --visual-overlay " + std::to_wstring(pid) + L" " + std::wstring(nonce.begin(), nonce.end());
    STARTUPINFOW si{}; si.cb = sizeof(si); si.dwFlags = STARTF_USESHOWWINDOW; si.wShowWindow = SW_HIDE; PROCESS_INFORMATION pi{};
    if (!CreateProcessW(exe, cmd.data(), nullptr, nullptr, FALSE, CREATE_NO_WINDOW, nullptr, nullptr, &si, &pi)) return false;
    CloseHandle(pi.hThread); CloseHandle(pi.hProcess); return true;
}
int runVisualOverlay(DWORD pid, const std::wstring& nonce) {
    if (!pid || nonce.size() != 32 || nonce.find_first_not_of(L"0123456789abcdef") != std::wstring::npos) return 2;
    SetProcessDPIAware(); HANDLE game = OpenProcess(SYNCHRONIZE, FALSE, pid); if (!game) return 3;
    ULONG_PTR gdiplus = 0; Gdiplus::GdiplusStartupInput startup;
    if (Gdiplus::GdiplusStartup(&gdiplus, &startup, nullptr) != Gdiplus::Ok) { CloseHandle(game); return 4; }
    HWND overlay = createOverlay();
    const auto name = L"\\\\.\\pipe\\sojuVisuals-" + nonce;
    Textures textures; int generation = 1;
    while (WaitForSingleObject(game, 0) == WAIT_TIMEOUT) {
        HANDLE pipe = CreateNamedPipeW(name.c_str(), PIPE_ACCESS_DUPLEX | FILE_FLAG_OVERLAPPED | FILE_FLAG_FIRST_PIPE_INSTANCE,
            PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT | PIPE_REJECT_REMOTE_CLIENTS, 1, MaxBytes, MaxBytes, 0, nullptr);
        if (pipe == INVALID_HANDLE_VALUE) break;
        OVERLAPPED op{}; op.hEvent = CreateEventW(nullptr, TRUE, FALSE, nullptr); DWORD done = 0;
        BOOL connected = ConnectNamedPipe(pipe, &op);
        if (!connected) {
            DWORD error = GetLastError();
            connected = error == ERROR_PIPE_CONNECTED || (error == ERROR_IO_PENDING && waitIo(pipe, op, game, overlay, pid, done));
        }
        CloseHandle(op.hEvent);
        textures.clear(); ++generation;
        while (connected && WaitForSingleObject(game, 0) == WAIT_TIMEOUT) {
            uint32_t length = 0;
            if (!transfer(pipe, &length, 4, false, game, overlay, pid) || length < 32 || length > MaxBytes - 4) break;
            std::vector<uint8_t> frame(length + 4); std::memcpy(frame.data(), &length, 4);
            if (!transfer(pipe, frame.data() + 4, length, false, game, overlay, pid)) break;
            std::vector<Command> commands; uint32_t status = overlay ? 1u : 16u;
            if (textures.size() > 4096) { textures.clear(); ++generation; status |= 2; }
            bool valid = decode(frame, commands);
            if (!valid) { status |= 8; if (overlay) ShowWindow(overlay, SW_HIDE); }
            else if (overlay) {
                // Keep acknowledged uploads even if a frame races focus loss.
                Gdiplus::Bitmap scratch(1, 1, PixelFormat32bppPARGB); Gdiplus::Graphics upload(&scratch);
                if (!render(upload, frame, commands, textures, true)) status |= 128;
                Target target{pid}; EnumWindows(findWindow, reinterpret_cast<LPARAM>(&target));
                if (!target.hwnd) { status |= 32; ShowWindow(overlay, SW_HIDE); }
                else {
                    DWORD foreground = 0; GetWindowThreadProcessId(GetForegroundWindow(), &foreground);
                    if (commands.empty() || foreground != pid || IsIconic(target.hwnd) || !IsWindowVisible(target.hwnd)) { status |= 64; ShowWindow(overlay, SW_HIDE); }
                    else if (present(overlay, target.hwnd, frame, commands, textures)) {
                        SetWindowPos(overlay, HWND_TOPMOST, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOACTIVATE | SWP_SHOWWINDOW);
                    } else { status |= 128; ShowWindow(overlay, SW_HIDE); }
                }
            }
            uint32_t response[] = {Magic, Version, status, uint32_t(generation), 0, 0};
            if (!transfer(pipe, response, sizeof(response), true, game, overlay, pid)) break;
            pump();
        }
        if (overlay) ShowWindow(overlay, SW_HIDE);
        DisconnectNamedPipe(pipe); CloseHandle(pipe);
    }
    textures.clear(); if (overlay) DestroyWindow(overlay); Gdiplus::GdiplusShutdown(gdiplus); CloseHandle(game); return 0;
}
int testVisualOverlay() {
    ULONG_PTR token; Gdiplus::GdiplusStartupInput startup;
    if (Gdiplus::GdiplusStartup(&token, &startup, nullptr) != Gdiplus::Ok) return 1;
    int result = 0;
    {
        HWND window = createOverlay(); if (!window) result = 2;
        else { DWORD affinity = 0; if (!GetWindowDisplayAffinity(window, &affinity) || affinity != 0x11) result = 3; DestroyWindow(window); }
        std::vector<uint8_t> frame(84, 0);
        const auto put = [&](size_t p, uint32_t v) { std::memcpy(frame.data() + p, &v, 4); };
        put(0, 80); put(4, Magic); put(8, Version); put(24, 64); put(28, 64); put(32, 1); put(36, 2); put(40, 0xFF55AD83);
        float data[] = {4, 4, 60, 60, 0, 5, 0, 0}; std::memcpy(frame.data() + 52, data, 32);
        std::vector<Command> commands; if (!decode(frame, commands)) result = 4;
        Gdiplus::Bitmap image(64, 64, PixelFormat32bppPARGB); Gdiplus::Graphics g(&image); g.Clear(Gdiplus::Color(0, 0, 0, 0)); Textures textures;
        if (!render(g, frame, commands, textures)) result = 5;
        g.Flush(); Gdiplus::Color pixel; image.GetPixel(32, 32, &pixel);
        if (pixel.GetG() != 173 || pixel.GetA() != 255) result = 6;
        HWND target = CreateWindowExW(0, L"STATIC", L"soju overlay test", WS_POPUP, 0, 0, 64, 64, nullptr, nullptr, GetModuleHandleW(nullptr), nullptr);
        HWND presentation = createOverlay();
        if (!target || !presentation || !present(presentation, target, frame, commands, textures)) result = 8;
        if (presentation) { ShowWindow(presentation, SW_HIDE); DestroyWindow(presentation); }
        if (target) DestroyWindow(target);
        frame.pop_back(); if (validateVisualFrame(frame)) result = 7;
    }
    Gdiplus::GdiplusShutdown(token); return result;
}
int testVisualFrameFile(const std::wstring& path) {
    std::ifstream file(std::filesystem::path(path), std::ios::binary);
    std::vector<uint8_t> frame((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
    std::vector<Command> commands; if (!decode(frame, commands)) return 1;
    ULONG_PTR token; Gdiplus::GdiplusStartupInput startup;
    if (Gdiplus::GdiplusStartup(&token, &startup, nullptr) != Gdiplus::Ok) return 2;
    int result = 0;
    {
        Gdiplus::Bitmap image(word(frame, 24), word(frame, 28), PixelFormat32bppPARGB);
        Gdiplus::Graphics graphics(&image); graphics.Clear(Gdiplus::Color(0, 0, 0, 0)); Textures textures;
        if (!render(graphics, frame, commands, textures)) result = 3;
        graphics.Flush(); Gdiplus::Color pixel; bool visible = false;
        for (UINT y = 0; y < image.GetHeight() && !visible; ++y) for (UINT x = 0; x < image.GetWidth() && !visible; ++x) {
            image.GetPixel(x, y, &pixel); visible = pixel.GetA() > 0;
        }
        if (!visible) result = 4;
    }
    Gdiplus::GdiplusShutdown(token); return result;
}
}
