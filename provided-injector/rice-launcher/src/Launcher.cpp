#include "Launcher.h"
#include "Logger.h"
#include "Process.h"
#include "Loader.h"
#include "resource.h"

#include <windows.h>
#include <windowsx.h>
#include <commctrl.h>
#include <objidl.h>
#include <dwmapi.h>
#include <gdiplus.h>
#include <algorithm>
#include <cwchar>
#include <cstring>
#include <vector>

namespace soju {

static const wchar_t* kClassName = L"sojuLauncherWindow";

#define WM_APP_MCSTATUS    (WM_APP + 1)
#define WM_APP_STATUS      (WM_APP + 2)
#define WM_APP_PHASE       (WM_APP + 3)
#define WM_APP_LOG         (WM_APP + 4)
#define WM_APP_AUTH_RESULT (WM_APP + 5)

#ifndef DWMWA_WINDOW_CORNER_PREFERENCE
#define DWMWA_WINDOW_CORNER_PREFERENCE 33
#endif
#ifndef DWMWCP_ROUND
#define DWMWCP_ROUND 2
#endif
#ifndef DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2
#define DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2 ((DPI_AWARENESS_CONTEXT)-4)
#endif
#ifndef CLEARTYPE_NATURAL_QUALITY
#define CLEARTYPE_NATURAL_QUALITY 6
#endif

// Blush surfaces, warm ink and rose accents.
static const COLORREF kBg       = RGB(255, 249, 251);
static const COLORREF kCard     = RGB(255, 255, 255);
static const COLORREF kCardLine = RGB(236, 216, 225);
static const COLORREF kChip     = RGB(249, 229, 237);
static const COLORREF kAccent   = RGB(162, 59, 100);
static const COLORREF kAccentIn = RGB(78, 31, 50);
static const COLORREF kText     = RGB(59, 40, 49);
static const COLORREF kDim      = RGB(117, 91, 104);
static const COLORREF kFaint    = RGB(137, 105, 119);
static const COLORREF kStatus   = RGB(117, 91, 104);
static const COLORREF kPillLine = RGB(226, 192, 207);
static const COLORREF kStepOff  = RGB(242, 225, 232);
static const COLORREF kLogBg    = RGB(255, 249, 251);
static const COLORREF kLogLine  = RGB(236, 216, 225);
static const COLORREF kLogText  = RGB(117, 91, 104);
static const COLORREF kDoneP    = RGB(117, 91, 104);
static const COLORREF kBad      = RGB(174, 47, 72);

// Paint with premultiplied alpha so DWM can show its backdrop through the window.
static thread_local Gdiplus::Graphics* frameGraphics = nullptr;
struct AlphaFrame {
    HDC dc;
    HBITMAP bitmap;
    HGDIOBJ previous;
    void* pixels = nullptr;
    int width, height;
    AlphaFrame(HDC screen, int w, int h) : width(w), height(h) {
        BITMAPINFO info{};
        info.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
        info.bmiHeader.biWidth = w;
        info.bmiHeader.biHeight = -h;
        info.bmiHeader.biPlanes = 1;
        info.bmiHeader.biBitCount = 32;
        info.bmiHeader.biCompression = BI_RGB;
        dc = CreateCompatibleDC(screen);
        bitmap = CreateDIBSection(screen, &info, DIB_RGB_COLORS, &pixels, nullptr, 0);
        previous = SelectObject(dc, bitmap);
        if (pixels) ZeroMemory(pixels, static_cast<size_t>(w) * h * 4);
    }
    ~AlphaFrame() { SelectObject(dc, previous); DeleteObject(bitmap); DeleteDC(dc); }
};

static void drawText(HDC dc, const RECT& r, const std::wstring& s, HFONT f,
                     COLORREF c, UINT fmt) {
    Gdiplus::Graphics fallback(dc);
    Gdiplus::Graphics& graphics = frameGraphics ? *frameGraphics : fallback;
    graphics.SetTextRenderingHint(Gdiplus::TextRenderingHintAntiAliasGridFit);
    graphics.SetPixelOffsetMode(Gdiplus::PixelOffsetModeHalf);

    LOGFONTW lf = {};
    GetObjectW(f, sizeof(lf), &lf);
    Gdiplus::Font font(lf.lfFaceName, static_cast<Gdiplus::REAL>(-lf.lfHeight),
        lf.lfWeight >= FW_SEMIBOLD ? Gdiplus::FontStyleBold : Gdiplus::FontStyleRegular, Gdiplus::UnitPixel);
    Gdiplus::SolidBrush brush(Gdiplus::Color(255, GetRValue(c), GetGValue(c), GetBValue(c)));
    Gdiplus::StringFormat format;
    format.SetTrimming((fmt & DT_END_ELLIPSIS) ? Gdiplus::StringTrimmingEllipsisCharacter : Gdiplus::StringTrimmingNone);
    if (fmt & DT_SINGLELINE) format.SetFormatFlags(format.GetFormatFlags() | Gdiplus::StringFormatFlagsNoWrap);
    if (fmt & DT_RIGHT) {
        format.SetAlignment(Gdiplus::StringAlignmentFar);
    } else if (fmt & DT_CENTER) {
        format.SetAlignment(Gdiplus::StringAlignmentCenter);
    } else {
        format.SetAlignment(Gdiplus::StringAlignmentNear);
    }
    format.SetLineAlignment((fmt & DT_VCENTER) ? Gdiplus::StringAlignmentCenter : Gdiplus::StringAlignmentNear);

    Gdiplus::RectF box(
        static_cast<Gdiplus::REAL>(r.left),
        static_cast<Gdiplus::REAL>(r.top),
        static_cast<Gdiplus::REAL>(r.right - r.left),
        static_cast<Gdiplus::REAL>(r.bottom - r.top)
    );
    graphics.DrawString(s.c_str(), -1, &font, box, &format, &brush);
}

static const UINT DT_CTR  = DT_CENTER | DT_VCENTER | DT_SINGLELINE | DT_END_ELLIPSIS;
static const UINT DT_LEFTV = DT_LEFT | DT_VCENTER | DT_SINGLELINE | DT_END_ELLIPSIS;
static const UINT DT_RIGHTV = DT_RIGHT | DT_VCENTER | DT_SINGLELINE;
static const UINT DT_WRAP = DT_CENTER | DT_WORDBREAK;

// ── Lifecycle ─────────────────────────────────────────────────────────────────

int Launcher::run(HINSTANCE hinst, const std::wstring& preview, const std::wstring& renderPath) {
    hinst_ = hinst;
    previewMode_ = !preview.empty();
    Gdiplus::GdiplusStartupInput gdiplusInput;
    Gdiplus::GdiplusStartup(&gdiplusToken_, &gdiplusInput, nullptr);
    if (!previewMode_) paths_ = Resources::resolve();
    bool console = false;
#ifdef SOJU_DEBUG
    console = true;
#endif
    if (!previewMode_) Logger::init(paths_.logs, console);
    Logger::info(L"Rice loader starting (v" SOJU_VERSION L")");
    if (!previewMode_) Resources::migrateFromJade(paths_);
    if (!previewMode_ && !Resources::materializeBundledRuntime(paths_)) {
        Logger::warn(L"Bundled runtime extraction failed; falling back to files beside the launcher.");
    }

    createWindow();
    if (!hwnd_) { Logger::error(L"Failed to create the injector window."); return 1; }

    if (previewMode_) {
        if (preview == L"ready") { phase_ = PHASE_READY; mcReady_ = true; mcStatus_ = L"Minecraft 1.8.9 · preview"; status_ = L"Minecraft is ready. Let's get you in."; }
        if (preview == L"loading") { phase_ = PHASE_INJECTING; loading_ = true; logLines_ = {{L"Minecraft 1.8.9 detected", true}, {L"Client prepared", true}, {L"Connecting to your game…", false}}; }
        if (preview == L"done") { phase_ = PHASE_DONE; status_ = L"Your client is ready."; }
        if (preview == L"error") { phase_ = PHASE_FAILED; status_ = L"Minecraft closed before the client finished loading. Open the game and try again."; }
        if (preview == L"checking") loading_ = true;
        if (phase_ != PHASE_AUTH) ShowWindow(licenseEdit_, SW_HIDE);
    }
    if (!renderPath.empty()) {
        HDC screen = GetDC(hwnd_);
        AlphaFrame frame(screen, W_, H_);
        Gdiplus::Bitmap image(W_, H_, W_ * 4, PixelFormat32bppPARGB, static_cast<BYTE*>(frame.pixels));
        {
            Gdiplus::Graphics graphics(&image);
            frameGraphics = &graphics;
            paint(frame.dc);
            frameGraphics = nullptr;
        }
        const CLSID png = {0x557cf406, 0x1a04, 0x11d3, {0x9a, 0x73, 0x00, 0x00, 0xf8, 0x1e, 0xf3, 0x2e}};
        if (shader_) {
            std::vector<unsigned char> composited;
            if (!shader_->render(frame.pixels, 2.f, POINT{680, 210}) || !shader_->readback(composited)) return 2;
            std::memcpy(frame.pixels, composited.data(), composited.size());
        }
        auto saved = image.Save(renderPath.c_str(), &png, nullptr);
        ReleaseDC(hwnd_, screen);
        DestroyWindow(hwnd_);
        return saved == Gdiplus::Ok ? 0 : 1;
    }
    SetTimer(hwnd_, 2, 33, nullptr);
    ShowWindow(hwnd_, SW_SHOW);
    UpdateWindow(hwnd_);
    if (phase_ == PHASE_AUTH && !previewMode_) SetFocus(licenseEdit_);

    MSG msg;
    while (GetMessageW(&msg, nullptr, 0, 0) > 0) {
        TranslateMessage(&msg);
        DispatchMessageW(&msg);
    }

    running_ = false;
    if (authThread_) {
        WaitForSingleObject(authThread_, INFINITE);
        CloseHandle(authThread_);
        authThread_ = nullptr;
    }
    if (detectThread_) {
        WaitForSingleObject(detectThread_, INFINITE);
        CloseHandle(detectThread_);
        detectThread_ = nullptr;
    }
    // Workers hold a pointer to this launcher until their entry point returns.
    if (loadThread_) {
        WaitForSingleObject(loadThread_, INFINITE);
        CloseHandle(loadThread_);
        loadThread_ = nullptr;
    }
    secureClearLicense();
    for (HFONT f : { fontBrand_, fontH1_, fontBody_, fontBtn_, fontSmall_, fontMono_, fontIcon_ })
        if (f) DeleteObject(f);
    if (bgBrush_) DeleteObject(bgBrush_);
    if (gdiplusToken_) {
        Gdiplus::GdiplusShutdown(gdiplusToken_);
        gdiplusToken_ = 0;
    }
    Logger::info(L"Rice loader exiting.");
    return 0;
}

void Launcher::createWindow() {
    SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);
    bgBrush_ = CreateSolidBrush(kBg);
    if (!previewMode_) loadSavedLicense();

