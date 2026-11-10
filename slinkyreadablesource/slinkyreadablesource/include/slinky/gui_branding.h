#pragma once

// Display text only. Never rewrite module/configuration identifiers or game data.
namespace rice_gui {
inline bool word(unsigned char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
           (c >= '0' && c <= '9') || c == '_';
}
inline unsigned char lower(unsigned char c) { return c >= 'A' && c <= 'Z' ? c + 32 : c; }
inline bool matches(const char* text, unsigned long long remaining, const char* token, unsigned& size) {
    size = 0;
    while (token[size]) {
        if (size >= remaining || lower(text[size]) != lower(token[size])) return false;
        ++size;
    }
    return size == remaining || !word(text[size]);
}
inline const char* clean(const char* text, const char*& end, char* buffer, unsigned capacity) {
    if (!text || !buffer || !capacity) return text;
    unsigned long long length = 0;
    if (end) length = end >= text ? static_cast<unsigned long long>(end - text) : 0;
    else while (text[length]) ++length;
    // Notification identity stays Slinky; other menu branding keeps its rules.
    unsigned notificationSize=0;
    if ((length==6 && text[0]=='S' && text[1]=='L' && text[2]=='I' &&
         text[3]=='N' && text[4]=='K' && text[5]=='Y') ||
        matches(text,length,"Slinky injected",notificationSize)) return text;
    struct Rule { const char* token; const char* replacement; };
    static const Rule rules[] = {
        {"Rice Slinky", "Rice"}, {"OpenSlinky", "Rice"}, {"Slinky", "Rice"},
        {"Raven bS", "Standard"}, {"Raven b+", "Standard"}, {"Raven b4", "Standard"},
        {"RavenBS", "Standard"}, {"Raven", "Standard"},
        {"OpenB4", "Standard"}, {"Open B4", "Standard"}, {"OpenJade", "Standard"},
        {"LiquidBounce", "Standard"}, {"Myau", "Standard"}, {"Mayu", "Standard"},
        {"Meow", "Standard"}, {"Vape", "Standard"}, {"Rise", "Standard"}
    };
    bool changed = false;
    unsigned written = 0;
    for (unsigned long long at = 0; at < length;) {
        const char* replacement = nullptr;
        unsigned size = 0;
        if (!at || !word(text[at - 1])) {
            for (const Rule& rule : rules) {
                if (matches(text + at, length - at, rule.token, size)) {
                    replacement = rule.replacement; changed = true; break;
                }
            }
        }
        if (replacement) {
            for (unsigned i = 0; replacement[i]; ++i)
                if (written + 1 < capacity) buffer[written++] = replacement[i];
            at += size;
        } else {
            if (written + 1 < capacity) buffer[written++] = text[at];
            ++at;
        }
    }
    if (!changed) return text;
    buffer[written] = 0;
    end = buffer + written;
    return buffer;
}
}
