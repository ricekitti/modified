#pragma once
#include <atomic>
#include <mutex>
#include <string>
#include <vector>
#include <windows.h>

#include "AuthManager.h"
#include "Resources.h"
#include "GlassRenderer.h"

// The soju injector window: a borderless, custom-painted card that mirrors
// soju_injector_mockup.html (brand, hero logo, target card,
// primary button, 3-segment step bar, status line; log while injecting;
// check-circle success / failure states).
namespace soju {

class Launcher {
public:
    int run(HINSTANCE hinst, const std::wstring& preview = L"", const std::wstring& renderPath = L"");

private:
    enum Phase { PHASE_AUTH, PHASE_READY, PHASE_INJECTING, PHASE_DONE, PHASE_FAILED };

    static LRESULT CALLBACK wndProc(HWND, UINT, WPARAM, LPARAM);
    static LRESULT CALLBACK licenseProc(HWND, UINT, WPARAM, LPARAM);
    static DWORD WINAPI authThreadProc(LPVOID);
    static DWORD WINAPI detectThreadProc(LPVOID);
    static DWORD WINAPI loadThreadProc(LPVOID);
    LRESULT handle(HWND, UINT, WPARAM, LPARAM);
    void paint(HDC dc);

    void createWindow();
    void layout();
    void startDetection();
    void detectionLoop();
    void onAuthClicked();
    void onLoadClicked();
    void doAuth(std::wstring licenseKey);
    void doLoad(DWORD pid);
    void secureClearLicense();
    void secureClearPendingLicense();
    void loadSavedLicense();
    void saveLicense(const std::wstring& licenseKey) const;
    std::wstring licenseStorePath() const;
    std::wstring maskedLicense() const;

    // Thread-safe UI updates (marshalled to the window thread).
    void postAuthResult(const KeyAuthResult& result);
    void postMcStatus(const std::wstring& text, bool ready);
    void postStatus(const std::wstring& text);
    void postPhase(Phase phase, const std::wstring& detail);
    void postLog(const std::wstring& text, bool ok);
    template<typename T> void postUiMessage(UINT message, T* payload) {
        std::lock_guard<std::mutex> lock(messageMutex_);
        if (!acceptingMessages_ || !PostMessageW(hwnd_, message, 0, reinterpret_cast<LPARAM>(payload)))
            delete payload;
    }

    // Painting primitives.
    void fillRoundRect(HDC dc, const RECT& r, int radius, COLORREF fill) const;
    void outlineRoundRect(HDC dc, const RECT& r, int radius, COLORREF outline, int width = 1) const;
    void drawSteps(HDC dc, int activeCount) const;
    static COLORREF mix(COLORREF a, COLORREF b, double t);

    HINSTANCE hinst_ = nullptr;
    HWND hwnd_ = nullptr;
    HWND licenseEdit_ = nullptr;
    WNDPROC originalEditProc_ = nullptr;
    bool previewMode_ = false;
    bool glassEnabled_ = false;
    std::unique_ptr<GlassRenderer> shader_;
    ULONGLONG shaderStarted_ = 0;
    HFONT fontBrand_ = nullptr, fontH1_ = nullptr, fontBody_ = nullptr,
          fontBtn_ = nullptr, fontSmall_ = nullptr, fontMono_ = nullptr,
          fontIcon_ = nullptr;
    HBRUSH bgBrush_ = nullptr;

    bool hoverClose_ = false, hoverMin_ = false, hoverBtn_ = false;

    // Geometry (client 720x490, recomputed in layout()).
    int W_ = 920, H_ = 600;
    RECT rcBrand_{}, rcClose_{}, rcMin_{}, rcIcon_{}, rcH1_{}, rcSub_{},
         rcTarget_{}, rcTargetDot_{}, rcTargetSmall_{}, rcTargetStrong_{}, rcTargetBadge_{},
         rcButton_{}, rcSteps_{}, rcStatus_{}, rcLog_{}, rcCheck_{}, rcDoneTitle_{}, rcDoneText_{},
         rcLicenseLabel_{}, rcLicenseField_{};

    Phase phase_ = PHASE_AUTH;
    std::wstring mcStatus_ = L"Not detected";
    bool mcReady_ = false;
    bool authenticated_ = false;
    std::wstring status_ = L"Enter your license key to continue.";
    std::wstring licenseKey_;
    std::wstring pendingLicense_;
    std::wstring lastLine_;
    std::vector<std::pair<std::wstring, bool>> logLines_;

    AuthManager authManager_;
    Paths paths_;
    ULONG_PTR gdiplusToken_ = 0;
    HANDLE authThread_ = nullptr;
    HANDLE loadThread_ = nullptr;
    HANDLE detectThread_ = nullptr;
    std::atomic<bool> running_{false};
    std::atomic<bool> loading_{false};
    std::atomic<DWORD> detectedPid_{0};
    std::mutex envMutex_;
    std::mutex messageMutex_;
    bool acceptingMessages_ = true;
    std::wstring environment_;
};

} // namespace soju