    auto makeFont = [&](int px, int weight, const wchar_t* face) {
        return CreateFontW(-px, 0, 0, 0, weight, FALSE, FALSE, FALSE, DEFAULT_CHARSET,
            OUT_TT_PRECIS, CLIP_DEFAULT_PRECIS, CLEARTYPE_NATURAL_QUALITY, FF_DONTCARE, face);
    };
    fontBrand_ = makeFont(42, FW_BOLD, L"Georgia");
    fontH1_    = makeFont(32, FW_SEMIBOLD,  L"Segoe UI");
    fontBody_  = makeFont(15, FW_NORMAL,    L"Segoe UI");
    fontBtn_   = makeFont(15, FW_SEMIBOLD,  L"Segoe UI");
    fontSmall_ = makeFont(12, FW_MEDIUM,    L"Segoe UI");
    fontMono_  = makeFont(11, FW_NORMAL,    L"Consolas");
    fontIcon_  = makeFont(30, FW_SEMIBOLD,  L"Segoe UI");

    WNDCLASSEXW wc = {};
    wc.cbSize = sizeof(wc);
    wc.lpfnWndProc = wndProc;
    wc.hInstance = hinst_;
    wc.hCursor = LoadCursorW(nullptr, IDC_ARROW);
    wc.hbrBackground = nullptr;
    wc.lpszClassName = kClassName;
    wc.hIcon = LoadIconW(hinst_, MAKEINTRESOURCEW(IDI_SOJU));
    RegisterClassExW(&wc);

