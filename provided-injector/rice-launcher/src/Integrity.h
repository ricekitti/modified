#pragma once
#include <string>
#include <vector>

// SHA-256 integrity checks for bundled resources (loader jar, client jar, native DLLs).
namespace soju {

struct IntegrityResult {
    bool ok = true;              // false if any listed file is missing or mismatched
    std::wstring failedFile;     // first offending file (relative name)
    std::wstring reason;         // "missing" or "hash mismatch"
};

class Integrity {
public:
    // Lowercase hex SHA-256 of a file; empty string if it cannot be read.
    static std::wstring sha256File(const std::wstring& path);

    // Verifies files listed in a manifest ("<sha256>  <relative-name>" per line) found in baseDir.
    // Missing manifest => ok with reason "manifest-absent" (caller decides whether to warn or block).
    static IntegrityResult verifyManifest(const std::wstring& baseDir, const std::wstring& manifestPath);
};

} // namespace soju
