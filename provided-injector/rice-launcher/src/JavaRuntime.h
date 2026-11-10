#pragma once
#include <string>
#include <vector>

// Locates Java runtimes on Windows without relying only on PATH.
// The loader uses the private runtime's Attach API; Minecraft may use a different Java version.
namespace soju {

struct JavaInstall {
    std::wstring home;      // e.g. C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot
    std::wstring javaExe;   // <home>\bin\java.exe
    int major = 0;          // 8, 17, 21, ...
    bool isJdk = false;     // has bin\javac.exe
};

class JavaRuntime {
public:
    // All runtimes discovered (JAVA_HOME, registry, common install dirs, PATH), de-duplicated.
    static std::vector<JavaInstall> discover();

    // Prefer the adjacent private runtime, then installed JDKs. found=false if none.
    static JavaInstall findBest(int minMajor, bool& found, const std::wstring& privateHome = L"");
};

} // namespace soju
