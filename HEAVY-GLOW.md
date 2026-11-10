Select Player ESP -> Mode -> Heavy Glow in the modified client.

Source: OpenB4 release u20+-4, the latest release listed when inspected.
https://codeberg.org/abusez/OpenB4/releases/tag/u20%2B-4

The six GLSL shader strings are copied byte for byte from ShaderSources.java
and Shader.java. Original Java files and decoded shader references are retained
under slinkyreadablesource/slinkyreadablesource/references/openb4-u20plus-4/.
The effect captures actual player models into a mask, runs two Kawase bloom
iterations with offset 6.5, excludes the mask interior, and adds the original
2-pixel outline with alpha threshold 0.1.

The Java rendering layer is adapted to native C++/JNI, not copied byte for byte.
It uses the existing native ESP range, friendly-player filters, colors, target
color, hurt tint, and 100-player limit. Existing 2D and 3D mode indices stay
unchanged; Heavy Glow is appended as mode 2. Box-specific settings are hidden
while Heavy Glow is selected. Their saved values remain available in 2D/3D.

Compatibility changes:
- The mask-check sampler uses texture unit 1 instead of OpenB4's unit 16.
  Shader source is unchanged; the sampler uniform requires only two units.
- Both core and EXT framebuffer entry points are supported.
- Native render targets use RGBA8, explicit per-target viewports, and retain
  full, half, and quarter resolution targets with the source's texture filters.
- Missing shaders, failed shader compilation, missing framebuffers, unavailable
  renderer mappings, and failed capture fall back to standard 3D ESP.
- Framebuffers/programs are released on mode change, ESP disable, and shutdown.
- GL bindings, matrices, viewport, blend/depth/texture state, renderer shadows,
  and Minecraft's cached GL state are restored/reconciled around capture.

The implementation is vendor-neutral OpenGL. Heavy Glow needs GLSL 1.20 and
framebuffer support; older hardware uses the non-shader 3D fallback. Universal
glow support cannot be guaranteed. GPU tests ran on NVIDIA RTX 3060 / OpenGL
4.6. Intel/AMD hardware and live Minecraft have not been verified.

All 12 tests pass. The GPU harness verifies unchanged shader bytes, real halo
output, interior exclusion, resizing, state restoration, capture filtering,
renderer exceptions, Java texture-cache synchronization, and GPU cleanup.
Separate runs simulate two texture units and missing shader support. Existing
Fireball Pearl, AntiFireball, Displace, Stasis, GUI startup, and launcher checks
continue to pass. The packaged loader's extracted DLL hash is also verified.

Only modified receives this feature. Authentication, the GUI startup fix, and
the existing additional modules are preserved. Regular is unchanged.

Crash fix: hs_err_pid34172.log showed a VM access violation inside
RenderGlobal.markBlockForUpdate after enabling Heavy Glow. The legacy helper
named get_render_manager reads Minecraft.renderGlobal, so it is unsuitable for
RenderManager method calls. Heavy Glow now uses the same version-aware renderer
accessor as the existing ESP, and verifies IsInstanceOf(RenderManager) before
any renderer dispatch. The GPU/native regression explicitly supplies a wrong
renderer receiver and checks that it is rejected for classic ESP fallback.

Video regression fix (2026-10-30 20-14-03.mp4): a GPU test reproduced the
lightmap/texture-combiner leak in the final fixed-function fullscreen copy.
Heavy Glow now uses an explicit native compositing shader, rejects the mask
interior again at final composite, and preserves destination alpha for both
glow and outline. Texture matrices for the base texture and lightmap units are
saved/restored around capture. The colored-player regression leaves a lightmap
and a modified texture matrix behind, then verifies player RGB/alpha and the
original matrix remain intact. The original OpenB4 GLSL strings remain unchanged;
the additional native compositing shader isolates the Minecraft render state.

Nametag fix: player-name rendering is suppressed only while each offscreen
player model is being captured. Normal world nametags and the existing No Name
Tags setting retain their usual behavior. Capture scope is restored after both
successful rendering and Java renderer exceptions. The GPU/native regression
uses the actual name-render predicate to verify labels never enter the glow
capture and normal name rendering resumes afterward.