    int x = (GetSystemMetrics(SM_CXSCREEN) - W_) / 2;
    int y = (GetSystemMetrics(SM_CYSCREEN) - H_) / 2;

    DWORD style = WS_POPUP | WS_SYSMENU | WS_MINIMIZEBOX | WS_CLIPCHILDREN;
    hwnd_ = CreateWindowExW(WS_EX_APPWINDOW, kClassName, L"Rice Slinky · Loader",
                            style, x, y, W_, H_, nullptr, nullptr, hinst_, this);
    if (!hwnd_) return;

    DWORD pref = 1; // DWMWCP_DONOTROUND: square window corners.
    DwmSetWindowAttribute(hwnd_, DWMWA_WINDOW_CORNER_PREFERENCE, &pref, sizeof(pref));
    // Windows 11 desktop acrylic: the compositor blurs the actual windows behind us.
    const DWORD backdrop = 3; // DWMSBT_TRANSIENTWINDOW
    glassEnabled_ = SUCCEEDED(DwmSetWindowAttribute(hwnd_, 38, &backdrop, sizeof(backdrop)));
    if (glassEnabled_) {
        const BOOL dark = FALSE;
        DwmSetWindowAttribute(hwnd_, 20, &dark, sizeof(dark));
        MARGINS glass = {-1, -1, -1, -1};
        glassEnabled_ = SUCCEEDED(DwmExtendFrameIntoClientArea(hwnd_, &glass));
    }
    shader_ = std::make_unique<GlassRenderer>();
    if (!shader_->initialize(hwnd_, W_, H_, previewMode_)) {
        shader_.reset();
        SetWindowDisplayAffinity(hwnd_, 0);
        Logger::warn(L"Shader backdrop unavailable; using the Windows backdrop.");
    }
    // Explicitly clip to a rectangular region as well as requesting square DWM corners.
    HRGN rectangularWindow = CreateRectRgn(0, 0, W_, H_);
    if (rectangularWindow && !SetWindowRgn(hwnd_, rectangularWindow, TRUE)) DeleteObject(rectangularWindow);
    shaderStarted_ = GetTickCount64();


}

LRESULT CALLBACK Launcher::licenseProc(HWND edit, UINT message, WPARAM wp, LPARAM lp) {
    auto* self = reinterpret_cast<Launcher*>(GetWindowLongPtrW(edit, GWLP_USERDATA));
    // Standard edit controls use GDI with zero alpha. Make their surface opaque
    // while retaining native password masking, selection, clipboard and caret behavior.
    if (message == WM_PAINT && self->glassEnabled_) {
        PAINTSTRUCT ps{};
        HDC dc = BeginPaint(edit, &ps);
        RECT rect{}; GetClientRect(edit, &rect);
        AlphaFrame frame(dc, rect.right, rect.bottom);
        CallWindowProcW(self->originalEditProc_, edit, WM_PRINTCLIENT,
            reinterpret_cast<WPARAM>(frame.dc), PRF_CLIENT | PRF_ERASEBKGND);
        GdiFlush();
        auto* pixels = static_cast<DWORD*>(frame.pixels);
        for (int i = 0; i < rect.right * rect.bottom; ++i) pixels[i] |= 0xff000000;
        BitBlt(dc, 0, 0, rect.right, rect.bottom, frame.dc, 0, 0, SRCCOPY);
        EndPaint(edit, &ps);
        return 0;
    }
    if (message == WM_KEYDOWN && wp == VK_RETURN) { self->onAuthClicked(); return 0; }
    if (message == WM_CHAR && wp == VK_RETURN) return 0;
    if (message == WM_KEYDOWN && wp == 'A' && (GetKeyState(VK_CONTROL) & 0x8000)) {
        SendMessageW(edit, EM_SETSEL, 0, -1); return 0;
    }
    return CallWindowProcW(self->originalEditProc_, edit, message, wp, lp);
}

struct AuthCtx { Launcher* self; std::wstring licenseKey; };
struct LoadCtx { Launcher* self; DWORD pid; };

DWORD WINAPI Launcher::authThreadProc(LPVOID param) {
    auto* ctx = reinterpret_cast<AuthCtx*>(param);
    ctx->self->doAuth(ctx->licenseKey);
    if (!ctx->licenseKey.empty()) {
        SecureZeroMemory(&ctx->licenseKey[0], ctx->licenseKey.size() * sizeof(wchar_t));
    }
    delete ctx;
    return 0;
}

DWORD WINAPI Launcher::detectThreadProc(LPVOID param) {
    reinterpret_cast<Launcher*>(param)->detectionLoop();
    return 0;
}

DWORD WINAPI Launcher::loadThreadProc(LPVOID param) {
    auto* ctx = reinterpret_cast<LoadCtx*>(param);
    ctx->self->doLoad(ctx->pid);
    delete ctx;
    return 0;
}

LRESULT CALLBACK Launcher::wndProc(HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
    Launcher* self = nullptr;
    if (msg == WM_NCCREATE) {
        auto* cs = reinterpret_cast<CREATESTRUCTW*>(lp);
        self = static_cast<Launcher*>(cs->lpCreateParams);
        SetWindowLongPtrW(hwnd, GWLP_USERDATA, reinterpret_cast<LONG_PTR>(self));
    } else {
        self = reinterpret_cast<Launcher*>(GetWindowLongPtrW(hwnd, GWLP_USERDATA));
    }
    if (self) return self->handle(hwnd, msg, wp, lp);
    return DefWindowProcW(hwnd, msg, wp, lp);
}

