#pragma once
#include <string>
#include <mutex>

// Minimal file+console logger. Never logs credentials, tokens or session secrets.
namespace soju {

enum class Level { Info, Debug, Warn, Error };

class Logger {
public:
    // Opens (or creates) %LOCALAPPDATA%\soju\logs\soju-YYYYMMDD.log. withConsole mirrors to stdout.
    static void init(const std::wstring& logDir, bool withConsole);
    static void log(Level level, const std::wstring& message);

    static void info(const std::wstring& m)  { log(Level::Info, m); }
    static void debug(const std::wstring& m) { log(Level::Debug, m); }
    static void warn(const std::wstring& m)  { log(Level::Warn, m); }
    static void error(const std::wstring& m) { log(Level::Error, m); }

private:
    static std::mutex mutex_;
    static std::wstring path_;
    static bool console_;
    static bool initialized_;
};

} // namespace soju
