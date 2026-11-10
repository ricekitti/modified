// NanoVG entry points the client calls by their address in the original image (fn_<rva>), bound to upstream NanoVG
// (third_party/nanovg, the same version: NVGstate is 0x110 bytes and nstates sits at +0x2288 in both).
// Identified from the argument shapes and checked on the state offsets each setter writes (e.g. nvg_StrokeWidth writes
// state+0xac = strokeWidth, nvg_TextAlign state+0x108 = textAlign, nvg_Scissor clamps w/h like nvgScissor).
#include "slinky/gl_slots.h"
#include "slinky/gui_branding.h"
extern "C" {   // the GL backend headers have no C++ guard
#include "nanovg.h"
#include "nanovg_gl.h"
#include "nanovg_gl_utils.h"
}

extern "C" {
void nvg_IntersectScissor(NVGcontext*c,float x,float y,float w,float h){nvgIntersectScissor(c,x,y,w,h);}
void nvg_Save(NVGcontext *c) { nvgSave(c); }
void nvg_Restore(NVGcontext *c) { nvgRestore(c); }
void nvg_BeginFrame(NVGcontext *c, float w, float h, float dpr) { nvgBeginFrame(c, w, h, dpr); }
void nvg_EndFrame(NVGcontext *c) { nvgEndFrame(c); }
NVGcolor nvg_rgb_f(float r, float g, float b) { return nvgRGBf(r, g, b); }
NVGcolor nvg_rgba_f(float r, float g, float b, float a) { return nvgRGBAf(r, g, b, a); }
NVGcolor nvg_trans_rgba_f(NVGcolor c, float a) { return nvgTransRGBAf(c, a); }
NVGcolor nvg_lerp_rgba(NVGcolor a, NVGcolor b, float t) { return nvgLerpRGBA(a, b, t); }
NVGcolor nvg_hsl(float h, float s, float l) { return nvgHSL(h, s, l); }
void nvg_StrokeWidth(NVGcontext *c, float w) { nvgStrokeWidth(c, w); }
void nvg_GlobalAlpha(NVGcontext *c, float a) { nvgGlobalAlpha(c, a); }
void nvg_Translate(NVGcontext *c, float x, float y) { nvgTranslate(c, x, y); }
void nvg_Scale(NVGcontext *c, float x, float y) { nvgScale(c, x, y); }
void nvg_StrokeColor(NVGcontext *c, NVGcolor col) { nvgStrokeColor(c, col); }
void nvg_FillColor(NVGcontext *c, NVGcolor col) { nvgFillColor(c, col); }
void nvg_FillPaint(NVGcontext *c, NVGpaint p) { nvgFillPaint(c, p); }
NVGpaint nvg_linear_gradient(NVGcontext *c, float sx, float sy, float ex, float ey, NVGcolor i, NVGcolor o) {
    return nvgLinearGradient(c, sx, sy, ex, ey, i, o);
}
NVGpaint nvg_image_pattern(NVGcontext *c, float ox, float oy, float ex, float ey, float angle, int image, float alpha) {
    return nvgImagePattern(c, ox, oy, ex, ey, angle, image, alpha);
}
void nvg_Scissor(NVGcontext *c, float x, float y, float w, float h) { nvgScissor(c, x, y, w, h); }
void nvg_BeginPath(NVGcontext *c) { nvgBeginPath(c); }
void nvg_MoveTo(NVGcontext *c, float x, float y) { nvgMoveTo(c, x, y); }
void nvg_LineTo(NVGcontext *c, float x, float y) { nvgLineTo(c, x, y); }
void nvg_Rect(NVGcontext *c, float x, float y, float w, float h) { nvgRect(c, x, y, w, h); }
void nvg_RoundedRect(NVGcontext *c, float x, float y, float w, float h, float r) { nvgRoundedRect(c, x, y, w, h, r); }
void nvg_RoundedRectVarying(NVGcontext *c, float x, float y, float w, float h, float tl, float tr, float br, float bl) {
    nvgRoundedRectVarying(c, x, y, w, h, tl, tr, br, bl);
}
void nvg_Circle(NVGcontext *c, float x, float y, float r) { nvgCircle(c, x, y, r); }
void nvg_Fill(NVGcontext *c) { nvgFill(c); }
void nvg_Stroke(NVGcontext *c) { nvgStroke(c); }
int nvg_create_font_mem(NVGcontext *c, const char *name, unsigned char *data, int n, int freeData) {
    return nvgCreateFontMem(c, name, data, n, freeData);
}
void nvg_FontSize(NVGcontext *c, float size) { nvgFontSize(c, size); }
void nvg_TextAlign(NVGcontext *c, int align) { nvgTextAlign(c, align); }
void nvg_FontFaceId(NVGcontext *c, int font) { nvgFontFaceId(c, font); }
float nvg_text(NVGcontext *c, float x, float y, const char *s, const char *end) {
    char display[2048];
    s = rice_gui::clean(s, end, display, sizeof display);
    return nvgText(c, x, y, s, end);
}
float nvg_text_bounds(NVGcontext *c, float x, float y, const char *s, const char *end, float *bounds) {
    char display[2048];
    s = rice_gui::clean(s, end, display, sizeof display);
    return nvgTextBounds(c, x, y, s, end, bounds);
}
NVGLUframebuffer *nvglu_create_framebuffer(NVGcontext *c, int w, int h, int flags) { return nvgluCreateFramebuffer(c, w, h, flags); }
void nvg_wrap_delete_framebuffer(NVGLUframebuffer *fb) { nvgluDeleteFramebuffer(fb); }
void nvg_wrap_bind_framebuffer(NVGLUframebuffer *fb) { nvgluBindFramebuffer(fb); }
}