// ── Message handling ──────────────────────────────────────────────────────────

LRESULT Launcher::handle(HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
    switch (msg) {
        case WM_CREATE:
            hwnd_ = hwnd;
            layout();
            licenseEdit_ = CreateWindowExW(0, L"EDIT", L"", WS_CHILD | WS_VISIBLE | WS_TABSTOP |
                ES_AUTOHSCROLL | ES_PASSWORD, rcLicenseField_.left + 14, rcLicenseField_.top + 13,
                rcLicenseField_.right - rcLicenseField_.left - 28, 24,
                hwnd, reinterpret_cast<HMENU>(100), hinst_, nullptr);
            SendMessageW(licenseEdit_, WM_SETFONT, reinterpret_cast<WPARAM>(fontBody_), TRUE);
            SendMessageW(licenseEdit_, EM_SETLIMITTEXT, 256, 0);
            SendMessageW(licenseEdit_, EM_SETPASSWORDCHAR, L'•', 0);
            SendMessageW(licenseEdit_, EM_SETCUEBANNER, FALSE, reinterpret_cast<LPARAM>(L"Paste your license key"));
            SetWindowLongPtrW(licenseEdit_, GWLP_USERDATA, reinterpret_cast<LONG_PTR>(this));
            originalEditProc_ = reinterpret_cast<WNDPROC>(SetWindowLongPtrW(licenseEdit_, GWLP_WNDPROC,
                reinterpret_cast<LONG_PTR>(licenseProc)));
            SetWindowTextW(licenseEdit_, licenseKey_.c_str());
            return 0;

        case WM_CTLCOLOREDIT:
            SetTextColor(reinterpret_cast<HDC>(wp), kText);
            SetBkColor(reinterpret_cast<HDC>(wp), kBg);
            return reinterpret_cast<LRESULT>(bgBrush_);

        case WM_COMMAND:
            if (LOWORD(wp) == 100 && HIWORD(wp) == EN_CHANGE) {
                wchar_t buffer[257]{};
                GetWindowTextW(licenseEdit_, buffer, 257);
                secureClearLicense();
                licenseKey_ = buffer;
                SecureZeroMemory(buffer, sizeof(buffer));
                InvalidateRect(hwnd_, nullptr, FALSE);
                return 0;
            }
            if (LOWORD(wp) == 100 && (HIWORD(wp) == EN_SETFOCUS || HIWORD(wp) == EN_KILLFOCUS)) {
                InvalidateRect(hwnd_, nullptr, FALSE);
                return 0;
            }
            break;

        case WM_NCCALCSIZE:
            if (wp) return 0;              // borderless: whole window is client
            break;

        case WM_NCHITTEST: {
            POINT pt = { GET_X_LPARAM(lp), GET_Y_LPARAM(lp) };
            ScreenToClient(hwnd_, &pt);
            if (PtInRect(&rcClose_, pt)) return HTCLOSE;
            if (PtInRect(&rcMin_, pt))   return HTMINBUTTON;
            if (pt.y < 84) return HTCAPTION;   // header strip drags the window
            return HTCLIENT;
        }

        case WM_NCMOUSEMOVE: {
            bool c = (wp == HTCLOSE), m = (wp == HTMINBUTTON);
            if (c != hoverClose_ || m != hoverMin_) {
                hoverClose_ = c; hoverMin_ = m;
                InvalidateRect(hwnd_, nullptr, FALSE);
            }
            TRACKMOUSEEVENT tme = { sizeof(tme), TME_LEAVE | TME_NONCLIENT, hwnd_, 0 };
            TrackMouseEvent(&tme);
            return 0;
        }
        case WM_NCMOUSELEAVE:
            if (hoverClose_ || hoverMin_) {
                hoverClose_ = hoverMin_ = false;
                InvalidateRect(hwnd_, nullptr, FALSE);
            }
            return 0;

        case WM_PAINT: {
            PAINTSTRUCT ps;
            HDC dc = BeginPaint(hwnd_, &ps);
            RECT rc; GetClientRect(hwnd_, &rc);
            AlphaFrame frame(dc, rc.right, rc.bottom);
            Gdiplus::Bitmap image(rc.right, rc.bottom, rc.right * 4, PixelFormat32bppPARGB, static_cast<BYTE*>(frame.pixels));
            {
                Gdiplus::Graphics graphics(&image);
                frameGraphics = &graphics;
                paint(frame.dc);
                frameGraphics = nullptr;
            }
            POINT pointer{}; GetCursorPos(&pointer); ScreenToClient(hwnd_, &pointer);
            BOOL animations = TRUE;
            SystemParametersInfoW(SPI_GETCLIENTAREAANIMATION, 0, &animations, 0);
            float seconds = animations ? static_cast<float>(GetTickCount64() - shaderStarted_) / 1000.f : 0.f;
            if (!shader_ || !shader_->render(frame.pixels, seconds, pointer))
                BitBlt(dc, 0, 0, rc.right, rc.bottom, frame.dc, 0, 0, SRCCOPY);
            EndPaint(hwnd_, &ps);
            return 0;
        }

        case WM_TIMER:
            if (wp == 2 && shader_ && !IsIconic(hwnd_)) InvalidateRect(hwnd_, nullptr, FALSE);
            return 0;

        case WM_ERASEBKGND:
            return 1;

        case WM_MOUSEMOVE: {
            POINT pt = { GET_X_LPARAM(lp), GET_Y_LPARAM(lp) };
            bool over = PtInRect(&rcButton_, pt) &&
                (phase_ == PHASE_AUTH ? (!loading_.load() && !licenseKey_.empty())
                 : phase_ == PHASE_READY ? (mcReady_ && !loading_.load())
                                         : (phase_ == PHASE_DONE || phase_ == PHASE_FAILED));
            if (over != hoverBtn_) { hoverBtn_ = over; InvalidateRect(hwnd_, nullptr, FALSE); }
            TRACKMOUSEEVENT tme = { sizeof(tme), TME_LEAVE, hwnd_, 0 };
            TrackMouseEvent(&tme);
            return 0;
        }
        case WM_MOUSELEAVE:
            if (hoverBtn_) { hoverBtn_ = false; InvalidateRect(hwnd_, nullptr, FALSE); }
            return 0;

        case WM_CHAR:
            if (!previewMode_ && phase_ == PHASE_AUTH && !loading_) {
                SetFocus(licenseEdit_);
                SendMessageW(licenseEdit_, WM_CHAR, wp, lp);
                return 0;
            }
            break;

        case WM_KEYDOWN:
            if (previewMode_) break;
            if (wp == VK_RETURN) {
                if (phase_ == PHASE_AUTH) onAuthClicked();
                else if (phase_ == PHASE_READY) onLoadClicked();
                else if (phase_ == PHASE_DONE || phase_ == PHASE_FAILED) PostMessageW(hwnd_, WM_CLOSE, 0, 0);
                return 0;
            }
            if (phase_ == PHASE_AUTH && !loading_) {
                if (wp == VK_TAB) { SetFocus(licenseEdit_); return 0; }
                if ((GetKeyState(VK_CONTROL) & 0x8000) && wp == 'V') {
                    SetFocus(licenseEdit_); SendMessageW(licenseEdit_, WM_PASTE, 0, 0); return 0;
                }
            }
            break;

        case WM_LBUTTONDOWN: {
            if (previewMode_) return 0;
            POINT pt = { GET_X_LPARAM(lp), GET_Y_LPARAM(lp) };
            if (phase_ == PHASE_AUTH && PtInRect(&rcLicenseField_, pt)) SetFocus(licenseEdit_);
            if (phase_ == PHASE_AUTH && !loading_.load() && PtInRect(&rcButton_, pt)) {
                onAuthClicked();
            } else if (phase_ == PHASE_READY && mcReady_ && !loading_.load() &&
                PtInRect(&rcButton_, pt)) {
                onLoadClicked();
            } else if ((phase_ == PHASE_DONE || phase_ == PHASE_FAILED) &&
                       PtInRect(&rcButton_, pt)) {
                PostMessageW(hwnd_, WM_CLOSE, 0, 0);
            }
            return 0;
        }

        case WM_SETCURSOR:
            if (LOWORD(lp) == HTCLIENT) {
                POINT pt; GetCursorPos(&pt); ScreenToClient(hwnd_, &pt);
                bool over = PtInRect(&rcButton_, pt) &&
                    (phase_ == PHASE_AUTH ? (!loading_.load() && !licenseKey_.empty())
                     : phase_ == PHASE_READY ? (mcReady_ && !loading_.load())
                                             : (phase_ == PHASE_DONE || phase_ == PHASE_FAILED));
                SetCursor(LoadCursorW(nullptr, over ? IDC_HAND : IDC_ARROW));
                return TRUE;
            }
            break;

        case WM_APP_MCSTATUS: {
            auto* s = reinterpret_cast<std::pair<std::wstring, bool>*>(lp);
            if (s) { mcStatus_ = s->first; mcReady_ = s->second; delete s;
                     InvalidateRect(hwnd_, nullptr, FALSE); }
            return 0;
        }
        case WM_APP_STATUS: {
            auto* s = reinterpret_cast<std::wstring*>(lp);
            if (s) { status_ = *s; delete s; InvalidateRect(hwnd_, nullptr, FALSE); }
            return 0;
        }
        case WM_APP_PHASE: {
            auto* p = reinterpret_cast<std::pair<int, std::wstring>*>(lp);
            if (p) {
                phase_ = static_cast<Phase>(p->first);
                ShowWindow(licenseEdit_, phase_ == PHASE_AUTH ? SW_SHOW : SW_HIDE);
                if (phase_ == PHASE_INJECTING) logLines_.clear();
                if (phase_ == PHASE_DONE)   status_ = L"Injection completed successfully.";
                if (phase_ == PHASE_FAILED && !p->second.empty()) status_ = p->second;
                delete p;
                InvalidateRect(hwnd_, nullptr, FALSE);
            }
            return 0;
        }
        case WM_APP_LOG: {
            auto* l = reinterpret_cast<std::pair<std::wstring, bool>*>(lp);
            if (l) { lastLine_ = l->first; logLines_.push_back({ l->first, l->second });
                     delete l; InvalidateRect(hwnd_, nullptr, FALSE); }
            return 0;
        }
        case WM_APP_AUTH_RESULT: {
            auto* result = reinterpret_cast<KeyAuthResult*>(lp);
            if (result) {
                loading_ = false;
                EnableWindow(licenseEdit_, TRUE);
                if (result->success) {
                    authenticated_ = true;
                    phase_ = PHASE_READY;
                    ShowWindow(licenseEdit_, SW_HIDE);
                    SetWindowTextW(licenseEdit_, L"");
                    status_ = L"License accepted — looking for Minecraft.";
                    saveLicense(pendingLicense_.empty() ? licenseKey_ : pendingLicense_);
                    secureClearPendingLicense();
                    secureClearLicense();
                    startDetection();
                } else {
                    secureClearPendingLicense();
                    status_ = result->message.empty() ? L"Authentication failed." : result->message;
                }
                delete result;
                InvalidateRect(hwnd_, nullptr, FALSE);
            }
            return 0;
        }

        case WM_DESTROY: {
            KillTimer(hwnd_, 2);
            running_ = false;
            // Stop producers before discarding messages owned by the closing window.
            std::lock_guard<std::mutex> lock(messageMutex_);
            acceptingMessages_ = false;
            MSG pending;
            while (PeekMessageW(&pending, hwnd, WM_APP_MCSTATUS, WM_APP_AUTH_RESULT, PM_REMOVE)) {
                switch (pending.message) {
                    case WM_APP_MCSTATUS:
                    case WM_APP_LOG:
                        delete reinterpret_cast<std::pair<std::wstring, bool>*>(pending.lParam);
                        break;
                    case WM_APP_STATUS:
                        delete reinterpret_cast<std::wstring*>(pending.lParam);
                        break;
                    case WM_APP_PHASE:
                        delete reinterpret_cast<std::pair<int, std::wstring>*>(pending.lParam);
                        break;
                    case WM_APP_AUTH_RESULT:
                        delete reinterpret_cast<KeyAuthResult*>(pending.lParam);
                        break;
                }
            }
            PostQuitMessage(0);
            return 0;
        }
    }
    return DefWindowProcW(hwnd, msg, wp, lp);
}

