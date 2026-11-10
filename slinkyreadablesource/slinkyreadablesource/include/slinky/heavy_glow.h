#pragma once
using HeavyGlowDraw = bool (*)(void*,void(*)(float,float,float));
// Must be called with a current OpenGL context, on the game's render thread.
bool heavy_glow_draw(int width,int height,HeavyGlowDraw draw,void* context);
void heavy_glow_release();
extern "C" void heavy_glow_extend_modes();
extern "C" const char* heavy_glow_mode_name(unsigned mode);
extern "C" bool heavy_glow_capture_active();
