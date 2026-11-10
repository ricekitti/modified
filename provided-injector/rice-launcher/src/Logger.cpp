#include "Logger.h"

#include <windows.h>
#include <fstream>
#include <iostream>
#include <ctime>

namespace soju {

std::mutex Logger::mutex_;
std::wstring Logger::path_;
bool Logger::console_ = false;
bool Logger::initialized_ = false;

static std::wstring timestamp(bool dateOnly) {
    SYSTEMTIME st;
    GetLocalTime(&st);
    wchar_t buf[32];
    if (dateOnly)
        swprintf(buf, 32, L"%04d%02d%02d", st.wYear, st.wMonth, st.wDay);
    else
        swprintf(buf, 32, L"%02d:%02d:%02d.%03d", st.wHour, st.wMinute, st.wSecond, st.wMilliseconds);
    return buf;
}

void Logger::init(const std::wstring& logDir, bool withConsole) {
    std::lock_guard<std::mutex> lock(mutex_);
    CreateDirectoryW(logDir.c_str(), nullptr);
    path_ = logDir + L"\\soju-" + timestamp(true) + L".log";
    console_ = withConsole;
    initialized_ = true;
}

void Logger::log(Level level, const std::wstring& message) {
    const wchar_t* tag = L"INFO";
    switch (level) {
        case Level::Debug: tag = L"DEBUG"; break;
        case Level::Warn:  tag = L"WARN";  break;
        case Level::Error: tag = L"ERROR"; break;
        default: break;
    }
#ifndef SOJU_DEBUG
    if (level == Level::Debug) return; // debug lines only in debug builds
#endif
    std::lock_guard<std::mutex> lock(mutex_);
    std::wstring line = L"[" + timestamp(false) + L"] [" + tag + L"] " + message;

    if (initialized_) {
        std::wofstream out(path_, std::ios::app);
        if (out) out << line << L"\n";
    }
    if (console_) {
        auto& stream = (level == Level::Error) ? std::wcerr : std::wcout;
        stream << line << std::endl;
    }
}

} // namespace soju