// ── Geometry (client 720x490, padding 34) ───────────────────────────────────

void Launcher::layout() {
    rcMin_ = {830, 18, 862, 50};
    rcClose_ = {868, 18, 900, 50};
    rcH1_ = {222, 174, 698, 222};
    rcSub_ = {222, 228, 698, 258};
    rcLicenseLabel_ = {222, 286, 698, 307};
    rcLicenseField_ = {222, 318, 698, 366};
    rcTarget_ = {222, 286, 698, 366};
    rcButton_ = {222, 386, 698, 436};
    rcStatus_ = {222, 454, 698, 504};
}

// ── Painting primitives ──────────────────────────────────────────────────────

COLORREF Launcher::mix(COLORREF a, COLORREF b, double t) {
    auto L = [&](int ca, int cb) { return static_cast<int>(ca + (cb - ca) * t + 0.5); };
    return RGB(L(GetRValue(a), GetRValue(b)),
               L(GetGValue(a), GetGValue(b)),
               L(GetBValue(a), GetBValue(b)));
}

void Launcher::fillRoundRect(HDC dc, const RECT& r, int radius, COLORREF fill) const {
    if (frameGraphics) {
        Gdiplus::SolidBrush brush(Gdiplus::Color(255, GetRValue(fill), GetGValue(fill), GetBValue(fill)));
        frameGraphics->FillRectangle(&brush, static_cast<int>(r.left), static_cast<int>(r.top), static_cast<int>(r.right - r.left), static_cast<int>(r.bottom - r.top));
    } else {
        HBRUSH brush = CreateSolidBrush(fill); FillRect(dc, &r, brush); DeleteObject(brush);
    }
    (void)radius;
}

