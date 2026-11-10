#include "slinky/gui_branding.h"
#undef NDEBUG
#include <cassert>
#include <cstring>

static void check(const char* input, const char* expected) {
    char buffer[2048];
    const char* end = nullptr;
    const char* display = rice_gui::clean(input, end, buffer, sizeof buffer);
    assert(std::strcmp(display, expected) == 0);
    if (display == buffer) assert(end == display + std::strlen(display));
}
int main() {
    check("Rice Slinky injected", "Rice injected");
    check("SLINKY", "SLINKY");
    check("Slinky injected", "Slinky injected");
    check("Slinky injected successfully", "Slinky injected successfully");
    check("Unload Slinky?", "Unload Rice?");
    check("Raven bS / Rise / Vape / OpenB4 / OpenJade", "Standard / Standard / Standard / Standard / Standard");
    check("Myau Mayu Meow LiquidBounce OpenSlinky", "Standard Standard Standard Standard Rice");
    check("Aim Assist", "Aim Assist");
    check("Surprise sunrise arises", "Surprise sunrise arises");
    check("RAVEN bs", "Standard");
    const char span[] = {'V','a','p','e','!','x'};
    const char* end = span + 5;
    char output[32];
    const char* result = rice_gui::clean(span, end, output, sizeof output);
    assert(result == output && std::strcmp(result, "Standard!") == 0 && end == result + 9);
    char tiny[4]; end = nullptr;
    result = rice_gui::clean("Raven", end, tiny, sizeof tiny);
    assert(result == tiny && std::strcmp(tiny, "Sta") == 0 && end == tiny + 3);
}
