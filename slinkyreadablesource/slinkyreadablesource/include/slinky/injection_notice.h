#pragma once
struct Ctx;
// Same prefix as the render target created on the game's render thread.
struct InjectionNoticeRenderer {
    void* ig;
    double time;
    int width, height;
    float scaledWidth, scaledHeight, scale;
    int pad;
    void* framebuffer;
    float clear[4];
    int font[2];
};
extern "C" void injection_notice_ready();
extern "C" bool injection_notice_visible(Ctx* context, double now);
extern "C" void injection_notice_draw(void* renderer, double now);