void Launcher::outlineRoundRect(HDC dc, const RECT& r, int radius, COLORREF outline, int width) const {
    if (frameGraphics) {
        Gdiplus::Pen pen(Gdiplus::Color(255, GetRValue(outline), GetGValue(outline), GetBValue(outline)), static_cast<float>(width));
        frameGraphics->DrawRectangle(&pen, static_cast<int>(r.left), static_cast<int>(r.top), static_cast<int>(r.right - r.left - 1), static_cast<int>(r.bottom - r.top - 1));
    }
    (void)dc; (void)radius;
}

void Launcher::drawSteps(HDC dc, int activeCount) const {
    const int gap = 8, n = 3;
    int fullW = rcSteps_.right - rcSteps_.left;
    int segW = (fullW - gap * (n - 1)) / n;
    for (int i = 0; i < n; ++i) {
        RECT s = { rcSteps_.left + i * (segW + gap), rcSteps_.top,
                   rcSteps_.left + i * (segW + gap) + segW, rcSteps_.bottom };
        fillRoundRect(dc, s, 2, i < activeCount ? kAccent : kStepOff);
    }
}

// ── Composite paint ──────────────────────────────────────────────────────────

void Launcher::paint(HDC dc) {
    RECT full = {0, 0, W_, H_};
    SetBkMode(dc, TRANSPARENT);
    if (frameGraphics) {
        frameGraphics->Clear((glassEnabled_ || shader_) ? Gdiplus::Color(0, 0, 0, 0) : Gdiplus::Color(255, 255, 249, 251));
        if (glassEnabled_ && !shader_) {
            Gdiplus::SolidBrush tint(Gdiplus::Color(52, 255, 225, 238));
            frameGraphics->FillRectangle(&tint, 0, 0, W_, H_);
        }
    } else FillRect(dc, &full, bgBrush_);
    const bool auth = phase_ == PHASE_AUTH, busy = loading_.load();
    const bool injecting = phase_ == PHASE_INJECTING;
    const bool done = phase_ == PHASE_DONE, failed = phase_ == PHASE_FAILED;
    auto text = [&](RECT box, const std::wstring& value, HFONT font, COLORREF ink, UINT align = DT_LEFTV) {
        drawText(dc, box, value, font, ink, align);
    };
    if (frameGraphics) {
        Gdiplus::SolidBrush panel(Gdiplus::Color(156, 255, 249, 252));
        frameGraphics->FillRectangle(&panel, 186, 126, 548, 394);
        Gdiplus::Pen edge(Gdiplus::Color(155, 255, 255, 255), 1.f);
        frameGraphics->DrawRectangle(&edge, 186, 126, 548, 394);
        Gdiplus::Pen frame(Gdiplus::Color(200, 239, 210, 224), 1.f);
        frameGraphics->DrawRectangle(&frame, 0, 0, W_ - 1, H_ - 1);
        Gdiplus::Pen divider(Gdiplus::Color(90, 255, 255, 255), 1.f);
        frameGraphics->DrawLine(&divider, 28, 72, W_ - 28, 72);
    }
    text({30, 20, 400, 52}, L"RICE  /  SLINKY", fontBtn_, kText);
    text({222, 144, 698, 164}, auth ? L"ACCOUNT ACCESS" : L"YOUR SESSION", fontSmall_, kAccent);
    if (hoverMin_) fillRoundRect(dc, rcMin_, 0, kChip);
    if (hoverClose_) fillRoundRect(dc, rcClose_, 0, kChip);
    text(rcMin_, L"–", fontBody_, kDim, DT_CTR);
    text(rcClose_, L"×", fontBody_, kDim, DT_CTR);
    text(rcH1_, auth ? L"Welcome back" : injecting ? L"Connecting" : done ? L"You’re in" : failed ? L"Connection failed" : L"Ready to launch", fontH1_, kText);
    text(rcSub_, auth ? L"Sign in to Rice Slinky to continue." : injecting ? L"Loading your client…" : done ? L"You're ready." : failed ? L"Couldn't load the client." : L"Connect to Minecraft.", fontBody_, kDim);
    bool enabled = false;
    std::wstring button, detail;
    if (auth) {
        text(rcLicenseLabel_, L"LICENSE KEY", fontSmall_, kDim);
        fillRoundRect(dc, rcLicenseField_, 0, kBg);
        outlineRoundRect(dc, rcLicenseField_, 0, kPillLine);
        if (previewMode_ || (licenseKey_.empty() && GetFocus() != licenseEdit_))
            text({236, 328, 684, 356}, L"Paste your license key", fontBody_, kFaint);
        enabled = !busy && !licenseKey_.empty();
        button = busy ? L"Checking…" : L"Continue  →";
        detail = status_;
    } else {
        fillRoundRect(dc, rcTarget_, 0, kChip);
        if (phase_ == PHASE_READY) {
            text({236, 296, 684, 320}, L"Minecraft 1.8.9", fontBtn_, kText);
            text({236, 330, 684, 356}, mcReady_ ? mcStatus_ : L"Waiting for your game…", fontSmall_, kDim);
            enabled = mcReady_ && !busy;
            button = L"Launch";
            detail = mcReady_ ? L"Minecraft is connected." : L"Open Minecraft 1.8.9 to continue.";
        } else if (injecting) {
            text({236, 296, 684, 320}, L"Connecting…", fontBtn_, kText);
            text({236, 330, 684, 356}, logLines_.empty() ? L"Preparing client" : logLines_.back().first, fontSmall_, kDim);
            button = L"Loading…";
            detail = L"Keep this window open.";
        } else {
            text({236, 296, 684, 356}, done ? L"Client loaded successfully." : L"Please try again.", fontBody_, failed ? kBad : kAccent);
            enabled = true;
            button = L"Close";
            detail = done ? L"You can return to Minecraft." : status_;
        }
    }
    COLORREF fill = enabled ? (hoverBtn_ ? RGB(141, 45, 84) : RGB(162, 59, 100)) : RGB(226, 188, 205);
    fillRoundRect(dc, rcButton_, 0, fill);
    text(rcButton_, button, fontBtn_, enabled ? RGB(255, 250, 252) : RGB(99, 60, 77), DT_CTR);
    text(rcStatus_, detail, fontSmall_, failed ? kBad : kStatus, DT_LEFT | DT_WORDBREAK);
    text({30, 551, 500, 575}, previewMode_ ? L"DESIGN PREVIEW" : L"RICE SLINKY   /   v1.8", fontSmall_, kDim);
    text({570, 551, 890, 575}, L"MINECRAFT 1.8.9", fontSmall_, kDim, DT_RIGHTV);
}

// ── Thread-safe UI updates ──────────────────────────────────────────────────

void Launcher::postMcStatus(const std::wstring& text, bool ready) {
    postUiMessage(WM_APP_MCSTATUS, new std::pair<std::wstring, bool>(text, ready));
}
void Launcher::postAuthResult(const KeyAuthResult& result) {
    postUiMessage(WM_APP_AUTH_RESULT, new KeyAuthResult(result));
}
void Launcher::postStatus(const std::wstring& text) {
    postUiMessage(WM_APP_STATUS, new std::wstring(text));
}
void Launcher::postPhase(Phase phase, const std::wstring& detail) {
    postUiMessage(WM_APP_PHASE, new std::pair<int, std::wstring>(phase, detail));
}
void Launcher::postLog(const std::wstring& text, bool ok) {
    postUiMessage(WM_APP_LOG, new std::pair<std::wstring, bool>(text, ok));
}

// ── Detection + load flow ───────────────────────────────────────────────────

void Launcher::startDetection() {
    if (running_) return;
    running_ = true;
    detectThread_ = CreateThread(nullptr, 0, detectThreadProc, this, 0, nullptr);
}

void Launcher::detectionLoop() {
    DWORD lastPid = 0;
    bool firstPass = true;
    std::wstring lastNote;
    while (running_) {
        if (!loading_) {
            std::vector<std::wstring> notes;
            auto games = Process::detectGames(notes);
            if (!games.empty()) {
                const auto& g = games.front();
                detectedPid_ = g.pid;
                { std::lock_guard<std::mutex> lk(envMutex_); environment_ = g.environment; }
                if (g.pid != lastPid) {
                    postMcStatus(g.environment + L"  ·  PID " + std::to_wstring(g.pid), true);
                    postStatus(L"Minecraft detected — ready to inject.");
                    lastPid = g.pid;
                    lastNote.clear();
                }
            } else {
                detectedPid_ = 0;
                std::wstring note = notes.empty() ? L"Waiting for Minecraft…" : notes.front();
                if (lastPid != 0 || firstPass || note != lastNote) {
                    postMcStatus(L"Not detected", false);
                    postStatus(note);
                    lastPid = 0;
                    lastNote = note;
                }
            }
            firstPass = false;
        }
        for (int i = 0; i < 15 && running_; ++i) Sleep(100);
    }
}

void Launcher::onAuthClicked() {
    if (previewMode_ || loading_) return;
    if (licenseKey_.empty()) {
        status_ = L"Enter your license key first.";
        InvalidateRect(hwnd_, nullptr, FALSE);
        return;
    }

    if (authThread_) {
        if (WaitForSingleObject(authThread_, 0) == WAIT_OBJECT_0) {
            CloseHandle(authThread_);
            authThread_ = nullptr;
        } else {
            return;
        }
    }

    loading_ = true;
    EnableWindow(licenseEdit_, FALSE);
    status_ = L"Checking your license…";
    InvalidateRect(hwnd_, nullptr, FALSE);

    std::wstring copy = licenseKey_;
    pendingLicense_ = copy;
    auto* ctx = new AuthCtx{ this, copy };
    authThread_ = CreateThread(nullptr, 0, authThreadProc, ctx, 0, nullptr);
    SecureZeroMemory(&copy[0], copy.size() * sizeof(wchar_t));
    if (!authThread_) {
        SecureZeroMemory(&ctx->licenseKey[0], ctx->licenseKey.size() * sizeof(wchar_t));
        delete ctx;
        loading_ = false;
        secureClearPendingLicense();
        EnableWindow(licenseEdit_, TRUE);
        status_ = L"Could not start authentication.";
        InvalidateRect(hwnd_, nullptr, FALSE);
    }
}

void Launcher::doAuth(std::wstring licenseKey) {
    KeyAuthResult result = authManager_.authenticateLicense(licenseKey);
    if (!licenseKey.empty()) {
        SecureZeroMemory(&licenseKey[0], licenseKey.size() * sizeof(wchar_t));
    }
    postAuthResult(result);
}

void Launcher::onLoadClicked() {
    if (previewMode_ || loading_ || !authenticated_) return;
    if (loadThread_) {
        if (WaitForSingleObject(loadThread_, 0) != WAIT_OBJECT_0) return;
        CloseHandle(loadThread_);
        loadThread_ = nullptr;
    }
    DWORD pid = detectedPid_.load();
    if (pid == 0) { postStatus(L"Minecraft not detected. Start the game first."); return; }
    loading_ = true;
    postPhase(PHASE_INJECTING, L"");
    auto* ctx = new LoadCtx{ this, pid };
    loadThread_ = CreateThread(nullptr, 0, loadThreadProc, ctx, 0, nullptr);
    if (!loadThread_) {
        delete ctx;
        loading_ = false;
        postPhase(PHASE_FAILED, L"Could not start the loader.");
    }
}

void Launcher::doLoad(DWORD pid) {
    struct Guard { std::atomic<bool>& f; ~Guard() { f = false; } } guard{ loading_ };

    postLog(L"Checking Minecraft process…", false);
    postLog(L"Minecraft 1.8.9 detected (PID " + std::to_wstring(pid) + L")", true);
    postLog(L"Preparing Rice Slinky…", false);
    std::wstring workDir = paths_.runtime;
    if (Resources::locate(workDir, L"RiceNative.dll").empty()) {
        postPhase(PHASE_FAILED, L"The client file is missing. Reopen the loader to restore it.");
        return;
    }
    postLog(L"Client prepared", true);

    LoadResult result = Loader::runNative(workDir, pid,
        [this](const std::wstring& line) {
            bool okLine = line.rfind(L"Jade is ready", 0) == 0 || line.rfind(L"soju is ready", 0) == 0
                || line.rfind(L"JVM accepted the agent", 0) == 0;
            postLog(line, okLine);
        });

    if (result.success) {
        postLog(L"Rice Slinky loaded — return to Minecraft", true);
        postPhase(PHASE_DONE, L"");
    } else {
        postLog(result.message, false);
        postPhase(PHASE_FAILED, result.message);
    }
}

void Launcher::secureClearLicense() {
    if (!licenseKey_.empty()) {
        SecureZeroMemory(&licenseKey_[0], licenseKey_.size() * sizeof(wchar_t));
        licenseKey_.clear();
    }
}

void Launcher::secureClearPendingLicense() {
    if (!pendingLicense_.empty()) {
        SecureZeroMemory(&pendingLicense_[0], pendingLicense_.size() * sizeof(wchar_t));
        pendingLicense_.clear();
    }
}

std::wstring Launcher::licenseStorePath() const {
    return paths_.dataRoot + L"\\license.dat";
}

void Launcher::loadSavedLicense() {
    std::wstring path = licenseStorePath();
    HANDLE file = CreateFileW(path.c_str(), GENERIC_READ, FILE_SHARE_READ, nullptr, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, nullptr);
    if (file == INVALID_HANDLE_VALUE) return;

    LARGE_INTEGER size = {};
    if (GetFileSizeEx(file, &size) && size.QuadPart > 0 && size.QuadPart <= 1024 * sizeof(wchar_t)) {
        std::vector<wchar_t> buffer(static_cast<size_t>(size.QuadPart / sizeof(wchar_t)) + 1, L'\0');
        DWORD read = 0;
        if (ReadFile(file, buffer.data(), static_cast<DWORD>(buffer.size() * sizeof(wchar_t) - sizeof(wchar_t)), &read, nullptr) && read >= sizeof(wchar_t)) {
            licenseKey_.assign(buffer.data(), read / sizeof(wchar_t));
            licenseKey_.erase(std::remove_if(licenseKey_.begin(), licenseKey_.end(), [](wchar_t c) {
                return c == L'\0' || c == L'\r' || c == L'\n' || c == L'\t';
            }), licenseKey_.end());
            if (!licenseKey_.empty()) status_ = L"Saved license loaded. Press Enter or click Continue.";
        }
    }
    CloseHandle(file);
}

void Launcher::saveLicense(const std::wstring& licenseKey) const {
    if (licenseKey.empty()) return;
    std::wstring path = licenseStorePath();
    HANDLE file = CreateFileW(path.c_str(), GENERIC_WRITE, 0, nullptr, CREATE_ALWAYS, FILE_ATTRIBUTE_HIDDEN, nullptr);
    if (file == INVALID_HANDLE_VALUE) return;

    DWORD written = 0;
    WriteFile(file, licenseKey.data(), static_cast<DWORD>(licenseKey.size() * sizeof(wchar_t)), &written, nullptr);
    CloseHandle(file);
}

std::wstring Launcher::maskedLicense() const {
    if (licenseKey_.empty()) return L"";
    if (licenseKey_.size() <= 4) return std::wstring(licenseKey_.size(), L'•');

    std::wstring masked(licenseKey_.size() - 4, L'•');
    masked += licenseKey_.substr(licenseKey_.size() - 4);
    return masked;
}

} // namespace soju
